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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.net;

import com.openexchange.net.HostList;
import junit.framework.TestCase;

/**
 * {@link HostListTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class HostListTest extends TestCase {

    /**
     * Initializes a new {@link HostListTest}.
     */
    public HostListTest() {
        super();
    }

    public void testValueOf_hostlistEmpty_returnEmpty() {
        HostList hl = HostList.valueOf("");

        assertTrue(hl.equals(HostList.EMPTY));
    }

    public void testValueOf_hostlistNull_returnEmpty() {
        HostList hl = HostList.valueOf(null);

        assertTrue(hl.equals(HostList.EMPTY));
    }

    public void testContains_containsEmpty_returnFalse() {
        HostList hl = HostList.valueOf("");

        boolean contains = hl.contains("");

        assertFalse(contains);
    }

    public void testContains_containsNull_returnEmpty() {
        HostList hl = HostList.valueOf(null);

        boolean contains = hl.contains(null);

        assertFalse(contains);
    }

    public void testHostListv4() {
        try {
            HostList hl = HostList.valueOf("192.168.0.1, localhost, *.open-xchange.com");

            String shl = hl.toString();
            assertNotNull("Host-list's string representation is null", shl);

            assertTrue(hl.contains("192.168.0.1"));
            assertFalse(hl.contains("127.168.32.4"));
            assertTrue(hl.contains("barfoo.open-xchange.com"));
            assertTrue(hl.contains("12.open-xchange.com"));
            assertTrue(hl.contains("localhost"));
            assertFalse(hl.contains("ox.io.com"));
            assertFalse(hl.contains("::1"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListv4_throwsException() {
        try {
            HostList.valueOf("**.open-xchange.com");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    public void testHostListv4_throwsException2() {
        try {
            HostList.valueOf("*");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
    
    public void testHostListv4_throwsException3() {
        try {
            HostList.valueOf("test.*.com");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    public void testHostListv4Ranges() {
        try {
            HostList hl = HostList.valueOf("127.0.0.1-127.255.255.255, 10.20.30.1-10.20.30.255");

            String shl = hl.toString();
            assertNotNull("Host-list's string representation is null", shl);

            assertFalse(hl.contains("192.168.0.1"));
            assertFalse(hl.contains("192.168.255.255"));
            assertTrue(hl.contains("127.168.32.4"));
            assertTrue(hl.contains("10.20.30.222"));
            assertFalse(hl.contains("::1"));
            assertFalse(hl.contains("barfoo.open-xchange.com"));
            assertFalse(hl.contains("12.open-xchange.com"));
            assertFalse(hl.contains("ox.io.com"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListv4CIDRRanges() {
        try {
            HostList hl = HostList.valueOf("192.168.0.1/16");

            String shl = hl.toString();
            assertNotNull("Host-list's string representation is null", shl);

            assertTrue(hl.contains("192.168.0.1"));
            assertTrue(hl.contains("192.168.255.255"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListv6() {
        try {
            HostList hl = HostList.valueOf("::1, FE80:0000:0000:0000:0202:B3FF:FE1E:8329, ");

            String shl = hl.toString();
            assertNotNull("Host-list's string representation is null", shl);

            assertTrue(hl.contains("::1"));
            assertTrue(hl.contains("FE80:0000:0000:0000:0202:B3FF:FE1E:8329"));
            assertTrue(hl.contains("FE80:0:0:0:0202:B3FF:FE1E:8329"));
            assertTrue(hl.contains("FE80::0202:B3FF:FE1E:8329"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListv6SimilarityCheck() {
        //        all the same
        //        2001:cdba:0000:0000:0000:0000:3257:9652
        //        2001:cdba:0:0:0:0:3257:9652
        //        2001:cdba::3257:9652

        try {
            HostList hl = HostList.valueOf("2001:cdba:0000:0000:0000:0000:3257:9652, 2001:cdba:0:0:0:0:3257:9652");

            String shl = hl.toString();
            assertNotNull("Host-list's string representation is null", shl);

            assertTrue(hl.contains("2001:cdba::3257:9652"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListv6SimilarityCheck2() {
        //      all the same
        //      2001:cdba:0000:0000:0000:0000:3257:9652
        //      2001:cdba:0:0:0:0:3257:9652
        //      2001:cdba::3257:9652

        try {
            HostList hl = HostList.valueOf("2001:cdba::3257:9652");

            String shl = hl.toString();
            assertNotNull("Host-list's string representation is null", shl);

            assertTrue(hl.contains("2001:cdba:0000:0000:0000:0000:3257:9652"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListv6Ranges() {
        try {
            HostList hl = HostList.valueOf("2001:DB8::64-2001:DB8::C8");

            String shl = hl.toString();
            assertNotNull("Host-list's string representation is null", shl);

            assertTrue(hl.contains("2001:db8:0:0:0:0:0:70"));
            assertTrue(hl.contains("2001:db8:0:0:0:0:0:7f"));

            assertFalse(hl.contains("::1"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListv6CIDRRanges() {
        try {
            HostList hl = HostList.valueOf("2001:DB1::0/120");

            String shl = hl.toString();
            assertNotNull("Host-list's string representation is null", shl);

            assertTrue(hl.contains("2001:db1:0:0:0:0:0:0"));
            assertTrue(hl.contains("2001:db1:0:0:0:0:0:ff"));

            assertFalse(hl.contains("::1"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListFailToParse() {
        try {
            String hostList = "www.google.*";
            HostList.valueOf(hostList);
            fail("Host list did not fail to parse: " + hostList);
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testHostListFailToParse2() {
        try {
            String hostList = "*.open-xchange.*";
            HostList.valueOf(hostList);
            fail("Host list did not fail to parse: " + hostList);
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
