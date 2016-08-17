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
    public void testValidURIs() throws Exception {
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
    public void testInvalidURIs() throws Exception {
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
    public void testEqualURIs() throws Exception {
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
    public void testUnequalURIs() throws Exception {
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
