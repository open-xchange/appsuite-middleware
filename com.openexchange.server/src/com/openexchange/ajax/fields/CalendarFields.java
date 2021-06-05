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

package com.openexchange.ajax.fields;

public interface CalendarFields extends CommonFields {

    public static final String TITLE = "title";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String NOTE = "note";

    public static final String ALARM = "alarm";

    public static final String RECURRENCE_ID = "recurrence_id";

    public static final String OLD_RECURRENCE_POSITION = "pos";

    public static final String RECURRENCE_POSITION = "recurrence_position";

    public static final String RECURRENCE_DATE_POSITION = "recurrence_date_position";

    public static final String RECURRENCE_TYPE = "recurrence_type";

    public static final String RECURRENCE_START = "recurrence_start";

    public static final String CHANGE_EXCEPTIONS = "change_exceptions";

    public static final String DELETE_EXCEPTIONS = "delete_exceptions";

    public static final String DAYS = "days";

    public static final String DAY_IN_MONTH = "day_in_month";

    public static final String MONTH = "month";

    public static final String INTERVAL = "interval";

    public static final String UNTIL = "until";

    public static final String OCCURRENCES = "occurrences";

    public static final String NOTIFICATION = "notification";

    public static final String RECURRENCE_CALCULATOR = "recurrence_calculator";

    public static final String PARTICIPANTS = "participants";

    public static final String USERS = "users";

    public static final String CONFIRMATIONS = "confirmations";

    public static final String ORGANIZER = "organizer";

    public static final String ORGANIZER_ID = "organizerId";

    public static final String PRINCIPAL = "principal";

    public static final String PRINCIPAL_ID = "principalId";

    public static final String SEQUENCE = "sequence";

    static final String FULL_TIME = "full_time";

}
