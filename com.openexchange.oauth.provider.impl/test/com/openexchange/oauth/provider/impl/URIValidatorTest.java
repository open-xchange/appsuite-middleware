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

package com.openexchange.oauth.provider.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import com.openexchange.oauth.provider.tools.URIValidator;


/**
 * {@link URIValidatorTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class URIValidatorTest {

     @Test
     public void testValidURIs() {
        List<String> uris = new LinkedList<>();
        uris.add("myapp://handle-oauth-redirect");
        uris.add("myapp://?no-authority=true");
        uris.add("http://localhost/oauth/redirect");
        uris.add("http://localhost/oauth/redirect?a=b&x=y");
        uris.add("http://127.0.0.1/");
        uris.add("http://[::1]");
        uris.add("https://appsuite-dev.open-xchange.com/appsuite/api/oauth?action=create");
        uris.add("https://appsuite-dev.open-xchange.com:8080/appsuite/api/oauth?action=create");

        for (String uri : uris) {
            assertTrue(uri, URIValidator.isValidRedirectURI(uri));
        }
    }

     @Test
     public void testInvalidURIs() {
        List<String> uris = new LinkedList<>();
        uris.add("myapp://handle-oauth-redirect?with=path#and-disallowed-fragment");
        uris.add("http://appsuite-dev.open-xchange.com/appsuite/api/oauth?action=create");
        uris.add("http://8.8.8.8");
        uris.add("http://[2a00:1450:4001:80b::1002]/");
        uris.add("http://[::1]#again-with-fragment");

        for (String uri : uris) {
            assertFalse(uri, URIValidator.isValidRedirectURI(uri));
        }
    }

     @Test
     public void testEqualURIs() {
        Map<String, String> uris = new HashMap<>();
        uris.put("https://appsuite-dev.open-xchange.com/appsuite/api/oauth?action=create", "https://appsuite-dev.open-xchange.com/appsuite/api/oauth?action=create");
        uris.put("http://localhost/path?query=true", "http://localhost:80/path?query=true");
        uris.put("https://example.com/path?query=true", "https://example.com:443/path?query=true");
        uris.put("HTTPS://EXAMPLE.COM/path/?query=true", "https://example.com:443/path/?query=true");

        for (Entry<String, String> pair : uris.entrySet()) {
            assertTrue(pair.getKey() + " unequal to " + pair.getValue(), URIValidator.urisEqual(pair.getKey(), pair.getValue()));
        }
    }

     @Test
     public void testUnequalURIs() {
        Map<String, String> uris = new HashMap<>();
        uris.put("https://appsuite-dev.open-xchange.com/appsuite/api/oauth?action=create", "http://appsuite-dev.open-xchange.com/appsuite/api/oauth?action=create");
        uris.put("http://localhost/path?query=true", "http://localhost:80/Path?query=true");
        uris.put("https://example.com/path?query=true", "https://example.com:443/path?Query=True");
        uris.put("HTTPS://EXAMPLE.COM/path?query=true", "HTTP://example.com:443/path?query=true");

        for (Entry<String, String> pair : uris.entrySet()) {
            assertFalse(pair.getKey() + " equal to " + pair.getValue(), URIValidator.urisEqual(pair.getKey(), pair.getValue()));
        }
    }

}
