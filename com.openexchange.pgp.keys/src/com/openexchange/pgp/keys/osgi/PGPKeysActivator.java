package com.openexchange.pgp.keys.osgi;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pgp.keys.parsing.PGPKeyRingParser;
import com.openexchange.pgp.keys.parsing.impl.AsciiArmoredKeyParser;
import com.openexchange.pgp.keys.parsing.impl.PGPKeyParserImpl;

public class PGPKeysActivator extends HousekeepingActivator {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PGPKeysActivator.class);

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
        registerService(PGPKeyRingParser.class, new PGPKeyParserImpl(new AsciiArmoredKeyParser()));
    }

    /* (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
    }
}
