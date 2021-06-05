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

package com.openexchange.passwordchange.service;

import com.openexchange.passwordchange.PasswordChangeService;

/**
 * Provides a static method for changing a user's password.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class PasswordChange {

    private static volatile PasswordChangeService service;

    /**
     * Default constructor.
     */
    private PasswordChange() {
        super();
    }

    /**
     * Gets the service
     *
     * @return the service
     */
    public static PasswordChangeService getService() {
        return service;
    }

    /**
     * Sets the service
     *
     * @param service the service to set
     */
    public static void setService(final PasswordChangeService service) {
        PasswordChange.service = service;
    }
}
