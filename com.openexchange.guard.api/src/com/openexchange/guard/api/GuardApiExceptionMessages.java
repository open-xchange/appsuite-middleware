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

package com.openexchange.guard.api;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link GuardApiExceptionMessages} - Exception messages for errors that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GuardApiExceptionMessages implements LocalizableStrings {

    // A Guard error occurred: %1$s
    public static final String GUARD_ERROR_MSG = "An error occurred: %1$s";

    // A Guard error occurred: %1$s
    public static final String GUARD_SERVER_ERROR_MSG = "A server error occurred with HTTP status code %1$s. Error message: %2$s";

    // Invalid Guard URL: %1$s
    public static final String INVALID_GUARD_URL_MSG = "The provided URL: %1$s is invalid";

    // The Guard resource does not exist: %1$s
    public static final String NOT_FOUND_MSG = "The provided resource does not exist: %1$s";

    // The Guard resource does not exist
    public static final String NOT_FOUND_SIMPLE_MSG = "The resource does not exist";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // Authentication failed: %1$s
    public static final String AUTH_ERROR_MSG = "Authentication failed: %1$s";

    public static final String JSON_ERROR_MSG = "A JSON error occurred: %1$s";

    /**
     * Initializes a new {@link GuardApiExceptionMessages}.
     */
    private GuardApiExceptionMessages() {
        super();
    }

}
