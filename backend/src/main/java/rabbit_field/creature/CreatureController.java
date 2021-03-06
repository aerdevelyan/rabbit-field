package rabbit_field.creature;

import static java.util.concurrent.TimeUnit.SECONDS;

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
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.google.common.eventbus.Subscribe;

import rabbit_field.creature.MasterMind.PendingProcess;
import rabbit_field.event.PauseResumeEvent;
import rabbit_field.event.ResetEvent;
import rabbit_field.event.ShutdownEvent;
import rabbit_field.event.OrderedExecutionEvent.OrderingComponent;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Cell;
import rabbit_field.field.Field.Direction;
import rabbit_field.field.Position;

/**
 * Manages creatures.
 * Logic of life cycle:
 * - take Action objects from MasterMind via decidedActions queue
 * - perform an action on behalf of a Creature
 * - enqueue Creature to MasterMind again
 */
@Singleton
public class CreatureController {
	private final static Logger log = LogManager.getLogger();
	private final BlockingQueue<StatusUpdate> statusUpdates = new LinkedBlockingQueue<>(); 
	private final ExecutorService mindOutcomeWatchExec = Executors.newSingleThreadExecutor(r -> new Thread(r, "mind outcome taker"));
	private final ExecutorService updatesExec = Executors.newSingleThreadExecutor(r -> new Thread(r, "updates fulfillment"));
	private final DecidedActionsWatcherTask mindOutcomeWatcherTask;
	private final UpdatesFulfillmentTask updatesFulfillmentTask;
	
	@Inject
	public CreatureController(MasterMind masterMind, Field field) {
		mindOutcomeWatcherTask = new DecidedActionsWatcherTask(masterMind.getDecidedActions(), statusUpdates);
		updatesFulfillmentTask = new UpdatesFulfillmentTask(statusUpdates, field, masterMind, true);
		mindOutcomeWatchExec.execute(mindOutcomeWatcherTask);
		updatesExec.execute(updatesFulfillmentTask);
	}
	
	public void introduce(Creature creature) {
		statusUpdates.add(new StatusUpdate(StatusType.NEW, creature, Action.NONE));
	}

	@Subscribe 
	public void shutdown(ShutdownEvent evt) {
		evt.add(OrderingComponent.UPDATES_FULFILLMENT, updatesFulfillmentTask, updatesExec)
		   .add(OrderingComponent.MIND_OUTCOME_WATCHER, mindOutcomeWatcherTask, mindOutcomeWatchExec);
	}
	
	@Subscribe
	public void reset(ResetEvent evt) {
		evt.addForExecution(OrderingComponent.UPDATES_FULFILLMENT, () -> {
			log.info("Resetting CreatureController: clearing creature status updates.");
			statusUpdates.clear();			
		});
	}
	
	@Subscribe
	public void pauseOrResume(PauseResumeEvent evt) {
		log.info("Received pause/resume event: {}", evt.isPause());
		evt.applyTo(updatesFulfillmentTask);
	}
}

class UpdatesFulfillmentTask extends AbstractCyclicTask {
	private final static Logger log = LogManager.getLogger();
	private static final Marker ACT_MARKER = MarkerManager.getMarker("ACT");
	private final BlockingQueue<StatusUpdate> statusUpdates;
	private final Field field;
	private final MasterMind masterMind;
	
	public UpdatesFulfillmentTask(BlockingQueue<StatusUpdate> statusUpdates, Field field, MasterMind masterMind, boolean paused) {
		super(paused);
		this.statusUpdates = statusUpdates;
		this.field = field;
		this.masterMind = masterMind;
	}
	
	public UpdatesFulfillmentTask(BlockingQueue<StatusUpdate> statusUpdates, Field field, MasterMind masterMind) {
		super(false);
		this.statusUpdates = statusUpdates;
		this.field = field;
		this.masterMind = masterMind;
	}

	@Override
	protected void runCycle() {
		try {
			StatusUpdate update = statusUpdates.poll(1, SECONDS);
			if (update == null) return;
			log.debug("Got creature status update {}", update);
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
			Direction desiredDir = ((Action.Move) action).getDirection();
			Position desiredPos = creature.getPosition().calculateNewPosition(desiredDir);
			Cell desiredCell = field.findCellBy(desiredPos);
			if (creature.canMove() && creature.canMoveToCellWith(desiredCell.getObjView())) {
				field.move(creature, desiredDir);  // TODO handle false return					
				creature.decrementStamina();
			}
			else {
				log.debug(ACT_MARKER, "Denied to move {} to {}, {}", creature, desiredPos, desiredDir);
			}
		}
		else if (action instanceof Action.Eat) {
			Action.Eat eat = (Action.Eat) action;
			if (creature.canEat(eat.getDesiredObject())) {
				Cell cell = field.findCellBy(creature.getPosition());
				cell.findFirstByClass(eat.getDesiredObject()).ifPresentOrElse(eaten -> {
					if (Creature.class.isInstance(eaten)) {
						((Creature) eaten).die("was eaten");
					}
					creature.boostStamina(eaten.calories());
					cell.removeObject(eaten);
					log.debug(ACT_MARKER, "{} ate {}", creature, eaten);					
				}, () -> {  
					log.warn(ACT_MARKER, "{} could not eat desired object: not found in the cell.", creature);
				});
			}
			else {
				log.error("Creature {} cannot eat {}", creature, eat.getDesiredObject());
			}
		}
		creature.incrementAge();
		if (creature.isAlive()) {
			masterMind.letCreatureThink(creature);
		}
		else {
			log.info("Removing dead creature {}", creature);
			Cell cell = field.findCellBy(creature.getPosition());
			if (cell != null) {
				cell.removeObject(creature);
			}
		}
	}

	private void addCreature(Creature creature) {
		Cell cell = field.findRandomEmptyCell();
		if (cell == null) {
			log.warn("Could not find empty cell to add a new creature.");
			return;
		}
		cell.addObject(creature);
		masterMind.letCreatureThink(creature);
	}
}

/** 
 * Takes creature actions from decidedActions queue after delay is done, 
 * produces and enqueues creature status updates.
 */
class DecidedActionsWatcherTask extends AbstractCyclicTask {
	private final static Logger log = LogManager.getLogger();
	private final DelayQueue<PendingProcess> decidedActions;
	private final BlockingQueue<StatusUpdate> statusUpdates;
	
	public DecidedActionsWatcherTask(DelayQueue<PendingProcess> decidedActions, BlockingQueue<StatusUpdate> statusUpdates) {
		this.decidedActions = decidedActions;
		this.statusUpdates = statusUpdates;
	}

	@Override protected void runCycle() {		
		try {
			log.debug("Examining decided actions queue.");
			PendingProcess process = decidedActions.poll(1, SECONDS);
			if (process == null) return;
			if (process.futureAction.isCancelled()) {
				statusUpdates.put(new StatusUpdate(StatusType.DECIDED, process.creature, Action.NONE_BY_CANCEL));
			}
			else {
				if (!process.creature.isAlive()) {
					log.debug("Discarding decision of dead creature {}", process.creature);
					return;
				}
				statusUpdates.put(new StatusUpdate(StatusType.DECIDED, process.creature, process.futureAction.get()));
			}
		} catch (InterruptedException e) {
			log.debug("DecidedActionsWatcherTask was interrupted.", e);
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

	@Override
	public String toString() {
		return "StatusUpdate [statusType=" + statusType + ", creature=" + creature + ", action=" + action + "]";
	}
}

