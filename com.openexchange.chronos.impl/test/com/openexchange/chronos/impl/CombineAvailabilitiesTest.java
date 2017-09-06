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
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.impl.availability.performer.GetPerformer;
import com.openexchange.exception.OXException;

/**
 * {@link CombineAvailabilitiesTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CombineAvailabilitiesTest extends AbstractCombineTest {

    /**
     * Initialises a new {@link CombineAvailabilitiesTest}.
     */
    public CombineAvailabilitiesTest() {
        super();
    }

    /**
     * Tests the combine logic when the free slots do not overlap.
     * 
     * @throws OXException
     */
    @Test
    public void testSingleAvailabilityNoOverlaps() throws OXException {
        // Create the free slots
        List<Available> freeSlots = new ArrayList<>(3);
        freeSlots.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(PropsFactory.createCalendarAvailable("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));

        // Initialise mocks
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        List<Availability> availableTime = get.getCombinedAvailableTime();

        // Asserts
        assertEquals("The amount of availability blocks does not match", 1, availableTime.size());
        assertEquals("The amount of available time slots does not match", 3, availableTime.get(0).getAvailable().size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.get(0).getBusyType());
    }

    /**
     * Tests the combine logic for multiple availability blocks
     * when the free slots are sorted and there are no overlaps
     */
    @Test
    public void testMultipleAvailabilitiesNoOverlaps() throws OXException {
        // Create the free slots
        List<Available> freeSlots = new ArrayList<>(3);
        freeSlots.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(PropsFactory.createCalendarAvailable("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots, new DateTime(2016, 11, 1), new DateTime(2017, 3, 1)));

        // Create the free slots
        freeSlots = new ArrayList<>(3);
        freeSlots.add(PropsFactory.createCalendarAvailable("April", new DateTime(2017, 4, 1), new DateTime(2017, 4, 31)));
        freeSlots.add(PropsFactory.createCalendarAvailable("May", new DateTime(2017, 5, 1), new DateTime(2017, 5, 30)));
        freeSlots.add(PropsFactory.createCalendarAvailable("June", new DateTime(2017, 6, 1), new DateTime(2017, 6, 31)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_TENTATIVE, freeSlots, new DateTime(2017, 3, 11), new DateTime(2017, 7, 5)));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        List<Availability> availableTime = get.getCombinedAvailableTime();

        // Asserts
        assertEquals("The amount of availability blocks does not match", 2, availableTime.size());
        assertEquals("The amount of available time slots does not match", 3, availableTime.get(0).getAvailable().size());
        assertEquals("The amount of available time slots does not match", 3, availableTime.get(1).getAvailable().size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.get(0).getBusyType());
        assertEquals("The busy type does not match", BusyType.BUSY_TENTATIVE, availableTime.get(1).getBusyType());
    }

    /**
     * Tests the combine logic for multiple availability blocks
     * with overlaps
     */
    @Test
    public void testMultipleAvailabilitiesWithOverlaps() throws OXException {
        // Create the free slots
        List<Available> freeSlots = new ArrayList<>(1);
        freeSlots.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February", new DateTime(2017, 1, 15), new DateTime(2017, 1, 20)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots, new DateTime(2016, 11, 1), new DateTime(2017, 3, 1), 5));

        // Create the free slots
        freeSlots = new ArrayList<>(2);
        freeSlots.add(PropsFactory.createCalendarAvailable("March", new DateTime(2017, 1, 21), new DateTime(2017, 2, 10)));
        freeSlots.add(PropsFactory.createCalendarAvailable("April", new DateTime(2017, 3, 1), new DateTime(2017, 3, 31)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_TENTATIVE, freeSlots, new DateTime(2017, 0, 31), new DateTime(2017, 3, 31), 2));

        // Create the free slots
        freeSlots = new ArrayList<>(2);
        freeSlots.add(PropsFactory.createCalendarAvailable("May", new DateTime(2017, 4, 1), new DateTime(2017, 4, 5)));
        freeSlots.add(PropsFactory.createCalendarAvailable("June", new DateTime(2017, 5, 15), new DateTime(2017, 5, 30)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY, freeSlots, new DateTime(2017, 2, 30), new DateTime(2017, 6, 5), 7));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        List<Availability> availableTime = get.getCombinedAvailableTime();

        // Asserts
        assertEquals("The amount of availability blocks does not match", 3, availableTime.size());
        Availability calendarAvailabilityA = availableTime.get(0);
        assertEquals("The 'from' of the availability block does not match", new DateTime(2016, 11, 1), calendarAvailabilityA.getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 0, 31), calendarAvailabilityA.getEndTime());

        assertEquals("The 'from' of the availability block does not match", new DateTime(2017, 0, 31), availableTime.get(1).getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 3, 31), availableTime.get(1).getEndTime());

        assertEquals("The 'from' of the availability block does not match", new DateTime(2017, 3, 31), availableTime.get(2).getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 6, 5), availableTime.get(2).getEndTime());
    }

    /**
     * Tests the combine logic for multiple availability blocks
     * with free slot overlaps
     */
    @Test
    public void testMultipleAvailabilitiesWithFreeSlotOverlaps() throws OXException {
        // Create the free slots
        List<Available> freeSlots = new ArrayList<>(1);
        freeSlots.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February", new DateTime(2017, 1, 15), new DateTime(2017, 1, 20)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots, new DateTime(2016, 11, 1), new DateTime(2017, 2, 1), 5));

        // Create the free slots
        freeSlots = new ArrayList<>(2);
        freeSlots.add(PropsFactory.createCalendarAvailable("March completely contains February", new DateTime(2017, 1, 10), new DateTime(2017, 2, 10)));
        freeSlots.add(PropsFactory.createCalendarAvailable("April", new DateTime(2017, 3, 1), new DateTime(2017, 3, 30)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_TENTATIVE, freeSlots, new DateTime(2017, 1, 5), new DateTime(2017, 3, 30), 2));

        // Create the free slots
        freeSlots = new ArrayList<>(2);
        freeSlots.add(PropsFactory.createCalendarAvailable("May overlaps with April", new DateTime(2017, 3, 20), new DateTime(2017, 4, 5)));
        freeSlots.add(PropsFactory.createCalendarAvailable("June", new DateTime(2017, 5, 15), new DateTime(2017, 5, 30)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY, freeSlots, new DateTime(2017, 2, 30), new DateTime(2017, 6, 5), 7));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        List<Availability> availableTime = get.getCombinedAvailableTime();

        // Asserts
        assertEquals("The amount of availability blocks does not match", 3, availableTime.size());
        Availability calendarAvailabilityA = availableTime.get(0);
        assertEquals("The 'from' of the availability block does not match", new DateTime(2016, 11, 1), calendarAvailabilityA.getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 1, 5), calendarAvailabilityA.getEndTime());
        assertEquals("The amount of free slots does not match", 1, calendarAvailabilityA.getAvailable().size());

        Availability calendarAvailabilityB = availableTime.get(1);
        assertEquals("The 'from' of the availability block does not match", new DateTime(2017, 1, 5), calendarAvailabilityB.getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 3, 30), calendarAvailabilityB.getEndTime());
        assertEquals("The amount of free slots does not match", 2, calendarAvailabilityB.getAvailable().size());

        Availability calendarAvailabilityC = availableTime.get(2);
        assertEquals("The 'from' of the availability block does not match", new DateTime(2017, 3, 30), calendarAvailabilityC.getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 6, 5), calendarAvailabilityC.getEndTime());
        assertEquals("The amount of free slots does not match", 2, calendarAvailabilityC.getAvailable().size());
    }

    /**
     * Tests the combine logic for multiple availability blocks
     * where the availability with the higher priority, completely contains
     * the availability with the lower priority.
     */
    @Test
    public void testMultipleAvailabilitiesWithFullContain() throws OXException {
        // Create the free slots
        List<Available> freeSlots = new ArrayList<>(2);
        freeSlots.add(PropsFactory.createCalendarAvailable("February A.1", new DateTime(2017, 1, 3), new DateTime(2017, 1, 5)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February A.2", new DateTime(2017, 1, 10), new DateTime(2017, 1, 13)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots, new DateTime(2017, 1, 1), new DateTime(2017, 1, 28), 8));

        // Create the free slots
        freeSlots = new ArrayList<>(4);
        freeSlots.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 3), new DateTime(2017, 0, 25)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February B.1", new DateTime(2017, 1, 4), new DateTime(2017, 1, 5)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February B.2", new DateTime(2017, 1, 8), new DateTime(2017, 1, 11)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February B.3", new DateTime(2017, 1, 12), new DateTime(2017, 1, 15)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_TENTATIVE, freeSlots, new DateTime(2017, 0, 1), new DateTime(2017, 2, 31), 5));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        List<Availability> availableTime = get.getCombinedAvailableTime();

        // Asserts
        assertEquals("The amount of availability blocks does not match", 1, availableTime.size());
        Availability calendarAvailabilityA = availableTime.get(0);
        assertEquals("The 'from' of the availability block does not match", new DateTime(2017, 0, 1), calendarAvailabilityA.getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 2, 31), calendarAvailabilityA.getEndTime());
        assertEquals("The amount of free slots does not match", 3, calendarAvailabilityA.getAvailable().size());

        Available freeSlotA = calendarAvailabilityA.getAvailable().get(0);
        assertEquals("The 'from' of the free slot does not match", new DateTime(2017, 0, 3), freeSlotA.getStartTime());
        assertEquals("The 'until' of the free slot does not match", new DateTime(2017, 0, 25), freeSlotA.getEndTime());

        Available freeSlotB = calendarAvailabilityA.getAvailable().get(1);
        assertEquals("The 'from' of the free slot does not match", new DateTime(2017, 1, 3), freeSlotB.getStartTime());
        assertEquals("The 'until' of the free slot does not match", new DateTime(2017, 1, 5), freeSlotB.getEndTime());

        Available freeSlotC = calendarAvailabilityA.getAvailable().get(2);
        assertEquals("The 'from' of the free slot does not match", new DateTime(2017, 1, 8), freeSlotC.getStartTime());
        assertEquals("The 'until' of the free slot does not match", new DateTime(2017, 1, 15), freeSlotC.getEndTime());
    }

    /**
     * Tests the combine logic for multiple availability blocks
     * where the availability with the higher priority, completely contains
     * the availability with the lower priority.
     */
    @Test
    public void testMultipleAvailabilitiesWithFullContainAndHigherPriority() throws OXException {
        // Create the free slots
        List<Available> freeSlots = new ArrayList<>(2);
        freeSlots.add(PropsFactory.createCalendarAvailable("February A.1", new DateTime(2017, 1, 3), new DateTime(2017, 1, 5)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February A.2", new DateTime(2017, 1, 10), new DateTime(2017, 1, 13)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots, new DateTime(2017, 1, 1), new DateTime(2017, 1, 28), 2));

        // Create the free slots
        freeSlots = new ArrayList<>(4);
        freeSlots.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 3), new DateTime(2017, 0, 25)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February B.1", new DateTime(2017, 1, 4), new DateTime(2017, 1, 5)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February B.2", new DateTime(2017, 1, 8), new DateTime(2017, 1, 11)));
        freeSlots.add(PropsFactory.createCalendarAvailable("February B.3", new DateTime(2017, 1, 12), new DateTime(2017, 1, 15)));
        availabilities.add(PropsFactory.createCalendarAvailability(BusyType.BUSY_TENTATIVE, freeSlots, new DateTime(2017, 0, 1), new DateTime(2017, 2, 31), 5));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        List<Availability> availableTime = get.getCombinedAvailableTime();

        // Asserts
        assertEquals("The amount of availability blocks does not match", 3, availableTime.size());
        Availability calendarAvailabilityA = availableTime.get(0);
        assertEquals("The 'from' of the availability block does not match", new DateTime(2017, 0, 1), calendarAvailabilityA.getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 1, 1), calendarAvailabilityA.getEndTime());
        assertEquals("The amount of free slots does not match", 1, calendarAvailabilityA.getAvailable().size());

        Availability calendarAvailabilityB = availableTime.get(1);
        assertEquals("The 'from' of the availability block does not match", new DateTime(2017, 1, 1), calendarAvailabilityB.getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 1, 28), calendarAvailabilityB.getEndTime());
        assertEquals("The amount of free slots does not match", 2, calendarAvailabilityB.getAvailable().size());

        Availability calendarAvailabilityC = availableTime.get(2);
        assertEquals("The 'from' of the availability block does not match", new DateTime(2017, 1, 28), calendarAvailabilityC.getStartTime());
        assertEquals("The 'until' of the availability block does not match", new DateTime(2017, 2, 31), calendarAvailabilityC.getEndTime());
        assertEquals("The amount of free slots does not match", 0, calendarAvailabilityC.getAvailable().size());

        Available freeSlotA = calendarAvailabilityA.getAvailable().get(0);
        assertEquals("The 'from' of the free slot does not match", new DateTime(2017, 0, 3), freeSlotA.getStartTime());
        assertEquals("The 'until' of the free slot does not match", new DateTime(2017, 0, 25), freeSlotA.getEndTime());

        Available freeSlotB = calendarAvailabilityB.getAvailable().get(0);
        assertEquals("The 'from' of the free slot does not match", new DateTime(2017, 1, 3), freeSlotB.getStartTime());
        assertEquals("The 'until' of the free slot does not match", new DateTime(2017, 1, 5), freeSlotB.getEndTime());

        Available freeSlotC = calendarAvailabilityB.getAvailable().get(1);
        assertEquals("The 'from' of the free slot does not match", new DateTime(2017, 1, 8), freeSlotC.getStartTime());
        assertEquals("The 'until' of the free slot does not match", new DateTime(2017, 1, 15), freeSlotC.getEndTime());
    }
}
