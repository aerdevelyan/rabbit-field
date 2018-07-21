package rabbit_field.event;

import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.CREATOR;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.MASTER_MIND;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.UPDATES_FULFILLMENT;
import static rabbit_field.event.OrderedExecutionEvent.OrderingComponent.VIEW_SENDER;

public class ResetEvent extends OrderedExecutionEvent<ExecutionUnit> {
	@Override
	protected OrderingComponent[] ordering() {
		return new OrderingComponent[] { UPDATES_FULFILLMENT, MASTER_MIND, CREATOR, VIEW_SENDER };
	}
	
	public void addForExecution(OrderingComponent orderingCmp, Runnable runnable) {
		addUnit(new ExecutionUnit(orderingCmp, runnable));
	}
}
