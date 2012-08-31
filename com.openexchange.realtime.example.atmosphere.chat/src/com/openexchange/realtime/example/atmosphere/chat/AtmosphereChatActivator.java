package com.openexchange.realtime.example.atmosphere.chat;

import org.osgi.service.http.HttpService;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.http.grizzly.services.atmosphere.AtmosphereService;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.atmosphere.OXRTConversionHandler;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import converters.ChatMessageToJSONConverter;
import converters.JSONToChatMessageConverter;

public class AtmosphereChatActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(AtmosphereChatActivator.class));
    
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{HttpService.class, AtmosphereService.class};
    }

    @Override
    protected void startBundle() throws Exception {

        track(HttpService.class);
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
        registerService(SimplePayloadConverter.class, new ChatMessageToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToChatMessageConverter());
        
        /*
         * After adding the new SimplePayloadConverters that are able to convert
         * from and to ChatMessage POJOS we can register a new OXRTConversionHandler
         * for ChatMessages. All this COnversionHandler does is to tell the
         * Payload to convert itself into the desired format.
         */
        registerService(OXRTHandler.class,  new OXRTConversionHandler("chat", "chatMessage"));
        
        //Add the atmosphere chat handler
        AtmosphereService service = getService(AtmosphereService.class);
        service.addAtmosphereHandler("/chat", new OriginalChatAtmosphereHandler());
        LOG.info("added \"/chat\" AtmosphereHandler");
        
        HttpService httpService = getService(HttpService.class);
        httpService.registerResources("/originalAtmosphereChat", "/originalAtmosphereChat", null);
        httpService.registerResources("/atmosphereChat", "/atmosphereChat", null);
    }

}
