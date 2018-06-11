package rabbit_field.creature;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import rabbit_field.event.ShutdownEvent;

/**
 * Controller for mind processes of all creatures.
 * One thread is allocated for all existing creatures to serve their
 * logic. 
 */
@Singleton
public class MasterMind {
	
	/**
	 * Wraps Creature.decideAction() as Callable
	 */
	private static class MindProcessTask implements Callable<Action> {
		final Creature creature;

		public MindProcessTask(Creature creature) {
			this.creature = creature;
		}

		@Override public Action call() {
			Action action = creature.decideAction();
			return action;
		}
	}
	
	/**
	 * Used to track enqueued MindProcessTask by watcher and completer tasks.
	 */
	protected static class PendingProcess implements Delayed {
		final Creature creature;
		final Future<Action> futureAction;
		final long timeStarted;
		
		public PendingProcess(Creature creature, Future<Action> futureAction, long timeStarted) {
			this.creature = creature;
			this.futureAction = futureAction;
			this.timeStarted = timeStarted;
		}

		@Override public int compareTo(Delayed other) {
			return (int) (this.getDelay(MILLISECONDS) - other.getDelay(MILLISECONDS));
		}

		@Override public long getDelay(TimeUnit unit) {
			// delay is calculated as time to do an action minus thinking time
			long msPerAction = (long) (((float) 1000) / creature.getSpeed());
			long elapsed = System.currentTimeMillis() - timeStarted;
			long delay = Math.max(msPerAction - elapsed, 0);
			log.debug("Creature {} has delay: {}", creature, delay);
			return unit.convert(delay, MILLISECONDS);
		}
	}
	
	/**
	 * Takes pending mind processes from enqueuedProcesses queue,
	 * checks if they are:
	 * - expired: cancel via Future
	 * - completed: send to decidedActions queue
	 */
	protected static class ProcessWatcherTask extends AbstractWatcherTask {
		private final static Logger log = LogManager.getLogger();
		private final List<PendingProcess> processesToWatch = new LinkedList<>();
		private final BlockingQueue<PendingProcess> enqueuedProcesses;
		private final DelayQueue<PendingProcess> decidedActions;

		public ProcessWatcherTask(BlockingQueue<PendingProcess> enqueuedProcesses, DelayQueue<PendingProcess> decidedActions) {
			this.enqueuedProcesses = enqueuedProcesses;
			this.decidedActions = decidedActions;
		}
		
		@Override public void watchCycle() {
			try {
				// check processes queue, proceed if non-empty or processesToWatch has something to watch for 
				if (enqueuedProcesses.isEmpty() && processesToWatch.isEmpty()) {
					MILLISECONDS.sleep(5);
					return;
				} else {
					int drained = enqueuedProcesses.drainTo(processesToWatch);
					log.debug("Drained {} enqueued processes.", drained);
				}

				long maxTimeAllowedMs = calculateAllowedTime(processesToWatch);
				// iterate processesToWatch, check PPs for completion and expiration
				for (Iterator<PendingProcess> iterator = processesToWatch.iterator(); iterator.hasNext();) {
					PendingProcess process = iterator.next();
					if (process.futureAction.isDone()) {
						log.debug("Found decided action for '{}'", process.creature);
						decidedActions.put(process);
						iterator.remove();
					} else {
						long elapsedMs = System.currentTimeMillis() - process.timeStarted;
						if (elapsedMs > maxTimeAllowedMs) {
							log.debug("Found expired process of '{}', cancelling.", process.creature);
							process.futureAction.cancel(true);
							// no need to do anything more since isDone() now returns true and 
							// on the next iteration the process will be put into decidedActions
						}
					}
				}
				MILLISECONDS.sleep(5);
			} catch (InterruptedException e) {
				log.debug("ProcessWatcherTask was interrupted", e);
				Thread.currentThread().interrupt();
			}
		}

		// expiration time: maximum allowed time per creature is calculated as 
		// inverse ratio of combined speed of watched creatures
		private long calculateAllowedTime(List<PendingProcess> processesToWatch) {
			float combinedSpeed = 0;
			for (PendingProcess process : processesToWatch) {
				combinedSpeed += process.creature.getSpeed();
			}
			if (combinedSpeed <= 0) {
				throw new IllegalStateException("Speed of a creature must be positive value.");
			}
			long maxTimeAllowedMs = (long) ((1f / combinedSpeed) * 1_000L);
			log.debug("Creatures: {}, comb speed: {}, max time: {}", processesToWatch.size(), combinedSpeed, maxTimeAllowedMs);
			return maxTimeAllowedMs;
		}
	}

	// Takes creature actions from decidedActions queue after delay is done, 
	// notifies the Creature to perform these actions on the Field.
	// TODO factor it out to creature controller?
	protected static class ActionCompleterTask extends AbstractWatcherTask {
		private final static Logger log = LogManager.getLogger();
		private final DelayQueue<PendingProcess> decidedActions;
		
		public ActionCompleterTask(DelayQueue<PendingProcess> decidedActions) {
			this.decidedActions = decidedActions;
		}

		@Override protected void watchCycle() {			
			try {
				log.debug("Examining decided actions queue.");
				PendingProcess process = decidedActions.poll(1, SECONDS);
				if (process == null) return;
				if (process.futureAction.isCancelled()) {
					process.creature.actionIsDecided(Action.NONE_BY_CANCEL);
				}
				else {
					process.creature.actionIsDecided(process.futureAction.get());
				}
			} catch (InterruptedException e) {
				log.debug("ActionCompleter was interrupted.", e);
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				log.warn("Exception during thinking on Action.", e);
			}
		}
	}
	
	protected static abstract class AbstractWatcherTask implements Runnable {
		private final static Logger log = LogManager.getLogger();
		protected volatile boolean shutdown;
		
		public void shutdown() {
			log.debug("Shutdown is requested");
			shutdown = true;
		}
		
		@Override public void run() {
			do {
				watchCycle();
			} while (!Thread.interrupted() && !shutdown);
		}
		
		protected abstract void watchCycle();
	}
	
	private final static Logger log = LogManager.getLogger();
	private final ExecutorService processExec = Executors.newSingleThreadExecutor(rnbl -> new Thread(rnbl, "mind process"));
	private final ExecutorService processWatchExec = Executors.newSingleThreadExecutor(rnbl -> new Thread(rnbl, "process watcher"));
	private final ExecutorService actionCompletionExec = Executors.newSingleThreadExecutor(rnbl -> new Thread(rnbl, "action completer"));
	private final BlockingQueue<PendingProcess> enqueuedProcesses = new LinkedBlockingQueue<>();
	private final DelayQueue<PendingProcess> decidedActions = new DelayQueue<>();
	private final ProcessWatcherTask processWatcherTask = new ProcessWatcherTask(enqueuedProcesses, decidedActions);
	private final ActionCompleterTask actionCompleterTask = new ActionCompleterTask(decidedActions);
	
	private void enqueue(MindProcessTask processTask) throws InterruptedException {
		log.debug("Enqueueing mind process task {}", Thread.currentThread().getName());
		Future<Action> futureAction = processExec.submit(processTask);
		long startTime = System.currentTimeMillis();
		PendingProcess pendingProcess = new PendingProcess(processTask.creature, futureAction, startTime);
		enqueuedProcesses.put(pendingProcess);
	}

	public MasterMind() {
		log.info("Initializing MasterMind");
		processWatchExec.execute(processWatcherTask);
		actionCompletionExec.execute(actionCompleterTask);
	}

	public void letCreatureThink(Creature creature) {
		try {
			enqueue(new MindProcessTask(creature));
		} catch (Exception e) {
			log.warn("Error trying to enqueue a MindProcessTask", e);
			creature.actionIsDecided(Action.NONE_BY_FAILURE);
		}
	}

	@Subscribe 
	public void shutdown(ShutdownEvent evt) {
		log.info("Shutting down tasks and executors.");
		processWatcherTask.shutdown();
		actionCompleterTask.shutdown();
		processExec.shutdown();
		processWatchExec.shutdown();
		actionCompletionExec.shutdown();
		try {
			processWatchExec.awaitTermination(10, SECONDS);
			actionCompletionExec.awaitTermination(10, SECONDS);
			processExec.awaitTermination(10, SECONDS);
		} catch (InterruptedException e) {
			log.error("Interrupt while waiting for termination of executors");
		}
		log.info("Shutdown completed");
	}
	
}
