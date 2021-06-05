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
package com.openexchange.authorization;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A utility class that provides {@link #getService() static access} to the tracked instance of {@link AuthorizationService}.
 */
public final class Authorization {

    private static final AtomicReference<AuthorizationService> SERVICE_REF = new AtomicReference<AuthorizationService>();

    /**
     * Default constructor.
     */
    private Authorization() {
        super();
    }

    /**
     * Gets the tracked instance of {@code AuthorizationService}.
     *
     * @return The service or <code>null</code> if no instance of <code>AuthorizationService</code> appeared yet
     */
    public static AuthorizationService getService() {
        return SERVICE_REF.get();
    }

    /**
     * Sets the given service instance.
     *
     * @param service The service to set
     * @return <code>true</code> if given service instance could be successfully set; otherwise <code>false</code> if a concurrent modification occurred
     */
    public static boolean setService(final AuthorizationService service) {
        return SERVICE_REF.compareAndSet(null, service);
    }

    /**
     * Unsets the given service in case that service is currently active.
     *
     * @param service The service to unset
     * @return <code>true</code> if service could be successfully unset; otherwise <code>false</code>
     */
    public static boolean dropService(final AuthorizationService service) {
        return SERVICE_REF.compareAndSet(service, null);
    }

}
