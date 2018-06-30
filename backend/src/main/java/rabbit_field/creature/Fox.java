package rabbit_field.creature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.field.Field;
import rabbit_field.field.Field.Direction;

public class Fox extends Creature {
	private static Logger log = LogManager.getLogger();
	public static final int MAX_AGE = 300;
	public static final float SPEED = 1.85f;

	public Fox(String name, Field field) {
		super(name, field);
	}

	@Override
	public int calories() {
		return 0;
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
	public String toString() {
		return "Fox " + getName() + "(a:" + getAge() + ",s:" + getStamina() + ")";
	}
	
	@Override
	public Action decideAction() {
		long start = System.currentTimeMillis();
		Action action = Action.NONE;
		Direction direction = chooseRandomDirection();
		if (direction != null) {
			action = new Action.Move(direction);
		}
		else {
			log.warn("{} could not find valid direction to move.", this);
		}
		log.debug("{} thinked {}ms, decided to: {}", getName(), (System.currentTimeMillis() - start), action);
		return action;
	}

}
