package rabbit_field;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;

import rabbit_field.creature.CreatureController;
import rabbit_field.creature.MasterMind;
import rabbit_field.creature.Rabbit;
import rabbit_field.event.ShutdownEvent;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Cell;

/**
 * Creates and initializes the field, creatures and issues commands.
 */
@Singleton
public class Creator {
	private final static Logger log = LogManager.getLogger();
	private final EventBus eventBus;
	private final Field field;
	private final CreatureController creatureController; 
	private final int INIT_RABBITS = 2;
	
	@Inject
	public Creator(EventBus eventBus, Field field, CreatureController creatureController) {
		this.eventBus = eventBus;
		this.field = field;
		this.creatureController = creatureController;
	}

	public void initWorld() {
		log.info("Initializing world.");
		field();
		creatures();
	}

	private void field() {
		
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
