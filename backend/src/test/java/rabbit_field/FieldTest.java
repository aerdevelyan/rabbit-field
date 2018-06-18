package rabbit_field;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import rabbit_field.Field.Cell;
import rabbit_field.Field.Direction;
import rabbit_field.Field.Position;
import rabbit_field.creature.Rabbit;

public class FieldTest {

	@Test
	public void objectMove() {
		Field field = new Field();
		FieldObject fo = new Rabbit("test_fo", field);
		Cell cell = field.findRandomFreeCell();
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
}
