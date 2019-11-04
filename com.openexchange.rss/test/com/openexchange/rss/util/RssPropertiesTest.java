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

package com.openexchange.rss.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * {@link RssPropertiesTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class RssPropertiesTest {

    @Test
    public void testIsDenied_everythingAllowed_returnFalse() {
        String url = "http://open-xchange.com:80";
        boolean denied = RssProperties.isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_everythingAllowed2_returnFalse() {
        String url = "https://open-xchange.com:80";
        boolean denied = RssProperties.isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_everythingAllowed3_returnFalse() {
        String url = "https://open-xchange.com";
        boolean denied = RssProperties.isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_everythingAllowed4_returnFalse() {
        String url = "https://open-xchange.com:443";
        boolean denied = RssProperties.isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_everythingAllowed5_returnFalse() {
        String url = "https://open-xchange.com:443/myDeeplink/test.xml";
        boolean denied = RssProperties.isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_schemeDenied_returnTrue() {
        String url = "rss://open-xchange.com:80";
        boolean denied = RssProperties.isDenied(url);

        assertTrue(denied);
    }

    @Test
    public void testIsDenied_hostDenied_returnTrue() {
        String url = "https://localhost:80";
        boolean denied = RssProperties.isDenied(url);

        assertTrue(denied);
    }

    @Test
    public void testIsDenied_hostIpDenied_returnTrue() {
        String url = "https://127.127.127.127:80";
        boolean denied = RssProperties.isDenied(url);

        assertTrue(denied);
    }

    @Test
    public void testIsDenied_hostIpDenied2_returnTrue() {
        String url = "https://127.0.0.1";
        boolean denied = RssProperties.isDenied(url);

        assertTrue(denied);
    }

    public void testIsDenied_hostIpDenied3_returnTrue() {
        String url = "https://127.0.0.11/myDeeplink/test.xml";
        boolean denied = RssProperties.isDenied(url);

        assertTrue(denied);
    }

    public void testIsDenied_portDenied_returnTrue() {
        String url = "https://open-xchange.com:993";
        boolean denied = RssProperties.isDenied(url);

        assertTrue(denied);
    }
}
