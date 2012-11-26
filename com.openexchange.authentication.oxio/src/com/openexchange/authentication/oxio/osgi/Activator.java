

package com.openexchange.authentication.oxio.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.oxio.impl.OXIOAuthentication;

public class Activator implements BundleActivator {

    private static transient final Log LOG = LogFactory.getLog(Activator.class);

    /**
     * Reference to the service registration.
     */
    private ServiceRegistration registration;


    /**
     * {@inheritDoc}
     */
    public void start(final BundleContext context) throws Exception {
        LOG.info("Starting authentication service: oxio");
        registration = context.registerService(AuthenticationService.class.getName(),new OXIOAuthentication(), null);
        LOG.info("Authentication service oxio started!");
    }

    /**
     * {@inheritDoc}
     */
    public void stop(final BundleContext context) throws Exception {
        LOG.info("Stopping authentication service: oxio!");
        registration.unregister();
        LOG.info("Authentication service oxio stopped!");
    }
}
