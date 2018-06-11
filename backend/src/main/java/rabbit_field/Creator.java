package rabbit_field;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;

import rabbit_field.event.ShutdownEvent;

/**
 * God-like object that creates the field, creatures and issues commands.
 *
 */
@Singleton
public class Creator {
	private static Logger log = LogManager.getLogger();
	private EventBus eventBus;
	
	@Inject
	public Creator(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void initWorld() {
		log.info("Initializing world.");
		
	}

	public void endWorld() {
		log.info("Apocalypse everyone!");
		eventBus.post(new ShutdownEvent());
		log.info("Apocalypse completed");
	}
}
