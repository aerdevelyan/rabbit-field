package rabbit_field;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;

import rabbit_field.creature.CreatureController;
import rabbit_field.creature.Rabbit;
import rabbit_field.event.ShutdownEvent;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Cell;
import rabbit_field.field.Plant;

/**
 * Creates and initializes the field, creatures and issues commands.
 */
@Singleton
public class Creator {
	private final static Logger log = LogManager.getLogger();
	private final EventBus eventBus;
	private final Field field;
	private final CreatureController creatureController; 
	private final int INIT_RABBITS = 5;
	private final int INIT_PLANTS = 50;
	
	@Inject
	public Creator(EventBus eventBus, Field field, CreatureController creatureController) {
		this.eventBus = eventBus;
		this.field = field;
		this.creatureController = creatureController;
	}

	public void initWorld() {
		log.info("Initializing world.");
		plants();
		creatures();
	}

	private void plants() {
		for (int n = 1; n <= INIT_PLANTS; n++) {
			if (n % Plant.Clover.RARITY == 0) {
				Cell cell = field.findRandomFreeCell();
				cell.addObject(new Plant.Clover());
			}
			if (n % Plant.Carrot.RARITY == 0) {
				Cell cell = field.findRandomFreeCell();
				cell.addObject(new Plant.Carrot());
			}
		}
	}

	private void creatures() {
		for (int n = 1; n <= INIT_RABBITS; n++) {
			Rabbit rabbit = new Rabbit("Rabbit-" + n, field);
			creatureController.introduce(rabbit);
		}
	}

	public void endWorld() {
		log.info("Apocalypse everyone!");
		eventBus.post(new ShutdownEvent());
		log.info("Apocalypse completed");
	}
}
