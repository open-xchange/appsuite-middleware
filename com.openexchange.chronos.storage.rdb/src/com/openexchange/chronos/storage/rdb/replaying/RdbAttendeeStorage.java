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

package com.openexchange.chronos.storage.rdb.replaying;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.ParticipationStatus;
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
    public Map<String, ParticipationStatus> loadPartStats(String[] eventIds, Attendee attendee) throws OXException {
        return delegate.loadPartStats(eventIds, attendee);
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
    public void deleteAllAttendees() throws OXException {
        delegate.deleteAllAttendees();
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
