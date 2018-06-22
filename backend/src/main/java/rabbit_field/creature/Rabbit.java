package rabbit_field.creature;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.creature.Action.Move;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Direction;

public class Rabbit extends Creature {
	private static Logger log = LogManager.getLogger();
	public static final int MAX_AGE = 100;
	public static final int INITIAL_STAMINA = 50;
	public static final float SPEED = 2f;
	
	/**
	 * Each rabbit can have a name. TODO move to Creature?
	 */
	private String name;
	
	public Rabbit(String name, /*MasterMind mind,*/ Field field) {
		super(field);
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
		Random rnd = new Random();
		Action action = new Action.Move(Field.Direction.values()[rnd.nextInt(3)]);
		try {
			log.debug(name + " thinking...");
			TimeUnit.MILLISECONDS.sleep(150 + rnd.nextInt(50));
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
