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

package com.openexchange.chronos.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface FreeBusyService {

    /**
     * Gets an array of <code>boolean</code> values representing the days where the current session's user has events at.
     *
     * @param session The calendar session
     * @param from The start date of the period to consider
     * @param until The end date of the period to consider
     * @return The "has" result, i.e. an array of <code>boolean</code> values representing the days where the user has events at
     */
    boolean[] hasEventsBetween(CalendarSession session, Date from, Date until) throws OXException;

    /**
     * Gets free/busy information in a certain interval for one ore more attendees.
     * <p/>
     * Optionally, the resulting data is pre-processed and sorted by time, so that any overlapping intervals each of the attendee's
     * free/busy time are merged implicitly to the most conflicting busy times.
     *
     * @param session The calendar session
     * @param attendees The attendees to get the free/busy data for
     * @param from The start of the requested time range
     * @param until The end of the requested time range
     * @param merge <code>true</code> to merge the resulting free/busy-times, <code>false</code>, otherwise
     * @return The free/busy times for each of the requested attendees, wrapped within a free/busy result structure
     */
    Map<Attendee, FreeBusyResult> getFreeBusy(CalendarSession session, List<Attendee> attendees, Date from, Date until, boolean merge) throws OXException;

    /**
     * Checks for potential conflicting events of the attendees with another event, typically prior event creation or update.
     *
     * @param session The calendar session
     * @param event The event to check (usually the event being created/updated)
     * @param attendees The attendees to check
     * @return The conflicts, or an empty list if there are none
     */
    List<EventConflict> checkForConflicts(CalendarSession session, Event event, List<Attendee> attendees) throws OXException;

}
