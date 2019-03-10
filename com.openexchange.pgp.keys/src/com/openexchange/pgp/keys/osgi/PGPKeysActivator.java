
package com.openexchange.pgp.keys.osgi;

import org.slf4j.Logger;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pgp.keys.parsing.PGPKeyRingParser;
import com.openexchange.pgp.keys.parsing.impl.AsciiArmoredKeyParser;
import com.openexchange.pgp.keys.parsing.impl.PGPKeyParserImpl;

/**
 * Activator for PGP keys bundle.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class PGPKeysActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link PGPKeysActivator}.
     */
    public PGPKeysActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PGPKeysActivator.class);
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());
        registerService(PGPKeyRingParser.class, new PGPKeyParserImpl(new AsciiArmoredKeyParser()));
    }

    @Override
    protected void stopBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PGPKeysActivator.class);
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
