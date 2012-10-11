package com.openexchange.realtime.example.websocket.chat;

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
		
		registerService(SimplePayloadConverter.class, new ChatMessageToJSON());
		registerService(SimplePayloadConverter.class, new JSONToChatMessage());
		
		registerService(WSHandler.class, new ConversionWSHandler("chat", "chatMessage"));
		
	}



}
