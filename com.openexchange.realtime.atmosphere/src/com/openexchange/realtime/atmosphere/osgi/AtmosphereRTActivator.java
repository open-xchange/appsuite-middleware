package com.openexchange.realtime.atmosphere.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.openexchange.http.grizzly.services.atmosphere.AtmosphereService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.atmosphere.OXRTConversionHandler;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import com.openexchange.realtime.atmosphere.impl.HandlerLibrary;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereChannel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereHandler;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.sessiond.SessiondService;

public class AtmosphereRTActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{SessiondService.class, AtmosphereService.class, MessageDispatcher.class};
	}

	/*
	 * Start the basic atmosphere bundle, initialize a handler library and
	 * listen for new OXRTHandler services being added. When new services are
	 * detected add them to the library. This is important to the
	 * AtmosphereChannel so that it can decide if it is able to process incoming
	 * Stanzas into POJOs and back again via the RTAtmosphereHandler.
	 */
	@SuppressWarnings("rawtypes")
    @Override
	protected void startBundle() throws Exception {
		
	    //Set the ServiceLookup reference directly as class variable
	    OXRTConversionHandler.SERVICES_REFERENCE.set(this);
	    
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
    protected void stopBundle() throws Exception {
	    OXRTConversionHandler.SERVICES_REFERENCE.set(null);
	    getService(AtmosphereService.class).unregister("rt");
	    super.stopBundle();
	}
	
}
