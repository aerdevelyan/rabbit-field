package rabbit_field;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import rabbit_field.ui.WebServer;

public class RabbitFieldApp {
	private static Logger log = LogManager.getLogger();
	
	private static EventBus uiBus = new EventBus("ui");
	
    public static void main(String[] args) throws InterruptedException {
        log.info("Application started!");
        WebServer server = new WebServer();
        server.start();
        
        Injector injector = Guice.createInjector(new AppGuiceModule());
        Creator force = injector.getInstance(Creator.class);
        force.initWorld();

        TimeUnit.SECONDS.sleep(100);
        server.stop();
        force.endWorld();
    }

	public static EventBus getUiBus() {
		return uiBus;
	}

}
