
package com.openexchange.realtime.atmosphere.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.http.grizzly.service.atmosphere.AtmosphereService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereChannel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereHandler;
import com.openexchange.realtime.atmosphere.osgi.service.AtmosphereRegistryService;
import com.openexchange.sessiond.SessiondService;

public class AtmosphereRTActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, AtmosphereService.class, MessageDispatcher.class };
    }

    @Override
    protected void startBundle() throws Exception {
        openTrackers();

        registerService(AtmosphereRegistryService.class, new AtmosphereRegistryServiceImpl());

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
