package rabbit_field.event;

public class PauseResumeEvent {
	private final boolean pause;
	
	public PauseResumeEvent(boolean pause) {
		this.pause = pause;
	}

	public boolean isPause() {
		return pause;
	}
}
