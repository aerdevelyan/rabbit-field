package rabbit_field.event;

import static java.util.concurrent.TimeUnit.SECONDS;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.CREATOR;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.MASTER_MIND;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.MIND_OUTCOME_WATCHER;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.MIND_PROCESS_WATCHER;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.UPDATES_FULFILLMENT;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.VIEW_SENDER;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.WEB_SERVER;

import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.creature.AbstractCyclicTask;
import rabbit_field.event.OrderedExecutionEvent.OrderingComponent;

public class ShutdownEvent extends OrderedExecutionEvent<ShutdownExecutionUnit> {
	private static Logger log = LogManager.getLogger();

	public ShutdownEvent add(OrderingComponent ordering, AbstractCyclicTask task, ExecutorService executor) {
		addUnit(new ShutdownExecutionUnit(ordering, task, executor, null));
		return this;
	}
	
	public void addForExecution(OrderingComponent orderingCmp, Runnable runnable) {
		addUnit(new ShutdownExecutionUnit(orderingCmp, null, null, runnable));
	}

	@Override
	protected void executeUnit(ShutdownExecutionUnit eu) {
		log.info("Shutting down {}", eu.orderingComponent());
		
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
	}

	@Override
	protected OrderingComponent[] ordering() {
		return new OrderingComponent[] { 
			CREATOR, MASTER_MIND, MIND_PROCESS_WATCHER, MIND_OUTCOME_WATCHER, 
			UPDATES_FULFILLMENT, VIEW_SENDER, WEB_SERVER 
		};
	}
}

class ShutdownExecutionUnit extends ExecutionUnit {
	public final AbstractCyclicTask task; 
	public final ExecutorService executor;
	
	public ShutdownExecutionUnit(OrderingComponent orderingCmp, AbstractCyclicTask task, ExecutorService executor, Runnable runnable) {
		super(orderingCmp, runnable);
		this.task = task;
		this.executor = executor;
	}
}
