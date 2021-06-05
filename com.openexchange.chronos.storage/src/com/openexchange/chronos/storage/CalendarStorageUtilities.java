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

import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarStorageUtilities}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarStorageUtilities {

    /**
     * Generates a <i>tombstone</i> attendee object based on the supplied attendee, as used to track the deletion in the storage.
     *
     * @param attendee The attendee to create the <i>tombstone</i> for
     * @return The <i>tombstone</i> attendee
     * @throws OXException In case of an error
     */
    Attendee getTombstone(Attendee attendee) throws OXException;

    /**
     * Generates <i>tombstone</i> attendee objects based on the supplied attendees, as used to track the deletion in the storage.
     *
     * @param attendees The attendees to create the <i>tombstone</i> for
     * @return The <i>tombstone</i> attendees
     * @throws OXException In case of an error
     */
    List<Attendee> getTombstones(List<Attendee> attendees) throws OXException;

    /**
     * Generates a <i>tombstone</i> event object based on the supplied event, as used to track the deletion in the storage.
     *
     * @param event The event to create the <i>tombstone</i> for
     * @param lastModified The last modification time to take over
     * @param modifiedBy The modifying calendar user to take over, or <code>null</code> to ignore
     * @return The <i>tombstone</i> event
     * @throws OXException In case of an error
     */
    Event getTombstone(Event event, Date lastModified, CalendarUser modifiedBy) throws OXException;

    /**
     * Loads additional event data from the storage, based on the requested fields. This currently includes
     * <ul>
     * <li>{@link EventField#ATTENDEES}</li>
     * <li>{@link EventField#ATTACHMENTS}</li>
     * <li>{@link EventField#CONFERENCES}</li>
     * <li>{@link EventField#ALARMS}</li> (of the calendar user)
     * </ul>
     *
     * @param userId The identifier of the calendar user to load additional data for, or <code>-1</code> to not load user-sensitive data
     * @param event The event to load additional data for
     * @param fields The requested fields, or <code>null</code> to assume all fields are requested
     * @return The event, enriched by the additionally loaded data
     * @throws OXException In case of an error
     */
    Event loadAdditionalEventData(int userId, Event event, EventField[] fields) throws OXException;

    /**
     * Loads additional event data from the storage, based on the requested fields. This currently includes
     * <ul>
     * <li>{@link EventField#ATTENDEES}</li>
     * <li>{@link EventField#ATTACHMENTS}</li>
     * <li>{@link EventField#CONFERENCES}</li>
     * <li>{@link EventField#ALARMS}</li> (of the calendar user)
     * </ul>
     *
     * @param userId The identifier of the calendar user to load additional data for, or <code>-1</code> to not load user-sensitive data
     * @param events The events to load additional data for
     * @param fields The requested fields, or <code>null</code> to assume all fields are requested
     * @return The events, enriched by the additionally loaded data
     * @throws OXException In case of an error
     */
    List<Event> loadAdditionalEventData(int userId, List<Event> events, EventField[] fields) throws OXException;

    /**
     * Loads additional event data from the tombstone storage, based on the requested fields. This currently only includes
     * {@link EventField#ATTENDEES}.
     *
     * @param events The events to load additional data for
     * @param fields The requested fields, or <code>null</code> to assume all fields are requested
     * @return The event tombstones, enriched by the additionally loaded data
     * @throws OXException In case of an error
     */
    List<Event> loadAdditionalEventTombstoneData(List<Event> events, EventField[] fields) throws OXException;

    /**
     * Removes all persisted data (events, alarms, attendees, conferences...) associated with the given account
     *
     * @throws OXException In case of an error
     */
    void deleteAllData() throws OXException;

}
