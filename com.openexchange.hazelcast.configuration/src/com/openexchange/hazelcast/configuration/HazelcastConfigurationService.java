
package com.openexchange.hazelcast.configuration;

import com.hazelcast.config.Config;
import com.openexchange.exception.OXException;

/**
 * {@link HazelcastConfigurationService} - Provides the Hazelcast configuration.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface HazelcastConfigurationService  {

    /**
     * Gets a value indicating whether Hazelcast services are enabled or not.
     *
     * @return <code>true</code> if Hazelcast is enabled, <code>false</code>, otherwise
     */
    boolean isEnabled() throws OXException;

    /**
     * Gets the Hazelcast configuration.
     *
     * @return The configuration
     */
    Config getConfig() throws OXException;

}
