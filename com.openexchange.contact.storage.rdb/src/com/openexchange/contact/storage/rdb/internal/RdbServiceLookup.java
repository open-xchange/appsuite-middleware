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

package com.openexchange.contact.storage.rdb.internal;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbServiceLookup} - Provides access to services.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbServiceLookup {

    /**
     * Initializes a new {@link DBChatServiceLookup}.
     */
    private RdbServiceLookup() {
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

    public static <S extends Object> S optService(final Class<? extends S> c) {
        ServiceLookup serviceLookup = ref.get();
        if (null == serviceLookup) {
            return null;
        }

        return serviceLookup.getOptionalService(c);
    }

    public static <S extends Object> S getService(final Class<? extends S> c) throws OXException {
        return RdbServiceLookup.getService(c, false);
    }

    public static <S extends Object> S getService(final Class<? extends S> c, boolean throwOnAbsence) throws OXException {
        final ServiceLookup serviceLookup = ref.get();
        final S service = null == serviceLookup ? null : serviceLookup.getService(c);
        if (null == service && throwOnAbsence) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(c.getName());
        }
        return service;
    }

    /**
     * Sets the service look-up
     *
     * @param serviceLookup The service look-up or <code>null</code>
     */
    public static void set(final ServiceLookup serviceLookup) {
        ref.set(serviceLookup);
    }

}
