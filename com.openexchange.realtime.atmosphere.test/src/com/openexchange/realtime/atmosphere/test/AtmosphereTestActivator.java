package com.openexchange.realtime.atmosphere.test;

import org.osgi.service.http.HttpService;
import com.openexchange.http.grizzly.services.atmosphere.AtmosphereService;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;

public class AtmosphereTestActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(AtmosphereTestActivator.class));
    
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
//        registerService(SimplePayloadConverter.class, new ChatMessageToJSONConverter());
//        registerService(SimplePayloadConverter.class, new JSONToChatMessageConverter());

        /*
         * After adding the new SimplePayloadConverters that are able to convert
         * from and to ChatMessage POJOS we can register a new OXRTConversionHandler
         * for ChatMessages. All this COnversionHandler does is to tell the
         * Payload to convert itself into the desired format. It takes chat
         * elements from the namespace default and transform them to ChatMessage POJOS.
         */
//        registerService(PayloadTransformer.class,  new OXRTConversionHandler(Message.class, "chatMessage"));
        
        //Add the atmosphere chat handler
        AtmosphereService service = getService(AtmosphereService.class);
        service.addAtmosphereHandler("/chat", new ChatHandler());
        LOG.info("added \"/chat\" AtmosphereHandler");
        
        HttpService httpService = getService(HttpService.class);
        httpService.registerResources("/atmosphere/originalChat", "/originalAtmosphereChat", null);
        httpService.registerResources("/atmosphere/chat", "/chat", null);
        httpService.registerResources("/atmosphere/presence","/presence", null);
    }

}
