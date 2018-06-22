package rabbit_field.field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.List;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.Test;

import rabbit_field.creature.Rabbit;
import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Cell;
import rabbit_field.field.Field.Direction;

public class FieldTest {
	Field field = new Field();
	FieldObject fo = new Rabbit("test_fo", field);

	@Test
	public void objectMove() {
		Cell cell = field.findCellBy(new Position(0, 0));
		assertThat(cell.getObjects().size()).isZero();
		cell.addObject(fo);
		assertThat(cell.getObjects().size()).isEqualTo(1);
		Position orig = fo.getPosition();
		assertThat(cell.getPosition()).isEqualTo(orig);
		field.move(fo, Direction.EAST);
		assertThat(fo.getPosition().getHpos()).isEqualTo(orig.getHpos() + 1);
		assertThat(fo.getPosition().getVpos()).isEqualTo(orig.getVpos());
		field.move(fo, Direction.SOUTH);
		assertThat(fo.getPosition().getHpos()).isEqualTo(orig.getHpos() + 1);
		assertThat(fo.getPosition().getVpos()).isEqualTo(orig.getVpos() + 1);
		field.move(fo, Direction.WEST);
		assertThat(fo.getPosition().getHpos()).isEqualTo(orig.getHpos());
		assertThat(fo.getPosition().getVpos()).isEqualTo(orig.getVpos() + 1);
		field.move(fo, Direction.NORTH);
		assertThat(fo.getPosition()).isEqualTo(orig);
	}
	
	@Test
	public void objectMoveBeyondBorders() {
		Cell ltCell = field.findCellBy(new Position(0, 0));  // left top
		ltCell.addObject(fo);
		assertThat(field.isMoveAllowed(fo.getPosition(), Direction.WEST)).isFalse();
		boolean moveRes = field.move(fo, Direction.WEST);
		assertThat(moveRes).isFalse();
		assertThat(field.isMoveAllowed(fo.getPosition(), Direction.NORTH)).isFalse();
		moveRes = field.move(fo, Direction.NORTH);
		assertThat(moveRes).isFalse();
		Cell rbCell = field.findCellBy(new Position(Field.HOR_SIZE - 1, Field.VERT_SIZE - 1));  // right bottom
		ltCell.moveObjectTo(fo, rbCell);
		assertThat(field.isMoveAllowed(fo.getPosition(), Direction.EAST)).isFalse();
		moveRes = field.move(fo, Direction.EAST);
		assertThat(moveRes).isFalse();
		assertThat(field.isMoveAllowed(fo.getPosition(), Direction.SOUTH)).isFalse();
		moveRes = field.move(fo, Direction.SOUTH);
		assertThat(moveRes).isFalse();
	}
	
	@Test
	public void createView() throws Exception {
		FieldObject fo2 = new Rabbit("test_fo2", field); // TODO replace by a plant
		Cell cell = field.findRandomFreeCell();
		cell.addObject(fo);
		cell = field.findRandomFreeCell();
		cell.addObject(fo2);
		List<CellView> fview = field.getView();
		assertThat(fview).isNotNull().hasSize(2);
		assertThat(fview).flatExtracting(CellView::getFobjects).containsExactly(FOView.RABBIT, FOView.RABBIT);
		assertThat(fview).extracting(CellView::getPosition).doesNotContainNull()
			.containsExactlyInAnyOrder(fo.getPosition(), fo2.getPosition());
	}
}
