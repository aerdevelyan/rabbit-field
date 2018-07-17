package rabbit_field.event;

import java.util.EnumMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.event.OrderedExecutionEvent.OrderingComponent;

/**
 * Base class for visitor events that collect executables for later 
 * execution (after event delivered to all subscribers) in defined order.
 * @param <T>
 */
public abstract class OrderedExecutionEvent<T extends ExecutionUnit> {
	private static Logger log = LogManager.getLogger();
	private EnumMap<OrderingComponent, T> units = new EnumMap<>(OrderingComponent.class);

	public enum OrderingComponent {
		CREATOR, MASTER_MIND, MIND_PROCESS_WATCHER, MIND_OUTCOME_WATCHER, 
		UPDATES_FULFILLMENT, VIEW_SENDER, WEB_SERVER,
	}

	public void addUnit(T unit) {
		units.put(unit.orderingComponent(), unit);
	}
	
	public void executeInOrder() {
		for (OrderingComponent component : ordering()) {
			T eu = units.get(component);
			if (eu != null) {
				executeUnit(eu);
				if (eu.runnable() != null) {
					eu.runnable().run();
				}
			}
			else {
				log.warn("No ExecutionUnit was found for component {}", component);
			}
		}
	}

	protected void executeUnit(T eu) {	}
	
	protected abstract OrderingComponent[] ordering();
}

class ExecutionUnit {
	private final OrderingComponent orderingCmp;
	private final Runnable runnable;

	public ExecutionUnit(OrderingComponent orderingCmp, Runnable runnable) {
		this.orderingCmp = orderingCmp;
		this.runnable = runnable;
	}

	public OrderingComponent orderingComponent() {
		return orderingCmp;
	}
	
	public Runnable runnable() {
		return runnable;
	}
}

