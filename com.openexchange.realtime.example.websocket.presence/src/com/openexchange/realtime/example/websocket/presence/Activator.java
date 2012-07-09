package com.openexchange.realtime.example.websocket.presence;

import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.example.websocket.ConversionWSHandler;
import com.openexchange.realtime.example.websocket.WSHandler;

public class Activator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(SimplePayloadConverter.class, new JSON2PresenceStatus());
		registerService(SimplePayloadConverter.class, new PresenceStatusToJSON());
		
		registerService(WSHandler.class, new ConversionWSHandler("presence", "presenceStatus"));
	}

	
}
