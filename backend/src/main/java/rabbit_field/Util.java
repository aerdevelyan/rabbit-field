package rabbit_field;

import java.util.concurrent.TimeUnit;

/**
 * Every application has this.
 */
public class Util {
	
	public static void sleepSec(long timeout) {
        try {
			TimeUnit.SECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			throw new Error("Interrupt while sleeping.", e);
		}
	}
}
