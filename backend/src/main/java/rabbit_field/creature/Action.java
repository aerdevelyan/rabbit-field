package rabbit_field.creature;

import java.util.Map;

import rabbit_field.field.Field;
import rabbit_field.field.FieldObject;
import rabbit_field.field.Plant;

/**
 * An action that a creature can perform on the field.
 *
 */
public abstract class Action {
	public static final None NONE = new None();
	// no action is decided because of some failure (e.g. passed time limit) 
	public static final None NONE_BY_FAILURE = new None();
	// no action is decided because of cancellation of mind process
	public static final None NONE_BY_CANCEL = new None();
	
	public static class None extends Action { }
	
	
	public static class Move extends Action {
		private final Field.Direction direction;
		
		public Move(Field.Direction direction) {
			this.direction = direction;
		}

		public Field.Direction getDirection() {
			return direction;
		}

		@Override
		public String toString() {
			return "Move [direction=" + direction + "]";
		}
	}
	
	
	public static class Eat extends Action {
		public static final Map<Class<? extends Creature>, Class<? extends FieldObject>> CAN_EAT;
		static {
			CAN_EAT = Map.of(Rabbit.class, Plant.class);
		}
		
		private final Class<? extends FieldObject> desiredObject;

		public Eat(Class<? extends FieldObject> desiredObject) {
			this.desiredObject = desiredObject;
		}

		public Class<? extends FieldObject> getDesiredObject() {
			return desiredObject;
		}
		
		@Override
		public String toString() {
			return "Eat " + desiredObject.getSimpleName();
		}

		public static boolean canEat(Class<? extends Creature> creature, Class<? extends FieldObject> desired) {
			return CAN_EAT.get(creature).isAssignableFrom(desired);
		}
	}
}
