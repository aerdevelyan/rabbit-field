package rabbit_field.field;

import static org.junit.Assert.*;

import org.junit.Test;

import rabbit_field.field.Field.Direction;

public class PositionTest {
	
	@Test
	public void directionToOtherPosition() throws Exception {
		Position position = new Position(10, 10);
		assertNull(position.directionTo(null));
		Position otherPos = new Position(10, 10);
		assertNull(position.directionTo(otherPos));
		assertEquals(Direction.NORTH, position.directionTo(new Position(10, 9)));
		assertEquals(Direction.NORTH, position.directionTo(new Position(10, 5)));
		assertEquals(Direction.NORTH, position.directionTo(new Position(11, 9)));
		assertEquals(Direction.EAST, position.directionTo(new Position(12, 9)));
		assertEquals(Direction.EAST, position.directionTo(new Position(11, 10)));
		assertEquals(Direction.EAST, position.directionTo(new Position(12, 11)));
		assertEquals(Direction.SOUTH, position.directionTo(new Position(11, 12)));
		assertEquals(Direction.SOUTH, position.directionTo(new Position(10, 12)));
		assertEquals(Direction.SOUTH, position.directionTo(new Position(9, 12)));
		assertEquals(Direction.WEST, position.directionTo(new Position(8, 11)));
		assertEquals(Direction.WEST, position.directionTo(new Position(9, 10)));
		assertEquals(Direction.WEST, position.directionTo(new Position(8, 9)));
	}
}
