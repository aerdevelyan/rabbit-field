package rabbit_field.creature;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import rabbit_field.field.CellView.FOView;
import rabbit_field.field.Field;


public class CreatureTest {
	Field field = Mockito.mock(Field.class);
	Creature creature;
	
	@Before
	public void setup() {
		creature = new Creature("testcr", field) {
			@Override public float getSpeed() {
				return 1f;
			}
			
			@Override public int getMaxAge() {
				return 10;
			}
			
			@Override public Action decideAction() {
				return null;
			}

			@Override
			public int calories() {
				return 0;
			}

			@Override
			public int lookAroundDistance() {
				return 1;
			}

			@Override
			public boolean canMoveToCellWith(List<FOView> fos) {
				return true;
			}

			@Override
			public boolean canMove() {
				return true;
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
	
	@Test
	public void staminaBoost() throws Exception {
		assertThat(creature.getStamina()).isEqualTo(Creature.MAX_STAMINA);
		creature.setStamina(1);
		assertThat(creature.boostStamina(49)).isTrue();
		assertThat(creature.getStamina()).isEqualTo(50);
		creature.boostStamina(200);
		assertThat(creature.getStamina()).isEqualTo(Creature.MAX_STAMINA);
		creature.die("test");
		assertThat(creature.boostStamina(1)).isFalse();
		assertThat(creature.getStamina()).isZero();
	}
	
}
