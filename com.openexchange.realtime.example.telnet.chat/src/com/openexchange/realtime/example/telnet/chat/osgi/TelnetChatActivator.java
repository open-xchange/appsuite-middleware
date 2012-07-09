package com.openexchange.realtime.example.telnet.chat.osgi;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.example.telnet.TelnetChatPlugin;
import com.openexchange.realtime.example.telnet.chat.ChatTransformer;

public class TelnetChatActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		ChatTransformer transformer = new ChatTransformer();
		
		registerService(TelnetChatPlugin.class, transformer);
	}



}
