package com.openexchange.spamhandler.spamassassin.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.spamhandler.spamassassin.api.SpamdService;


/**
 * A service tracker which handle the appearance and disappearance of the {@link SpamdService} and adds
 * it to the registry of this bundle
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class SpamdInstallationServiceListener implements ServiceTrackerCustomizer {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SpamdInstallationServiceListener.class);

    private final BundleContext context;

    public SpamdInstallationServiceListener(BundleContext context) {
        this.context = context;
    }

    public Object addingService(ServiceReference serviceReference) {
        final Object service = context.getService(serviceReference);
        if (service instanceof SpamdService) {
            if (null == ServiceRegistry.getInstance().getService(SpamdService.class)) {
                ServiceRegistry.getInstance().addService(SpamdService.class, (SpamdService) service);
            } else {
                LOG.error("Duplicate SpamdInstallationService detected: " + serviceReference.getClass().getName());
            }
        }
        return service;
    }

    public void modifiedService(ServiceReference arg0, Object arg1) {
        // Nothing to do
    }

    public void removedService(ServiceReference arg0, Object o) {
        if (o instanceof SpamdService) {
            ServiceRegistry.getInstance().removeService(SpamdService.class);
        }
        context.ungetService(arg0);
    }

}
