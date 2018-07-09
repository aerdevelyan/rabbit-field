package rabbit_field.creature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import rabbit_field.field.CellView;
import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field;
import rabbit_field.field.Field.Direction;
import rabbit_field.field.Position;

public class RabbitTest {
	Field field = Mockito.mock(Field.class);
	Rabbit rabbit = new Rabbit("test_rabbit", field);

	@Before
	public void setup() {
	}
	
	@Test
	public void decidesToEat() throws Exception {
		when(field.getViewAt(any())).thenReturn(List.of(FOView.CARROT, FOView.RABBIT));
		rabbit.decrementStamina();	// make it a little hungry 
		Action action = rabbit.decideAction();
		assertThat(action).isExactlyInstanceOf(Action.Eat.class);
		assertThat(((Action.Eat) action).getDesiredObject()).isEqualTo(FOView.CARROT.getOriginalClass());
	}
	
	@Test
	public void searchForFood() throws Exception {
		Position foodPos = new Position(1, 1);
		CellView cellView = new CellView(foodPos, List.of(FOView.CARROT));
		when(field.whatIsAround(rabbit, Direction.NORTH, Rabbit.MAX_DISTANCE + 1)).thenReturn(cellView);
		Position foundPos = rabbit.searchFoodNearby();
		assertThat(foundPos).isEqualTo(null);  // too far
		when(field.whatIsAround(rabbit, Direction.NORTH, Rabbit.MAX_DISTANCE)).thenReturn(cellView);
		foundPos = rabbit.searchFoodNearby();
		assertThat(foundPos).isEqualTo(foodPos);
	}

	@Test
	public void avoidFox() throws Exception {
		rabbit.setPosition(new Position(0, 1));
		Position foxPos = new Position(0, 0);
		CellView cellView = new CellView(foxPos, List.of(FOView.FOX));
		when(field.whatIsAround(rabbit, Direction.NORTH, Rabbit.MAX_DISTANCE)).thenReturn(cellView);
		Action action = rabbit.decideAction();
		assertThat(action).isExactlyInstanceOf(Action.Move.class);
		Action.Move moveAction = (Action.Move) action;
		assertThat(moveAction.getDirection()).isNotEqualTo(Direction.NORTH);
	}
	
	@Test
	public void caughtByFox() throws Exception {
		Position rabbitPos = new Position(1, 1);
		rabbit.setPosition(rabbitPos);
		when(field.getViewAt(rabbitPos)).thenReturn(List.of(FOView.RABBIT, FOView.FOX));
		Action action = rabbit.decideAction();
		assertThat(action).isEqualTo(Action.NONE);
	}
	
}
