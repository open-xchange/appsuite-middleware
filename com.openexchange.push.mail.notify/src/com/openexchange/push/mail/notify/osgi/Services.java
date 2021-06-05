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

package com.openexchange.push.mail.notify.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link Services}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Services {

    /**
     * Initializes a new {@link Services}.
     */
    private Services() {
        super();
    }

    private static final AtomicReference<ServiceLookup> ref = new AtomicReference<ServiceLookup>();

    /**
     * Gets the service look-up
     *
     * @return The service look-up or <code>null</code>
     */
    public static ServiceLookup get() {
        return ref.get();
    }

    /**
     * Sets the service look-up
     *
     * @param serviceLookup The service look-up or <code>null</code>
     */
    public static void set(final ServiceLookup serviceLookup) {
        ref.set(serviceLookup);
    }

    /**
     * Gets the service.
     *
     * @param c The service type
     * @return The service or <code>null</code>
     */
    public static <S extends Object> S optService(final Class<? extends S> c) {
        final ServiceLookup serviceLookup = ref.get();
        return null == serviceLookup ? null : serviceLookup.getOptionalService(c);
    }

    /**
     * Gets the service.
     *
     * @param c The service type
     * @return The service
     * @throws OXException If service is absent
     */
    public static <S extends Object> S getService(final Class<? extends S> c, final boolean throwOnAbsence) throws OXException {
        final S service = optService(c);
        if (null == service && throwOnAbsence) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(c.getName());
        }
        return service;
    }

}
