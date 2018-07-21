package rabbit_field.creature;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import rabbit_field.Util;

public class AbstractCyclicTaskTest {
	
	@Test
	public void pauseResume() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AbstractCyclicTask task = new AbstractCyclicTask(true) {
			@Override
			protected void runCycle() {
				latch.countDown();
			}
		};
		assertThat(task.isPaused()).isTrue();
		Thread thread = new Thread(task);
		thread.start();
		task.pause();	// trying to pause already paused task
		assertThat(task.isPaused()).isTrue();
		task.resume();
		assertThat(task.isPaused()).isFalse();
		if (!latch.await(1, SECONDS)) fail("Calling resume() is expected to open the latch.");
		task.shutdown();
		thread.join(1000);
	}
	
	@Test
	public void waitUntilIntervalPass() throws Exception {
		final int cycleTime = 100;
		AbstractCyclicTask task = createTask(cycleTime);
		task.setInterval(150, MILLISECONDS);	// interval is more than cycle period
		long startMs = System.currentTimeMillis();
		task.run();
		assertThat(System.currentTimeMillis() - startMs).isCloseTo(150, within(1L));
		
		task = createTask(cycleTime);
		task.setInterval(50, MILLISECONDS);	// interval is less than cycle period
		startMs = System.currentTimeMillis();
		task.run();
		assertThat(System.currentTimeMillis() - startMs).isCloseTo(cycleTime, within(1L));
	}

	private AbstractCyclicTask createTask(int cycleTime) {
		AbstractCyclicTask task = new AbstractCyclicTask() {
			@Override
			protected void runCycle() {
				Util.sleepMSec(cycleTime);
				shutdown();	// do not loop
			}
		};
		return task;
	}
}

