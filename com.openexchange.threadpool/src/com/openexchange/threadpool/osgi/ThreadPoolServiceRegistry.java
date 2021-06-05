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

package com.openexchange.threadpool.osgi;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceRegistry;

/**
 * {@link ThreadPoolServiceRegistry} - The service registry for thread pool bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadPoolServiceRegistry {

    private static final ServiceRegistry SERVICE_REGISTRY = new ServiceRegistry();

    /**
     * Initializes a new {@link ThreadPoolServiceRegistry}.
     */
    private ThreadPoolServiceRegistry() {
        super();
    }

    /**
     * Gets the service registry.
     *
     * @return The service registry
     */
    public static ServiceRegistry getServiceRegistry() {
        return SERVICE_REGISTRY;
    }

    /**
     * Gets the service defined by given class
     *
     * @param <S> The type of service's class
     * @param clazz The service's class
     * @param errorOnAbsence <code>true</code> to throw an error on service absence; otherwise <code>false</code>
     * @return The service if found; otherwise <code>null</code> if <code>errorOnAbsence</code> is <code>false</code>
     * @throws OXException If <code>errorOnAbsence</code> is <code>true</code> and service could not be found
     */
    public static <S> S getService(final Class<? extends S> clazz, final boolean errorOnAbsence) throws OXException {
        return SERVICE_REGISTRY.getService(clazz, errorOnAbsence);
    }

    /**
     * Gets the service defined by given class
     *
     * @param <S> The type of service's class
     * @param clazz The service's class
     * @return The service if found; otherwise <code>null</code>
     */
    public static <S> S getService(final Class<? extends S> clazz) {
        return SERVICE_REGISTRY.getService(clazz);
    }

}
