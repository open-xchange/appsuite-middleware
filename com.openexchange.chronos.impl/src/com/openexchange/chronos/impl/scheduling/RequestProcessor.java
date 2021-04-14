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

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.performer.AbstractUpdatePerformer;
import com.openexchange.chronos.impl.performer.PutPerformer;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RequestProcessor} - Processes incoming REQUEST method
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
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
                return new PutPerformer(this).perform(resource, shallReplace(resource));
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
