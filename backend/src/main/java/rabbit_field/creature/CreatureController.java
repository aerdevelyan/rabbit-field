package rabbit_field.creature;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import rabbit_field.Field;
import rabbit_field.Field.Cell;
import rabbit_field.creature.Action.Move;
import rabbit_field.creature.MasterMind.PendingProcess;
import rabbit_field.event.ShutdownEvent;

/**
 * Manages creatures.
 *  
 */
@Singleton
public class CreatureController {
	private final static Logger log = LogManager.getLogger();
//	private final MasterMind masterMind;
//	private final Field field;
	private final BlockingQueue<StatusUpdate> statusUpdates = new LinkedBlockingQueue<>(); 
	private final ExecutorService mindOutcomeWatchExec = Executors.newSingleThreadExecutor(r -> new Thread(r, "mind outcome taker"));
	private final ExecutorService updatesExec = Executors.newSingleThreadExecutor(r -> new Thread(r, "updates fulfillment"));
	private final ActionCompleterTask actionCompleterTask;
	private final UpdatesFulfillmentTask updatesFulfillmentTask;
	
	@Inject
	public CreatureController(MasterMind masterMind, Field field) {
//		this.masterMind = masterMind;
//		this.field = field;
		actionCompleterTask = new ActionCompleterTask(masterMind.getDecidedActions(), statusUpdates);
		mindOutcomeWatchExec.execute(actionCompleterTask);
		updatesFulfillmentTask = new UpdatesFulfillmentTask(statusUpdates, field, masterMind);
		updatesExec.execute(updatesFulfillmentTask);
	}
	
	public void introduce(Creature creature) {
		statusUpdates.add(new StatusUpdate(StatusType.NEW, creature, Action.NONE));
	}

	@Subscribe 
	public void shutdown(ShutdownEvent evt) {
		actionCompleterTask.shutdown();
		updatesFulfillmentTask.shutdown();
		mindOutcomeWatchExec.shutdown();
		updatesExec.shutdown();
		try {
			mindOutcomeWatchExec.awaitTermination(10, SECONDS);
			updatesExec.awaitTermination(10, SECONDS);
		} catch (InterruptedException e) {
			log.error("Interrupt while waiting for termination of executors", e);
		}
	}
}

class UpdatesFulfillmentTask extends AbstractWatcherTask {
	private final static Logger log = LogManager.getLogger();
	private final BlockingQueue<StatusUpdate> statusUpdates;
	private final Field field;
	private final MasterMind masterMind;
	
	public UpdatesFulfillmentTask(BlockingQueue<StatusUpdate> statusUpdates, Field field, MasterMind masterMind) {
		this.statusUpdates = statusUpdates;
		this.field = field;
		this.masterMind = masterMind;
	}

	@Override
	protected void watchCycle() {
		try {
			StatusUpdate update = statusUpdates.poll(1, SECONDS);
			switch (update.getStatusType()) {
			case DECIDED: accomplishAction(update.getCreature(), update.getAction());
				break;
			case NEW: addCreature(update.getCreature());
				break;
			}
		} catch (InterruptedException e) {
			log.debug("UpdatesFulfillmentTask was interrupted.", e);
			Thread.currentThread().interrupt();
		}
	}

	private void accomplishAction(Creature creature, Action action) {
		if (action instanceof Action.Move) {
			field.move(creature, ((Action.Move) action).getDirection());
		}
		creature.decrementStamina();
		creature.incrementAge();
	}

	private void addCreature(Creature creature) {
		Cell freeCell = field.findRandomFreeCell();
		freeCell.addObject(creature);
		try {
			masterMind.letCreatureThink(creature);
		} catch (MasterMindException e) {
			accomplishAction(creature, Action.NONE_BY_FAILURE);
		}
	}
	

}

/** 
 * Takes creature actions from decidedActions queue after delay is done, 
 * notifies the Creature to perform these actions on the Field.
 */
class ActionCompleterTask extends AbstractWatcherTask {
	private final static Logger log = LogManager.getLogger();
	private final DelayQueue<PendingProcess> decidedActions;
	private final BlockingQueue<StatusUpdate> statusUpdates;
	
	public ActionCompleterTask(DelayQueue<PendingProcess> decidedActions, BlockingQueue<StatusUpdate> statusUpdates) {
		this.decidedActions = decidedActions;
		this.statusUpdates = statusUpdates;
	}

	@Override protected void watchCycle() {			
		try {
			log.debug("Examining decided actions queue.");
			PendingProcess process = decidedActions.poll(1, SECONDS);
			if (process == null) return;
			if (process.futureAction.isCancelled()) {
//				process.creature.actionIsDecided(Action.NONE_BY_CANCEL);
				statusUpdates.put(new StatusUpdate(StatusType.DECIDED, process.creature, Action.NONE_BY_CANCEL));
			}
			else {
//				process.creature.actionIsDecided(process.futureAction.get());
				statusUpdates.put(new StatusUpdate(StatusType.DECIDED, process.creature, process.futureAction.get()));
			}
		} catch (InterruptedException e) {
			log.debug("ActionCompleter was interrupted.", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			log.warn("Exception during thinking on Action.", e);
		}
	}
}

enum StatusType {
	NEW, DECIDED		
}

class StatusUpdate {
	private final StatusType statusType;
	private final Creature creature;
	private final Action action;

	public StatusUpdate(StatusType statusType, Creature creature, Action action) {
		this.statusType = statusType;
		this.creature = creature;
		this.action = action;
	}

	public StatusType getStatusType() {
		return statusType;
	}

	public Creature getCreature() {
		return creature;
	}

	public Action getAction() {
		return action;
	}		
}

