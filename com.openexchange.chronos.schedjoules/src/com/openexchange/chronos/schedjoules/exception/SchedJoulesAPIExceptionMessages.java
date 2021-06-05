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

package com.openexchange.chronos.schedjoules.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SchedJoulesAPIExceptionMessages}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class SchedJoulesAPIExceptionMessages implements LocalizableStrings {

    // The requested page does not denote a calendar.
    static final String NO_CALENDAR_MSG = "The requested page does not denote a calendar.";
    // You have no access to this calendar.
    public static final String NO_ACCESS_MSG = "You have no access to this calendar.";
    // The requested page was not found.
    public static final String PAGE_NOT_FOUND = "The requested page was not found.";
    // The remote calendar service is unavailable at the moment. Please try again later.
    public static final String REMOTE_SERVICE_UNAVAILABLE_MSG = "The remote calendar service is unavailable at the moment. Please try again later.";
    // An error occurred on the remote calendar service. Please try again later.
    public static final String REMOTE_SERVER_ERROR_MSG = "An error occurred at the remote calendar service. Please try again later.";
}
