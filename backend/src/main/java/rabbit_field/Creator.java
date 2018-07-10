package rabbit_field;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import rabbit_field.creature.AbstractCyclicTask;
import rabbit_field.creature.Creature;
import rabbit_field.creature.CreatureController;
import rabbit_field.creature.Fox;
import rabbit_field.creature.Rabbit;
import rabbit_field.event.PauseResumeEvent;
import rabbit_field.event.ShutdownEvent;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Cell;
import rabbit_field.field.Plant;

/**
 * Creates and initializes the field, generates plants and creatures.
 */
@Singleton
public class Creator {
	private final static Logger log = LogManager.getLogger();
	private final int INIT_FOXES = 5;
	private final int INIT_RABBITS = 15;
	private final int INIT_PLANTS = 50;
	private final Field field;
	private final CreatureController creatureController; 
	private final ExecutorService plantGenExec = Executors.newSingleThreadExecutor(r -> new Thread(r, "Plant generator"));
	private final PlantGeneratorTask plantGenTask;
	
	@Inject
	public Creator(Field field, CreatureController creatureController) {
		this.field = field;
		this.creatureController = creatureController;
		plantGenTask = this.new PlantGeneratorTask(true);
	}

	public void initWorld() {
		log.info("Initializing world.");
		initPlants();
		initCreatures();
		plantGenExec.execute(plantGenTask);
	}
	
	@Subscribe
	public void pauseOrResume(PauseResumeEvent evt) {
		log.info("Received pause/resume event: {}", evt.isPause());
		evt.applyTo(plantGenTask);
	}
	
	@Subscribe 
	public void shutdown(ShutdownEvent evt) {
		evt.add(ShutdownEvent.Ordering.CREATOR, plantGenTask, plantGenExec, null);
	}
	
	private void initCreatures() {
		for (int n = 1; n <= INIT_RABBITS; n++) {
			Creature rabbit = new Rabbit("R-" + n, field);
			creatureController.introduce(rabbit);
		}
		for (int n = 1; n <= INIT_FOXES; n++) {
			Creature fox = new Fox("F-" + n, field);
			creatureController.introduce(fox);
		}
	}

	private void initPlants() {
		for (int n = 1; n <= INIT_PLANTS; n++) {
			generatePlants(n);
		}
	}

	private void generatePlants(int cycle) {
		if (cycle % Plant.Clover.RARITY == 0) {
			Cell cell = field.findRandomEmptyCell();
			if (cell != null) cell.addObject(new Plant.Clover());
		}
		if (cycle % Plant.Carrot.RARITY == 0) {
			Cell cell = field.findRandomEmptyCell();
			if (cell != null) cell.addObject(new Plant.Carrot());
		}		
	}
	
	class PlantGeneratorTask extends AbstractCyclicTask {
		private int cycle;
		
		public PlantGeneratorTask(boolean paused) {
			super(paused);
			setInterval(500, TimeUnit.MILLISECONDS);
		}

		@Override
		protected void runCycle() {
			generatePlants(++cycle);			
		}
	}
}

