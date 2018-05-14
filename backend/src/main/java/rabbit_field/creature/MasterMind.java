package rabbit_field.creature;

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

/**
 * Controller for mind processes of all creatures.
 * One thread is allocated for all existing creatures to serve their
 * logic. 
 */
@Singleton
public class MasterMind {
	
	// wraps Creature.decideAction() as Callable
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
			return (int) (this.getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS));
		}

		@Override public long getDelay(TimeUnit unit) {
			// delay is calculated as time to do an action minus thinking time
			long msPerAction = (long) (((float) 1000) / creature.getSpeed());
			long elapsed = System.currentTimeMillis() - timeStarted;
			long delay = Math.max(msPerAction - elapsed, 0);
			log.debug(creature + " has delay: " + delay);
			return unit.convert(delay, TimeUnit.MILLISECONDS);
		}
	}
	
	// Takes pending mind processes from enqueuedProcesses queue,
	// checks if they are
	// - expired: cancel via Future 
	// - completed: send to decidedActions queue
	protected static class ProcessWatcherTask implements Runnable {
		private List<PendingProcess> processesToWatch = new LinkedList<>();
		private BlockingQueue<PendingProcess> enqueuedProcesses;
		private BlockingQueue<PendingProcess> decidedActions;

		public ProcessWatcherTask(BlockingQueue<PendingProcess> enqueuedProcesses, BlockingQueue<PendingProcess> decidedActions) {
			this.enqueuedProcesses = enqueuedProcesses;
			this.decidedActions = decidedActions;
		}

		@Override public void run() {
			while (!Thread.interrupted()) {
				try {
					// check processes queue
					if (enqueuedProcesses.isEmpty()) {
						TimeUnit.MILLISECONDS.sleep(1);
					} else {
						enqueuedProcesses.drainTo(processesToWatch);
					}

					// expiration time: maximum allowed time is calculated as 
					// inverse ratio of combined speed of watched creatures
					float combinedSpeed = 0;
					for (PendingProcess process : processesToWatch) {
						combinedSpeed += process.creature.getSpeed();
					}
					long maxTimeAllowedMs = (long) ((1f / combinedSpeed) * 1_000L);
					
					// iterate list, check PDs for completion and expiration
					for (Iterator<PendingProcess> iterator = processesToWatch.iterator(); iterator.hasNext();) {
						PendingProcess process = iterator.next();
						if (process.futureAction.isDone()) {
							log.debug("Process watcher found decided action");
							decidedActions.put(process);
							iterator.remove();
						} else {
							long elapsedMs = System.currentTimeMillis() - process.timeStarted;
							if (elapsedMs > maxTimeAllowedMs) {
								log.debug("Found expired process of {}, cancelling.", process.creature);
								process.futureAction.cancel(true);
								// no need to do anything more since isDone() now returns true and 
								// on the next iteration the process will be put into decidedActions
							}
						}
					}
				} catch (InterruptedException e) {
					log.debug("ProcessWatcherTask was interrupted", e);
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	// Takes creature actions from decidedActions queue after delay is done, 
	// notifies the Creature to perform these actions on the Field.
	protected static class ActionCompleterTask implements Runnable {
		private BlockingQueue<PendingProcess> decidedActions;
		
		public ActionCompleterTask(BlockingQueue<PendingProcess> decidedActions) {
			this.decidedActions = decidedActions;
		}

		@Override public void run() {
			while (!Thread.interrupted()) {
				try {
					log.debug("Examining decided actions queue.");
					PendingProcess process = decidedActions.take();
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
	}
	
	private static Logger log = LogManager.getLogger();
	private ExecutorService processExec = Executors.newSingleThreadExecutor();
	private ExecutorService processWatchExec = Executors.newSingleThreadExecutor();
	private ExecutorService actionCompletionExec = Executors.newSingleThreadExecutor();
	private BlockingQueue<PendingProcess> enqueuedProcesses = new LinkedBlockingQueue<>();
	private DelayQueue<PendingProcess> decidedActions = new DelayQueue<>();
	
	private void enqueue(MindProcessTask processTask) throws InterruptedException {
		log.debug("Enqueueing mind process task " + Thread.currentThread().getName());
		Future<Action> futureAction = processExec.submit(processTask);
		long startTime = System.currentTimeMillis();
		PendingProcess pendingProcess = new PendingProcess(processTask.creature, futureAction, startTime);
		enqueuedProcesses.put(pendingProcess);
	}

	public MasterMind() {
		processWatchExec.execute(new ProcessWatcherTask(enqueuedProcesses, decidedActions));
		actionCompletionExec.execute(new ActionCompleterTask(decidedActions));
	}
	
	public void letCreatureThink(Creature creature) {
		try {
			enqueue(new MindProcessTask(creature));
		} catch (Exception e) {
			log.warn("Error trying to enqueue a MindProcessTask", e);
			creature.actionIsDecided(Action.NONE_BY_FAILURE);
		}
	}

	public void shutdown() {
		log.info("Shutting down executors.");
		processExec.shutdownNow();
		processWatchExec.shutdownNow();
		actionCompletionExec.shutdownNow();
	}
}
