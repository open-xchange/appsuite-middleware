

package com.openexchange.custom.parallels.osgi;

import com.openexchange.osgi.ServiceRegistry;


/**
 * {@link ParallelsServiceRegistry} - A registry for services
 * 
 */
public final class ParallelsServiceRegistry {

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
     * Initializes a new {@link ParallelsServiceRegistry}
     */
    private ParallelsServiceRegistry() {
        super();
    }

}
