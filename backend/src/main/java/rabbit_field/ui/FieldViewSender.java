package rabbit_field.ui;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import rabbit_field.event.ShutdownEvent;

@Singleton
public class FieldViewSender implements Runnable {
	private final static Logger log = LogManager.getLogger();
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private long prevTime;
	
	public FieldViewSender() {
	}

	@Override
	public void run() {
		try {
			Session session = WSEndpoint.session;
			if (session != null && session.isOpen()) {
				session.getBasicRemote().sendText("time: " + (System.currentTimeMillis() - prevTime));
			}
			prevTime = System.currentTimeMillis();
		} catch (IOException e) {
			log.error("Error while sending data", e);
		}
	}

	public void start() {
		executorService.scheduleAtFixedRate(this, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	@Subscribe
	public void shutdown(ShutdownEvent evt) {
		executorService.shutdown();
	}
}