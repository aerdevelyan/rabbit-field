package rabbit_field;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import rabbit_field.creature.Action;
import rabbit_field.creature.Creature;

@Singleton
public class Field {

	/**
	 * Cell is a container for {@link FieldObject}s
	 */
	public static class Cell {
		private final List<FieldObject> objects = new ArrayList<>();
		
		public List<FieldObject> getObjects() {
			return objects;
		}
		
		public void addObject(FieldObject fo) {
			objects.add(fo);
		}
		
//		public void setObjects(List<FieldObject> objects) {
//			this.objects = objects;
//		}
	}
	
	public static final int HOR_SIZE = 50;
	public static final int VERT_SIZE = 50;
	
	private final Cell[][] cells = new Cell[HOR_SIZE][VERT_SIZE];
	
	public Field() {
		// create cells and populate the array
		for (int hidx = 0; hidx < HOR_SIZE; hidx++) {
			for (int vidx = 0; vidx < VERT_SIZE; vidx++) {
				cells[hidx][vidx] = new Cell();
			}
		}
	}
	
	public Cell[][] getCells() {
		return cells;
	}
	
	public List<FieldObject> whatIsAround(FieldObject obj, int horOffset, int vertOffset) {
		
		return null;
	}
	
	public void placeObject(FieldObject obj, int horPosition, int vertPosition) {
		
	}

	public void perform(Action action, Creature creature) {
		
	}
}

