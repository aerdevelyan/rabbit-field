package rabbit_field.field;

import rabbit_field.field.Field.Direction;

public class Position {
	private final int hpos, vpos;
	
	public Position(int hpos, int vpos) {
		if (!isValid(hpos, vpos)) {
			throw new IllegalArgumentException("Passed coordinates are invalid: " + hpos + ", " + vpos);
		}
		this.hpos = hpos;
		this.vpos = vpos;
	}

	public int getHpos() {
		return hpos;
	}

	public int getVpos() {
		return vpos;
	}

	public Position calculateNewPosition(Direction direction) {
		return new Position(this.getHpos() + direction.hoffset, this.getVpos() + direction.voffset);
	}

	public static boolean isValid(int hpos, int vpos) {
		return hpos >= 0 && hpos < Field.HOR_SIZE && vpos >= 0 && vpos < Field.VERT_SIZE;
	}

	public static boolean isValid(Position position) {
		return isValid(position.hpos, position.vpos);
	}
	
	@Override
	public int hashCode() {
		return (hpos + 1) * (vpos + 1) + hpos;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (hpos != other.hpos)
			return false;
		if (vpos != other.vpos)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Position [hpos=" + hpos + ", vpos=" + vpos + "]";
	}
}