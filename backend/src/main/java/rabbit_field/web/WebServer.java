package rabbit_field.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import rabbit_field.RabbitFieldApp;
import rabbit_field.event.ShutdownEvent;

@Singleton
public class WebServer {
	public static final int PORT = 8080;
	private static Logger log = LogManager.getLogger();
	private Undertow server;
	private FieldViewSender fieldViewSender;
	private DeploymentManager manager;
	
	@Inject
	public WebServer(FieldViewSender fieldViewSender) {
		this.fieldViewSender = fieldViewSender;
	}

	public void start() {
		log.info("Starting web server.");
		
		WebSocketDeploymentInfo wsdi = new WebSocketDeploymentInfo();
		wsdi.addEndpoint(WSEndpoint.class);
        DeploymentInfo deploymentInfo = new DeploymentInfo()
                .setClassLoader(RabbitFieldApp.class.getClassLoader())
                .setContextPath("/")
                .addWelcomePage("index.html")
                .setResourceManager(new ClassPathResourceManager(RabbitFieldApp.class.getClassLoader(), "webui"))
                .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, wsdi)
                .setDeploymentName("rabbit_field.war");

        final ServletContainer container = ServletContainer.Factory.newInstance();
        manager = container.addDeployment(deploymentInfo);
        manager.deploy();
        
		PathHandler pathHandler = Handlers.path();
        try {
            pathHandler.addPrefixPath("/", manager.start());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        
        server = Undertow.builder()
                .addHttpListener(PORT, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
        fieldViewSender.start();
	}
	
	public void stop() {
		log.info("Stopping web server.");
		try {
			WSEndpoint.session.close();
			manager.stop();
		} catch (IOException | ServletException e) {
			log.error("Could not close WebSocket session.", e);
		}
		server.stop();
	}
	
	@Subscribe 
	public void shutdown(ShutdownEvent evt) {
		evt.add(ShutdownEvent.Ordering.WEB_SERVER, null, null, () -> stop());
	}
}

