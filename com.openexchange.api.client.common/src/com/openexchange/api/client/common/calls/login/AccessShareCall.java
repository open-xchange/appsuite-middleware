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

package com.openexchange.api.client.common.calls.login;

import static com.openexchange.api.client.common.ApiClientUtils.checkSameOrigin;
import static com.openexchange.api.client.common.ApiClientUtils.getHeaderValue;
import static com.openexchange.api.client.common.ApiClientUtils.getSessionCookie;
import static com.openexchange.api.client.common.ApiClientUtils.parseParameters;
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
     */
    protected ShareLoginInformation parseLocationHeader(String location) {
        return ShareLoginInformation.parse(parseParameters(location));
    }

    @Override
    public HttpResponseParser<ShareLoginInformation> getParser() throws OXException {
        return new HttpResponseParser<ShareLoginInformation>() {

            @Override
            public ShareLoginInformation parse(HttpResponse response, HttpContext httpContext) throws OXException {
                /*
                 * Check that we got a redirect and that the redirect is set
                 */
                if (HttpStatus.SC_MOVED_TEMPORARILY != response.getStatusLine().getStatusCode()) {
                    throw ApiClientExceptions.NO_ACCESS.create(loginLink);
                }
                String location = getHeaderValue(response, HttpHeaders.LOCATION);
                if (Strings.isEmpty(location)) {
                    throw ApiClientExceptions.NO_ACCESS.create(loginLink);
                }

                /*
                 * Check if the redirect is still on the same server.
                 */
                checkSameOrigin(loginLink, location);
                ShareLoginInformation infos = parseLocationHeader(location);
                /*
                 * Parse prefix from location header
                 */
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
