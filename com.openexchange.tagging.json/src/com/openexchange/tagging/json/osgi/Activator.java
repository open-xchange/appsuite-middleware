package com.openexchange.tagging.json.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.service.http.HttpService;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.tagging.TaggingService;
import com.openexchange.tagging.json.TaggingServlet;

public class Activator extends DeferredActivator {
    
    private static final Class[] NEEDED_SERVICES = new Class[]{HttpService.class, TaggingService.class};
    private static final String ALIAS = "ajax/tagging";
    
    private static final Log LOG = LogFactory.getLog(Activator.class);
    
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        TaggingService taggingService = getService(TaggingService.class);
        TaggingServlet.setTaggingService(taggingService);
        
        final HttpService httpService = getService(HttpService.class);
        try {
            httpService.registerServlet(ALIAS, new TaggingServlet(), null, null);
            LOG.info(TaggingServlet.class.getName() + " successfully re-registered due to re-appearing of " + clazz.getName());
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        final HttpService httpService = getService(HttpService.class);
        if (httpService != null ) {
            httpService.unregister(ALIAS);
            LOG.info(TaggingServlet.class.getName() + " unregistered. ");
        }
    }

    @Override
    protected void startBundle() throws Exception {
        TaggingService taggingService = getService(TaggingService.class);
        TaggingServlet.setTaggingService(taggingService);
        
        if(null == taggingService) {
            return;
        }
        
        final HttpService httpService = getService(HttpService.class);
        if(null == httpService) {
            return;
        }
        try {
            httpService.registerServlet(ALIAS, new TaggingServlet(), null, null);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        handleUnavailability(null);
    }

}
