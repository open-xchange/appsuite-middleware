
package com.openexchange.hazelcast.configuration.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.configuration.internal.HazelcastConfigurationServiceImpl;
import com.openexchange.hazelcast.configuration.internal.Services;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link HazelcastConfigurationActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class HazelcastConfigurationActivator extends HousekeepingActivator {

    protected static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastConfigurationActivator.class);

    /**
     * Initializes a new {@link HazelcastActivator}.
     */
    public HazelcastConfigurationActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, StringParser.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.set(this);
        registerService(HazelcastConfigurationService.class, new HazelcastConfigurationServiceImpl());
    }

    @Override
    public void stopBundle() throws Exception {
        super.stopBundle();
        Services.set(null);
    }

}
