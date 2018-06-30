package rabbit_field.field;

public abstract class Plant implements FieldObject {
	private Position position;
	
	public static class Clover extends Plant {
		public static final int CALORIES = 5, RARITY = 1;
		@Override
		public int calories() {
			return CALORIES;
		}
		@Override
		public int rarity() {
			return RARITY;
		}
		@Override
		public String toString() {
			return "Clover";
		}
	}

	public static class Carrot extends Plant {
		public static final int CALORIES = 10, RARITY = 5;
		@Override
		public int calories() {
			return CALORIES;
		}
		@Override
		public int rarity() {
			return RARITY;
		}
		@Override
		public String toString() {
			return "Carrot";
		}
	}
	
	public abstract int rarity();
	
	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void setPosition(Position position) {
		this.position = position;
	}
}
