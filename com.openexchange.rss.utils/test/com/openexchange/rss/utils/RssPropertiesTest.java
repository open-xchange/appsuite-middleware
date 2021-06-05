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

package com.openexchange.rss.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.rss.utils.internal.RssPropertiesImpl;
import com.openexchange.server.SimpleServiceLookup;

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
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_everythingAllowed2_returnFalse() {
        String url = "https://open-xchange.com:80";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_everythingAllowed3_returnFalse() {
        String url = "https://open-xchange.com";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_everythingAllowed4_returnFalse() {
        String url = "https://open-xchange.com:443";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_everythingAllowed5_returnFalse() {
        String url = "https://open-xchange.com:443/myDeeplink/test.xml";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertFalse(denied);
    }

    @Test
    public void testIsDenied_schemeDenied_returnTrue() {
        String url = "rss://open-xchange.com:80";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertTrue(denied);
    }

    @Test
    public void testIsDenied_hostDenied_returnTrue() {
        String url = "https://localhost:80";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertTrue(denied);
    }

    @Test
    public void testIsDenied_hostIpDenied_returnTrue() {
        String url = "https://127.127.127.127:80";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertTrue(denied);
    }

    @Test
    public void testIsDenied_hostIpDenied2_returnTrue() {
        String url = "https://127.0.0.1";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertTrue(denied);
    }

    @Test
    public void testIsDenied_hostIpDenied3_returnTrue() {
        String url = "https://127.0.0.11/myDeeplink/test.xml";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertTrue(denied);
    }

    @Test
    public void testIsDenied_portDenied_returnTrue() {
        String url = "https://open-xchange.com:993";
        boolean denied = new RssPropertiesImpl(new SimpleServiceLookup()).isDenied(url);

        assertTrue(denied);
    }
}
