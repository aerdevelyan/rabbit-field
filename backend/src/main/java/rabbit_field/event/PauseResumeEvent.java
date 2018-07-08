package rabbit_field.event;

import rabbit_field.creature.AbstractCyclicTask;
import rabbit_field.msg.PauseResumeMsg;

public class PauseResumeEvent {
	private final boolean pause;
	
	public PauseResumeEvent(boolean pause) {
		this.pause = pause;
	}
	
	public PauseResumeEvent(PauseResumeMsg msg) {
		this.pause = msg.isPause();
	}

	public boolean isPause() {
		return pause;
	}
	
	public void applyTo(AbstractCyclicTask task) {
		if (isPause()) {
			task.pause();
		}
		else {
			task.resume();
		}
	}
}
