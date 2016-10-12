
package com.openexchange.report.appsuite.serialization.internal;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.server.ServiceLookup;

public class Services {

    private static final AtomicReference<ServiceLookup> SERVICES = new AtomicReference<ServiceLookup>();

    /**
     * Initializes a new {@link Services}.
     */
    private Services() {
        super();
    }

    /**
     * Sets the {@link ServiceLookup} reference.
     *
     * @param services The reference
     */
    public static void setServices(final ServiceLookup services) {
        SERVICES.set(services);
    }

    /**
     * Gets the {@link ServiceLookup} reference.
     *
     * @return The reference
     */
    public static ServiceLookup getServices() {
        return SERVICES.get();
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     * @throws IllegalStateException If an error occurs while returning the demanded service
     */
    public static <S extends Object> S getService(final Class<? extends S> clazz) {
        final ServiceLookup serviceLookup = SERVICES.get();
        if (null == serviceLookup) {
            throw new IllegalStateException("ServiceLookup is absent. Check bundle activator.");
        }
        return serviceLookup.getService(clazz);
    }
}
