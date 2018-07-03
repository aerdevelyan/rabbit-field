package rabbit_field.event;

import rabbit_field.creature.AbstractCyclicTask;

public class PauseResumeEvent {
	private final boolean pause;
	
	public PauseResumeEvent(boolean pause) {
		this.pause = pause;
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
