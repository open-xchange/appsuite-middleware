package com.openexchange.realtime.atmosphere.presence.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.http.grizzly.services.atmosphere.AtmosphereService;
import com.openexchange.http.grizzly.services.http.OSGiServletContext;
//import com.openexchange.log.Log;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.atmosphere.OXRTConversionHandler;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import com.openexchange.realtime.example.atmosphere.chat.AtmosphereChatActivator;
import com.openexchange.realtime.example.atmosphere.chat.ChatHandler;

import converters.ChatMessageToJSONConverter;
import converters.JSONToChatMessageConverter;

public class AtmospherePresenceActivator extends HousekeepingActivator {

//    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(AtmospherePresenceActivator.class));
    private static final Log LOG = com.openexchange.log.Log.loggerFor(OSGiServletContext.class);
    
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{AtmosphereService.class};
    }

    @Override
    protected void startBundle() throws Exception {

        trackService(AtmosphereService.class);
        openTrackers();
        
        /*
         * Register the package specific payload converters. The
         * SimpleConverterActivator listens for registrations of new
         * SimplePayloadConverters. When new SimplePayloadConverters are added
         * they are wrapped in a PayloadConverterAdapter and registered as
         * ResultConverter service so they can be added to the DefaultConverter
         * (as the DispatcherActivator is listening for new ResultConverter
         * services) which then can be used by the {@link Payload} to convert
         * itself via the conversion service offered by the
         * {@link DefaultConverter} 
         */
        registerService(SimplePayloadConverter.class, new PresenceStatusToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToPresenceStatusConverter());
        
        /*
         * After adding the new SimplePayloadConverters that are able to convert
         * from and to PresenceStatus POJOS we can register a new OXRTConversionHandler
         * for PresenceStatus. All this ConversionHandler does is to tell the
         * Payload to convert itself into the desired format.
         */
        registerService(OXRTHandler.class,  new OXRTConversionHandler("presence", "presenceStatus"));
    }

}
