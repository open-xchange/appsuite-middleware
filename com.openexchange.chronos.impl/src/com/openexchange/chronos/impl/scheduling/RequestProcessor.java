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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.impl.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.IncomingCalendarObjectResource;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.performer.AbstractUpdatePerformer;
import com.openexchange.chronos.impl.performer.PutPerformer;
import com.openexchange.chronos.impl.performer.ResolvePerformer;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RequestProcessor} - Processes incoming REQUEST method
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class RequestProcessor extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link RequestProcessor}.
     * 
     * @param session The calendar session
     * @param storage The {@link ServiceLookup}
     * @param folder The calendar folder
     * @throws OXException In case of error
     */
    public RequestProcessor(CalendarSession session, CalendarStorage storage, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Puts a new or updated calendar object resource, i.e. an event and/or its change exceptions, to the calendar and afterwards
     * updates the attendee status as needed.
     * 
     * @param resource The calendar object resource to store
     * @return The result
     * @throws OXException In case of error
     */
    public InternalCalendarResult process(CalendarObjectResource resource) throws OXException {
        /*
         * Check if user is a party-crasher, put data as-is to storage if not
         */
        Attendee userAttendee = session.getEntityResolver().prepareUserAttendee(calendarUserId);
        for (Event event : resource.getEvents()) {
            if (null != CalendarUtils.find(event.getAttendees(), userAttendee)) {
                return new PutPerformer(this).perform(patch(resource), shallReplace(resource));
            }
        }
        /*
         * Current calendar user is part-crasher, prepare event by inserting the user with needs action
         */
        userAttendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
        List<Event> modified = EventMapper.getInstance().copy(resource.getEvents(), (EventField[]) null);
        for (Event event : modified) {
            List<Attendee> attendees = AttendeeMapper.getInstance().copy(event.getAttendees(), (AttendeeField[]) null);
            attendees.add(userAttendee);
            event.setAttendees(attendees);
        }
        return new PutPerformer(this).perform(new DefaultCalendarObjectResource(modified), shallReplace(resource));
    }

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Patches the given resource
     * <p>
     * In details:
     * <li> ensures consistent participant status of the calendar user
     * 
     * @param resource The resource to patch
     * @return The patched resource
     * @throws OXException
     */
    private CalendarObjectResource patch(CalendarObjectResource resource) throws OXException {
        List<Event> patched = new ArrayList<>(resource.getEvents().size());
        for (Event event : resource.getEvents()) {
            event = ensureConsistentPartStat(event);
            if (null == event) {
                // Indicates UID not found, new events
                return resource;
            }
            patched.add(event);
        }
        return new IncomingCalendarObjectResource(patched);
    }

    private Event ensureConsistentPartStat(Event event) throws OXException {
        Attendee attendee = CalendarUtils.find(event.getAttendees(), calendarUser);
        if (null == attendee) {
            /*
             * Use as-is
             */
            return event;
        }
        /*
         * Search for existing event, indicate new event if unknown
         */
        ResolvePerformer resolvePerformer = new ResolvePerformer(session, storage);
        EventID eventID = resolvePerformer.resolveByUid(event.getUid(), calendarUserId);
        if (null == eventID) {
            return null; // Indicate new
        } else if (null != event.getRecurrenceId()) {
            /*
             * Try to load for exception
             */
            EventID excpetionID = resolvePerformer.resolveByUid(event.getUid(), event.getRecurrenceId(), calendarUserId);
            if (null != excpetionID) {
                eventID = excpetionID;
            }
        }
        Event originalEvent = loadEventData(eventID.getObjectID());
        /*
         * Check if status is reset and if it is allowed
         */
        if (ParticipationStatus.NEEDS_ACTION.equals(attendee.getPartStat())) {
            if (event.getSequence() > originalEvent.getSequence()) {
                /*
                 * use as-is
                 */
                return event;
            }
        }
        /*
         * Avoid implicit reset
         */
        return ensureConsistentPartStat(originalEvent, event);
    }

    private final static AttendeeField[] COPY = new AttendeeField[] { AttendeeField.PARTSTAT, AttendeeField.COMMENT, AttendeeField.CN, AttendeeField.EMAIL, AttendeeField.ENTITY, AttendeeField.HIDDEN, AttendeeField.FOLDER_ID };

    /**
     * Restores certain properties for the current calendar user a organizer is not allowed to change
     * and which might be out of sync
     *
     * @param event The event to adjust the attendee in
     * @param originalAttendee The original attendee as saved in the DB
     * @return The event with the adjusted attendee
     * @throws OXException In case copy fails
     */
    private Event ensureConsistentPartStat(Event originalEvent, Event event) throws OXException {
        Attendee originalAttendee = CalendarUtils.find(originalEvent.getAttendees(), calendarUserId);
        List<Attendee> attendees = AttendeeMapper.getInstance().copy(event.getAttendees(), (AttendeeField[]) null);
        for (ListIterator<Attendee> iterator = attendees.listIterator(); iterator.hasNext();) {
            Attendee attendee = iterator.next();
            if (CalendarUtils.matches(attendee, originalAttendee)) {
                /*
                 * Copy from original if needed, return event if not
                 */
                if (false == originalAttendee.getPartStat().matches(attendee.getPartStat())) {
                    iterator.set(AttendeeMapper.getInstance().copy(originalAttendee, attendee, COPY));
                    break;
                }
                return event;
            }
        }
        Event updated = EventMapper.getInstance().copy(event, null, (EventField[]) null);
        updated.setAttendees(attendees);
        return updated;
    }

    /**
     * Gets a value indicating whether non-transmitted events shall be replaced or not
     *
     * @param resource The transmitted resource
     * @return <code>true</code> if events shall be replaced, <code>false</code> if not
     */
    private boolean shallReplace(CalendarObjectResource resource) {
        return null != resource.getSeriesMaster();
    }
}
