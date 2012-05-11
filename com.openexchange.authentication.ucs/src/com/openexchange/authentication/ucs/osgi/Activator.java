

package com.openexchange.authentication.ucs.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.ucs.impl.UCSAuthentication;

public class Activator implements BundleActivator {

    private static transient final Log LOG = LogFactory.getLog(Activator.class);

    /**
     * Reference to the service registration.
     */
    private ServiceRegistration<AuthenticationService> registration;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        LOG.info("starting bundle: com.openexchange.authentication.ucs");

        registration = context.registerService(AuthenticationService.class,
            new UCSAuthentication(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        LOG.info("stopping bundle: com.openexchange.authentication.ucs");

        registration.unregister();
    }
}
