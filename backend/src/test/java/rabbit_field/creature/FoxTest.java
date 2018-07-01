package rabbit_field.creature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import rabbit_field.field.Field;
import rabbit_field.field.CellView.FOView;

public class FoxTest {
	Field field = Mockito.mock(Field.class);

	@Before
	public void setup() {
		when(field.getViewAt(any())).thenReturn(List.of(FOView.FOX, FOView.RABBIT));
	}
	
	@Test
	public void decidesToEat() throws Exception {
		Fox fox = new Fox("test_fox", field);
		fox.decrementStamina();	 // make it a little hungry
		Action action = fox.decideAction();
		assertThat(action).isExactlyInstanceOf(Action.Eat.class);
		assertThat(((Action.Eat) action).getDesiredObject()).isEqualTo(FOView.RABBIT.getOriginalClass());
	}

}
