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

package com.openexchange.http.grizzly.servletfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.openexchange.http.grizzly.util.IPTools;

/**
 * {@link RemoteIPFinderTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RemoteIPFinderTest {

    // extra spaces for trim() test
    private final static String validIPv4Remotes   = " 192.168.32.49 , 192.168.32.50 , 192.168.33.225, 192.168.33.224";
    private final static String invalidIPv4Remotes = "192.168.32.49, 192.168.32.555, 192.168.33.225, 192.168.33.224";
    private final static String knownIPv4          = " 192.168.33.225 , 192.168.33.224 ";

    private final static String validIPv6Remotes   = "2001:db8:0:8d3:0:8a2e:70:7341, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
    private final static String invalidIPv6Remotes = "2001:db8:0:8d3:0:8a2e:70:, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
    private final static String knownIPv6          = "2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";

    private final String emptyRemote = "";

    @Test
    public void testValidIPv4() {
        evaluate(knownIPv4, validIPv4Remotes, "192.168.32.50");
    }

    @Test
    public void testEmptyKnownIPv4() {
        evaluate(emptyRemote, validIPv4Remotes, "192.168.33.224");
    }

    @Test
    public void testEmptyRemoteIPv4() {
        String remoteIP = IPTools.getRemoteIP(emptyRemote, IPTools.filterIP(knownIPv4));
        assertNull(remoteIP);
    }

    @Test
    public void testInvalidRemoteIPv4() {
        String remoteIP = IPTools.getRemoteIP(invalidIPv4Remotes, IPTools.filterIP(knownIPv4));
        assertNull(remoteIP);
    }

    @Test
    public void testValidIPv6() {
        evaluate(knownIPv6, validIPv6Remotes, "2001:db8:0:8d3:0:8a2e:70:7341");
    }

    @Test
    public void testEmptyKnownIPv6() {
        evaluate(emptyRemote, validIPv6Remotes, "2001:db8:0:8d3:0:8a2e:70:7344");
    }

    @Test
    public void testEmptyRemoteIPv6() {
        String remoteIP = IPTools.getRemoteIP(emptyRemote, IPTools.filterIP(knownIPv6));
        assertNull(remoteIP);
    }

    @Test
    public void testInvalidRemoteIPv6() {
        String remoteIP = IPTools.getRemoteIP(invalidIPv6Remotes, IPTools.filterIP(knownIPv6));
        assertNull(remoteIP);
    }

    public void testAllProxiesMatchingIPv4() {
        String knownProxies = "192.168.32.0/24, 192.168.33.36";
        String xForwardFor = "206.36.25.126, 192.168.32.1, 192.168.32.2, 192.168.33.36";
        String expectedIP = "206.36.25.126";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    public void testFirstProxyNotMatchingIPv4() {
        String knownProxies = "192.168.32.0/24";
        String xForwardFor = "206.36.25.126, 192.168.32.1, 192.168.32.2, 192.168.33.36";
        String expectedIP = "192.168.33.36";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    public void testAllProxiesMatchingIPv6() {
        String knownProxies = "2001:db8:0:8d3:0:8a2e:70::/112";
        String xForwardFor = "2001:db8:1:8d3:0:ce6:0:1337, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
        String expectedIP = "2001:db8:1:8d3:0:ce6:0:1337";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    public void testFirtsProxyNotMatchingIPv6() {
        String knownProxies = "2001:db8:0:8d3:0:8a2e:70::/112";
        String xForwardFor = "2001:db8:1:8d3:0:ce6:0:1337, 2001:db8:0:8d3:0:8a2e:80:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
        String expectedIP = "2001:db8:0:8d3:0:8a2e:80:7342";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    private void evaluate(String knownProxies, String xForwardFor, String expectedIP) {
        String remoteIP = IPTools.getRemoteIP(xForwardFor, IPTools.filterIP(knownProxies));
        assertEquals(expectedIP, remoteIP);
    }
}
