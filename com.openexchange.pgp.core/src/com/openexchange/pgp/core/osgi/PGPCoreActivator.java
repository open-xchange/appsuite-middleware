package com.openexchange.pgp.core.osgi;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pgp.core.BouncyCastleProviderInitializer;

public class PGPCoreActivator extends HousekeepingActivator {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PGPCoreActivator.class);

    /* (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#getNeededServices()
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#startBundle()
     */
    @Override
    protected void startBundle() throws Exception {
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());
        BouncyCastleProviderInitializer.initialize();
    }

    /* (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
    }
}
