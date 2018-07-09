package rabbit_field.creature;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.field.Field;
import rabbit_field.field.FieldObject;
import rabbit_field.field.Position;
import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field.Direction;

public class Fox extends Creature {
	private static Logger log = LogManager.getLogger();
	public static final int MAX_AGE = 400;
	public static final int MAX_DISTANCE = 2;
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
	public int lookAroundDistance() {
		return MAX_DISTANCE;
	}
	
	@Override
	public String toString() {
		return "Fox " + getName() + "(a:" + getAge() + ",s:" + getStamina() + ")";
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

	private Action move() {
		Position foodPos = searchFoodNearby();
		Direction direction = getPosition().directionTo(foodPos);
		if (direction != null) {
			return new Action.Move(direction);
		}		
		direction = chooseRandomDirection();
		if (direction != null) {
			return new Action.Move(direction);
		}		
		log.warn("{} could not find valid direction to move.", this);
		return Action.NONE;
	}
	
	@Override
	public boolean canMove() {
		return true;
	}
	
	@Override
	public boolean canMoveToCellWith(List<FOView> fos) {
		if (fos.contains(FOView.FOX)) {
			return false;			
		}
		return true;
	}
}
