package rabbit_field.creature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.field.Field;
import rabbit_field.field.Field.Direction;
import rabbit_field.field.FieldObject;
import rabbit_field.field.Position;

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
	public int calories() {
		return 25;
	}

	@Override
	public int lookAroundDistance() {
		return 0;
	}

	@Override
	public String toString() {
		return "Rabbit " + getName() + "(a:" + getAge() + ",s:" + getStamina() + ")";
	}

	@Override
	public Action decideAction() {
		long start = System.currentTimeMillis();
		Action action = Action.NONE;
		Class<? extends FieldObject> food = checkForFood();
		if (food != null && getStamina() < MAX_STAMINA) {
			action = new Action.Eat(food);
		}
		else {
			action = move();
		}
		log.debug("{} thinked {}ms, decided to: {}", getName(), (System.currentTimeMillis() - start), action);
		return action;
	}
	
	private Action avoidFox() {
		
		return Action.NONE;
	}
	
	private Action move() {
		Position foodPos = searchFoodNearby();
		Direction direction = getPosition().directionTo(foodPos);
		if (direction != null) {
			return new Action.Move(direction);
		}
		direction =	chooseRandomDirection();
		if (direction != null) {
			return new Action.Move(direction);
		}
		log.warn("{} could not find valid direction to move.", this);
		return Action.NONE;
	}

}
