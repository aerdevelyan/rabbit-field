package rabbit_field;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import rabbit_field.creature.Action;
import rabbit_field.creature.Creature;

@Singleton
public class Field {
	public static final int HOR_SIZE = 50;
	public static final int VERT_SIZE = 50;
	
	private Cell[][] cells = new Cell[HOR_SIZE][VERT_SIZE];
	
	public Field() {
		// create cells and populate the array
		for (int hidx = 0; hidx < HOR_SIZE; hidx++) {
			for (int vidx = 0; vidx < VERT_SIZE; vidx++) {
				cells[hidx][vidx] = new Cell();
			}
		}
	}
	
	public List<FieldObject> whatIsAroundMe(FieldObject obj, int horOffset, int vertOffset) {
		
		return null;
	}
	
	public void placeObject(FieldObject obj, int horPosition, int vertPosition) {
		
	}

	public static class Cell {
		List<FieldObject> objects = new ArrayList<FieldObject>();

		public List<FieldObject> getObjects() {
			return objects;
		}

		public void setObjects(List<FieldObject> objects) {
			this.objects = objects;
		}
	}

	public void perform(Action action, Creature creature) {
		
	}
}

