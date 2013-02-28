
package com.openexchange.realtime.atmosphere.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.http.grizzly.service.atmosphere.AtmosphereService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereChannel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereHandler;
import com.openexchange.realtime.atmosphere.payload.converter.AtmospherePayloadElementConverter;
import com.openexchange.realtime.atmosphere.payload.converter.primitive.ByteToJSONConverter;
import com.openexchange.realtime.atmosphere.payload.converter.primitive.JSONToByteConverter;
import com.openexchange.realtime.atmosphere.payload.converter.primitive.JSONToStringConverter;
import com.openexchange.realtime.atmosphere.payload.converter.primitive.StringToJSONConverter;
import com.openexchange.realtime.atmosphere.presence.converter.JSONToPresenceStateConverter;
import com.openexchange.realtime.atmosphere.presence.converter.PresenceStateToJSONConverter;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.handle.StanzaQueueService;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.sessiond.SessiondService;

public class AtmosphereRTActivator extends HousekeepingActivator {

    final ExtensionRegistry extensions = ExtensionRegistry.getInstance();

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, AtmosphereService.class, MessageDispatcher.class, SimpleConverter.class, ResourceDirectory.class, StanzaQueueService.class };
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
        
        /*
         * Register the package specific payload converters. The SimpleConverterActivator listens for registrations of new
         * SimplePayloadConverters. When new SimplePayloadConverters are added they are wrapped in a PayloadConverterAdapter and registered
         * as ResultConverter service so they can be added to the DefaultConverter (as the DispatcherActivator is listening for new
         * ResultConverter services) which then can be used by the {@link PayloadElementTransformer} to convert them via the conversion
         * service offered by the {@link DefaultConverter}
         */
        registerService(SimplePayloadConverter.class, new ByteToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToByteConverter());
        registerService(SimplePayloadConverter.class, new StringToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToStringConverter());
        registerService(SimplePayloadConverter.class, new JSONToPresenceStateConverter());
        registerService(SimplePayloadConverter.class, new PresenceStateToJSONConverter());
        
     // Add Transformers using Converters
        registerService(
            AtmospherePayloadElementConverter.class,
            new AtmospherePayloadElementConverter(PresenceState.class.getSimpleName(), Presence.STATUS_PATH));
        registerService(AtmospherePayloadElementConverter.class, new AtmospherePayloadElementConverter(
            String.class.getSimpleName(),
            Presence.MESSAGE_PATH));
        registerService(AtmospherePayloadElementConverter.class, new AtmospherePayloadElementConverter(
            Byte.class.getSimpleName(),
            Presence.PRIORITY_PATH));

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        getService(AtmosphereService.class).unregister("rt");
        AtmosphereServiceRegistry.SERVICES.set(null);
        ExtensionRegistry.getInstance().clearRegistry();
        super.stop(context);
    }

}
