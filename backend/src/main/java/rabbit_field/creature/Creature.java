package rabbit_field.creature;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Direction;
import rabbit_field.field.FieldObject;
import rabbit_field.field.Plant;
import rabbit_field.field.Position;

/**
 * Creature can move across the field and perform actions.
 */
public abstract class Creature implements FieldObject {
	private static Logger log = LogManager.getLogger();
	private final Map<Class<? extends Creature>, Class<? extends FieldObject>> CAN_EAT;
	
	/**
	 * Every creature is created with 100% of stamina
	 */
	public static final int MAX_STAMINA = 100;
	
	/**
	 * Location on field
	 */
	private Position position;
	
	/**
	 * Is creature alive or dead?
	 */
	private boolean alive = true;

	/**
	 * The current age of a creature. Age grows with each turn.
	 * When it reaches MAX_AGE creature dies.
	 */
	private int age;

	/**
	 * A life force of a creature. Each action (except eating) causes a creature to loose some of it. 
	 * When creature eats it increases. When it drops to zero creature dies.
	 */
	private int stamina;

	/**
	 * The field creature lives on.
	 */
	private Field field;

	/**
	 * Each creature have an unique name.
	 */
	private String name;
	
	
	public Creature(String name, Field field) {
		this.name = name;
		this.field = field;
		setStamina(MAX_STAMINA);
		CAN_EAT = Map.of(Rabbit.class, Plant.class, Fox.class, Rabbit.class);
	}
	
	public int getAge() {
		return age;
	}
	
	public boolean incrementAge() {
		if (!isAlive()) {
			return false;
		}
		if (age < getMaxAge()) {
			age++;
		}
		else {
			die("too old");
		}
		return true;
	}	

	public boolean decrementStamina() {
		if (!isAlive()) {
			return false;
		}
		if (stamina > 0) {
			stamina--;
		}
		else {
			die("starved");
		}
		return true;
	}
	
	public boolean boostStamina(int staminaToAdd) {
		if (!isAlive()) {
			return false;
		}		
		if (this.stamina + staminaToAdd > MAX_STAMINA) {
			this.stamina = MAX_STAMINA;
		}
		else {
			this.stamina += staminaToAdd;
		}
		return true;
	}

	public int getStamina() {
		return stamina;
	}

	public void setStamina(int stamina) {
		if (stamina > MAX_STAMINA || stamina < 1) {
			throw new IllegalArgumentException("Stamina must be positive and no more than " + MAX_STAMINA);
		}
		this.stamina = stamina;
	}
		
	public boolean isAlive() {
		return alive;
	}
	
	public void die(String reason) {
		log.info("Creature {} died, reason: {}.", this, reason);
		alive = false;
		stamina = 0;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean canEat(Class<? extends FieldObject> desired) {
		return CAN_EAT.get(this.getClass()).isAssignableFrom(desired);
	}
	
	protected Direction chooseRandomDirection() {
		int tries = 0;
		Direction direction = null;
		do {
			if (++tries > 100) return null;
			direction = Direction.values()[new Random().nextInt(Direction.values().length)];
		} 
		while (!getField().isMoveAllowed(this.getPosition(), direction));
		return direction;
	}

	protected Class<? extends FieldObject> checkForFood() {
		// TODO define & use method otherObjAtMyPos
		return checkForFood(getField().getViewAt(getPosition()));
	}
	
	protected Class<? extends FieldObject> checkForFood(List<FOView> cellView) {
		if (cellView == null) return null;
		for (FOView fov : cellView) {
			if (canEat(fov.getOriginalClass())) {
				log.debug("{} found food: {}", this, fov.name());
				return fov.getOriginalClass();
			}
		}
		return null;		
	}
	
	protected Direction searchFoodNearby(int distance) {
		for (int d = 0; d <= distance; d++) {
			for (Direction dir : Direction.values()) {
				List<FOView> viewNearby = getField().whatIsAround(this, dir, d);
				Class<? extends FieldObject> food = checkForFood(viewNearby);
				if (food != null) {
					return dir; 
				}
			}
		}
		return null;
	}
	
	
	/**
	 * For implementations to access the Field.
	 */
	protected Field getField() {
		return field;
	}
	
	/**
	 * Tell for how long do you want to live on that beautiful Field.
	 */
	public abstract int getMaxAge();
	
	/** 
	 * Maximum actions per second.
	 */
	public abstract float getSpeed();
		
	/**
	 * Every specific creature should place here its action taking logic.
	 * Analyze the field, think about what action to take (move, eat).
	 * @return
	 */
	public abstract Action decideAction();

}
