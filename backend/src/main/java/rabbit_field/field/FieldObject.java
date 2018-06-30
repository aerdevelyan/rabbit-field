package rabbit_field.field;

/**
 * Any object living on the field must implement this.
 */
public interface FieldObject {
	
	Position getPosition();
	
	void setPosition(Position position);
	
	int calories();
}
