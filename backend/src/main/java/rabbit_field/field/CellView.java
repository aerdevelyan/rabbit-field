package rabbit_field.field;

import java.util.List;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;

import rabbit_field.creature.Rabbit;

public class CellView {
	public static final BiMap<FOView, Class<? extends FieldObject>> FOMAP; 
	
	static {
		FOMAP = EnumHashBiMap.create(Map.of(FOView.RABBIT, Rabbit.class));
	}
	
	public static enum FOView {
		RABBIT("r"), CLOVER("cl"), CARROT("ca");
		
		private final String code;

		private FOView(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
	}	
	
	private final Position position;
	private final List<FOView> fobjects;
	
	public CellView(Position position, List<FOView> fobjects) {
		this.position = position;
		this.fobjects = fobjects;
	}

	public List<FOView> getFobjects() {
		return fobjects;
	}

	public Position getPosition() {
		return position;
	}
	
}
