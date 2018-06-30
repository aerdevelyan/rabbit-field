package rabbit_field.creature;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Direction;
import rabbit_field.field.FieldObject;
import rabbit_field.field.Plant;

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
		Action action = Action.NONE;
		Class<? extends FieldObject> food = checkForFood();
		if (food != null) {
			action = new Action.Eat(food);
		}
		else {
			Direction direction = chooseRandomDirection();
			if (direction != null) {
				action = new Action.Move(direction);
			}
			else {
				log.warn("{} could not find valid direction to move.", this);
			}
		}
//		try {
//			TimeUnit.MILLISECONDS.sleep(new Random().nextInt(150));
//		} catch (InterruptedException e) {
//			log.warn("Got interrupted {}", this);
//		}
		log.debug("{} thinked {}ms, decided to: {}", getName(), (System.currentTimeMillis() - start), action);
		return action;
	}

	private Class<? extends FieldObject> checkForFood() {
		for (FOView fov : getField().getViewAt(getPosition())) {
			if (Plant.class.isAssignableFrom(fov.getOriginalClass())) {
				log.debug("{} found a plant: {}", this, fov.name());
				return fov.getOriginalClass();
			}
		}
		return null;
	}
	
	private Direction chooseRandomDirection() {
		int tries = 0;
		Direction direction = null;
		do {
			if (++tries > 100) return null;
			direction = Direction.values()[new Random().nextInt(Direction.values().length)];
		} 
		while (!getField().isMoveAllowed(this.getPosition(), direction));
		return direction;
	}

	@Override
	public String toString() {
		return "Rabbit " + getName() + "(a:" + getAge() + ",s:" + getStamina() + ")";
	}

	@Override
	public int calories() {
		return 20;
	}
	
}
