package rabbit_field;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import rabbit_field.creature.CreatureController;

@Singleton
public class Field {
	
	public static class Position {
		private final int hpos, vpos;
		
		public Position(int hpos, int vpos) {
			this.hpos = hpos;
			this.vpos = vpos;
		}

		public int getHpos() {
			return hpos;
		}

		public int getVpos() {
			return vpos;
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
	}
		
	/**
	 * Cell is a container for {@link FieldObject}s.
	 * Thread safety holds only with single writer: {@link CreatureController}.
	 * TODO exception on unsuccessful add or remove
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
		
		public synchronized void addObject(FieldObject fo) {
			fo.setPosition(position);
			objects.add(fo);
		}
		
		public synchronized void removeObject(FieldObject fo) {
			objects.remove(fo);
		}
		
		public synchronized void moveObjectTo(FieldObject fo, Cell otherCell) {
			removeObject(fo);
			otherCell.addObject(fo);
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
	
	public List<FieldObject> whatIsAround(FieldObject obj, int horOffset, int vertOffset) {
		
		return null;
	}
	
	public void move(FieldObject fo, Direction direction) {
		Cell cell = cells[fo.getPosition().getHpos()][fo.getPosition().getVpos()];
		Cell otherCell = cells[fo.getPosition().getHpos() + direction.hoffset][fo.getPosition().getVpos() + direction.voffset];
		cell.moveObjectTo(fo, otherCell);
	}

}

