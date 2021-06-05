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

package com.openexchange.chronos.storage;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.exception.OXException;

/**
 * {@link AttendeeStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface AttendeeStorage {

    /**
     * Loads all attendees for a specific event.
     *
     * @param eventId The identifier of the event to load the attendees for
     * @return The attendees
     */
    List<Attendee> loadAttendees(String eventId) throws OXException;

    /**
     * Loads the attendees for specific events.
     *
     * @param eventIds The identifiers of the events to load the attendees for
     * @return The attendees, mapped to the identifiers of the corresponding events
     */
    Map<String, List<Attendee>> loadAttendees(String[] eventIds) throws OXException;

    /**
     * Loads the attendees for specific events.
     *
     * @param eventIds The identifiers of the events to load the attendees for
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @return The attendees, mapped to the identifiers of the corresponding events
     */
    Map<String, List<Attendee>> loadAttendees(String[] eventIds, Boolean internal) throws OXException;

    /**
     * Loads the number of (internal) attendees for specific events.
     *
     * @param eventIds The identifiers of the events to load the attendee counts for
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @return The number of attendees, mapped to the identifiers of the corresponding events
     */
    Map<String, Integer> loadAttendeeCounts(String[] eventIds, Boolean internal) throws OXException;

    /**
     * Loads data of a specific attendee for a number of events.
     *
     * @param eventIds The identifiers of the events to load the attendee data for
     * @param attendee The attendee to load
     * @param fields The attendee fields to load, or <code>null</code> to include all properties
     * @return The loaded attendee data, mapped to the identifiers of the corresponding events
     */
    Map<String, Attendee> loadAttendee(String[] eventIds, Attendee attendee, AttendeeField[] fields) throws OXException;

    /**
     * Deletes all attendees for a specific event.
     *
     * @param eventId The identifier of the event to delete the attendees for
     */
    void deleteAttendees(String eventId) throws OXException;

    /**
     * Deletes all attendees for multiple events.
     *
     * @param eventIds The identifiers of the events to delete the attendees for
     */
    void deleteAttendees(List<String> eventIds) throws OXException;

    /**
     * Deletes multiple attendees for a specific event.
     *
     * @param eventId The identifier of the event to delete the attendees for
     * @param attendees The attendees to delete
     */
    void deleteAttendees(String eventId, List<Attendee> attendees) throws OXException;

    /**
     * Deletes all existing attendees for an account.
     *
     * @return <code>true</code> if something was actually deleted, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean deleteAllAttendees() throws OXException;

    /**
     * Inserts attendees for a specific event.
     *
     * @param eventId The identifier of the event to insert the attendees for
     * @param attendees The attendees to insert
     */
    void insertAttendees(String eventId, List<Attendee> attendees) throws OXException;

    /**
     * Inserts attendees for a multiple events.
     *
     * @param attendeesByEventId The attendees to insert, mapped to the corresponding event identifier
     */
    void insertAttendees(Map<String, List<Attendee>> attendeesByEventId) throws OXException;

    /**
     * Updates attendees for a specific event.
     *
     * @param eventId The identifier of the event to update the attendees for
     * @param attendees The attendees to update
     */
    void updateAttendees(String eventId, List<Attendee> attendees) throws OXException;

    /**
     * Updates an attendee for a specific event.
     *
     * @param eventId The identifier of the event to update the attendee for
     * @param attendee The attendee to update
     */
    void updateAttendee(String eventId, Attendee attendee) throws OXException;

    /**
     * Inserts a new (or overwrites previously existing) <i>tombstone</i> record for a specific attendee into the database.
     *
     * @param eventId The identifier of the event to insert the tombstone for
     * @param attendee The attendee to insert the tombstone for
     */
    void insertAttendeeTombstone(String eventId, Attendee attendee) throws OXException;

    /**
     * Inserts new (or overwrites previously existing) <i>tombstone</i> records for multiple attendees into the database.
     *
     * @param eventId The identifier of the event to insert the tombstones for
     * @param attendees The attendees to insert the tombstones for
     */
    void insertAttendeeTombstones(String eventId, List<Attendee> attendees) throws OXException;

    /**
     * Inserts new (or overwrites previously existing) <i>tombstone</i> records for multiple attendees into the database.
     *
     * @param attendeesByEventId The attendees to insert, mapped to the corresponding event identifier
     */
    void insertAttendeeTombstones(Map<String, List<Attendee>> attendeesByEventId) throws OXException;

    /**
     * Loads attendees for specific events in the stored <i>tombstone</i> records.
     *
     * @param eventIds The identifiers of the events to load the attendees for
     * @return The attendees, mapped to the identifiers of the corresponding events
     */
    Map<String, List<Attendee>> loadAttendeeTombstones(String[] eventIds) throws OXException;
}
