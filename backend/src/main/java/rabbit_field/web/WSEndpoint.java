package rabbit_field.web;

import java.io.IOException;

import javax.websocket.EndpointConfig;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ServerEndpoint("/ws")
public class WSEndpoint {
	private final static Logger log = LogManager.getLogger();
	protected static Session session;
	
	@OnOpen
	public void onOpen(Session session, EndpointConfig conf) throws IOException {
		log.info("Client connected.");
		synchronized (WSEndpoint.class) {			
			if (WSEndpoint.session != null) {
				log.warn("Closing existing session.");
				WSEndpoint.session.close();
			}
			WSEndpoint.session = session; 
		}
	}
	
	@OnMessage
	public void msgHandler(String msg, Session session) throws IOException {
		log.info("recieved: " + msg);
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		log.error("WS endpoint error", t);
	}
}
