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
import static com.openexchange.http.grizzly.util.IPTools.COMMA_SEPARATOR;
import static com.openexchange.http.grizzly.util.IPTools.splitAndTrim;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.openexchange.http.grizzly.util.IPTools;

/**
 * {@link RemoteIPFinderTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RemoteIPFinderTest {

    // extra spaces for trim() test
    private final static String validIPv4Remotes = " 192.168.32.49 , 192.168.32.50 , 192.168.33.225, 192.168.33.224";

    private final static String invalidIPv4Remotes = "192.168.32.49, 192.168.32.555, 192.168.33.225, 192.168.33.224";

    private final static List<String> knownIPv4 = splitAndTrim(" 192.168.33.225 , 192.168.33.224 ", COMMA_SEPARATOR);

    private final static String validIPv6Remotes = "2001:db8:0:8d3:0:8a2e:70:7341, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";

    private final static String invalidIPv6Remotes = "2001:db8:0:8d3:0:8a2e:70:, 2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344";

    private final static List<String> knownIPv6 = IPTools.splitAndTrim(
        "2001:db8:0:8d3:0:8a2e:70:7342, 2001:db8:0:8d3:0:8a2e:70:7343, 2001:db8:0:8d3:0:8a2e:70:7344",
        COMMA_SEPARATOR);

    private final String emptyRemote = "";

    private final List<String> emptyKnown = Collections.emptyList();

    @Test
    public void testValidIPv4() {
        String remoteIP = IPTools.getRemoteIP(validIPv4Remotes, knownIPv4);
        assertEquals("192.168.32.50", remoteIP);
    }

    @Test
    public void testEmptyKnownIPv4() {
        String remoteIP = IPTools.getRemoteIP(validIPv4Remotes, emptyKnown);
        assertEquals("192.168.33.224", remoteIP);
    }

    @Test
    public void testEmptyRemoteIPv4() {
        String remoteIP = IPTools.getRemoteIP(emptyRemote, knownIPv4);
        assertNull(remoteIP);
    }

    @Test
    public void testInvalidRemoteIPv4() {
        String remoteIP = IPTools.getRemoteIP(invalidIPv4Remotes, knownIPv4);
        assertNull(remoteIP);
    }

    @Test
    public void testValidIPv6() {
        String remoteIP = IPTools.getRemoteIP(validIPv6Remotes, knownIPv6);
        assertEquals("2001:db8:0:8d3:0:8a2e:70:7341", remoteIP);
    }

    @Test
    public void testEmptyKnownIPv6() {
        String remoteIP = IPTools.getRemoteIP(validIPv6Remotes, emptyKnown);
        assertEquals("2001:db8:0:8d3:0:8a2e:70:7344", remoteIP);
    }

    @Test
    public void testEmptyRemoteIPv6() {
        String remoteIP = IPTools.getRemoteIP(emptyRemote, knownIPv6);
        assertNull(remoteIP);
    }

    @Test
    public void testInvalidRemoteIPv6() {
        String remoteIP = IPTools.getRemoteIP(invalidIPv6Remotes, knownIPv6);
        assertNull(remoteIP);
    }

}
