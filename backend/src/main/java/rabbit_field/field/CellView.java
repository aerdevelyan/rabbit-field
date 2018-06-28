package rabbit_field.field;

import java.util.List;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;

import rabbit_field.creature.Rabbit;
import rabbit_field.msg.CodedEnum;

public class CellView {
	public static final BiMap<FOView, Class<? extends FieldObject>> FO_VIEW_MAP; 
	
	static {
		FO_VIEW_MAP = EnumHashBiMap.create(Map.of(FOView.RABBIT, Rabbit.class, 
				FOView.CLOVER, Plant.Clover.class, FOView.CARROT, Plant.Carrot.class));
	}
	
	public static enum FOView implements CodedEnum<FOView> {
		RABBIT("r"), FOX("f"), CLOVER("cl"), CARROT("ca");
		
		private static final Map<String, FOView> lookupMap = Map.of("r", RABBIT); 
		private final String code;

		private FOView(String code) {
			this.code = code;
		}
		
		@Override
		public String getCode() {
			return code;
		}
		
		@Override
		public Map<String, FOView> getLookupMap() {
			return lookupMap;
		}

		@Override
		public String toString() {
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
