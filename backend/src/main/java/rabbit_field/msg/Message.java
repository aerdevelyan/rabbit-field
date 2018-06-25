package rabbit_field.msg;

/**
 * Base class for messages exchanged with client.
 */
public class Message {
	public enum MsgType {
		FIELD_VIEW
	}
	
	protected MsgType type;

	public MsgType getType() {
		return type;
	}

	public void setType(MsgType type) {
		this.type = type;
	}
}
