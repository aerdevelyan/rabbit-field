package rabbit_field.ui;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class WSEndpoint {

	@OnMessage
	public void msgHandler(String msg, Session session) {
		
	}
}
