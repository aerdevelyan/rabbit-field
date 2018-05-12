package rabbit_field.creature;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import rabbit_field.Field;
import rabbit_field.creature.MasterMind.ActionCompleterTask;
import rabbit_field.creature.MasterMind.PendingProcess;
import rabbit_field.creature.MasterMind.ProcessWatcherTask;

@Deprecated
@Ignore
public class JMMasterMindTest {
	ExecutorService exec = Executors.newSingleThreadExecutor();
	@Mocked Creature creature;
	@Mocked Field field;
	
	@Test @Ignore("too heavy to execute routinely")
	public void wholeMasterMindExecution() throws InterruptedException {
		MasterMind masterMind = new MasterMind();
		Creature rabbit1 = new Rabbit("Bugz", masterMind, field);
		Creature rabbit2 = new Rabbit("Bill", masterMind, field);
		Creature rabbit3 = new Rabbit("Nina", masterMind, field);
		Creature rabbit4 = new Rabbit("Anna", masterMind, field);
		masterMind.letMeThink(rabbit1);
		masterMind.letMeThink(rabbit2);
		masterMind.letMeThink(rabbit3);
		masterMind.letMeThink(rabbit4);
		TimeUnit.SECONDS.sleep(10);
		masterMind.shutdown();
	}

	@Test
	public void completer() throws InterruptedException {
		new Expectations() {{
			creature.getSpeed(); result = 2f;
			creature.actionIsDecided(Action.NONE); times = 1;
		}};
		
		Future<Action> completedAction = CompletableFuture.completedFuture(Action.NONE);
		BlockingQueue<PendingProcess> decidedActions = new DelayQueue<>();
		ActionCompleterTask completerTask = new ActionCompleterTask(decidedActions);
		exec.execute(completerTask);
		PendingProcess process = new PendingProcess(creature, completedAction, System.currentTimeMillis()+400);
		decidedActions.put(process);
		TimeUnit.SECONDS.sleep(1);
		exec.shutdown();
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
		new Expectations() {{
			creature.getSpeed(); result = 2f;
		}};
		
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
	
	
	
	private Future<Action> createFuture(boolean canceled, boolean done, Optional<Function<Boolean, Boolean>> onCancel) {
		return new Future<Action>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				if (onCancel.isPresent()) {
					return onCancel.get().apply(mayInterruptIfRunning);
				}
				return false;
			}

			@Override
			public boolean isCancelled() {
				return canceled;
			}

			@Override
			public boolean isDone() {
				return done;
			}

			@Override
			public Action get() throws InterruptedException, ExecutionException {
				return null;
			}

			@Override
			public Action get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return null;
			}
		};
	}
}