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
	public static final int MAX_AGE = 200;
	public static final int INITIAL_STAMINA = 100; //TODO 100% of init stamina should be standard (impl in Creature) 
	public static final float SPEED = 2.0f;
	
	/**
	 * Each rabbit can have a name. TODO move to Creature?
	 */
	private String name;
	
	public Rabbit(String name, Field field) {
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
		long start = System.currentTimeMillis();
		Action action = new Action.Move(chooseRandomDirection());
//		try {
//			TimeUnit.MILLISECONDS.sleep(new Random().nextInt(150));
//		} catch (InterruptedException e) {
//			log.warn("Got interrupted {}", this);
//		}
		log.debug("{} thinked {}ms, decided to: {}", name, (System.currentTimeMillis() - start), action);
		return action;
	}

	private Direction chooseRandomDirection() {
		Direction direction;
		do {
			direction = Direction.values()[new Random().nextInt(Direction.values().length)];
		} 
		while (!getField().isMoveAllowed(this.getPosition(), direction));
		return direction;
	}

	@Override
	public String toString() {
		return "Rabbit " + name + "(a:" + getAge() + ",s:" + getStamina() + ")";
	}
	
}
