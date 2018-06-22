package rabbit_field.field;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import rabbit_field.creature.CreatureController;
import rabbit_field.field.Field.Position;

@Singleton
public class Field {
	
	public static class Position {
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
			return hpos >= 0 && hpos < HOR_SIZE && vpos >= 0 && vpos < VERT_SIZE;
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
		
	/**
	 * Cell is a container for {@link FieldObject}s.
	 * Thread safety holds only with single writer: {@link CreatureController}.
	 */ 
	@ThreadSafe
	public static class Cell {
		private final Position position;
		private final Set<FieldObject> objects = new HashSet<>();
		
		public Cell(int hpos, int vpos) {
			position = new Position(hpos, vpos);
		}

		public synchronized Set<FieldObject> getObjects() {
			return new HashSet<>(objects);
		}
		
		public synchronized boolean addObject(FieldObject fo) {
			if (fo.getPosition() != null) {
				return false;
			}
			fo.setPosition(position);
			return objects.add(fo);
		}
		
		public synchronized boolean removeObject(FieldObject fo) {
			fo.setPosition(null);
			return objects.remove(fo);
		}
		
		public synchronized boolean moveObjectTo(FieldObject fo, Cell otherCell) {
			boolean remResult = removeObject(fo);
			boolean addresult = otherCell.addObject(fo);
			return remResult && addresult;
		}

		public Position getPosition() {
			return position;
		}
	}
	
	public enum Direction {
		NORTH(0, -1), SOUTH(0, 1), EAST(1, 0), WEST(-1, 0);
		
		private final int hoffset, voffset;

		private Direction(int hoffset, int voffset) {
			this.hoffset = hoffset;
			this.voffset = voffset;
		}

		public int getHoffset() {
			return hoffset;
		}
		
		public int getVoffset() {
			return voffset;
		}		
	}

	public static final int HOR_SIZE = 50;
	public static final int VERT_SIZE = 50;
	
	private final Cell[][] cells = new Cell[HOR_SIZE][VERT_SIZE];
	
	public Field() {
		initCells();
	}
	
	// create cells and populate the array
	private void initCells() {
		for (int hidx = 0; hidx < HOR_SIZE; hidx++) {
			for (int vidx = 0; vidx < VERT_SIZE; vidx++) {
				cells[hidx][vidx] = new Cell(hidx, vidx);
			}
		}		
	}
	
	public Cell findRandomFreeCell() {
		Cell freeCell = null;
		Random rnd = new Random();
		while (true) {
			Cell rndCell = cells[rnd.nextInt(Field.HOR_SIZE)][rnd.nextInt(Field.VERT_SIZE)];
			if (rndCell.getObjects().size() == 0) {
				freeCell = rndCell;
				break;
			}
		}
		return freeCell;
	}
	
//	public List<FieldObject> whatIsAround(FieldObject obj, int horOffset, int vertOffset) {
//		
//		return null;
//	}
	
	public boolean isMoveAllowed(Position position, Direction direction) {
		return Position.isValid(position.getHpos() + direction.hoffset, position.getVpos() + direction.voffset);
	}
	
	/**
	 * Move given FieldObject to a next Cell by provided Direction.
	 * @param fo - FieldObject to be moved
	 * @param direction - where to move
	 * @return true if move was successful
	 */
	public boolean move(FieldObject fo, Direction direction) {
		Position fopos = fo.getPosition();
		if (!isMoveAllowed(fopos, direction)) {
			return false;
		}
		Cell cell = cells[fopos.getHpos()][fopos.getVpos()];
		Cell otherCell = cells[fopos.getHpos() + direction.hoffset][fopos.getVpos() + direction.voffset];
		cell.moveObjectTo(fo, otherCell);
		return true;
	}

	public Cell findCellBy(Position position) {
		if (!Position.isValid(position)) {
			return null;
		}
		return cells[position.getHpos()][position.getVpos()];
	}
}

