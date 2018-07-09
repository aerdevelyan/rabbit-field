package rabbit_field.creature;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Direction;
import rabbit_field.field.FieldObject;
import rabbit_field.field.Position;

public class Rabbit extends Creature {
	private static Logger log = LogManager.getLogger();
	public static final int MAX_AGE = 300;
	public static final int MAX_DISTANCE = 0;
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
		return 40;
	}

	@Override
	public int lookAroundDistance() {
		return MAX_DISTANCE;
	}

	@Override
	public String toString() {
		return "Rabbit " + getName() + "(a:" + getAge() + ",s:" + getStamina() + ")";
	}

	@Override
	public Action decideAction() {
		long start = System.currentTimeMillis();
		if (caughtByFox()) {
			return Action.NONE;
		}
		Action action = avoidFox();
		if (action != null) {
			return action;
		}
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
		Position foxPos = searchAround(cellView -> {
			if (cellView.getFobjects().contains(FOView.FOX)) {
				return cellView.getPosition();
			}
			return null;
		});
		if (foxPos != null) {
			return new Action.Move(chooseRandomDirectionExcept(this.getPosition().directionTo(foxPos)));
		}
		return null;
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

	private boolean caughtByFox() {	
		List<FOView> view = getField().getViewAt(getPosition());
		if (view.contains(FOView.FOX)) {
			log.info("{} got caught by a fox, cannot move.", this);
			return true;
		}
		return false;
	}

	@Override
	public boolean canMove() {
		return !caughtByFox();
	}
	
	@Override
	public boolean canMoveToCellWith(List<FOView> fos) {
		if (fos.contains(FOView.RABBIT) || fos.contains(FOView.FOX)) {
			return false;			
		}
		return true;
	}
}
