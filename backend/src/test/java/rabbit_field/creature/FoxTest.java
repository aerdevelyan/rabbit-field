package rabbit_field.creature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import rabbit_field.field.CellView;
import rabbit_field.field.Field;
import rabbit_field.field.Position;
import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field.Direction;

public class FoxTest {
	Field field = Mockito.mock(Field.class);
	Fox fox = new Fox("test_fox", field);

	@Before
	public void setup() {
		when(field.getViewAt(any())).thenReturn(List.of(FOView.FOX, FOView.RABBIT));
	}
	
	@Test
	public void decidesToEat() throws Exception {
		fox.decrementStamina();	 // make it a little hungry
		Action action = fox.decideAction();
		assertThat(action).isExactlyInstanceOf(Action.Eat.class);
		assertThat(((Action.Eat) action).getDesiredObject()).isEqualTo(FOView.RABBIT.getOriginalClass());
	}

	@Test
	public void searchForFood() throws Exception {
		CellView cellView1 = new CellView(new Position(1, 1), List.of(FOView.RABBIT));
		CellView cellView2 = new CellView(new Position(2, 2), List.of(FOView.RABBIT));
		when(field.whatIsAround(fox, Direction.WEST, Fox.MAX_DISTANCE)).thenReturn(cellView1);
		Position foundPos = fox.searchFoodNearby();
		assertThat(foundPos).isEqualTo(cellView1.getPosition());
		when(field.whatIsAround(fox, Direction.WEST, Fox.MAX_DISTANCE - 1)).thenReturn(cellView2);
		foundPos = fox.searchFoodNearby();
		assertThat(foundPos).isEqualTo(cellView2.getPosition());	// prefer nearer position		
	}
}

