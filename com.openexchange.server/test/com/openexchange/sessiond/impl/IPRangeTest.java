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

package com.openexchange.sessiond.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * {@link IPRangeTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IPRangeTest {

    @Test
    public void simpleIP() {
        final IPRange singleIP = IPRange.parseRange("192.168.32.99");

        assertTrue(singleIP.contains("192.168.32.99"));
        assertFalse(singleIP.contains("192.168.32.98"));

    }

    @Test
    public void range() {
        final IPRange range = IPRange.parseRange("192.168.32.100-192.168.32.200");
        assertTrue(range.contains("192.168.32.150"));
        assertFalse(range.contains("192.168.32.99"));
        assertFalse(range.contains("191.168.32.150"));
    }

    @Test
    public void rangeWithCarryOver() {
        final IPRange range = IPRange.parseRange("192.168.32.99-192.168.33.20");

        assertTrue(range.contains("192.168.32.100"));
        assertTrue(range.contains("192.168.33.19"));
        assertFalse(range.contains("192.168.34.0"));

    }

    @Test
    public void rangeWithCarryOverIpv6() {
        final IPRange range = IPRange.parseRange("::1-::128");

        assertTrue(range.contains("::12"));
        assertTrue(range.contains("::24"));
        assertFalse(range.contains("::168"));

    }

}
