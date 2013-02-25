
package com.openexchange.realtime.atmosphere.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.http.grizzly.service.atmosphere.AtmosphereService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereChannel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereHandler;
import com.openexchange.realtime.atmosphere.payload.transformer.AtmospherePayloadElementTransformer;
import com.openexchange.realtime.atmosphere.stanza.StanzaHandler;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.sessiond.SessiondService;

public class AtmosphereRTActivator extends HousekeepingActivator {

    final ExtensionRegistry extensions = ExtensionRegistry.getInstance();

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, AtmosphereService.class, MessageDispatcher.class, SimpleConverter.class, ResourceDirectory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        AtmosphereServiceRegistry.SERVICES.set(this);
        AtmospherePayloadElementTransformer.SERVICES.set(this);

        track(AtmospherePayloadElementTransformer.class, new SimpleRegistryListener<AtmospherePayloadElementTransformer>() {

            @Override
            public void added(final ServiceReference<AtmospherePayloadElementTransformer> ref, final AtmospherePayloadElementTransformer transformer) {
                extensions.addPayloadElementTransFormer(transformer);
            }

            @Override
            public void removed(final ServiceReference<AtmospherePayloadElementTransformer> ref, final AtmospherePayloadElementTransformer transformer) {
                extensions.removePayloadElementTransformer(transformer);
            }
        });

        track(StanzaHandler.class, new SimpleRegistryListener<StanzaHandler>() {

            @Override
            public void added(final ServiceReference<StanzaHandler> ref, final StanzaHandler handler) {
                extensions.addStanzaHandler(handler);
            }

            @Override
            public void removed(final ServiceReference<StanzaHandler> ref, final StanzaHandler handler) {
                extensions.removeStanzaHandler(handler);
            }
        });

        openTrackers();

        AtmosphereService atmosphereService = getService(AtmosphereService.class);
        RTAtmosphereHandler handler = new RTAtmosphereHandler();
        atmosphereService.addAtmosphereHandler("rt", handler);
        registerService(Channel.class, new RTAtmosphereChannel(handler));

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        getService(AtmosphereService.class).unregister("rt");
        AtmosphereServiceRegistry.SERVICES.set(null);
        ExtensionRegistry.getInstance().clearRegistry();
        super.stop(context);
    }

}
