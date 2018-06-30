package rabbit_field;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import rabbit_field.creature.CreatureController;
import rabbit_field.creature.MasterMind;
import rabbit_field.web.FieldViewSender;
import rabbit_field.web.WebServer;

@Singleton
public class RabbitFieldApp {
	private static final Logger log = LogManager.getLogger();
	private final EventBus eventBus;
	private final Creator creator;
	private Injector injector;
	
	@Inject
	public RabbitFieldApp(EventBus eventBus, Creator creator) {
		this.eventBus = eventBus;
		this.creator = creator;
	}

	public static void main(String[] args) {
        log.info("Starting Application");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	log.warn("Shutdown hook");
            }
        });
        Injector injector = Guice.createInjector(new MainGuiceModule());
        RabbitFieldApp app = injector.getInstance(RabbitFieldApp.class);
        app.setInjector(injector);
        app.startApp();
        log.info("Exiting Application");
    }

    public void startApp() {
        registerSubscribers();
        WebServer server = injector.getInstance(WebServer.class);
        server.start();
        creator.initWorld();
        log.info("Initialization complete, point your browser to http://localhost:{}", WebServer.PORT);
        Util.sleepSec(90);

        creator.endWorld();
    }
    
    private void registerSubscribers() {
    	eventBus.register(injector.getInstance(MasterMind.class));
    	eventBus.register(injector.getInstance(CreatureController.class));
    	eventBus.register(injector.getInstance(WebServer.class));
    	eventBus.register(injector.getInstance(FieldViewSender.class));
    }

	public Injector getInjector() {
		return injector;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}
    
}
