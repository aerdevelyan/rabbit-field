package rabbit_field.creature;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.field.Field;
import rabbit_field.field.Field.Direction;

public class Rabbit extends Creature {
	private static Logger log = LogManager.getLogger();
	public static final int MAX_AGE = 200; 
	public static final float SPEED = 2.0f;
	
	public Rabbit(String name, Field field) {
		super(name, field);
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
		log.debug("{} thinked {}ms, decided to: {}", getName(), (System.currentTimeMillis() - start), action);
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
		return "Rabbit " + getName() + "(a:" + getAge() + ",s:" + getStamina() + ")";
	}
	
}
