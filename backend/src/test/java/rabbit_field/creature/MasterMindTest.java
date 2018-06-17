package rabbit_field.creature;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;

import rabbit_field.creature.MasterMind.PendingProcess;
import rabbit_field.creature.MasterMind.ProcessWatcherTask;

public class MasterMindTest {
	ExecutorService exec = Executors.newCachedThreadPool();
	Creature creature = mock(Creature.class);
	BlockingQueue<PendingProcess> enqueuedProcesses = new LinkedBlockingQueue<>();
	DelayQueue<PendingProcess> decidedActions = new DelayQueue<>();
	
	@Before
	public void prepare() {
		when(creature.getSpeed()).thenReturn(2f);
		when(creature.toString()).thenReturn("testcreature");
	}
	
	@Test
	public void processWatcherHandlesCompletedProcess() throws InterruptedException {
		ProcessWatcherTask processWatcher = new ProcessWatcherTask(enqueuedProcesses, decidedActions);
		Future<Action> completedAction = CompletableFuture.completedFuture(Action.NONE);
		PendingProcess process = new PendingProcess(creature, completedAction, System.currentTimeMillis());
		enqueuedProcesses.add(process);
		
		exec.execute(processWatcher);
		processWatcher.shutdown();
		exec.shutdown();
		exec.awaitTermination(5, SECONDS);
		
		assertThat(enqueuedProcesses).isEmpty();
		assertThat(decidedActions).hasSize(1);
		assertThat(decidedActions).contains(process);
	}

	@Test
	public void processWatcherExpiration() throws InterruptedException, ExecutionException {		
		ProcessWatcherTask processWatcher = new ProcessWatcherTask(enqueuedProcesses, decidedActions);		
		exec.execute(processWatcher);
		Future<Action> slowAction = exec.submit(() -> { SECONDS.sleep(2); return Action.NONE; });
		PendingProcess process = new PendingProcess(creature, slowAction, System.currentTimeMillis());
		enqueuedProcesses.add(process);
		SECONDS.sleep(1);
		processWatcher.shutdown();
		exec.shutdown();
		exec.awaitTermination(5, SECONDS);
		
		assertThat(enqueuedProcesses).isEmpty();
		assertThat(decidedActions).hasSize(1);
		assertThat(decidedActions.take().futureAction.isCancelled()).isTrue();
	}
}
