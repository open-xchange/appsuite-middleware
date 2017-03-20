package com.openexchange.pgp.mail.osgi;

import org.slf4j.Logger;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pgp.mail.PGPMimeService;
import com.openexchange.pgp.mail.impl.PGPMimeServiceImpl;

/**
 *
 * {@link PGPMailActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPMailActivator extends HousekeepingActivator {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PGPMailActivator.class);

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
        registerService(PGPMimeService.class, new PGPMimeServiceImpl());
    }

    /* (non-Javadoc)
     * @see com.openexchange.osgi.HousekeepingActivator#stopBundle()
     */
    @Override
    protected void stopBundle() throws Exception {
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
