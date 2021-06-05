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

package com.openexchange.api.client.common.calls.login;

import static com.openexchange.api.client.common.ApiClientUtils.getHeaderValue;
import static com.openexchange.api.client.common.ApiClientUtils.getSessionCookie;
import static com.openexchange.api.client.common.ApiClientUtils.parseParameters;
import static com.openexchange.api.client.common.Checks.checkSameOrigin;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.rest.client.httpclient.util.HttpContextUtils.getCookieStore;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import org.apache.http.protocol.HttpContext;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link AccessShareCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class AccessShareCall extends AbstractGetCall<ShareLoginInformation> {

    protected final URL loginLink;

    /**
     * Initializes a new {@link AccessShareCall}.
     *
     * @param loginLink The login link
     */
    public AccessShareCall(URL loginLink) {
        super();
        this.loginLink = loginLink;
    }

    @Override
    public boolean appendDispatcherPrefix() {
        return false;
    }

    @Override
    @NonNull
    public String getModule() {
        String path = loginLink.getPath();
        if (null == path) {
            return "";
        }
        return path;
    }

    @Override
    protected String getAction() {
        return "";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {}

    /**
     * Parses the location header into its parameters and make them available to the information member
     * <p>
     * Values from
     * <li> com.openexchange.share.servlet.utils.ShareRedirectUtils.getWebSessionRedirectURL()</li>
     * <li> com.openexchange.share.servlet.utils.LoginLocation</li>
     *
     * @param location The location header
     * @throws OXException In case the remote server indicates an error
     */
    protected ShareLoginInformation parseLocationHeader(String location) throws OXException {
        return ShareLoginInformation.parse(parseParameters(location));
    }

    @Override
    public HttpResponseParser<ShareLoginInformation> getParser() {
        return new HttpResponseParser<ShareLoginInformation>() {

            @Override
            public ShareLoginInformation parse(HttpResponse response, HttpContext httpContext) throws OXException {
                /*
                 * Check that we got a redirect and that the redirect is set
                 */
                if (HttpStatus.SC_MOVED_TEMPORARILY != response.getStatusLine().getStatusCode()) {
                    throw ApiClientExceptions.UNEXPECTED_ERROR.create("The remote server responded with an unexpected HTTP status of {}", I(response.getStatusLine().getStatusCode()));
                }
                String location = getHeaderValue(response, HttpHeaders.LOCATION);
                if (Strings.isEmpty(location)) {
                    throw ApiClientExceptions.UNEXPECTED_ERROR.create("The remote server didn't send the location of the share");
                }

                /*
                 * Check if the redirect is still on the same server.
                 */
                checkSameOrigin(loginLink, location);

                /*
                 * Parse prefix from location header
                 */
                ShareLoginInformation infos = parseLocationHeader(location);
                if (Strings.isEmpty(infos.getRemoteSessionId())) {
                    Optional<Cookie> sessionCookie = getSessionCookie(getCookieStore(httpContext));
                    if (sessionCookie.isPresent()) {
                        infos.setRemoteSessionId(sessionCookie.get().getValue());
                    }
                }
                return infos;
            }

        };
    }
}
