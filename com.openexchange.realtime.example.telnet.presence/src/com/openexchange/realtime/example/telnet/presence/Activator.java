package com.openexchange.realtime.example.telnet.presence;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.example.presence.PresenceService;
import com.openexchange.realtime.example.telnet.TelnetChatPlugin;

public class Activator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{PresenceService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		
		registerService(TelnetChatPlugin.class, new PresencePlugin(this));
		
	}


}
