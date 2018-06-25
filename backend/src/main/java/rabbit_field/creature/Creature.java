package rabbit_field.creature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.field.Field;
import rabbit_field.field.FieldObject;
import rabbit_field.field.Position;

/**
 * Creature can move across the field and perform actions.
 * Logic of life cycle:
 * - analyze the field, think about what action to take (move, eat)
 * - take action 
 * - wait for action to complete
 */
public abstract class Creature implements FieldObject {
	private static Logger log = LogManager.getLogger();
	
	private Position position;
	
	private boolean alive = true;

	/**
	 * The current age of a creature. When it reaches MAX_AGE creature dies.
	 * Age grows with each turn.
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

	public Creature(Field field) {
		this.field = field;
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
	
	public int getAge() {
		return age;
	}

	public int getStamina() {
		return stamina;
	}

	public void setStamina(int stamina) {
		this.stamina = stamina;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void die(String reason) {
		log.info("Creature {} died, reason: {}.", this, reason);
		alive = false;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
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
	 * Every specific creature should place here its survival logic.
	 * @return
	 */
	public abstract Action decideAction();
}
