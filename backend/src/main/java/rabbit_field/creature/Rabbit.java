package rabbit_field.creature;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.Field;
import rabbit_field.creature.Action.Move;
import rabbit_field.creature.Action.Move.Direction;

public class Rabbit extends Creature {
	private static Logger log = LogManager.getLogger();
	public static final int MAX_AGE = 100;
	public static final int INITIAL_STAMINA = 50;
	public static final float SPEED = 2f;
	
	/**
	 * Each rabbit can have a name.
	 */
	private String name;
	
	public Rabbit(String name, MasterMind mind, Field field) {
		super(mind, field);
		this.name = name;
		setStamina(INITIAL_STAMINA);
	}

	@Override
	public int getMaxAge() {
		return MAX_AGE;
	}

	@Override
	public float getSpeed() {
		return SPEED;
	}

	@Override
	public Action decideAction() {
		Random r = new Random(47);
		Action action = new Action.Move(Direction.values()[r.nextInt(3)]);
		try {
			log.debug(name + " thinking...");
			TimeUnit.MILLISECONDS.sleep(50 + r.nextInt(50));
		} catch (InterruptedException e) {
			log.warn("Got interrupted");
		}
		log.debug(name + " decided: " + action);
		return action;
	}


	@Override
	public String toString() {
		return "Rabbit " + name + "(" + getAge() + "/" + getStamina() + ")";
	}
	
}
