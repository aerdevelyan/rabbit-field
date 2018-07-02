package rabbit_field.creature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class AbstractWatcherTaskTest {
	
	@Test
	public void pauseResume() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AbstractWatcherTask task = new AbstractWatcherTask(true) {
			@Override
			protected void watchCycle() {
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
		if (!latch.await(1, TimeUnit.SECONDS)) fail("Calling resume() is expected to open the latch.");
		task.shutdown();
		thread.join(1000);
	}
}
