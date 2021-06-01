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

package com.openexchange.find.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 *
 * {@link Services}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Services {

    private static final AtomicReference<ServiceLookup> LOOKUP = new AtomicReference<ServiceLookup>();

    /**
     * Initialises a new {@link Services}.
     */
    private Services() {
        super();
    }

    /**
     * Sets the service lookup instance
     *
     * @param lookup the {@link ServiceLookup} instance
     */
    public static void setServiceLookup(ServiceLookup lookup) {
        LOOKUP.set(lookup);
    }

    /**
     * Looks up a required service
     *
     * @param clazz The class of the service
     * @return The service
     * @throws OXException if the service is absent
     */
    public static <T> T requireService(Class<T> clazz) throws OXException {
        T service = getServiceLookup().getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.absentService(clazz);
        }
        return service;
    }

    /**
     * Looks up an optional service
     *
     * @param clazz The class of the service
     * @return The service or <code>null</code> if absent
     */
    public static <T> T optService(Class<T> clazz) {
        try {
            return getServiceLookup().getService(clazz);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Gets the {@link ServiceLookup} instance
     *
     * @return the {@link ServiceLookup} instance
     */
    private static ServiceLookup getServiceLookup() {
        ServiceLookup serviceLookup = LOOKUP.get();
        if (serviceLookup == null) {
            throw new IllegalStateException("ServiceLookup was null!");
        }
        return serviceLookup;
    }
}
