package rabbit_field;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;

import rabbit_field.Field.Cell;
import rabbit_field.creature.MasterMind;
import rabbit_field.creature.Rabbit;
import rabbit_field.event.ShutdownEvent;

/**
 * God-like object that creates the field, creatures and issues commands.
 *
 */
@Singleton
public class Creator {
	private final static Logger log = LogManager.getLogger();
	private final EventBus eventBus;
	private final Field field;
	private final MasterMind masterMind; 
	private final int INIT_RABBITS = 2;
	
	@Inject
	public Creator(EventBus eventBus, Field field, MasterMind masterMind) {
		this.eventBus = eventBus;
		this.field = field;
		this.masterMind = masterMind;
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
			Rabbit rabbit = new Rabbit("Rabbit-" + n, masterMind, field);
			Cell freeCell = findRandomFreeCell();
			freeCell.addObject(rabbit);
		}
	}

	private Field.Cell findRandomFreeCell() {
		Cell freeCell;
		Cell[][] cells = field.getCells();
		Random rnd = new Random();
		while (true) {
			Cell rndCell = cells[rnd.nextInt(Field.HOR_SIZE)][rnd.nextInt(Field.VERT_SIZE)];
			if (rndCell.getObjects().size() == 0) {
				freeCell = rndCell;
				break;
			}
		}
		return freeCell;
	}

	public void endWorld() {
		log.info("Apocalypse everyone!");
		eventBus.post(new ShutdownEvent());
		log.info("Apocalypse completed");
	}
}
