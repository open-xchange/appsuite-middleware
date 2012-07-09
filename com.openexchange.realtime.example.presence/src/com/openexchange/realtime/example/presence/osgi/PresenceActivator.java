package com.openexchange.realtime.example.presence.osgi;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.example.presence.DummyPresenceService;
import com.openexchange.realtime.example.presence.PresenceService;

public class PresenceActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{MessageDispatcher.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(PresenceService.class, new DummyPresenceService(this));
	}


}
