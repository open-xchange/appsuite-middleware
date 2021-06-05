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

package com.openexchange.http.grizzly.servletfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.openexchange.net.IPTools;

/**
 * {@link RemoteIPFinderTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RemoteIPFinderTest {

    // extra spaces for trim() test
    private final static String validIPv4Remotes = " 192.168.32.49 , 192.168.32.50 , 192.168.33.225, 192.168.33.224";
    private final static String invalidIPv4Remotes = "192.168.32.49, 192.168.32.555, 192.168.33.225, 192.168.33.224";
    private final static String knownIPv4 = " 192.168.33.225 , 192.168.33.224 ";

    private final static String validIPv6Remotes = "2001:db8:0:8d3:0:8a2e:70:7341, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
    private final static String invalidIPv6Remotes = "2001:db8:0:8d3:0:8a2e:70:, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
    private final static String knownIPv6 = "2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";

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

    @Test
    public void testAllProxiesMatchingIPv4() {
        String knownProxies = "192.168.32.0/24, 192.168.33.36";
        String xForwardFor = "206.36.25.126, 192.168.32.1, 192.168.32.2, 192.168.33.36";
        String expectedIP = "206.36.25.126";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    @Test
    public void testFirstProxyNotMatchingIPv4() {
        String knownProxies = "192.168.32.0/24";
        String xForwardFor = "206.36.25.126, 192.168.32.1, 192.168.32.2, 192.168.33.36";
        String expectedIP = "192.168.33.36";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    @Test
    public void testAllProxiesMatchingIPv6() {
        String knownProxies = "2001:db8:0:8d3:0:8a2e:70::/112";
        String xForwardFor = "2001:db8:1:8d3:0:ce6:0:1337, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
        String expectedIP = "2001:db8:1:8d3:0:ce6:0:1337";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    @Test
    public void testFirtsProxyNotMatchingIPv6() {
        String knownProxies = "2001:db8:0:8d3:0:8a2e:70::/112";
        String xForwardFor = "2001:db8:1:8d3:0:ce6:0:1337, 2001:db8:0:8d3:0:8a2e:80:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
        String expectedIP = "2001:db8:0:8d3:0:8a2e:80:7342";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    @Test
    public void testAllProxiesMatchingIPv4Slash() {
        String knownProxies = "192.168.32.0-192.168.33.36";
        String xForwardFor = "206.36.25.126, 192.168.32.1, 192.168.32.2, 192.168.33.36";
        String expectedIP = "206.36.25.126";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    @Test
    public void testFirstProxieNotMatchingIPv4Slash() {
        String knownProxies = "192.168.32.0-192.168.33.35";
        String xForwardFor = "206.36.25.126, 192.168.32.1, 192.168.32.2, 192.168.33.36";
        String expectedIP = "192.168.33.36";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    @Test
    public void testAllProxiesMatchingIPv6Slash() {
        // Equal to "2001:DB8:0:8D3:0:8A2E:70:0-2001:DB8:0:8D3:0:8A2E:70:FFFF";
        String knownProxies = "2001:0DB8:0000:08D3:0000:8A2E:0070:0000-2001:0DB8:0000:08D3:0000:8A2E:0070:FFFF";
        String xForwardFor = "2001:db8:1:8d3:0:ce6:0:1337, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
        String expectedIP = "2001:db8:1:8d3:0:ce6:0:1337";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    @Test
    public void testFirtsProxyNotMatchingIPv6Slash() {
        // Equal to "2001:0DB8:0000:08D3:0000:8A2E:0070:0000-2001:0DB8:0000:08D3:0000:8A2E:0070:FFFF"
        String knownProxies = "2001:DB8:0:8D3:0:8A2E:70:0-2001:DB8:0:8D3:0:8A2E:70:FFFF";
        String xForwardFor = "2001:db8:1:8d3:0:ce6:0:1337, 2001:db8:0:8d3:0:8a2e:80:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";
        String expectedIP = "2001:db8:0:8d3:0:8a2e:80:7342";

        evaluate(knownProxies, xForwardFor, expectedIP);
    }

    private void evaluate(String knownProxies, String xForwardFor, String expectedIP) {
        String remoteIP = IPTools.getRemoteIP(xForwardFor, IPTools.filterIP(knownProxies));
        assertEquals(expectedIP, remoteIP);
    }
}
