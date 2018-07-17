package rabbit_field.web;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

import rabbit_field.creature.AbstractCyclicTask;
import rabbit_field.event.OrderedExecutionEvent.OrderingComponent;
import rabbit_field.event.PauseResumeEvent;
import rabbit_field.event.ShutdownEvent;
import rabbit_field.field.Field;
import rabbit_field.msg.FieldViewMsg;

/**
 * Transfers Field state view data to client for display.
 */
@Singleton
public class FieldViewSender {
	private final static Logger log = LogManager.getLogger();
	private final int INTERVAL_MS = 500;
	private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Field view sender"));
	private final Field field;
	private final Jsonb jsonb;
	private final SenderTask senderTask;
	
	@Inject
	public FieldViewSender(Field field) {
		this.field = field;
		jsonb = initJsonb();
		senderTask = this.new SenderTask();
		executor.execute(senderTask);
	}

	private Jsonb initJsonb() {
		JsonbConfig config = new JsonbConfig();
		config.withAdapters(new FieldViewMsg.CellViewAdapter());
		return JsonbBuilder.create(config);
	}
	
	@Subscribe
	public void shutdown(ShutdownEvent evt) {
		evt.add(OrderingComponent.VIEW_SENDER, senderTask, executor);
	}
	
	@Subscribe
	public void pauseOrResume(PauseResumeEvent evt) {
		log.info("Received pause/resume event: {}", evt.isPause());
		evt.applyTo(senderTask);
	}
	
	private class SenderTask extends AbstractCyclicTask {

		public SenderTask() {
			super(true);
			setInterval(INTERVAL_MS, TimeUnit.MILLISECONDS);
		}
		
		@Override
		protected void runCycle() {
			try {
				Session session = WSEndpoint.session;
				if (session != null && session.isOpen()) {
					String json = jsonb.toJson(new FieldViewMsg(field.getView()));
					session.getBasicRemote().sendText(json);
				}
			} catch (IOException e) {
				log.error("Error while sending field view data", e);
			}
		}		
	}
}

