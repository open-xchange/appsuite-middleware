
package com.openexchange.publish.json.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.json.PublicationJSONErrorMessage;
import com.openexchange.publish.json.PublicationServlet;
import com.openexchange.publish.json.PublicationTargetServlet;
import com.openexchange.publish.json.types.FolderType;
import com.openexchange.publish.json.types.IDType;
import com.openexchange.server.osgiservice.DeferredActivator;

public class Activator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Activator.class);

    private static final String TARGET_ALIAS = "ajax/publicationTargets";
    private static final String PUB_ALIAS = "ajax/publications";

    private static final Class<?>[] NEEDED_SERVICES = { HttpService.class, PublicationTargetDiscoveryService.class };

    private ComponentRegistration componentRegistration;

    private PublicationTargetServlet targetServlet;

    private PublicationServlet pubServlet;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        registerServlets();

    }

    private void registerServlets() {
        PublicationTargetDiscoveryService discovery = getService(PublicationTargetDiscoveryService.class);
        if(discovery == null) {
            return;
        }
        PublicationTargetServlet.setPublicationTargetDiscoveryService(discovery);
        PublicationServlet.setPublicationTargetDiscoveryService(discovery);
        
        final HttpService httpService = getService(HttpService.class);
        try {
            httpService.registerServlet(TARGET_ALIAS, (targetServlet = new PublicationTargetServlet()), null, null);
            LOG.info(PublicationTargetServlet.class.getName() + " successfully re-registered.");
            httpService.registerServlet(PUB_ALIAS, (pubServlet = new PublicationServlet()), null, null);
            LOG.info(PublicationServlet.class.getName() + " successfully re-registered.");
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        unregisterServlets();
    }

    private void unregisterServlets() {
        final HttpService httpService = getService(HttpService.class);
        if (httpService != null && targetServlet != null) {
            httpService.unregister(TARGET_ALIAS);
            targetServlet = null;
            LOG.info(PublicationTargetServlet.class.getName() + " unregistered.");
            httpService.unregister(PUB_ALIAS);
            pubServlet = null;
            LOG.info(PublicationServlet.class.getName() + " unregistered.");
        }
    }

    @Override
    protected void startBundle() throws Exception {
        componentRegistration = new ComponentRegistration(
            context,
            "PUBH",
            "com.openexchange.publish.json",
            PublicationJSONErrorMessage.EXCEPTIONS);

        registerServlets();
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterServlets();
        componentRegistration.unregister();
    }
}
