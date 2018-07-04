package rabbit_field.event;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.creature.AbstractCyclicTask;

public class ShutdownEvent {
	private static Logger log = LogManager.getLogger();
	private PriorityQueue<ExecutionUnit> queue = new PriorityQueue<>();
	
	public enum Ordering {
		CREATOR, MASTER_MIND, MIND_PROCESS_WATCHER, MIND_OUTCOME_WATCHER, 
		UPDATES_FULFILLMENT, VIEW_SENDER, WEB_SERVER,
	}
	
	public ShutdownEvent add(Ordering ordering, AbstractCyclicTask task, ExecutorService executor, Runnable runnable) {
		queue.add(new ExecutionUnit(ordering, task, executor, runnable));
		return this;
	}
	
	public void performShutdown() {
		log.info("Shutting down tasks and executors.");
		while (!queue.isEmpty()) {
			ExecutionUnit eu = queue.remove();
			log.info("Shutting down {}", eu.ordering);
			
			if (eu.task != null) {
				eu.task.shutdown();
			}
			
			if (eu.executor != null) {
				eu.executor.shutdown();
				boolean terminated = false;
				try {
					terminated = eu.executor.awaitTermination(5, SECONDS);
				} catch (InterruptedException e) {
					log.warn("Interrupt while waiting for termination of executor.", e);
				}

				if (!terminated) {
					log.warn("Executor did not terminated before timeout. Halting with shutdownNow().");
					eu.executor.shutdownNow();
				}
			}
			
			if (eu.runnable != null) {
				eu.runnable.run();
			}
		}
		log.info("Shutdown completed.");
	}
	
	private static class ExecutionUnit implements Comparable<ExecutionUnit> {
		public final Ordering ordering;
		public final AbstractCyclicTask task; 
		public final ExecutorService executor;
		public final Runnable runnable;

		public ExecutionUnit(Ordering ordering, AbstractCyclicTask task, ExecutorService executor, Runnable runnable) {
			this.ordering = ordering;
			this.task = task;
			this.executor = executor;
			this.runnable = runnable;
		}

		@Override
		public int compareTo(ExecutionUnit other) {
			return this.ordering.compareTo(other.ordering);
		}
	}
}
