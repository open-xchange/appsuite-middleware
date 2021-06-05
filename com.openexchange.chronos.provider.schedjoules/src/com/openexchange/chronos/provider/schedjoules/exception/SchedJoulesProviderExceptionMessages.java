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

package com.openexchange.chronos.provider.schedjoules.exception;

/**
 * {@link SchedJoulesProviderExceptionMessages}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class SchedJoulesProviderExceptionMessages {

    // The requested page does not denote a calendar.
    static final String NO_CALENDAR_MSG = "The requested page does not denote a calendar.";
    // You have no access to this calendar.
    public static final String NO_ACCESS_MSG = "You have no access to this calendar.";
    // The requested calendar does not exist.
    static final String CALENDAR_DOES_NOT_EXIST_MSG = "The requested calendar does not exist.";
    // Your SchedJoules account is malformed. Please re-create it.
    static final String MALFORMED_ACCOUNT_MSG = "Your SchedJoules account is malformed. Please re-create it.";
    // You have specified an invalid refresh minimum interval for the calendar subscription
    static final String INVALID_MINIMUM_REFRESH_INTERVAL_MSG = "You have specified an invalid refresh minimum interval for the calendar subscription";
    // You have specified an invalid alarm value for the calendar subscription
    static final String INVALID_ALARM_VALUE_MSG = "You have specified an invalid alarm value for the calendar subscription";
}
