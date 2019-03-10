package com.openexchange.pgp.core.osgi;

import org.slf4j.Logger;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pgp.core.BouncyCastleProviderInitializer;

/**
 * Activator for PGP core bundle.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPCoreActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link PGPCoreActivator}.
     */
    public PGPCoreActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PGPCoreActivator.class);
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());
        BouncyCastleProviderInitializer.initialize();
    }

    @Override
    protected void stopBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PGPCoreActivator.class);
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
