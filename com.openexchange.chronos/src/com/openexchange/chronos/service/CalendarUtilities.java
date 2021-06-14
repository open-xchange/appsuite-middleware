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

import java.util.Comparator;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link CalendarUtilities}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@SingletonService
public interface CalendarUtilities {

    /**
     * Compares all properties of an event to another one.
     *
     * @param original The original event
     * @param update The updated event
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences
     * @return The event update providing the differences
     */
    EventUpdate compare(Event original, Event update, boolean considerUnset, EventField... ignoredFields);

    /**
     * Initializes a new event and copies some or all fields over from another event.
     *
     * @param event The event to copy
     * @param fields The fields to copy, or <code>null</code> to copy all event fields
     * @return The copied event
     */
    Event copyEvent(Event event, EventField... fields) throws OXException;

    /**
     * Gets an entity resolver for a specific context.
     *
     * @param contextId The identifier of the context to get the entity resolver for
     * @return The entity resolver
     */
    EntityResolver getEntityResolver(int contextId) throws OXException;

    /**
     * Gets an event comparator based on the supplied sort order of event fields.
     *
     * @param sortOrders The sort orders to get the comparator for
     * @param timeZone The timezone to consider for comparing <i>floating</i> date properties, i.e. the actual 'perspective' of the
     *            comparison, or <code>null</code> to fall back to UTC
     * @return The comparator
     */
    Comparator<Event> getComparator(SortOrder[] sortOrders, TimeZone timeZone);

    /**
     * Selects a well-known and valid timezone based on a client-supplied timezone, using different fallbacks if no exactly matching
     * timezone is available.
     *
     * @param session The users session
     * @param calendarUserId The identifier of the calendar user
     * @param timeZone The timezone as supplied by the client
     * @param originalTimeZone The original timezone in case of updates, or <code>null</code> if not available
     * @return The selected timezone, or <code>null</code> if passed timezone reference was <code>null</code>
     */
    TimeZone selectTimeZone(Session session, int calendarUserId, TimeZone timeZone, TimeZone originalTimeZone) throws OXException;

    /**
     * Checks and adjusts the timezones of the event's start- and end-time (in case they are <i>set</i>) to match well-known & valid
     * timezones, using different fallbacks if no exactly matching timezone is available.
     * <p/>
     * For series events (both series master and overridden instances), also the timezone references in recurrence-related fields
     * (recurrence id, recurrence dates, delete- and change exception dates) are adjusted implicitly as needed.
     *
     * @param session The users session
     * @param calendarUserId The identifier of the user to get the fallback timezone from
     * @param event The event to set the timezones in
     * @param originalEvent The original event, or <code>null</code> if not applicable
     */
    void adjustTimeZones(Session session, int calendarUserId, Event event, Event originalEvent) throws OXException;

}
