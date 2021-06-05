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

package com.openexchange.chronos.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.dmfs.rfc5545.DateTime;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.Availability;

/**
 * {@link AvailabilityUtilsTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AvailabilityUtilsTest {

    /** Base Interval: 17/11/2017 - 17/12/2017 */
    Availability base = new Availability();

    /**
     * Initialises a new {@link AvailabilityUtilsTest}.
     */
    public AvailabilityUtilsTest() {
        super();
    }

    /**
     * Initialise
     */
    @Before
    public void beforeClass() {
        base.setStartTime(new DateTime(2017, 10, 17));
        base.setEndTime(new DateTime(2017, 11, 17));
    }

    /**
     * Tests that the interval B is completely contained in the base interval
     */
    @Test
    public void testIntervalContained() {
        Availability b = new Availability();
        b.setStartTime(new DateTime(2017, 10, 18));
        b.setEndTime(new DateTime(2017, 10, 19));

        assertFalse(AvailabilityUtils.contained(base, b));
        assertTrue(AvailabilityUtils.contained(b, base));
    }

    /**
     * Tests that the interval B is completely before the base interval
     */
    @Test
    public void testIntervalBefore() {
        Availability b = new Availability();
        b.setStartTime(new DateTime(2016, 0, 1));
        b.setEndTime(new DateTime(2016, 1, 1));

        assertFalse(AvailabilityUtils.intersect(base, b));
        assertFalse(AvailabilityUtils.intersect(b, base));
    }

    /**
     * Tests that the interval B is completely after the base interval
     */
    @Test
    public void testIntervalAfter() {
        Availability b = new Availability();
        b.setStartTime(new DateTime(2018, 0, 1));
        b.setEndTime(new DateTime(2018, 1, 1));

        assertFalse(AvailabilityUtils.intersect(base, b));
        assertFalse(AvailabilityUtils.intersect(b, base));
    }

    /**
     * Tests that the interval B intersects at the begining of the base interval
     */
    @Test
    public void testIntervalIntersectsStart() {
        Availability b = new Availability();
        b.setStartTime(new DateTime(2017, 10, 10));
        b.setEndTime(new DateTime(2017, 10, 20));

        assertTrue(AvailabilityUtils.intersect(base, b));
        assertTrue(AvailabilityUtils.intersect(b, base));
    }

    /**
     * Tests that the interval B intersects at the end of the base interval
     */
    @Test
    public void testIntervalIntersectsEnd() {
        Availability b = new Availability();
        b.setStartTime(new DateTime(2017, 11, 10));
        b.setEndTime(new DateTime(2017, 11, 20));

        assertTrue(AvailabilityUtils.intersect(base, b));
        assertTrue(AvailabilityUtils.intersect(b, base));
    }

    /**
     * Tests that the interval B is completely contained in the base interval
     */
    @Test
    public void testContained() {
        Availability b = new Availability();
        b.setStartTime(new DateTime(2017, 11, 5));
        b.setEndTime(new DateTime(2017, 11, 8));

        assertTrue(AvailabilityUtils.contained(b.getStartTime(), b.getEndTime(), base.getStartTime(), base.getEndTime()));
        assertFalse(AvailabilityUtils.contained(base.getStartTime(), base.getEndTime(), b.getStartTime(), b.getEndTime()));
    }
}
