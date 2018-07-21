package rabbit_field.creature;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for Runnable tasks that run as a loop that 
 * can be paused or shut down.
 */
public abstract class AbstractCyclicTask implements Runnable {
	private final static Logger log = LogManager.getLogger();
	private volatile boolean shutdown;
	private volatile boolean paused;
	@GuardedBy("this") private long intervalNs;
	private final ReentrantLock pauseLock = new ReentrantLock();
	private final Condition unpaused = pauseLock.newCondition();

	public AbstractCyclicTask() {
	}

	public AbstractCyclicTask(boolean paused) {
		this.paused = paused;
	}

	public void shutdown() {
		log.debug("Shutdown is requested for {}", getClass().getSimpleName());
		shutdown = true;
		if (isPaused()) resume();
	}

	public boolean isPaused() {
		return paused;
	}

	public void pause() {
		if (paused) {
			log.warn("Attempt to pause already paused task, ignoring.");
			return;
		}
		log.info("Pausing task {}", getClass().getSimpleName());
		pauseLock.lock();
		try {
			paused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	public void resume() {
		if (!paused) {
			log.warn("Attempt to resume normally running task {}, ignoring.", getClass().getSimpleName());
			return;
		}
		pauseLock.lock();
		try {
			paused = false;
			unpaused.signalAll();
			log.info("Resuming task {}, by {}", getClass().getSimpleName(), Thread.currentThread());
		} finally {
			pauseLock.unlock();
		}
	}
	
	public synchronized void setInterval(long interval, TimeUnit timeUnit) {
		this.intervalNs = timeUnit.toNanos(interval);
	}

	@Override
	public void run() {
		do {
			long cycleBeginNs = System.nanoTime();
			pauseLock.lock();
			try {
				while (paused) {
					unpaused.await(2, TimeUnit.SECONDS);
					log.debug("Await passed by {}, paused is {}", Thread.currentThread(), paused);
				}
			} catch (InterruptedException ie) {
				log.debug("Await for resume was interrupted.", ie);
				Thread.currentThread().interrupt();
			} finally {
				pauseLock.unlock();
			}
			if (shutdown) return;
			
			runCycle();
			
			if (intervalNs > 0) {		// sleep until interval period passes
				long passedNs = System.nanoTime() - cycleBeginNs;
				try {
					TimeUnit.NANOSECONDS.sleep(intervalNs - passedNs);
				} catch (InterruptedException ie) {
					log.debug("Sleep until interval pass was interrupted.", ie);
					Thread.currentThread().interrupt();					
				}
			}
		} while (!Thread.interrupted() && !shutdown);
	}

	protected abstract void runCycle();
}

