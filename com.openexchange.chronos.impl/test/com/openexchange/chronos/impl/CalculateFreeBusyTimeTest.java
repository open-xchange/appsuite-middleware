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
import com.openexchange.chronos.Availability;
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
    private Map<Attendee, Availability> availabilitiesPerAttendee;
    /** A List with the free/busy times for the attendees */
    private List<FreeBusyTime> freeBusyTimes;

    private FreeBusyPerformer freeBusyPerformer;
    private GetPerformer getPerformer;

    private CalendarAvailabilityService calendarAvailabilityService;

    // Base interval 01/01/2017 - 30/06/2017
    private final Date from = PropsFactory.createDate(2017, 0, 1);
    private final Date until = PropsFactory.createDate(2017, 5, 30);

    /**
     * Initialises a new {@link CalculateFreeBusyTimeTest}.
     */
    public CalculateFreeBusyTimeTest() {
        super();
    }

    /**
     * Initialise mocks
     */
    @Override
    @Before
    public void init() throws OXException {
        super.init();
        // Initialise maps and lists
        attendees = new ArrayList<>();
        freeBusyTimes = new ArrayList<>();
        freeBusyPerAttendee = new HashMap<>();
        availabilitiesPerAttendee = new HashMap<>();

        // Mock the Utils
        PowerMockito.mockStatic(Utils.class);
        BDDMockito.given(Utils.getTimeZone(session)).willReturn(TimeZone.getTimeZone("Europe/Berlin"));

        // Mock the FreeBusyPerformer
        freeBusyPerformer = mock(FreeBusyPerformer.class);
        when(freeBusyPerformer.performMerged(attendees, from, until)).thenReturn(freeBusyPerAttendee);
        when(freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until)).thenCallRealMethod();

        // Mock the GetPerformer
        getPerformer = mock(GetPerformer.class);
        when(getPerformer.getSession()).thenReturn(session);
        when(getPerformer.performForAttendees(attendees, from, until)).thenReturn(availabilitiesPerAttendee);
        when(getPerformer.getCombinedAvailability(attendees, from, until)).thenCallRealMethod();
    }

    /**
     * Since Mockito does not allow mocks to be return from other mocks when mocking,
     * we have to finish the setup with in each test case, i.e.:
     * <ul>
     * <li>Mock the {@link CalendarAvailabilityService}</li>
     * <li>Calculate the combinedAvailableTimes</li>
     * <li>Mock the previous method call with the real result</li>
     * </ul>
     *
     * This intermediate step is required in order to feed the {@link FreeBusyPerformer}
     * with the correct combined times from the {@link CalendarAvailabilityService}
     *
     * This call has to happen AFTER setting up each individual test case and BEFORE the
     * {@link FreeBusyPerformer#performCalculateFreeBusyTime(List, Date, Date)} call happens
     */
    private void finishMocking() throws OXException {
        // Mock the CalendarAvailabilityService...
        calendarAvailabilityService = mock(CalendarAvailabilityService.class);
        // ...and calculate the combinedAvailability...
        Map<Attendee, Availability> combinedAvailability = getPerformer.getCombinedAvailability(attendees, from, until);
        // ...so they can be used inside the FreeBusyPerformer
        // We basically bypass the service and all its prerequisites (storage, session, services) and we hook the call directly to GetPerformer
        when(calendarAvailabilityService.getAttendeeAvailability(null, attendees, from, until)).thenReturn(combinedAvailability);

        // Mock the Services for the FreeBusyPerformer
        PowerMockito.mockStatic(Services.class);
        BDDMockito.given(Services.getService(CalendarAvailabilityService.class)).willReturn(calendarAvailabilityService);
    }

    /**
     * Tests the free busy calculation out of a single availability block
     * and a single free/busy time coming from an event
     */
    @Test
    public void testSingles() throws OXException {
        // Initialise attendee
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        // Initialise the free/busy time
        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 26)));
        // Set the free/busy time for the attendee
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        // Initialise the available and availability block
        //List<Available> available = new ArrayList<>(3);
        available.add(PropsFactory.createCalendarAvailable("May 1", new DateTime(2017, 4, 1), new DateTime(2017, 4, 2)));
        available.add(PropsFactory.createCalendarAvailable("May 2", new DateTime(2017, 4, 21), new DateTime(2017, 4, 22)));
        // Set the availability block for the attendee
        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(5);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 2, 25)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 26)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 2, 26), PropsFactory.createDate(2017, 4, 1)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 4, 2), PropsFactory.createDate(2017, 4, 21)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 4, 22), PropsFactory.createDate(2017, 5, 30)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Tests the edges of the requested range.
     */
    @Test
    public void testEdges() throws OXException {
        // Initialise attendee
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        // Initialise the free/busy time
        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 0, 5)));
        // Set the free/busy time for the attendee
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        // Initialise the free slots and availability block
        available.add(PropsFactory.createCalendarAvailable("Mid June", new DateTime(2017, 5, 15), new DateTime(2017, 5, 16)));
        // Set the availability block for the attendee
        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(4);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 0, 5)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 5), PropsFactory.createDate(2017, 5, 15)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 5, 16), PropsFactory.createDate(2017, 5, 30)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Tests a single overlap between a preceding event and a free slot of an availability
     */
    @Test
    public void testSingleOverlapAvailabilityWithPrecedingEvent() throws OXException {
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 30)));
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        available.add(PropsFactory.createCalendarAvailable("Overlapping with preceding event", PropsFactory.createDateTime(2017, 2, 27), PropsFactory.createDateTime(2017, 3, 3)));
        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(3);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 2, 25)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 30)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 3, 3), PropsFactory.createDate(2017, 5, 30)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Tests a single overlap between a succeeding event and a free slot of an availability
     */
    @Test
    public void testSingleOverlapAvailabilityWithSucceedingEvent() throws OXException {
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 29), PropsFactory.createDate(2017, 3, 5)));
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        available.add(PropsFactory.createCalendarAvailable("Overlapping with succeding event", PropsFactory.createDateTime(2017, 2, 27), PropsFactory.createDateTime(2017, 3, 3)));
        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(3);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 2, 27)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 29), PropsFactory.createDate(2017, 3, 5)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 3, 5), PropsFactory.createDate(2017, 5, 30)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Tests a single overlap between an event that is contained with in a free slot of an availability
     */
    @Test
    public void testSingleOverlapAvailabilityWithContainingEvent() throws OXException {
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 30)));
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        available.add(PropsFactory.createCalendarAvailable("Overlapping with succeding event", PropsFactory.createDateTime(2017, 2, 20), PropsFactory.createDateTime(2017, 3, 5)));
        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(3);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 2, 20)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 30)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 3, 5), PropsFactory.createDate(2017, 5, 30)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Tests a single overlap between an event that contains a free slot of an availability
     */
    @Test
    public void testSingleOverlapAvailabilityWithContainedEvent() throws OXException {
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY_TENTATIVE, PropsFactory.createDate(2017, 2, 20), PropsFactory.createDate(2017, 3, 5)));
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        available.add(PropsFactory.createCalendarAvailable("Overlapping with succeding event", PropsFactory.createDateTime(2017, 2, 20), PropsFactory.createDateTime(2017, 3, 5)));
        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(5);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 2, 20)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_TENTATIVE, PropsFactory.createDate(2017, 2, 20), PropsFactory.createDate(2017, 3, 5)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 3, 5), PropsFactory.createDate(2017, 5, 30)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Tests a single range query with a single busy block
     */
    @Test
    public void testSingleRangeQueryWithNoFreeSlotOverlaps() throws OXException {
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 20), PropsFactory.createDate(2017, 2, 25)));
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        available.add(PropsFactory.createCalendarAvailable("Outside of requested range", PropsFactory.createDateTime(2016, 4, 20), PropsFactory.createDateTime(2016, 4, 25)));
        available.add(PropsFactory.createCalendarAvailable("Outside of requested range", PropsFactory.createDateTime(2017, 10, 1), PropsFactory.createDateTime(2017, 10, 31)));

        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(3);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 2, 20)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 20), PropsFactory.createDate(2017, 2, 25)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 5, 30)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Tests a single range query with a single busy block and two edge free slots
     */
    @Test
    public void testSingleRangeQueryWithEdgeFreeSlotOverlaps() throws OXException {
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 20), PropsFactory.createDate(2017, 2, 25)));
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        available.add(PropsFactory.createCalendarAvailable("Overlap A", PropsFactory.createDateTime(2016, 11, 29), PropsFactory.createDateTime(2017, 0, 3)));
        available.add(PropsFactory.createCalendarAvailable("Overlap B", PropsFactory.createDateTime(2017, 5, 20), PropsFactory.createDateTime(2017, 6, 5)));

        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(3);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 3), PropsFactory.createDate(2017, 2, 20)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 20), PropsFactory.createDate(2017, 2, 25)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 5, 20)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Tests a single range query with a single busy block and four edge free slots in the
     * beginning and end of each availability
     */
    @Test
    public void testSingleRangeQueryWithStartingEndingEdgeFreeSlotOverlaps() throws OXException {
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY_TENTATIVE, PropsFactory.createDate(2017, 2, 20), PropsFactory.createDate(2017, 2, 25)));
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        available.add(PropsFactory.createCalendarAvailable("Starting Edge Overlap A", PropsFactory.createDateTime(2016, 11, 29), PropsFactory.createDateTime(2017, 0, 3)));
        available.add(PropsFactory.createCalendarAvailable("Ending Edge Overlap A", PropsFactory.createDateTime(2017, 0, 29), PropsFactory.createDateTime(2017, 1, 1)));
        available.add(PropsFactory.createCalendarAvailable("Starting Edge Overlap B", PropsFactory.createDateTime(2017, 5, 1), PropsFactory.createDateTime(2017, 5, 10)));
        available.add(PropsFactory.createCalendarAvailable("Ending Edge Overlap B", PropsFactory.createDateTime(2017, 5, 20), PropsFactory.createDateTime(2017, 6, 5)));

        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(5);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 3), PropsFactory.createDate(2017, 0, 29)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 1, 1), PropsFactory.createDate(2017, 2, 20)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_TENTATIVE, PropsFactory.createDate(2017, 2, 20), PropsFactory.createDate(2017, 2, 25)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 5, 1)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 5, 10), PropsFactory.createDate(2017, 5, 20)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }

    /**
     * Test that the recurrence rule is correctly applied over the
     * the course of a specific range
     */
    @Test
    public void testSingleRecurrenceOverRange() throws OXException {
        // Initialise attendee
        Attendee attendee = PropsFactory.createAttendee("foobar@ox.io");
        attendees.add(attendee);

        // Initialise the free/busy time
        freeBusyTimes.add(new FreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 26)));
        // Set the free/busy time for the attendee
        freeBusyPerAttendee.put(attendee, freeBusyTimes);

        // Initialise the available and availability block
        available.add(PropsFactory.createRecurringCalendarFreeSlot("May - Recurring every Wednesday", new DateTime(2017, 4, 3), new DateTime(2017, 4, 4), "FREQ=WEEKLY;BYDAY=WE"));
        // Set the availability block for the attendee
        availabilitiesPerAttendee.put(attendee, getPerformer.prepareForDelivery(available));

        // Finish mocking
        finishMocking();

        List<FreeBusyTime> expectedFreeBusyTimes = new ArrayList<>(12);
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 0, 1), PropsFactory.createDate(2017, 2, 25)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY, PropsFactory.createDate(2017, 2, 25), PropsFactory.createDate(2017, 2, 26)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 2, 26), PropsFactory.createDate(2017, 4, 3)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 4, 4), PropsFactory.createDate(2017, 4, 10)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 4, 11), PropsFactory.createDate(2017, 4, 17)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 4, 18), PropsFactory.createDate(2017, 4, 24)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 4, 25), PropsFactory.createDate(2017, 4, 31)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 5, 1), PropsFactory.createDate(2017, 5, 7)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 5, 8), PropsFactory.createDate(2017, 5, 14)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 5, 15), PropsFactory.createDate(2017, 5, 21)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 5, 22), PropsFactory.createDate(2017, 5, 28)));
        expectedFreeBusyTimes.add(PropsFactory.createFreeBusyTime(FbType.BUSY_UNAVAILABLE, PropsFactory.createDate(2017, 5, 29), PropsFactory.createDate(2017, 5, 30)));

        // Perform the calculation
        Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime = freeBusyPerformer.performCalculateFreeBusyTime(attendees, from, until);
        FreeBusyResult freeBusyResult = performCalculateFreeBusyTime.get(attendee);

        // Asserts
        assertNotNull(freeBusyResult);
        AssertUtil.assertFreeBusyTimes(expectedFreeBusyTimes, freeBusyResult.getFreeBusyTimes());
    }
}
