package rabbit_field.web;

import java.io.StringReader;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;

import rabbit_field.RabbitFieldApp;
import rabbit_field.event.PauseResumeEvent;
import rabbit_field.event.ResetEvent;
import rabbit_field.msg.Message;
import rabbit_field.msg.Message.MsgType;
import rabbit_field.msg.PauseResumeMsg;

public class ClientMsgHandler {
	private final EventBus eventBus;
	private final Jsonb jsonb;
	
	@Inject
	public ClientMsgHandler(EventBus eventBus) {
		this.eventBus = eventBus;
		jsonb = JsonbBuilder.create();
	}

	public void handleMsg(String msgStr) {
		if (Strings.isNullOrEmpty(msgStr)) {
			throw new IllegalArgumentException("Message is empty.");
		}
		JsonObject msgObj;		
		try (JsonReader reader = Json.createReader(new StringReader(msgStr))) {;
			msgObj = reader.readObject();
		}
		String msgTypeStr = msgObj.getString("type");
		MsgType msgType = Message.MsgType.valueOf(msgTypeStr);
		Class<? extends Message> msgImplClass = msgType.getImplementationClass();
		Message message = jsonb.fromJson(msgStr, msgImplClass);
		switch (msgType) {
			case PAUSE_RESUME: eventBus.post(new PauseResumeEvent((PauseResumeMsg) message));
			break;
			case SHUTDOWN: RabbitFieldApp.proceedToShutdown();
			break;
			case RESET: reset();
			break;
			default:
		}
	}

	private void reset() {
		eventBus.post(new PauseResumeEvent(true));
		ResetEvent resetEvent = new ResetEvent();
		eventBus.post(resetEvent);
		resetEvent.executeInOrder();
	}
}
