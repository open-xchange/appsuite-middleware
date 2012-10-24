
package com.openexchange.realtime.atmosphere.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.http.grizzly.service.atmosphere.AtmosphereService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereChannel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereHandler;
import com.openexchange.realtime.atmosphere.impl.stanza.handler.StanzaHandler;
import com.openexchange.realtime.payload.PayloadElementTransformer;
import com.openexchange.sessiond.SessiondService;

public class AtmosphereRTActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, AtmosphereService.class, MessageDispatcher.class };
    }

    /**
     * Start the basic atmosphere bundle.
     * <p>
     * Initialize a PayloadElementTransformerlibrary and listen for new PayloadElementTransformer services being added. When new services
     * are detected add them to the registry. This is important for the RTAtmosphereHandler associated with the AtmosphereChannel so that it
     * can decide if it is able to completely transform incoming Stanzas into POJOs and back again via the StanzaTransformer.
     * </p>
     * <p>
     * Initialize a StanzaHandlerlibrary and listen for new StanzaHandler services being added. When new services are detected add them to
     * the registry. This is important for the RTAtmosphereHandler associated with The AtmosphereChannel so that it can delegate handling of
     * the different Stanzas subclasses.
     * </p>
     */
    @Override
    protected void startBundle() throws Exception {
        openTrackers();

        track(PayloadElementTransformer.class, new SimpleRegistryListener<PayloadElementTransformer>() {

            final PayloadElementTransformerRegistry payloadTransformerRegistry = PayloadElementTransformerRegistry.getInstance();

            @Override
            public void added(ServiceReference<PayloadElementTransformer> ref, PayloadElementTransformer service) {
                payloadTransformerRegistry.add(service);
            }

            @Override
            public void removed(ServiceReference<PayloadElementTransformer> ref, PayloadElementTransformer service) {
                payloadTransformerRegistry.remove(service);
            }

        });

        track(StanzaHandler.class, new SimpleRegistryListener<StanzaHandler>() {

            final StanzaHandlerRegistry payloadTransformerRegistry = StanzaHandlerRegistry.getInstance();

            @Override
            public void added(ServiceReference<StanzaHandler> ref, StanzaHandler service) {
                payloadTransformerRegistry.add(service);
            }

            @Override
            public void removed(ServiceReference<StanzaHandler> ref, StanzaHandler service) {
                payloadTransformerRegistry.remove(service);
            }

        });

        AtmosphereService atmosphereService = getService(AtmosphereService.class);
        RTAtmosphereHandler handler = new RTAtmosphereHandler();
        atmosphereService.addAtmosphereHandler("rt", new RTAtmosphereHandler());
        registerService(Channel.class, new RTAtmosphereChannel(handler));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        getService(AtmosphereService.class).unregister("rt");
        AtmosphereServiceRegistry.getInstance().clearRegistry();
        PayloadElementTransformerRegistry.getInstance().clearRegistry();
        StanzaHandlerRegistry.getInstance().clearRegistry();
        super.stop(context);
    }

}
