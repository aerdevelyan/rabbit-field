package rabbit_field.creature;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import rabbit_field.creature.MasterMind.PendingProcess;

public class CreatureControllerTest {
	ExecutorService exec = Executors.newCachedThreadPool();
	Creature creature = mock(Creature.class);
	DelayQueue<PendingProcess> decidedActions = new DelayQueue<>();
	BlockingQueue<StatusUpdate> statusUpdates = new LinkedBlockingQueue<>();
	
	@Test
	public void testCompleter() throws InterruptedException {
		Future<Action> completedAction = CompletableFuture.completedFuture(Action.NONE);
		ActionCompleterTask completerTask = new ActionCompleterTask(decidedActions, statusUpdates);
		exec.execute(completerTask);
		PendingProcess process = new PendingProcess(creature, completedAction, System.currentTimeMillis() + 400);
		decidedActions.put(process);
		completerTask.shutdown();
		exec.shutdown();
		exec.awaitTermination(5, SECONDS);
//		verify(creature).actionIsDecided(Action.NONE);
	}
	
}
