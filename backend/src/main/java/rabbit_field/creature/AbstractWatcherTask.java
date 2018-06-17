package rabbit_field.creature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

abstract class AbstractWatcherTask implements Runnable {
	private final static Logger log = LogManager.getLogger();
	protected volatile boolean shutdown;
	
	public void shutdown() {
		log.debug("Shutdown is requested");
		shutdown = true;
	}
	
	@Override public void run() {
		do {
			watchCycle();
		} while (!Thread.interrupted() && !shutdown);
	}
	
	protected abstract void watchCycle();
}