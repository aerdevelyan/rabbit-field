package rabbit_field.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import rabbit_field.event.PauseResumeEvent;

public class ClientMsgHandlerTest {
	EventBus eventBus = Mockito.mock(EventBus.class);
	
	@Test
	public void handlePauseResume() throws Exception {
		ClientMsgHandler handler = new ClientMsgHandler(eventBus);
		handler.handleMsg("{\"type\":\"PAUSE_RESUME\",\"pause\":true}");		
		ArgumentCaptor<PauseResumeEvent> arg = ArgumentCaptor.forClass(PauseResumeEvent.class);
		verify(eventBus).post(arg.capture());
		PauseResumeEvent event = arg.getValue();
		assertThat(event.isPause()).isTrue();
	}
}
