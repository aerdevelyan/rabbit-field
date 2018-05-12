package rabbit_field.ui;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import rabbit_field.RabbitFieldApp;

public class WebServer {
	private static Logger log = LogManager.getLogger();
	private Undertow server;
	
	public void start() {
		log.info("Starting web server.");
        DeploymentInfo deploymentInfo = new DeploymentInfo()
                .setClassLoader(RabbitFieldApp.class.getClassLoader())
                .setContextPath("/")
                .addWelcomePage("index.html")
                .setResourceManager(new ClassPathResourceManager(RabbitFieldApp.class.getClassLoader(), "web"))
                .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME,
                        new WebSocketDeploymentInfo().addEndpoint(WSEndpoint.class))
                .setDeploymentName("rabbit_field.war");

        final ServletContainer container = ServletContainer.Factory.newInstance();
        DeploymentManager manager = container.addDeployment(deploymentInfo);
        manager.deploy();
        
		PathHandler pathHandler = Handlers.path();
        try {
            pathHandler.addPrefixPath("/", manager.start());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        
        server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
	}
	
	public void stop() {
		log.info("Stopping web server.");
		server.stop();
	}
}