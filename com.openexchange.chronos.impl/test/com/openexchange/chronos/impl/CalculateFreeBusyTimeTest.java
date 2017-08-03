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

package com.openexchange.chronos.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.FbType;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.impl.availability.performer.GetPerformer;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.impl.performer.FreeBusyPerformer;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.exception.OXException;

/**
 * {@link CalculateFreeBusyTimeTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, Utils.class })
public class CalculateFreeBusyTimeTest extends AbstractCombineTest {

    /** A list holding information about the attendees */
    private List<Attendee> attendees;
    /** A map that holds the free busy times per attendee */
    private Map<Attendee, List<FreeBusyTime>> freeBusyPerAttendee;
    /** A map that holds the availability blocks per attendee */
    private Map<Attendee, List<CalendarAvailability>> availabilitiesPerAttendee;
    /** A List with the free/busy times for the attendees */
    private List<FreeBusyTime> freeBusyTimes;

    private FreeBusyPerformer freeBusyPerformer;
    private GetPerformer getPerformer;

    private CalendarAvailabilityService calendarAvailabilityService;

    // Base interval 01/01/2017 - 30/06/2017
    private Date from = PropsFactory.createDate(2017, 0, 1);
    private Date until = PropsFactory.createDate(2017, 5, 30);
    private Attendee attendee;

    /**
     * Initialises a new {@link CalculateFreeBusyTimeTest}.
     */
    public CalculateFreeBusyTimeTest() {
        super();
    }

    /**
     * Initialise mocks
     */
    @Before
    public void init() throws OXException {
        super.init();
        // Initialise maps and lists
        attendees = new ArrayList<>();
        freeBusyTimes = new ArrayList<>();
        freeBusyPerAttendee = new HashMap<>();
        availabilitiesPerAttendee = new HashMap<>();

        // Mock the FreeBusyPerformer
        freeBusyPerformer = mock(FreeBusyPerformer.class);
        when(freeBusyPerformer.performMerged(attendees, from, until)).thenReturn(freeBusyPerAttendee);
        when(freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until)).thenCallRealMethod();

        // Mock the GetPerformer
        getPerformer = mock(GetPerformer.class);
        when(getPerformer.performForAttendees(attendees, from, until)).thenReturn(availabilitiesPerAttendee);
        when(getPerformer.getCombinedAvailableTimes(attendees, from, until)).thenCallRealMethod();
    }

    /**
     * Tests the free busy calculation out of a single availability block
     * and a single free/busy time coming from an event
     */
    @Test
    public void testSingles() throws OXException {
        // Initialise attendee
        attendee = new Attendee();
        attendee.setEMail("foobar@ox.io");
        attendees.add(attendee);

        // Initialise the free/busy time
        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 26)));
        // Set the free/busy time for the attendee
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        // Initialise the free slots and availability block
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(3);
        freeSlots.add(PropsFactory.createCalendarFreeSlot("May 1", new DateTime(2017, 4, 1), new DateTime(2017, 4, 2)));
        freeSlots.add(PropsFactory.createCalendarFreeSlot("May 2", new DateTime(2017, 4, 21), new DateTime(2017, 4, 22)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots, new DateTime(2017, 3, 25), new DateTime(2017, 4, 30)));
        // Set the availability block for the attendee
        availabilitiesPerAttendee.put(attendee, availabilities);

        // Mock the CalendarAvailabilityService...
        calendarAvailabilityService = mock(CalendarAvailabilityService.class);
        // ...and calculate the combinedAvailableTimes...
        Map<Attendee, List<CalendarAvailability>> combinedAvailableTimes = getPerformer.getCombinedAvailableTimes(attendees, from, until);
        // ...so they can be used inside the FreeBusyPerformer
        // We basically bypass the service and all its prerequisites (storage, session, services) and we hook the call directly to GetPerformer
        when(calendarAvailabilityService.getCombinedAvailableTime(null, attendees, from, until)).thenReturn(combinedAvailableTimes);

        // Mock the Services for the FreeBusyPerformer
        PowerMockito.mockStatic(Services.class);
        BDDMockito.given(Services.getService(CalendarAvailabilityService.class)).willReturn(calendarAvailabilityService);

        // Mock the Utils
        PowerMockito.mockStatic(Utils.class);
        BDDMockito.given(Utils.getTimeZone(session)).willReturn(TimeZone.getDefault());

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        assertEquals("The amount of the free/busy times does not match", 9, freeBusyResult.getFreeBusyTimes().size());
    }
}
