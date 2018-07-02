package rabbit_field.msg;

public class PauseResumeMsg extends Message {
	private boolean pause;
	
	public PauseResumeMsg(boolean pause) {
		setType(Message.MsgType.PAUSE_RESUME);
		this.pause = pause;
	}

	public PauseResumeMsg() {
		setType(Message.MsgType.PAUSE_RESUME);
	}

	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}
	
}
