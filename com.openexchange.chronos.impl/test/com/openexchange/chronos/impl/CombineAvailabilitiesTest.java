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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.impl.availability.performer.GetPerformer;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;

/**
 * {@link CombineAvailabilitiesTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CombineAvailabilitiesTest extends AbstractCombineTest {

    private CalendarAvailabilityStorage storage;
    private CalendarSession session;
    private List<CalendarAvailability> availabilities;

    /**
     * Initialises a new {@link CombineAvailabilitiesTest}.
     */
    public CombineAvailabilitiesTest() {
        super();
    }

    /**
     * Initialise mocks
     */
    @Before
    public void init() throws OXException {
        availabilities = new ArrayList<>();

        // Mock the session
        session = mock(CalendarSession.class);
        when(session.getUserId()).thenReturn(1);

        // Mock the storage
        storage = mock(CalendarAvailabilityStorage.class);
        when(storage.loadCalendarAvailabilities(1)).thenReturn(availabilities);
    }

    /**
     * Tests the combine logic when the free slots do not overlap.
     * 
     * @throws OXException
     */
    @Test
    public void testSingleAvailabilityNoOverlaps() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(createCalendarFreeSlot("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));

        // Initialise mocks
        availabilities.add(createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        List<CalendarAvailability> availableTime = get.getCombinedAvailableTime();

        // Asserts
        assertEquals("The amount of availability blocks does not match", 1, availableTime.size());
        assertEquals("The amount of available time slots does not match", 3, availableTime.get(0).getCalendarFreeSlots().size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.get(0).getBusyType());
    }

    /**
     * Tests the combine logic for multiple availability blocks
     * when the free slots are sorted and there are no overlaps
     */
    @Test
    public void testMultipleAvailabilitiesNoOverlaps() throws OXException {
        // Create the free slots
        List<CalendarFreeSlot> freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        freeSlots.add(createCalendarFreeSlot("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        freeSlots.add(createCalendarFreeSlot("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY_UNAVAILABLE, freeSlots, new DateTime(2016, 11, 1), new DateTime(2017, 3, 1)));

        // Create the free slots
        freeSlots = new ArrayList<>(3);
        freeSlots.add(createCalendarFreeSlot("April", new DateTime(2017, 4, 1), new DateTime(2017, 4, 31)));
        freeSlots.add(createCalendarFreeSlot("May", new DateTime(2017, 5, 1), new DateTime(2017, 5, 30)));
        freeSlots.add(createCalendarFreeSlot("June", new DateTime(2017, 6, 1), new DateTime(2017, 6, 31)));
        availabilities.add(createCalendarAvailability(BusyType.BUSY_TENTATIVE, freeSlots, new DateTime(2017, 3, 11), new DateTime(2017, 7, 5)));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        List<CalendarAvailability> availableTime = get.getCombinedAvailableTime();

        // Asserts
        assertEquals("The amount of availability blocks does not match", 2, availableTime.size());
        assertEquals("The amount of available time slots does not match", 3, availableTime.get(0).getCalendarFreeSlots().size());
        assertEquals("The amount of available time slots does not match", 3, availableTime.get(1).getCalendarFreeSlots().size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availableTime.get(0).getBusyType());
        assertEquals("The busy type does not match", BusyType.BUSY_TENTATIVE, availableTime.get(1).getBusyType());
    }
}
