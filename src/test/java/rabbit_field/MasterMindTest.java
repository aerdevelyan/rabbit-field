package rabbit_field;

import static org.junit.Assert.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import rabbit_field.MasterMind.ActionCompleterTask;
import rabbit_field.MasterMind.PendingProcess;

public class MasterMindTest {
	ExecutorService exec = Executors.newSingleThreadExecutor();
	@Mocked Creature creature;
	@Mocked Field field;
	
	//@Test
	public void testWholeMasterMind() throws InterruptedException {
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
	public void testCompleter() throws InterruptedException {
		new Expectations() {{
			creature.getSpeed(); result = 2f;
			creature.actionIsDecided(Action.NONE); times = 1;
		}};
		
		Future<Action> futureAction = CompletableFuture.completedFuture(Action.NONE);
		BlockingQueue<PendingProcess> decidedActions = new DelayQueue<>();
		ActionCompleterTask completerTask = new ActionCompleterTask(decidedActions);
		exec.execute(completerTask);
		PendingProcess process = new PendingProcess(creature, futureAction, System.currentTimeMillis()+400);
		decidedActions.put(process);
		TimeUnit.SECONDS.sleep(1);
		exec.shutdown();
	}
}
