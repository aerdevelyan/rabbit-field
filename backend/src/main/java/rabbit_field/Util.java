package rabbit_field;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rabbit_field.creature.Creature;


/**
 * Every application has this.
 */
public class Util {
	private static Logger log = LogManager.getLogger();
	
	public static void sleepSec(long timeout) {
        try {
			TimeUnit.SECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			throw new Error("Interrupt while sleeping.", e);
		}
	}
	
	public static void imitateLongThinking(Creature creature) {
		try {
			TimeUnit.MILLISECONDS.sleep(new Random().nextInt(150));
		} catch (InterruptedException e) {
			log.warn("Got interrupted {}", creature);
		}
	}
}
