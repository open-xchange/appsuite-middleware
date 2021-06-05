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

package com.openexchange.chronos.provider;

import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link AdministrativeCalendarProvider}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public interface AdministrativeCalendarProvider extends CalendarProvider {

    /**
     * Retrieves the event for the given alarm
     *
     * @param context The context
     * @param account The {@link CalendarAccount}
     * @param eventId The event id
     * @param recurrence An optional event {@link RecurrenceId}
     * @return The event for the alarm
     * @throws OXException
     */
    public Event getEventByAlarm(Context context, CalendarAccount account, String eventId, RecurrenceId recurrence) throws OXException;

    /**
     * Touches the event defined by the event id
     *
     * @param context The {@link Context}
     * @param account The {@link CalendarAccount}
     * @param event The event id
     * @throws OXException
     */
    public void touchEvent(Context context, CalendarAccount account, String eventId) throws OXException;

}
