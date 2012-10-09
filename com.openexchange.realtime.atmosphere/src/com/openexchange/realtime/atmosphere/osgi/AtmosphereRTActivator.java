
package com.openexchange.realtime.atmosphere.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.http.grizzly.services.atmosphere.AtmosphereService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.atmosphere.OXRTConversionHandler;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereChannel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereHandler;
import com.openexchange.realtime.atmosphere.impl.payload.PayloadTransformerRegistry;
import com.openexchange.realtime.payload.PayloadTransformer;
import com.openexchange.sessiond.SessiondService;

public class AtmosphereRTActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, AtmosphereService.class, MessageDispatcher.class };
    }

    /*
     * Start the basic atmosphere bundle, initialize a handler library and
     * listen for new PayloadTransformer services being added. When new services are
     * detected add them to the registry. This is important for:
     * <ol>
     * <li>The AtmosphereChannel so that it can decide if it is able to process incoming
     * Stanzas into POJOs and back again via the RTAtmosphereHandler.</li>
     * <li>Stanzatransformers as primary users of the payloadTransformers</li>
     */
    @Override
    protected void startBundle() throws Exception {

        openTrackers();

        // Set the ServiceLookup reference directly as class variable
        OXRTConversionHandler.services = this;

        final PayloadTransformerRegistry payloadTransformerRegistry = PayloadTransformerRegistry.getInstance();

        track(PayloadTransformer.class, new SimpleRegistryListener<PayloadTransformer>() {

            @Override
            public void added(ServiceReference<PayloadTransformer> ref, PayloadTransformer service) {
                payloadTransformerRegistry.add(service);
            }

            @Override
            public void removed(ServiceReference<PayloadTransformer> ref, PayloadTransformer service) {
                payloadTransformerRegistry.remove(service);
            }

        });

        AtmosphereService atmosphereService = getService(AtmosphereService.class);
        RTAtmosphereHandler handler = new RTAtmosphereHandler(payloadTransformerRegistry, this);
        atmosphereService.addAtmosphereHandler("rt", handler);

        registerService(Channel.class, new RTAtmosphereChannel(handler, payloadTransformerRegistry));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        getService(AtmosphereService.class).unregister("rt");
        super.stop(context);
    }

}
