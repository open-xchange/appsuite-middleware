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
import java.util.ArrayList;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.BusyType;
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
     * Tests the combine logic when the available blocks do not overlap.
     */
    @Test
    public void testAvailabilityNoAvailableOverlaps() throws OXException {
        // Create the available blocks
        available.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        available.add(PropsFactory.createCalendarAvailable("February", new DateTime(2017, 1, 1), new DateTime(2017, 1, 27)));
        available.add(PropsFactory.createCalendarAvailable("March", new DateTime(2017, 2, 1), new DateTime(2017, 2, 31)));
        available.add(PropsFactory.createCalendarAvailable("April", new DateTime(2017, 4, 1), new DateTime(2017, 4, 31)));
        available.add(PropsFactory.createCalendarAvailable("May", new DateTime(2017, 5, 1), new DateTime(2017, 5, 30)));
        available.add(PropsFactory.createCalendarAvailable("June", new DateTime(2017, 6, 1), new DateTime(2017, 6, 31)));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        Availability availability = get.getCombinedAvailableTime();

        // Asserts
        assertNotNull("The availability is null", availability);
        assertEquals("The amount of available time slots does not match", 6, availability.getAvailable().size());
        assertEquals("The busy type does not match", BusyType.BUSY_UNAVAILABLE, availability.getBusyType());
        AssertUtil.assertAvailableBlocks(available, availability.getAvailable());
    }

    /**
     * Tests the combine logic for an availability block with available overlaps
     */
    @Test
    public void testAvailabilityWithAvailableOverlaps() throws OXException {
        // Create the available blocks
        available.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        available.add(PropsFactory.createCalendarAvailable("February", new DateTime(2017, 1, 15), new DateTime(2017, 1, 20)));
        available.add(PropsFactory.createCalendarAvailable("March completely contains February", new DateTime(2017, 1, 10), new DateTime(2017, 2, 10)));
        available.add(PropsFactory.createCalendarAvailable("April", new DateTime(2017, 3, 1), new DateTime(2017, 3, 30)));
        available.add(PropsFactory.createCalendarAvailable("May overlaps with April", new DateTime(2017, 3, 20), new DateTime(2017, 4, 5)));
        available.add(PropsFactory.createCalendarAvailable("June", new DateTime(2017, 5, 15), new DateTime(2017, 5, 30)));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        Availability availability = get.getCombinedAvailableTime();

        List<Available> expected = new ArrayList<>(4);
        expected.add(PropsFactory.createCalendarAvailable("January", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        expected.add(PropsFactory.createCalendarAvailable("February", new DateTime(2017, 1, 10), new DateTime(2017, 2, 10)));
        expected.add(PropsFactory.createCalendarAvailable("April", new DateTime(2017, 3, 1), new DateTime(2017, 4, 5)));
        expected.add(PropsFactory.createCalendarAvailable("June", new DateTime(2017, 5, 15), new DateTime(2017, 5, 30)));

        // Asserts
        assertNotNull("The availability is null", availability);
        assertEquals("The amount of available time slots does not match", 4, availability.getAvailable().size());
        AssertUtil.assertAvailableBlocks(expected, availability.getAvailable());
    }

    /**
     * Tests the combine logic for available blocks with multiple overlaps
     */
    @Test
    public void testMultipleOverlapsWithPreceedsAndSuceeds() throws OXException {
        // Create the available blocks
        available.add(PropsFactory.createCalendarAvailable("January precedes", new DateTime(2017, 0, 1), new DateTime(2017, 0, 31)));
        available.add(PropsFactory.createCalendarAvailable("February in the middle", new DateTime(2017, 0, 20), new DateTime(2017, 1, 20)));
        available.add(PropsFactory.createCalendarAvailable("March succeeds", new DateTime(2017, 1, 10), new DateTime(2017, 2, 10)));

        // Execute
        GetPerformer get = new GetPerformer(storage, session);
        Availability availability = get.getCombinedAvailableTime();

        List<Available> expected = new ArrayList<>(1);
        expected.add(PropsFactory.createCalendarAvailable("Merged", new DateTime(2017, 0, 1), new DateTime(2017, 2, 10)));

        // Asserts
        assertNotNull("The availability is null", availability);
        assertEquals("The amount of available time slots does not match", 1, availability.getAvailable().size());
        AssertUtil.assertAvailableBlocks(expected, availability.getAvailable());
    }
}
