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

package com.openexchange.chronos.itip;

import java.util.Date;
import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Type;
import com.openexchange.session.Session;

/**
 *
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public interface ITipIntegrationUtility {

    Event resolveUid(String uid, CalendarSession session) throws OXException;

    /**
     * Resolves an event by searching for the given UID at the specific recurrence
     *
     * @param uid The UID of the event
     * @param recurrenceId The recurrence the event should be at
     * @param session The calendar session
     * @return The event as stored in the DB or <code>null</code>
     * @throws OXException In case searching fails
     */
    Event resolveUid(String uid, RecurrenceId recurrenceId, CalendarSession session) throws OXException;

    List<EventConflict> getConflicts(Event event, CalendarSession session) throws OXException;

    List<Event> getExceptions(Event original, CalendarSession session) throws OXException;

    String getPrivateCalendarFolderId(int cid, int userId) throws OXException;

    void deleteEvent(Event event, CalendarSession session, Date clientLastModified) throws OXException;

    Event loadEvent(Event event, CalendarSession session) throws OXException;

    String getFolderIdForUser(Session session, String eventId, int userId) throws OXException;

    /**
     * Looks up if the current user is acting on behalf of the organizer of the event
     *
     * @param event The {@link Event} to extract the organizer from
     * @param session The {@link Session} to get the current user from
     * @return <code>true</code> only if the organizers {@link CalendarUser#getSentBy()} field is set
     *         and if it matches the currents users ID
     *         <code>false</code> otherwise
     */
    boolean isActingOnBehalfOf(Event event, Session session);

    Type getFolderType(Event event, CalendarSession session) throws OXException;
}
