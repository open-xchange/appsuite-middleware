/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.service;

import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

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
    EventUpdate compare(Event original, Event update, boolean considerUnset, EventField... ignoredFields) throws OXException;

    /**
     * Initializes a new event and copies some or all fields over from another event.
     *
     * @param event The event to copy
     * @param fields The fields to copy, or <code>null</code> to copy all event fields
     * @return The copied event
     */
    Event copyEvent(Event event, EventField... fields) throws OXException;

    /**
     * Handles a possible {@link CalendarExceptionCodes#INCORRECT_STRING} exception that occurred when attempting to store the event data
     * by removing any character sequences that are indicated in the exception's <i>problematics</i>.
     *
     * @param e The exception to handle
     * @param event The event being stored
     * @return <code>true</code> if incorrect strings have been successfully replaced, <code>false</code>, otherwise
     */
    boolean handleIncorrectString(OXException e, Event event);

    /**
     * Handles a possible {@link CalendarExceptionCodes#INCORRECT_STRING} exception that occurred when attempting to store the attendees data
     * by removing any character sequences that are indicated in the exception's <i>problematics</i>.
     *
     * @param e The exception to handle
     * @param attendees The attendees being stored
     * @return <code>true</code> if incorrect strings have been successfully replaced, <code>false</code>, otherwise
     */
    boolean handleIncorrectString(OXException e, List<Attendee> attendees);

    /**
     * Handles a possible {@link CalendarExceptionCodes#DATA_TRUNCATION} exception that occurred when attempting to store the event data
     * by trimming the affected values to the maximum allowed length that are indicated in the exception's <i>problematics</i>.
     *
     * @param e The exception to handle
     * @param event The event being stored
     * @return <code>true</code> if truncated values were trimmed successfully, <code>false</code>, otherwise
     */
    boolean handleDataTruncation(OXException e, Event event);

    /**
     * Handles a possible {@link CalendarExceptionCodes#DATA_TRUNCATION} exception that occurred when attempting to store the attendees data
     * by trimming the affected values to the maximum allowed length that are indicated in the exception's <i>problematics</i>.
     *
     * @param e The exception to handle
     * @param attendees The attendees being stored
     * @return <code>true</code> if truncated values were trimmed successfully, <code>false</code>, otherwise
     */
    boolean handleDataTruncation(OXException e, List<Attendee> attendees);

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
     * @param calendarUserId The identifier of the calendar user
     * @param timeZone The timezone as supplied by the client
     * @param originalTimeZone The original timezone in case of updates, or <code>null</code> if not available
     * @return The selected timezone, or <code>null</code> if passed timezone reference was <code>null</code>
     */
    TimeZone selectTimeZone(int calendarUserId, TimeZone timeZone, TimeZone originalTimeZone) throws OXException;

    /**
     * Checks and adjusts the timezones of the event's start- and end-time (in case they are <i>set</i>) to match well-known & valid
     * timezones, using different fallbacks if no exactly matching timezone is available.
     *
     * @param calendarUserId The identifier of the user to get the fallback timezone from
     * @param event The event to set the timezones in
     * @param originalEvent The original event, or <code>null</code> if not applicable
     */
    void adjustTimeZones(int calendarUserId, Event event, Event originalEvent) throws OXException;

}
