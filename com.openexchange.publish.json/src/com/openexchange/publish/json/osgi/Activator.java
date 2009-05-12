
package com.openexchange.publish.json.osgi;

import javax.servlet.Servlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.json.PublicationJSONErrorMessage;
import com.openexchange.publish.json.PublishJSONServlet;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;

public class Activator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Activator.class);

    private static final String ALIAS = "ajax/publications";

    private Servlet publishServlet;

    private static final Class<?>[] NEEDED_SERVICES = { HttpService.class, PublicationService.class};

    private ComponentRegistration componentRegistration;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        PublicationService publicationService = getService(PublicationService.class);
        PublishJSONServlet.setPublicationService(publicationService);

        final HttpService httpService = getService(HttpService.class);
        try {
            httpService.registerServlet(ALIAS, (publishServlet = new PublishJSONServlet()), null, null);
            LOG.info(PublishJSONServlet.class.getName() + " successfully re-registered due to re-appearing of " + clazz.getName());
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        final HttpService httpService = getService(HttpService.class);
        if (httpService != null && publishServlet != null) {
            httpService.unregister(ALIAS);
            publishServlet = null;
            LOG.info(PublishJSONServlet.class.getName() + " unregistered due to disappearing of " + clazz.getName());
        }
    }

    @Override
    protected void startBundle() throws Exception {
        componentRegistration = new ComponentRegistration(context, "PUBH","com.openexchange.publish.json", PublicationJSONErrorMessage.EXCEPTIONS);
        PublicationService publicationService = getService(PublicationService.class);
        PublishJSONServlet.setPublicationService(publicationService);

        if(publicationService == null) {
            return;
        }
        
        try {
            final HttpService httpService = getService(HttpService.class);
            if(httpService == null) {
                return;
            }
            httpService.registerServlet(ALIAS, (publishServlet = new PublishJSONServlet()), null, null);
            LOG.info(PublishJSONServlet.class.getName() + " successfully registered");
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            final HttpService httpService = getService(HttpService.class);
            if (httpService != null && publishServlet != null) {
                httpService.unregister(ALIAS);
                publishServlet = null;
                LOG.info(PublishJSONServlet.class.getName() + " unregistered due to bundle stop");
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
        componentRegistration.unregister();
    }
}
