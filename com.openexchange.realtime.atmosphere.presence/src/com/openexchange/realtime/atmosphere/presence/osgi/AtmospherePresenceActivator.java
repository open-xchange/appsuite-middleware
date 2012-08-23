
package com.openexchange.realtime.atmosphere.presence.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.atmosphere.OXRTConversionHandler;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import com.openexchange.realtime.atmosphere.presence.JSONToPresenceStatusConverter;
import com.openexchange.realtime.atmosphere.presence.PresenceStatusToJSONConverter;
import com.openexchange.realtime.example.presence.PresenceService;

/**
 * {@link AtmospherePresenceActivator} - Register the presence specific payload converters. The SimpleConverterActivator listens for
 * registrations of new SimplePayloadConverters. When we register our presence specific SimplePayloadConverters they are wrapped in a
 * PayloadConverterAdapter and registered as ResultConverter service so they can be added to the DefaultConverter (as the
 * DispatcherActivator is listening for new ResultConverter services) which then can be used by the {@link Payload} to convert itself via
 * the conversion service offered by the {@link DefaultConverter}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmospherePresenceActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AtmospherePresenceActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
//        return new Class[] { PresenceSubscriptionService.class, PresenceStatusService.class };
        return new Class[] {};
    }

    @Override
    protected void startBundle() throws Exception {

        trackService(PresenceService.class);
        openTrackers();

        registerService(SimplePayloadConverter.class, new PresenceStatusToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToPresenceStatusConverter());

        /*
         * After adding the new SimplePayloadConverters that are able to convert from and to presenceStatus we can register a new
         * OXRTConversionHandler for PresenceStatus. All this ConversionHandler does is to tell the payload to convert itself into the
         * desired format.
         */
        registerService(OXRTHandler.class, new OXRTConversionHandler("presence", "presenceStatus"));
        if(LOG.isInfoEnabled()) {
            LOG.info("Added presence status converters and handler.");
        }
    }

}
