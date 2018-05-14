package rabbit_field.creature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rabbit_field.creature.MasterMind.ActionCompleterTask;
import rabbit_field.creature.MasterMind.PendingProcess;
import rabbit_field.creature.MasterMind.ProcessWatcherTask;

public class MasterMindTest {
	ExecutorService exec = Executors.newSingleThreadExecutor();
	Creature creature = mock(Creature.class);

	@Test
	public void testWithAJ() {
		ConcurrentMap<Integer, String> map = new ConcurrentHashMap<>(Map.ofEntries(Map.entry(1, "a"), Map.entry(2, "s")));
		String prev1 = map.put(2, "b");
		String prev2 = map.putIfAbsent(3, "c");
		
		assertThat(prev1).isEqualTo("s");
		assertThat(prev2).isNull();
		assertThat(map).containsEntry(1, "a");
		assertThat(map).containsEntry(3, "c");
	}

	@Test
	public void testCompleter() throws InterruptedException {
		when(creature.getSpeed()).thenReturn(2f);
		
		Future<Action> completedAction = CompletableFuture.completedFuture(Action.NONE);
		BlockingQueue<PendingProcess> decidedActions = new DelayQueue<>();
		ActionCompleterTask completerTask = new ActionCompleterTask(decidedActions);
		exec.execute(completerTask);
		PendingProcess process = new PendingProcess(creature, completedAction, System.currentTimeMillis() + 400);
		decidedActions.put(process);
		TimeUnit.SECONDS.sleep(1);
		exec.shutdown();

		verify(creature).actionIsDecided(Action.NONE);
	}
	
	@Test
	public void processWatcher() throws InterruptedException {
		BlockingQueue<PendingProcess> enqueuedProcesses = new LinkedBlockingQueue<>();
		BlockingQueue<PendingProcess> decidedActions = new DelayQueue<>();
		ProcessWatcherTask processWatcher = new ProcessWatcherTask(enqueuedProcesses, decidedActions);

		Future<Action> completedAction = CompletableFuture.completedFuture(Action.NONE);
		PendingProcess process = new PendingProcess(creature, completedAction, System.currentTimeMillis());
		enqueuedProcesses.add(process);
		
		exec.execute(processWatcher);
		TimeUnit.SECONDS.sleep(1);
		exec.shutdown();
		
		assertThat(enqueuedProcesses, is(empty()));
		assertThat(decidedActions, hasSize(1));
		assertThat(decidedActions, hasItem(process));
	}

	@Test
	public void processWatcherExpiration() throws InterruptedException, ExecutionException {
		when(creature.getSpeed()).thenReturn(2f);
		
		BlockingQueue<PendingProcess> enqueuedProcesses = new LinkedBlockingQueue<>();
		BlockingQueue<PendingProcess> decidedActions = new DelayQueue<>();
		ProcessWatcherTask processWatcher = new ProcessWatcherTask(enqueuedProcesses, decidedActions);
		exec = Executors.newCachedThreadPool();
		
		Future<Action> slowAction = exec.submit(() -> { TimeUnit.SECONDS.sleep(1); return Action.NONE; });
		PendingProcess process = new PendingProcess(creature, slowAction, System.currentTimeMillis());
		enqueuedProcesses.add(process);
		exec.execute(processWatcher);
		TimeUnit.SECONDS.sleep(1);
		exec.shutdownNow();
		
		assertThat(enqueuedProcesses, is(empty()));
		assertThat(decidedActions, hasSize(1));
		assertThat(decidedActions.take().futureAction.isCancelled(), is(true));
	}
}
