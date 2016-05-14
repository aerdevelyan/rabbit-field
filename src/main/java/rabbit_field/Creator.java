package rabbit_field;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * God-like object that creates all the world, animals and issues commands.
 *
 */
@Singleton
public class Creator {
	private static Logger log = LogManager.getLogger();
	
	public void initWorld() {
		log.info("Initializing world.");
		
		
	}

	public void endWorld() {
		log.info("Apocalypse!");
		
	}
}
