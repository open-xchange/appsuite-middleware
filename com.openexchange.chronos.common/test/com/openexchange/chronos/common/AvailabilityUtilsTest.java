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
