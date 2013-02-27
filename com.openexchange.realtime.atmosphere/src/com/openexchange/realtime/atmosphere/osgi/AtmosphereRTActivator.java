
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
import com.openexchange.realtime.atmosphere.payload.converter.AtmospherePayloadElementConverter;
import com.openexchange.realtime.dispatch.StanzaHandler;
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
        AtmospherePayloadElementConverter.SERVICES.set(this);

        track(AtmospherePayloadElementConverter.class, new SimpleRegistryListener<AtmospherePayloadElementConverter>() {

            @Override
            public void added(final ServiceReference<AtmospherePayloadElementConverter> ref, final AtmospherePayloadElementConverter transformer) {
                extensions.addPayloadElementTransFormer(transformer);
            }

            @Override
            public void removed(final ServiceReference<AtmospherePayloadElementConverter> ref, final AtmospherePayloadElementConverter transformer) {
                extensions.removePayloadElementTransformer(transformer);
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
