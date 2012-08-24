
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
 * {@link AtmospherePresenceActivator} - Register the presence specific payload converters as SimplePayloadConverters and add a new
 * OXRTHandler service that can handle incoming and outgoing Stanzas.
 * <ol>
 * <li>The <code>SimpleConverterActivator</code> listens for registrations of new <code>SimplePayloadConverters</code>.</li>
 * <li>When we register our presence specific <code>SimplePayloadConverters</code> the <code>SimpleconverterActivator</code> wraps them in a
 * <code>PayloadConverterAdapter</code> and registers them as <code>ResultConverter</code> services</li>
 * <li>The <code>DispatcherActivator</code> is listening for new <code>ResultConverter</code> services and adds them to the
 * <code>DefaultConverter</code></li>
 * <li>The presence <code>Payload</code> can then convert itself via the conversion service offered by the <code>DefaultConverter</code></li>
 * </ol>
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmospherePresenceActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AtmospherePresenceActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        // return new Class[] { PresenceSubscriptionService.class, PresenceStatusService.class };
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
         * OXRTConversionHandler for the PresenceStatus. All this ConversionHandler does is to tell the payload to convert itself into the
         * desired format.
         */
        registerService(OXRTHandler.class, new OXRTConversionHandler("presence", "presenceStatus"));
    }

}
