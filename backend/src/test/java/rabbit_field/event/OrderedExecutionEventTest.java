package rabbit_field.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutorService;

import org.junit.Test;

import rabbit_field.creature.AbstractCyclicTask;
import rabbit_field.event.OrderedExecutionEvent.OrderingComponent;

public class OrderedExecutionEventTest {

	@Test
	public void shutdownEvent() throws Exception {
		AbstractCyclicTask task = mock(AbstractCyclicTask.class);
		ExecutorService executor = mock(ExecutorService.class);
		ShutdownEvent event = new ShutdownEvent();
		event.add(OrderingComponent.UPDATES_FULFILLMENT, task, executor);
		event.add(OrderingComponent.CREATOR, task, executor);
		event.executeInOrder();
		verify(task, times(2)).shutdown();
		verify(executor, times(2)).shutdown();
	}
	
	@Test
	public void resetEvent() throws Exception {
		Runnable runnable = mock(Runnable.class);
		ResetEvent event = new ResetEvent();
		event.addForExecution(OrderingComponent.UPDATES_FULFILLMENT, runnable);
		event.addForExecution(OrderingComponent.MASTER_MIND, runnable);
		event.executeInOrder();
		verify(runnable, times(2)).run();
	}
}
