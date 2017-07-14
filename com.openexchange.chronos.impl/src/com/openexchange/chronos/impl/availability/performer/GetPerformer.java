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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.impl.availability.performer;

import static com.openexchange.chronos.common.CalendarUtils.filter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link GetPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetPerformer extends AbstractPerformer {

    /**
     * Initialises a new {@link GetPerformer}.
     */
    public GetPerformer(CalendarAvailabilityStorage storage, CalendarSession session) {
        super(storage, session);
    }

    public CalendarAvailability perform(String calendarAvailabilityId) throws OXException {
        return storage.loadCalendarAvailability(calendarAvailabilityId);
    }

    public List<CalendarAvailability> perform() throws OXException {
        return storage.loadCalendarAvailabilities(session.getUserId());
    }

    public List<CalendarAvailability> performInRange(Date from, Date until) throws OXException {
        return storage.loadCalenarAvailabilityInRange(session.getUserId(), from, until);
    }

    public Map<Attendee, List<CalendarAvailability>> performForAttendees(List<Attendee> attendees, Date from, Date until) throws OXException {
        // Prepare the attendees
        attendees = session.getEntityResolver().prepare(attendees);
        // Filter the external ones
        attendees = filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL);
        if (attendees.size() == 0) {
            return java.util.Collections.emptyMap();
        }

        // Create a reverse lookup index

        Map<Integer, Attendee> reverseLookup = new HashMap<>();
        for (Attendee a : attendees) {
            reverseLookup.put(a.getEntity(), a);
        }
        // Fetch
        List<CalendarAvailability> availabilities = storage.loadAttendeeCalendarAvailability(attendees, from, until);
        // Build the return map
        Map<Attendee, List<CalendarAvailability>> map = new HashMap<>();
        for (CalendarAvailability availability : availabilities) {
            Attendee attendee = reverseLookup.get(availability.getCalendarUser());
            Collections.put(map, attendee, map.get(attendee));
        }

        return map;
    }

    public Map<CalendarUser, List<CalendarAvailability>> performForUsers(List<CalendarUser> users, Date from, Date until) throws OXException {
        // Create a reverse lookup index
        Map<Integer, CalendarUser> reverseLookup = new HashMap<>();
        for (CalendarUser u : users) {
            reverseLookup.put(u.getEntity(), u);
        }
        // Fetch
        List<CalendarAvailability> availabilities = storage.loadUserCalendarAvailability(users, from, until);
        // Build the return map
        Map<CalendarUser, List<CalendarAvailability>> map = new HashMap<>();
        for (CalendarAvailability availability : availabilities) {
            CalendarUser user = reverseLookup.get(availability.getCalendarUser());
            Collections.put(map, user, map.get(user));
        }

        return map;
    }
}
