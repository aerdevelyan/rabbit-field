package rabbit_field;

import javax.inject.Singleton;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class MainGuiceModule extends AbstractModule {

	@Override
	protected void configure() {
		
	}

	@Provides @Singleton
	public EventBus provideEventBus() {
		return new EventBus("main");
	}
}
