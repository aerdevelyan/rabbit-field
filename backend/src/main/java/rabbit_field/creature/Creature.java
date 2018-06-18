package rabbit_field.creature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.Field;
import rabbit_field.FieldObject;

/**
 * Creature can move across the field and perform actions.
 * Logic of life cycle:
 * - analyze the field, think about what action to take (move, eat)
 * - take action 
 * - wait for action to complete
 */
public abstract class Creature implements FieldObject {
	private static Logger log = LogManager.getLogger();
	
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
	
	public void incrementAge() {
		if (age < getMaxAge()) {
			age++;
		}
		else {
			die();
		}
	}	

	public void decrementStamina() {
		if (stamina > 0) {
			stamina--;
		}
		else {
			die();
		}
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
	
	public void die() {
		log.info("Creature {} is dead.", this);
		alive = false;
	}

	
	/**
	 * Called by MasterMind after computations by decideAction() are finished.
	 * @param action
	 */
//	public void actionIsDecided(Action action) {
//		log.debug(this + " performing " + action);
//		getField().perform(action, this);
//		adjustState();
//		startNewMindProcess();
//	}
	
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
