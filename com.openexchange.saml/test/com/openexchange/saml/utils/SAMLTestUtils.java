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

package com.openexchange.saml.utils;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.sim.SimHttpServletRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.Origin;
import com.openexchange.sessiond.AddSessionParameter;

/**
 * {@link SAMLTestUtils}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class SAMLTestUtils {

    public static SimHttpServletRequest prepareHTTPRequest(String method, URI location) {
        SimHttpServletRequest request = new SimHttpServletRequest();
        request.setRequestURI(location.getRawPath());
        request.setRequestURL(location.getScheme() + "://" + location.getHost() + location.getPath());
        request.setMethod(method);
        request.setScheme(location.getScheme());
        request.setSecure("https".equals(location.getScheme()));
        request.setServerName(location.getHost());
        request.setQueryString(location.getRawQuery());
        request.setCookies(Collections.<Cookie> emptyList());
        request.setRemoteAddr("127.0.0.1");
        Map<String, String> params = parseURIQuery(location);
        for (String name : params.keySet()) {
            request.setParameter(name, params.get(name));
        }
        return request;
    }

    public static Map<String, String> parseURIQuery(URI uri) {
        Map<String, String> map = new HashMap<String, String>();
        List<NameValuePair> pairs = URLEncodedUtils.parse(uri, Charset.forName("UTF-8"));
        for (NameValuePair pair : pairs) {
            map.put(pair.getName(), pair.getValue());
        }
        return map;
    }

    public static AddSessionParameter buildAddSessionParameter() {
        return buildAddSessionParameter(null);
    }

    public static AddSessionParameter buildAddSessionParameter(final SessionEnhancement enhancement) {
        return new AddSessionParameter() {

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public boolean isStaySignedIn() {
                return false;
            }

            @Override
            public String getUserLoginInfo() {
                return "test.user";
            }

            @Override
            public int getUserId() {
                return 1;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getHash() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }

            @Override
            public String getFullLogin() {
                return "test.user@example.com";
            }

            @Override
            public List<SessionEnhancement> getEnhancements() {
                return null != enhancement ? Arrays.asList(enhancement) : Collections.emptyList();
            }

            @Override
            public Context getContext() {
                return new SimContext(1);
            }

            @Override
            public String getClientToken() {
                return null;
            }

            @Override
            public String getClientIP() {
                return "127.0.0.1";
            }

            @Override
            public String getClient() {
                return "Test Client";
            }

            @Override
            public String getAuthId() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }

            @Override
            public String getUserAgent() {
                return "User-Agent";
            }

            @Override
            public Origin getOrigin() {
                return Origin.HTTP_JSON;
            }
        };
    }

}
