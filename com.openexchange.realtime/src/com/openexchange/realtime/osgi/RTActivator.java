package com.openexchange.realtime.osgi;

import org.osgi.framework.ServiceReference;

import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.impl.MessageDispatcherImpl;
import com.openexchange.realtime.packet.Payload;

public class RTActivator extends HousekeepingActivator {
	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{SimpleConverter.class};
	}

	@Override
	protected void startBundle() throws Exception {
		Payload.services = this;
		
		final MessageDispatcherImpl dispatcher = new MessageDispatcherImpl();
		
		registerService(MessageDispatcher.class, dispatcher);
		
		track(Channel.class, new SimpleRegistryListener<Channel>() {

			public void added(ServiceReference<Channel> ref, Channel service) {
				dispatcher.addChannel(service);
			}

			public void removed(ServiceReference<Channel> ref, Channel service) {
				dispatcher.removeChannel(service);
			}
		});
		
		openTrackers();
	}



}
