package rabbit_field.creature;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import rabbit_field.event.OrderedExecutionEvent.OrderingComponent;
import rabbit_field.event.ResetEvent;
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
	public static class PendingProcess implements Delayed {
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
	protected static class ProcessWatcherTask extends AbstractCyclicTask {
		private final static Logger log = LogManager.getLogger();
		private final List<PendingProcess> processesToWatch = new LinkedList<>();
		private final BlockingQueue<PendingProcess> enqueuedProcesses;
		private final DelayQueue<PendingProcess> decidedActions;
		private final Semaphore inCycleSem = new Semaphore(1);
		
		public ProcessWatcherTask(BlockingQueue<PendingProcess> enqueuedProcesses, DelayQueue<PendingProcess> decidedActions) {
			this.enqueuedProcesses = enqueuedProcesses;
			this.decidedActions = decidedActions;
		}
		
		@Override public void runCycle() {
			try {
				inCycleSem.acquire();
				// check processes queue, proceed if non-empty or processesToWatch has something to watch for 
				if (enqueuedProcesses.isEmpty() && processesToWatch.isEmpty()) {
					MILLISECONDS.sleep(5);
					inCycleSem.release();
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
				inCycleSem.release();
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
			log.debug("Creatures: {}, comb. speed: {}, max time: {}", processesToWatch.size(), combinedSpeed, maxTimeAllowedMs);
			return maxTimeAllowedMs;
		}
		
		public void cancelAll() {
			if (!isPaused()) pause();
			try {
				inCycleSem.acquire();
				log.debug("To cancel & remove in enqueuedProcesses: {}, in processesToWatch: {}, in decidedActions: {}", 
						enqueuedProcesses.size(), processesToWatch.size(), decidedActions.size());
				enqueuedProcesses.forEach(pp -> pp.futureAction.cancel(true));
				enqueuedProcesses.clear();
				processesToWatch.forEach(pp -> pp.futureAction.cancel(true));
				processesToWatch.clear();
				decidedActions.clear();
				inCycleSem.release();
			} catch (InterruptedException e) {
				log.error("Interrupt while cancelling processes and clearing queues.", e);
			}
			resume();
		}
	}
	
	private final static Logger log = LogManager.getLogger();
	private final ExecutorService processExec = Executors.newSingleThreadExecutor(rnbl -> new Thread(rnbl, "mind process"));
	private final ExecutorService processWatchExec = Executors.newSingleThreadExecutor(rnbl -> new Thread(rnbl, "process watcher"));
	private final BlockingQueue<PendingProcess> enqueuedProcesses = new LinkedBlockingQueue<>();
	private final DelayQueue<PendingProcess> decidedActions = new DelayQueue<>();
	private final ProcessWatcherTask processWatcherTask = new ProcessWatcherTask(enqueuedProcesses, decidedActions);
	private volatile boolean shuttingDown = false;
	
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
	}

	public boolean letCreatureThink(Creature creature) {
		if (shuttingDown) {
			log.info("Ignoring think request for {} due to shutdown.", creature);
			return false;
		}
		
		try {
			enqueue(new MindProcessTask(creature));
		} catch (Exception e) {
			log.error("Error trying to enqueue a MindProcessTask for {}", creature, e);
			PendingProcess pp = new PendingProcess(creature, 
					CompletableFuture.completedFuture(Action.NONE_BY_FAILURE), 
					System.currentTimeMillis());
			decidedActions.put(pp);
			return false;
		}
		return true;
	}

	public DelayQueue<PendingProcess> getDecidedActions() {
		return decidedActions;
	}
	
	@Subscribe
	public void shutdown(ShutdownEvent evt) {
		shuttingDown = true;
		evt.add(OrderingComponent.MIND_PROCESS_WATCHER, processWatcherTask, processWatchExec)
		   .add(OrderingComponent.MASTER_MIND, null, processExec);
	}
	
	@Subscribe
	public void reset(ResetEvent evt) {
		evt.addForExecution(OrderingComponent.MASTER_MIND, () -> {
			log.info("Resetting MasterMind: cancelling and clearing enqueued processes.");
			processWatcherTask.cancelAll();			
		});
	}
}

class MasterMindException extends Exception {
	private static final long serialVersionUID = 1L;
	private FailureType failureType;
	
	public enum FailureType { ENQUEUING }

	public MasterMindException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public MasterMindException(String message, Throwable cause, FailureType failureType) {
		super(message, cause);
		this.failureType = failureType;
	}

	public FailureType getFailureType() {
		return failureType;
	}
}
