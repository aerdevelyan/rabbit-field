package rabbit_field.creature;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for Runnable tasks that run as a loop that 
 * can be paused or shut down.
 */
public abstract class AbstractCyclicTask implements Runnable {
	private final static Logger log = LogManager.getLogger();
	protected volatile boolean shutdown;
	protected volatile boolean paused;
	private final ReentrantLock pauseLock = new ReentrantLock();
	private final Condition unpaused = pauseLock.newCondition();

	public AbstractCyclicTask() {
	}

	public AbstractCyclicTask(boolean paused) {
		this.paused = paused;
	}

	public void shutdown() {
		log.debug("Shutdown is requested");
		shutdown = true;
	}

	public boolean isPaused() {
		return paused;
	}

	public void pause() {
		if (paused) {
			log.warn("Attempt to pause already paused task, ignoring.");
			return;
		}
		log.info("Pausing task");
		pauseLock.lock();
		try {
			paused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	public void resume() {
		if (!paused) {
			log.warn("Attempt to resume normally running task, ignoring.");
			return;
		}
		pauseLock.lock();
		try {
			paused = false;
			unpaused.signalAll();
			log.info("Resuming task, by {}", Thread.currentThread());
		} finally {
			pauseLock.unlock();
		}
	}

	@Override
	public void run() {
		do {
			pauseLock.lock();
			try {
				while (paused) {
					unpaused.await(2, TimeUnit.SECONDS);
					log.debug("Await passed by {}, paused is {}", Thread.currentThread(), paused);
				}
			} catch (InterruptedException ie) {
				log.warn("Await for resume was interrupted.", ie);
				Thread.currentThread().interrupt();
			} finally {
				pauseLock.unlock();
			}
			
			runCycle();
			
		} while (!Thread.interrupted() && !shutdown);
	}

	protected abstract void runCycle();
}

