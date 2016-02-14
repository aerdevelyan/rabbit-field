package rabbit_field;

/**
 * An action that a creature can perform on the field.
 *
 */
public abstract class Action {
	public static final None NONE = new None();
	public static final None NONE_BY_FAILURE = new None();
	
	public static class None extends Action { }
	
	
	public static class Move extends Action {
		// TODO consider refactor: move to field?
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
