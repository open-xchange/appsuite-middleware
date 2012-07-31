package com.openexchange.realtime.atmosphere.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.openexchange.http.grizzly.services.atmosphere.AtmosphereService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import com.openexchange.realtime.atmosphere.impl.HandlerLibrary;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereChannel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereHandler;

public class AtmosphereRTActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{AtmosphereService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		
		final HandlerLibrary handlerLibrary = new HandlerLibrary();
		track(OXRTHandler.class, new SimpleRegistryListener<OXRTHandler>() {

			@Override
            public void added(ServiceReference<OXRTHandler> ref,
					OXRTHandler service) {
				handlerLibrary.add(service);
			}

			@Override
            public void removed(ServiceReference<OXRTHandler> ref,
					OXRTHandler service) {
				handlerLibrary.remove(service);
			}
			
		});
		
		AtmosphereService atmosphereService = getService(AtmosphereService.class);
		RTAtmosphereHandler handler = new RTAtmosphereHandler(handlerLibrary, this);
		atmosphereService.addAtmosphereHandler("rt", handler);
		
		openTrackers();
		
		registerService(Channel.class, new RTAtmosphereChannel(handler, handlerLibrary));
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		getService(AtmosphereService.class).unregister("rt");
		super.stop(context);
	}

	
}
