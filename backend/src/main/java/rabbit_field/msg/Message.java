package rabbit_field.msg;

/**
 * Base class for messages exchanged with client.
 */
public class Message {
	
	public enum MsgType {
		FIELD_VIEW(FieldViewMsg.class), PAUSE_RESUME(PauseResumeMsg.class), SHUTDOWN(Message.class);
		
		private final Class<? extends Message> implementationClass;

		private MsgType(Class<? extends Message> implementationClass) {
			this.implementationClass = implementationClass;
		}

		public Class<? extends Message> getImplementationClass() {
			return implementationClass;
		}
	}
	
	protected MsgType type;

	public MsgType getType() {
		return type;
	}

	public void setType(MsgType type) {
		this.type = type;
	}
}
