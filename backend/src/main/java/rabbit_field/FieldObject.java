package rabbit_field;

/**
 * Any object living on the field must implement this.
 */
public interface FieldObject {
	
	Field.Position getPosition();
	
	void setPosition(Field.Position position);
}
