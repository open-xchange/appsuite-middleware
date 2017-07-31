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
import java.util.ArrayList;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.AvailableTime;
import com.openexchange.chronos.AvailableTimeSlot;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.impl.availability.performer.GetPerformer;
import com.openexchange.exception.OXException;

/**
 * {@link CombineAvailableTimeTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CombineAvailableTimeTest extends AbstractCombineTest {

    /**
     * Initialises a new {@link CombineAvailableTimeTest}.
     */
    public CombineAvailableTimeTest() {
        super();
    }

    /**
     * Tests the combine logic when the free slots are
     * sorted and there are no overlaps.
     * 
     * @throws OXException
     */
    @Test
    public void testCombineSingleAvailabilitySortedNoOverlaps() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(createCalendarFreeSlot("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));

        // Initialise mocks
        availabilities.add(createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        AvailableTime availableTime = get.getAvailableTime();

        // Asserts
        assertEquals("The amount of available time slots does not match", 3, availableTime.size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.getBusyType());
    }

    /**
     * Tests the combine logic when the free slots are
     * unsorted and there are no overlaps.
     * 
     * @throws OXException
     */
    @Test
    public void testCombineSingleAvailabilityUnsortedNoOverlaps() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));

        availabilities.add(createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        AvailableTime availableTime = get.getAvailableTime();

        // Asserts
        assertEquals("The amount of available time slots does not match", 3, availableTime.size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.getBusyType());
    }

    /**
     * Tests the combine logic for multiple availability blocks
     * when the free slots are sorted and there are no overlaps
     */
    @Test
    public void testCombineMultipleAvailabilitySortedNoOverlaps() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(createCalendarFreeSlot("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots));

        // Create the free slots
        freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("April", new DateTime(2017, 3, 1), new DateTime(2017, 3, 30)));
        freeSlots.add(createCalendarFreeSlot("May", new DateTime(2017, 4, 1), new DateTime(2017, 4, 31)));
        freeSlots.add(createCalendarFreeSlot("June", new DateTime(2017, 5, 1), new DateTime(2017, 5, 30)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY_TENTATIVE, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        AvailableTime availableTime = get.getAvailableTime();

        // Asserts
        assertEquals("The amount of available time slots does not match", 6, availableTime.size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.getBusyType());
    }

    /**
     * Tests the combine logic for a single availability block
     * with overlapping free slots
     */
    @Test
    public void testCombineSingleAvailabilityWithOverlaps() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("Overlap January/February", new DateTime(2017, 0, 5), new DateTime(2017, 1, 4)));
        freeSlots.add(createCalendarFreeSlot("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        AvailableTime availableTime = get.getAvailableTime();

        // Asserts
        assertEquals("The amount of available time slots does not match", 2, availableTime.size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.getBusyType());

        // Assert the merged slot
        AvailableTimeSlot ats = availableTime.get(0);
        assertEquals("The 'from' of the time slot does not match", new DateTime(2017, 0, 1), ats.getFrom());
        assertEquals("The 'until' of the time slot does not match", new DateTime(2017, 1, 4), ats.getUntil());
    }

    /**
     * Tests the combine logic for a single availability block
     * with overlapping free slots
     */
    @Test
    public void testCombineSingleAvailabilityWithContained() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(createCalendarFreeSlot("Contained in February", new DateTime(2017, 1, 10), new DateTime(2017, 1, 12)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        AvailableTime availableTime = get.getAvailableTime();

        // Asserts
        assertEquals("The amount of available time slots does not match", 2, availableTime.size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.getBusyType());

        AvailableTimeSlot ats = availableTime.get(1);
        assertEquals("The 'from' of the time slot does not match", new DateTime(2017, 1, 1), ats.getFrom());
        assertEquals("The 'until' of the time slot does not match", new DateTime(2017, 1, 27), ats.getUntil());
    }

    /**
     * Tests the combine logic for a single availability block
     * with overlapping and contained free slots
     */
    @Test
    public void testCombineSingleAvailabilityWithContainedAndOverlap() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(4);
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(createCalendarFreeSlot("Contained in February", new DateTime(2017, 1, 10), new DateTime(2017, 1, 12)));
        freeSlots.add(createCalendarFreeSlot("Overlaps with February", new DateTime(2017, 1, 20), new DateTime(2017, 2, 10)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        AvailableTime availableTime = get.getAvailableTime();

        // Asserts
        assertEquals("The amount of available time slots does not match", 2, availableTime.size());

        AvailableTimeSlot ats = availableTime.get(0);
        assertEquals("The 'from' of the time slot does not match", new DateTime(2017, 0, 1), ats.getFrom());
        assertEquals("The 'until' of the time slot does not match", new DateTime(2017, 0, 31), ats.getUntil());

        ats = availableTime.get(1);
        assertEquals("The 'from' of the time slot does not match", new DateTime(2017, 1, 1), ats.getFrom());
        assertEquals("The 'until' of the time slot does not match", new DateTime(2017, 2, 10), ats.getUntil());
    }

    /**
     * Tests the combine logic for multiple availability blocks with
     * overlapping and contained free slots.
     */
    @Test
    public void testCombineMultipleAvailabilitiesWithContainedAndOverlaps() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(4);
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(createCalendarFreeSlot("Contained in February", new DateTime(2017, 1, 10), new DateTime(2017, 1, 12)));
        freeSlots.add(createCalendarFreeSlot("Overlaps with February", new DateTime(2017, 1, 20), new DateTime(2017, 2, 10)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY, freeSlots));

        freeSlots = new ArrayList<>(2);
        freeSlots.add(createCalendarFreeSlot("Overlaps with February/March", new DateTime(2017, 1, 27), new DateTime(2017, 2, 30)));
        freeSlots.add(createCalendarFreeSlot("Overlaps with January", new DateTime(2016, 11, 24), new DateTime(2017, 0, 7)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY_TENTATIVE, freeSlots));

        freeSlots = new ArrayList<>(2);
        freeSlots.add(createCalendarFreeSlot("Overlaps with December", new DateTime(2016, 10, 17), new DateTime(2016, 11, 31)));
        freeSlots.add(createCalendarFreeSlot("Overlaps with February", new DateTime(2016, 11, 1), new DateTime(2017, 0, 15)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        AvailableTime availableTime = get.getAvailableTime();

        // Asserts
        assertEquals("The amount of available time slots does not match", 2, availableTime.size());
        assertEquals("The busy type does not match", BusyType.BUSY, availableTime.getBusyType());

        AvailableTimeSlot ats = availableTime.get(0);
        assertEquals("The 'from' of the time slot does not match", new DateTime(2016, 10, 17), ats.getFrom());
        assertEquals("The 'until' of the time slot does not match", new DateTime(2017, 0, 31), ats.getUntil());

        ats = availableTime.get(1);
        assertEquals("The 'from' of the time slot does not match", new DateTime(2017, 1, 1), ats.getFrom());
        assertEquals("The 'until' of the time slot does not match", new DateTime(2017, 2, 30), ats.getUntil());
    }
}
