package rabbit_field.creature;

import rabbit_field.field.Field;

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
	
	
	public static class Eat extends Action {}
}
