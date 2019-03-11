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

    /**
     * Initializes a new {@link PGPMailActivator}.
     */
    public PGPMailActivator() {
        super();
    }
    
    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PGPMailActivator.class);
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());
        registerService(PGPMimeService.class, new PGPMimeServiceImpl());
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PGPMailActivator.class);
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
