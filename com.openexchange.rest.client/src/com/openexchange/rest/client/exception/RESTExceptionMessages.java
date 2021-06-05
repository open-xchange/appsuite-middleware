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

package com.openexchange.rest.client.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link RESTExceptionMessages}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
final class RESTExceptionMessages implements LocalizableStrings {

    // The requested page was not found.
    public static final String PAGE_NOT_FOUND = "The requested page was not found.";
    // The remote '%1$s' service is unavailable at the moment. There is nothing we can do about it. Please try again later.
    public static final String REMOTE_SERVICE_UNAVAILABLE_MSG = "The remote '%2$s' service is unavailable at the moment. There is nothing we can do about it. Please try again later.";
    // An internal server error occurred on '%1$s' side. There is nothing we can do about it.
    public static final String REMOTE_INTERNAL_SERVER_ERROR_MSG = "An internal server error occurred on '%2$s' side. There is nothing we can do about it.";
    // A remote server error occurred on '%1$s' side. There is nothing we can do about it.
    public static final String REMOTE_SERVER_ERROR_MSG = "A remote server error occurred on '%2$s' side. There is nothing we can do about it.";
}
