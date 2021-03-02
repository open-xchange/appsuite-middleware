
package com.openexchange.ajax.framework;

import com.openexchange.test.common.test.TestClassConfig;

/**
 * 
 * {@link ConfigurableResource}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public interface ConfigurableResource {

    /**
     * Configures the resource represented by this class with the given
     * configuration
     *
     * @param testConfig The configuration to apply
     * @throws Exception In case configuration fails
     */
    void configure(TestClassConfig testConfig) throws Exception;

}
