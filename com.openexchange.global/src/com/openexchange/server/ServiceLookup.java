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

package com.openexchange.server;

import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;

/**
 * {@link ServiceLookup} - A service look-up.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ServiceLookup {

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     * @throws IllegalStateException If an error occurs while returning the demanded service
     * @throws ShutDownRuntimeException If system is currently shutting down
     */
    <S extends Object> S getService(final Class<? extends S> clazz);

    /**
     * Gets the service of specified type. Throws error if service is absent.
     *
     * @param clazz The service's class
     * @return The service instance
     * @throws ShutDownRuntimeException If system is currently shutting down
     * @throws OXException In case of missing service
     */
    default @NonNull <S extends Object> S getServiceSafe(final Class<? extends S> clazz) throws OXException {
        S service = getOptionalService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }
        return service;
    }

    /**
     * Gets the optional service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     * @throws ShutDownRuntimeException If system is currently shutting down
     */
    <S extends Object> S getOptionalService(final Class<? extends S> clazz);

}
