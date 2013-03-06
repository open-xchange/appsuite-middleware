
package com.openexchange.realtime.atmosphere.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.http.grizzly.service.atmosphere.AtmosphereService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereChannel;
import com.openexchange.realtime.atmosphere.impl.RTAtmosphereHandler;
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
import com.openexchange.realtime.payload.converter.PayloadTreeConverter;
import com.openexchange.sessiond.SessiondService;

public class AtmosphereRTActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, AtmosphereService.class, MessageDispatcher.class, SimpleConverter.class, ResourceDirectory.class, StanzaQueueService.class, PayloadTreeConverter.class, CapabilityService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        AtmosphereServiceRegistry.SERVICES.set(this);

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
        PayloadTreeConverter converter = getService(PayloadTreeConverter.class);
        converter.declarePreferredFormat(Presence.STATUS_PATH, PresenceState.class.getSimpleName());
        converter.declarePreferredFormat(Presence.MESSAGE_PATH, String.class.getSimpleName());
        converter.declarePreferredFormat(Presence.PRIORITY_PATH, Byte.class.getSimpleName());
        
        getService(CapabilityService.class).declareCapability("rt");

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        getService(AtmosphereService.class).unregister("rt");
        AtmosphereServiceRegistry.SERVICES.set(null);
        super.stop(context);
    }

}
