package rabbit_field;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import rabbit_field.creature.CreatureController;
import rabbit_field.creature.MasterMind;
import rabbit_field.event.ShutdownEvent;
import rabbit_field.web.FieldViewSender;
import rabbit_field.web.WebServer;

@Singleton
public class RabbitFieldApp {
	private static final Logger log = LogManager.getLogger();
	private static final CountDownLatch appMainThreadLatch = new CountDownLatch(1);
	private final EventBus eventBus;
	private final Creator creator;
	public static Injector injector;
	
	@Inject
	public RabbitFieldApp(EventBus eventBus, Creator creator) {
		this.eventBus = eventBus;
		this.creator = creator;
	}

	public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	log.warn("Shutdown hook executed.");
            }
        });
        log.info("Initializinng injector and main application class.");
        Injector injector = Guice.createInjector(new MainGuiceModule());
        RabbitFieldApp.injector = injector;
        RabbitFieldApp app = injector.getInstance(RabbitFieldApp.class);
        app.startApp();
        appMainThreadLatch.await();
        app.initShutdown();
        log.info("Exiting application.");
    }

	public static void proceedToShutdown() {
		appMainThreadLatch.countDown();
	}
	
    private void startApp() {
    	log.info("Starting application.");
        registerSubscribers();
        WebServer server = injector.getInstance(WebServer.class);
        server.start();
        creator.initWorld();
        log.info("Initialization complete, point your browser to http://localhost:{}", WebServer.PORT);
    }
    
	private void initShutdown() {
		log.info("Sending shutdown event.");
		ShutdownEvent event = new ShutdownEvent();
		eventBus.post(event);
		event.performShutdown();
	}
    
    private void registerSubscribers() {
    	eventBus.register(creator);
    	eventBus.register(injector.getInstance(MasterMind.class));
    	eventBus.register(injector.getInstance(CreatureController.class));
    	eventBus.register(injector.getInstance(WebServer.class));
    	eventBus.register(injector.getInstance(FieldViewSender.class));
    }
    
}
