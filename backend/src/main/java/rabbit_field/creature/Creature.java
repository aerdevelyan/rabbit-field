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

	/**
	 * The current age of a creature. When it reaches MAX_AGE creature dies.
	 */
	private int age;

	/**
	 * A life force of a creature. Each move causes a creature to loose some of it. 
	 * When creature eats it increases. When it drops to zero creature dies.
	 */
	private int stamina;

	/**
	 * The field creature lives on.
	 */
	private Field field;

	public Creature(/*MasterMind masterMind,*/ Field field) {
//		this.masterMind = masterMind;
		this.field = field;
	}
	
	public void incrementAge() { //TODO limit
		age++;
	}
	
	public void decrementStamina() {
		stamina--;
	}
	
	public int getAge() {
		return age;
	}

	public int getStamina() {
		return stamina;
	}

	public void setStamina(int stamina) {
		this.stamina = stamina;
//		adjustState();
	}
	
//	private void adjustState() {
//		incrementAge();
//		if (getAge() >= getMaxAge() || getStamina() <= 0) {
//			// die
//		}
//		
//	}

	protected Field getField() {
		return field;
	}

//	private void startNewMindProcess() {
//		masterMind.letCreatureThink(this);
//	}
	
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
	 * Tell your creator for how long do you want to live on that beautiful Field.
	 * @return
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
