package com.openexchange.realtime.presence.osgi;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.MessageDispatcher;

public class PresenceActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{MessageDispatcher.class};
	}

	@Override
	protected void startBundle() throws Exception {
		//registerService(PresenceStatusService.class, new DummyPresenceService(this));
	}


}
