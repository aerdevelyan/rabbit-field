package rabbit_field.field;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import rabbit_field.creature.Rabbit;
import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field.Cell;
import rabbit_field.field.Field.Direction;
import rabbit_field.field.Field.FieldException;

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
	public void objectMoveBeyondBorders() throws FieldException {
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
		field.findRandomEmptyCell().addObject(fo);
		FieldObject fo2 = new Plant.Clover();
		field.findRandomEmptyCell().addObject(fo2);
		FieldObject fo3 = new Plant.Carrot();
		field.findRandomEmptyCell().addObject(fo3);
		List<CellView> fview = field.getView();
		assertThat(fview).isNotNull().hasSize(3);
		assertThat(fview).flatExtracting(CellView::getFobjects).containsExactlyInAnyOrder(
				FOView.RABBIT, FOView.CLOVER, FOView.CARROT);
		assertThat(fview).extracting(CellView::getPosition).containsExactlyInAnyOrder(
				fo.getPosition(), fo2.getPosition(), fo3.getPosition());
	}
	
	@Test
	public void findRandomEmptyCell_onEmptyField() throws Exception {
		Cell cell1 = field.findRandomEmptyCell();
		assertThat(cell1).isNotNull().returns(true, Cell::isEmpty);
		cell1.addObject(fo);
		Cell cell2 = field.findRandomEmptyCell();
		assertThat(cell2).isNotNull().returns(true, Cell::isEmpty).isNotSameAs(cell1);
	}
	
	@Test
	public void findRandomEmptyCell_onSaturatedField() throws Exception {
		field.interateIndexes((hidx, vidx) -> field.findCellBy(new Position(hidx, vidx)).addObject(new Plant.Carrot()));
		Cell emptyCell = field.findRandomEmptyCell();
		assertThat(emptyCell).isNull();
		
		Cell firstCell = field.findCellBy(new Position(0, 0));
		firstCell.clear();
		emptyCell = field.findRandomEmptyCell();
		assertThat(emptyCell).isNotNull().returns(true, Cell::isEmpty).isSameAs(firstCell);		
		firstCell.addObject(new Plant.Carrot());
		
		Cell lastCell = field.findCellBy(new Position(Field.HOR_SIZE - 1, Field.VERT_SIZE - 1));
		lastCell.clear();
		emptyCell = field.findRandomEmptyCell();
		assertThat(emptyCell).isNotNull().returns(true, Cell::isEmpty).isSameAs(lastCell);
	}
}

