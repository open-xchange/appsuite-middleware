/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
