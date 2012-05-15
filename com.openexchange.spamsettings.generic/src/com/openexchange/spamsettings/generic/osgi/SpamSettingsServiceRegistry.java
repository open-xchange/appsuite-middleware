
package com.openexchange.spamsettings.generic.osgi;

import com.openexchange.osgi.ServiceRegistry;

/**
 * {@link SpamSettingsServiceRegistry} - A registry for services
 */
public final class SpamSettingsServiceRegistry {

    private static final ServiceRegistry REGISTRY = new ServiceRegistry();

    /**
     * Gets the service registry
     * 
     * @return The service registry
     */
    public static ServiceRegistry getServiceRegistry() {
        return REGISTRY;
    }

    /**
     * Initializes a new {@link SpamSettingsServiceRegistry}
     */
    private SpamSettingsServiceRegistry() {
        super();
    }

}
