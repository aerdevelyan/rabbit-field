package rabbit_field.web;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import rabbit_field.event.ShutdownEvent;
import rabbit_field.field.Field;
import rabbit_field.msg.FieldViewMsg;

@Singleton
public class FieldViewSender implements Runnable {
	private final static Logger log = LogManager.getLogger();
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Field view sender"));
	private final Field field;
	private final Jsonb jsonb;
	
	@Inject
	public FieldViewSender(Field field) {
		this.field = field;
		jsonb = initJsonb();
	}

	private Jsonb initJsonb() {
		JsonbConfig config = new JsonbConfig();
		config.withAdapters(new FieldViewMsg.CellViewAdapter());
		return JsonbBuilder.create(config);
	}

	@Override
	public void run() {
		try {
			Session session = WSEndpoint.session;
			if (session != null && session.isOpen()) {
				String json = jsonb.toJson(new FieldViewMsg(field.getView()));
				session.getBasicRemote().sendText(json);
			}
		} catch (IOException e) {
			log.error("Error while sending data", e);
		}
	}

	public void start() {
		log.info("Scheduling a field view sender task");
		executorService.scheduleAtFixedRate(this, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	@Subscribe
	public void shutdown(ShutdownEvent evt) {
		evt.add(ShutdownEvent.Ordering.VIEW_SENDER, null, executorService, null);
	}
	
}

