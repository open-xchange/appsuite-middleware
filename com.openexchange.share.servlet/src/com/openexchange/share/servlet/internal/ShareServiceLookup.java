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

package com.openexchange.share.servlet.internal;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ShareServiceLookup}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareServiceLookup {

    /**
     * Initializes a new {@link ShareServiceLookup}.
     */
    private ShareServiceLookup() {
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
    public static void set(ServiceLookup serviceLookup) {
        ref.set(serviceLookup);
    }

    public static <S extends Object> S getService(Class<? extends S> c) {
        ServiceLookup serviceLookup = ref.get();
        S service = null == serviceLookup ? null : serviceLookup.getService(c);
        return service;
    }

    public static <S extends Object> S getService(Class<? extends S> c, boolean throwOnAbsence) throws OXException {
        S service = getService(c);
        if (null == service && throwOnAbsence) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(c.getName());
        }
        return service;
    }

    public static <S extends Object> S getOptionalService(Class<? extends S> c) {
        ServiceLookup serviceLookup = ref.get();
        S service = null == serviceLookup ? null : serviceLookup.getOptionalService(c);
        return service;
    }

}
