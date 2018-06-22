package rabbit_field.creature;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import rabbit_field.field.Field;


public class CreatureTest {
	Field field = Mockito.mock(Field.class);
	Creature creature;
	
	@Before
	public void setup() {
		creature = new Creature(field) {
			
			@Override public float getSpeed() {
				return 1f;
			}
			
			@Override public int getMaxAge() {
				return 10;
			}
			
			@Override public Action decideAction() {
				return null;
			}
		};
	}
	
	@Test
	public void diesOfOldAge() {		
		assertThat(creature.isAlive()).isTrue();
		for (int turn = 0; turn < 10; turn++) {
			creature.incrementAge();
		}
		assertThat(creature.getAge()).isEqualTo(10);
		assertThat(creature.isAlive()).isTrue();
		creature.incrementAge();
		assertThat(creature.isAlive()).isFalse();
		assertThat(creature.getAge()).isEqualTo(10);
	}
	
	@Test
	public void diesOfExhaustion() {
		creature.setStamina(10);
		for (int turn = 0; turn < 10; turn++) {
			creature.decrementStamina();
		}
		assertThat(creature.getStamina()).isEqualTo(0);
		assertThat(creature.isAlive()).isTrue();
		creature.decrementStamina();
		assertThat(creature.isAlive()).isFalse();
		assertThat(creature.getStamina()).isEqualTo(0);
	}
}
