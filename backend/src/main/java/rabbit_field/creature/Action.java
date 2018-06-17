package rabbit_field.creature;

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

		public enum Direction {
			NORTH, SOUTH, EAST, WEST, 
		}
		
		private final Direction direction;
		
		public Move(Direction direction) {
			this.direction = direction;
		}

		public Direction getDirection() {
			return direction;
		}

		@Override
		public String toString() {
			return "Move [direction=" + direction + "]";
		}
	}
	
	
	public static class Eat extends Action {}
}
