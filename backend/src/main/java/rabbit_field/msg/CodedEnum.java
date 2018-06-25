package rabbit_field.msg;

import java.util.Map;

public interface CodedEnum<T extends Enum<T>> {
	String getCode();
	
	Map<String, T> getLookupMap();
	
	public static <T extends Enum<T>> T fromCode(Class<T> enumType, String code) {
		T enumConst = enumType.getEnumConstants()[0];
		return ((CodedEnum<T>) enumConst).getLookupMap().get(code);
	}
}
