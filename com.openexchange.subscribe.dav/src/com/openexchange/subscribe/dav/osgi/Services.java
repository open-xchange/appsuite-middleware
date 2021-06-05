/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.subscribe.dav.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.server.ServiceLookup;

/**
 * {@link Services} - Provides static access to {@link ServiceLookup} reference.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Services {

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

    /**
     * Gets the optional service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    public static <S extends Object> S getOptionalService(final Class<? extends S> clazz) {
        final ServiceLookup serviceLookup = SERVICES.get();
        if (null == serviceLookup) {
            return null;
        }
        return serviceLookup.getOptionalService(clazz);
    }

}
