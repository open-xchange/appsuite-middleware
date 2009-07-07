
package com.openexchange.publish.json.osgi;

import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.json.PublicationJSONErrorMessage;
import com.openexchange.publish.json.PublicationMultipleHandlerFactory;
import com.openexchange.publish.json.PublicationServlet;
import com.openexchange.publish.json.PublicationTargetMultipleHandlerFactory;
import com.openexchange.publish.json.PublicationTargetServlet;
import com.openexchange.publish.json.types.EntityMap;
import com.openexchange.publish.json.types.FolderType;
import com.openexchange.publish.json.types.IDType;
import com.openexchange.server.osgiservice.DeferredActivator;

public class ServletActivator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ServletActivator.class);

    private static final String TARGET_ALIAS = "ajax/publicationTargets";
    private static final String PUB_ALIAS = "ajax/publications";

    private static final Class<?>[] NEEDED_SERVICES = { HttpService.class, PublicationTargetDiscoveryService.class };

    private ComponentRegistration componentRegistration;

    private PublicationTargetServlet targetServlet;

    private PublicationServlet pubServlet;

    private List<ServiceRegistration> serviceRegistrations = new LinkedList<ServiceRegistration>();
    
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        register();

    }

    private void register() {
        PublicationTargetDiscoveryService discovery = getService(PublicationTargetDiscoveryService.class);
        if(discovery == null) {
            return;
        }
        
        PublicationMultipleHandlerFactory publicationHandlerFactory = new PublicationMultipleHandlerFactory(discovery, new EntityMap());
        PublicationTargetMultipleHandlerFactory publicationTargetHandlerFactory = new PublicationTargetMultipleHandlerFactory(discovery);
        
        serviceRegistrations.add(context.registerService(MultipleHandlerFactoryService.class.getName(), publicationHandlerFactory, null));
        serviceRegistrations.add(context.registerService(MultipleHandlerFactoryService.class.getName(), publicationTargetHandlerFactory, null));
        
        
        PublicationServlet.setFactory(publicationHandlerFactory);
        PublicationTargetServlet.setFactory(publicationTargetHandlerFactory);
        
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
        unregister();
    }

    private void unregister() {
        PublicationServlet.setFactory(null);
        PublicationTargetServlet.setFactory(null);
        
        for(ServiceRegistration registration : serviceRegistrations) {
            registration.unregister();
        }
        serviceRegistrations.clear();
        
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

        register();
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();
        componentRegistration.unregister();
    }
}
