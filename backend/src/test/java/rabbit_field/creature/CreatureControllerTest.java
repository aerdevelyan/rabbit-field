package rabbit_field.creature;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import rabbit_field.Field;
import rabbit_field.Field.Direction;
import rabbit_field.Field.Position;
import rabbit_field.FieldObject;
import rabbit_field.creature.MasterMind.PendingProcess;

public class CreatureControllerTest {
	ExecutorService exec = Executors.newCachedThreadPool();
	Creature creature = mock(Creature.class);
	MasterMind masterMind = mock(MasterMind.class);
	DelayQueue<PendingProcess> decidedActions = new DelayQueue<>();
	BlockingQueue<StatusUpdate> statusUpdates = new LinkedBlockingQueue<>();
	Field field = new Field(); //mock(Field.class);
	
	@Test
	public void testCompleter() throws InterruptedException {
		Future<Action> completedAction = CompletableFuture.completedFuture(Action.NONE);
		DecidedActionsWatcherTask completerTask = new DecidedActionsWatcherTask(decidedActions, statusUpdates);
		exec.execute(completerTask);
		PendingProcess process = new PendingProcess(creature, completedAction, System.currentTimeMillis() + 400);
		decidedActions.put(process);
		completerTask.shutdown();
		exec.shutdown();
		exec.awaitTermination(5, SECONDS);
//		verify(creature).actionIsDecided(Action.NONE);
	}
	
	
	@Test
	public void updateFulfillment_NewCreature() throws Exception {
		creature = new Rabbit("test_cr", field);
		statusUpdates.add(new StatusUpdate(StatusType.NEW, creature, Action.NONE));
		UpdatesFulfillmentTask uft = new UpdatesFulfillmentTask(statusUpdates, field, masterMind);
		uft.shutdown();
		uft.run();
		assertThat(creature.getPosition()).isNotNull();
		Set<FieldObject> fobjects = field.findCellBy(creature.getPosition()).getObjects();
		assertThat(fobjects).hasSize(1).contains(creature);
		verify(masterMind).letCreatureThink(creature);
	}
	
	@Test
	public void updateFulfillment_AccomplishAction() throws Exception {
		creature = new Rabbit("test_cr", field);
		field.findRandomFreeCell().addObject(creature);
		Position origPosition = creature.getPosition();
		int origAge = creature.getAge();
		int origStamina = creature.getStamina();
		statusUpdates.add(new StatusUpdate(StatusType.DECIDED, creature, new Action.Move(Direction.NORTH))); //TODO check movement possible
		UpdatesFulfillmentTask uft = new UpdatesFulfillmentTask(statusUpdates, field, masterMind);
		uft.shutdown();
		uft.run();
		assertThat(creature.getPosition().getVpos()).isEqualTo(origPosition.getVpos() - 1);
		assertThat(creature.getAge()).isEqualTo(origAge + 1);
		assertThat(creature.getStamina()).isEqualTo(origStamina - 1);
		verify(masterMind).letCreatureThink(creature);
	}
	
}
