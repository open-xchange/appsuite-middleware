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

package com.openexchange.chronos.storage.rdb.replaying;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAttendeeStorage implements AttendeeStorage {

    private final AttendeeStorage delegate;
    private final AttendeeStorage legacyDelegate;

    /**
     * Initializes a new {@link RdbAttendeeStorage}.
     *
     * @param delegate The delegate storage
     * @param legacyDelegate The legacy delegate storage
     */
    public RdbAttendeeStorage(AttendeeStorage delegate, AttendeeStorage legacyDelegate) {
        super();
        this.delegate = delegate;
        this.legacyDelegate = legacyDelegate;
    }

    @Override
    public List<Attendee> loadAttendees(String eventId) throws OXException {
        return delegate.loadAttendees(eventId);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendees(String[] eventIds) throws OXException {
        return delegate.loadAttendees(eventIds);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendees(String[] eventIds, Boolean internal) throws OXException {
        return delegate.loadAttendees(eventIds, internal);
    }

    @Override
    public Map<String, Integer> loadAttendeeCounts(String[] eventIds, Boolean internal) throws OXException {
        return delegate.loadAttendeeCounts(eventIds, internal);
    }

    @Override
    public Map<String, Attendee> loadAttendee(String[] eventIds, Attendee attendee, AttendeeField[] fields) throws OXException {
        return delegate.loadAttendee(eventIds, attendee, fields);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendeeTombstones(String[] eventIds) throws OXException {
        return delegate.loadAttendeeTombstones(eventIds);
    }

    @Override
    public void deleteAttendees(String eventId) throws OXException {
        delegate.deleteAttendees(eventId);
        legacyDelegate.deleteAttendees(eventId);
    }

    @Override
    public void deleteAttendees(List<String> eventIds) throws OXException {
        delegate.deleteAttendees(eventIds);
        legacyDelegate.deleteAttendees(eventIds);
    }

    @Override
    public void deleteAttendees(String eventId, List<Attendee> attendees) throws OXException {
        delegate.deleteAttendees(eventId, attendees);
        legacyDelegate.deleteAttendees(eventId, attendees);
    }

    @Override
    public boolean deleteAllAttendees() throws OXException {
        return delegate.deleteAllAttendees();
    }

    @Override
    public void insertAttendees(String eventId, List<Attendee> attendees) throws OXException {
        delegate.insertAttendees(eventId, attendees);
        legacyDelegate.insertAttendees(eventId, attendees);
    }

    @Override
    public void insertAttendees(Map<String, List<Attendee>> attendeesByEventId) throws OXException {
        delegate.insertAttendees(attendeesByEventId);
        legacyDelegate.insertAttendees(attendeesByEventId);
    }

    @Override
    public void updateAttendees(String eventId, List<Attendee> attendees) throws OXException {
        delegate.updateAttendees(eventId, attendees);
        legacyDelegate.updateAttendees(eventId, attendees);
    }

    @Override
    public void updateAttendee(String eventId, Attendee attendee) throws OXException {
        delegate.updateAttendee(eventId, attendee);
        legacyDelegate.updateAttendee(eventId, attendee);
    }

    @Override
    public void insertAttendeeTombstone(String eventId, Attendee attendee) throws OXException {
        delegate.insertAttendeeTombstone(eventId, attendee);
        legacyDelegate.insertAttendeeTombstone(eventId, attendee);
    }

    @Override
    public void insertAttendeeTombstones(String eventId, List<Attendee> attendees) throws OXException {
        delegate.insertAttendeeTombstones(eventId, attendees);
        legacyDelegate.insertAttendeeTombstones(eventId, attendees);
    }

    @Override
    public void insertAttendeeTombstones(Map<String, List<Attendee>> attendeesByEventId) throws OXException {
        delegate.insertAttendeeTombstones(attendeesByEventId);
        legacyDelegate.insertAttendeeTombstones(attendeesByEventId);
    }

}
