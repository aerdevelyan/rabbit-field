package rabbit_field.field;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.creature.CreatureController;
import rabbit_field.field.CellView.FOView;

@Singleton
public class Field {
	
	public static class FieldException extends Exception {
		private static final long serialVersionUID = 1L;
		private final Cell cell;
		private final FieldObject fieldObject;
		
		public FieldException(String message, Cell cell, FieldObject fieldObject) {
			super(message);
			this.cell = cell;
			this.fieldObject = fieldObject;
		}

		public Cell getCell() {
			return cell;
		}
		public FieldObject getFieldObject() {
			return fieldObject;
		}
	}

	/**
	 * Cell is a container for {@link FieldObject}s.
	 * Thread safety holds only with single FO mover: {@link CreatureController}.
	 * TODO move to separate file
	 */
	@ThreadSafe
	public static class Cell {
		private final Position position;
		private final Set<FieldObject> objects = new HashSet<>();
		private List<FOView> fovList;
		
		public Cell(int hpos, int vpos) {
			position = new Position(hpos, vpos);
		}

		public synchronized Set<FieldObject> getObjects() {
			return new HashSet<>(objects);
		}
		
		public synchronized List<FOView> getObjView() {
			if (fovList == null) {
				fovList = updateView();
			}
			return fovList;
		}
		
		public synchronized CellView getView() {
			return new CellView(position, getObjView());
		}
		
		public synchronized boolean addObject(FieldObject fo) {
			if (fo.getPosition() != null) {  // allow only new or removed from other cell
				return false;
			}
			fo.setPosition(position);
			if (objects.add(fo)) {
				fovList = null;
				return true;
			}
			return false; 
		}
		
		public synchronized boolean removeObject(FieldObject fo) {
			fo.setPosition(null);
			if (objects.remove(fo)) {
				fovList = null;
				return true;
			}
			return false;
		}
		
		public synchronized void moveObjectTo(FieldObject fo, Cell otherCell) throws FieldException {
			boolean rmSuccess = removeObject(fo);
			if (rmSuccess) {
				boolean addSuccess = otherCell.addObject(fo);
				if (!addSuccess) {
					throw new FieldException("Could not add field object to cell.", otherCell, fo);
				}
			}
			else {
				throw new FieldException("Could not remove field object from cell.", this, fo);
			}
		}

		public synchronized boolean isEmpty() {
			return objects.isEmpty();
		}
		
		public synchronized void clear() {
			objects.clear();
		}
		
		public Position getPosition() {
			return position;
		}
		
		public Optional<FieldObject> findFirstByClass(Class<? extends FieldObject> foClass) {
			if (foClass == null) throw new IllegalArgumentException("Class of field object can not be null.");
			return objects.stream()
					.filter(fo -> foClass.isInstance(fo))
					.findFirst();
		}

		// called on each change of contained objects set
		private List<FOView> updateView() {
			return objects.stream()
				.map(fo -> CellView.FO_VIEW_MAP.inverse().get(fo.getClass()))
				.collect(toList());
		}
	}
	
	public enum Direction {
		NORTH(0, -1), SOUTH(0, 1), EAST(1, 0), WEST(-1, 0);
		
		final int hoffset;
		final int voffset;

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

	public static final int HOR_SIZE = 40;
	public static final int VERT_SIZE = 25;
	private final static Logger log = LogManager.getLogger();
	private final Cell[][] cells = new Cell[HOR_SIZE][VERT_SIZE];
	
	public Field() {
		initCells();
	}


	public Cell cellAtLinearIndex(int linIndex) {
		int vidx = linIndex / HOR_SIZE;
		int hidx = linIndex - vidx * HOR_SIZE;
		if (!Position.isValid(hidx, vidx)) {
			return null;
		}
		return cells[hidx][vidx];
	}
	
	public Cell findRandomEmptyCell() {
		int linearSize = HOR_SIZE * VERT_SIZE;
		int initialIdx = new Random().nextInt(linearSize);
		Cell cell = cellAtLinearIndex(initialIdx);
		if (cell.isEmpty()) return cell;
		int forwardLoc = initialIdx, backLoc = initialIdx;
		boolean movedIdx = true;
		while (movedIdx) {
			movedIdx = false;
			if (forwardLoc < linearSize - 1) {
				cell = cellAtLinearIndex(++forwardLoc);
				if (cell.isEmpty()) return cell;
				movedIdx = true;
			}
			if (backLoc > 0) {
				cell = cellAtLinearIndex(--backLoc);
				if (cell.isEmpty()) return cell;
				movedIdx = true;
			}
		}
		return null;
	}
		
	public boolean isMoveAllowed(Position position, Direction direction) {
		if (position == null || direction == null) return false;
		return Position.isValid(position.getHpos() + direction.hoffset, position.getVpos() + direction.voffset);
	}
	
	/**
	 * Move given FieldObject to a next Cell by provided Direction.
	 * @param fo - FieldObject to be moved
	 * @param direction - where to move
	 * @return true if move was successful
	 */
	public boolean move(FieldObject fo, Direction direction) { // TODO narrow only to creatures?
		Position fopos = fo.getPosition();
		if (!isMoveAllowed(fopos, direction)) {
			return false;
		}
		Cell cell = cells[fopos.getHpos()][fopos.getVpos()];
		Cell otherCell = cells[fopos.getHpos() + direction.hoffset][fopos.getVpos() + direction.voffset];
		try {
			cell.moveObjectTo(fo, otherCell);
		} catch (FieldException e) {
			log.error("Error moving {}.", fo);
			return false;
		}
		return true;
	}

	public void removeAt(Position position, Class<? extends FieldObject> foClass) {
		Cell cell = findCellBy(position);
		cell.findFirstByClass(foClass).ifPresent(fo -> cell.removeObject(fo)); // TODO what if not found?
	}
	
	public Cell findCellBy(Position position) {
		if (position == null || !Position.isValid(position)) {
			return null;
		}
		return cells[position.getHpos()][position.getVpos()];
	}
	
	/**
	 * TODO optimize: return cached if not changed
	 * @return
	 */
	public List<CellView> getView() {
		List<CellView> view = new ArrayList<>();
		interateIndexes((hidx, vidx) -> {
			Cell cell = cells[hidx][vidx];
			if (!cell.isEmpty()) {
				view.add(new CellView(cell.getPosition(), cell.getObjView()));
			}
		});
		return view;
	}
	
	public List<FOView> getViewAt(Position position) {
		Cell cell = findCellBy(position);
		if (cell == null) return null;
		return cell.getObjView();
	}
	
	public CellView whatIsAround(FieldObject fo, Direction direction, int distance) {
		int hidx = fo.getPosition().getHpos() + direction.hoffset + distance;
		int vidx = fo.getPosition().getVpos() + direction.voffset + distance;
		if (Position.isValid(hidx, vidx)) {
			return cells[hidx][vidx].getView();
		}
		return null;
	}
	
	
	@FunctionalInterface
	protected interface IdxConsumer {
		void accept(int hidx, int vidx);
	}
	
	protected void interateIndexes(IdxConsumer ic) {		
		for (int vidx = 0; vidx < VERT_SIZE; vidx++) {
			for (int hidx = 0; hidx < HOR_SIZE; hidx++) {
				ic.accept(hidx, vidx);
			}
		}
	}
	
	// create cells and populate the array
	private void initCells() {
		interateIndexes((hidx, vidx) -> cells[hidx][vidx] = new Cell(hidx, vidx));
	}
}

