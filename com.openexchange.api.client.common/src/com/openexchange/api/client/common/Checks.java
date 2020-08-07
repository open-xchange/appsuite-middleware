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

package com.openexchange.api.client.common;

import static com.openexchange.api.client.common.OXExceptionParser.parseException;
import static com.openexchange.java.Autoboxing.I;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link Checks} - Util class with checks for HTTP response content
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public final class Checks {

    private static final Logger LOGGER = LoggerFactory.getLogger(Checks.class);

    /**
     * Initializes a new {@link Checks}.
     */
    private Checks() {}

    /**
     * Handles a status error. Either will throw the transmitted exception
     * or will generate an appropriated exception if the status code implies
     * an client or server error.
     *
     * @param response The response
     * @throws OXException The OXException to throw for the status code
     */
    public static void checkStatusError(HttpResponse response) throws OXException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < HttpStatus.SC_BAD_REQUEST) {
            // Nothing to do, pass response to caller
            return;
        }
        OXException parsedException = null;
        try {
            JSONValue json = JSONUtils.getJSON(response);
            if (null != json && json instanceof JSONObject) {
                parsedException = parseException((JSONObject) json);
            }
        } catch (Exception e) {
            LOGGER.debug("Error while getting error stack", e);
        }

        if (null != parsedException) {
            throw parsedException;
        }

        if (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw ApiClientExceptions.CLIENT_ERROR.create(I(statusCode));
        }
        throw ApiClientExceptions.REMOTE_SERVER_ERROR.create(I(statusCode));
    }

    /**
     * Check that the request targets the same origin as the given URL
     *
     * @param request The HTTP request
     * @param target The desired target
     * @throws OXException In case the domain doesn't match
     */
    public static void checkSameOrigin(HttpRequestBase request, URL target) throws OXException {
        if (null == request.getURI() || false == request.getURI().getHost().equals(target.getHost())) {
            throw ApiClientExceptions.NOT_SAME_ORIGIN.create(request.getURI(), target.getHost());
        }
    }

    /**
     * Checks that the redirect target is still on the same server
     *
     * @param originHost The original targeted host
     * @param redirectTarget The redirect to follow
     * @throws OXException In case the new target is invalid or redirects to another host
     */
    public static void checkSameOrigin(URL originHost, String redirectTarget) throws OXException {
        try {
            //relative or absolute redirect?
            URL redirect = redirectTarget.startsWith("/") ? new URL(originHost, redirectTarget) : new URL(redirectTarget);
            if (null != redirect.getHost()) {
                if (false == originHost.getHost().equals(redirect.getHost())) {
                    throw ApiClientExceptions.NOT_SAME_ORIGIN.create(redirect.getHost(), originHost.getHost());
                }
            }
        } catch (MalformedURLException e) {
            throw ApiClientExceptions.INVALID_TARGET.create(e, redirectTarget);
        }
    }

    /**
     * Checks that the JSESSION cookie is set
     *
     * @param cookieStore The cookie store to get the cookies from
     * @throws OXException In case the cookie is missing
     */
    public static void checkJSESSIONCookie(CookieStore cookieStore) throws OXException {
        checkCookieSet(Tools.JSESSIONID_COOKIE, cookieStore);
    }

    /**
     * Checks that the secret cookie is set
     *
     * @param cookieStore The cookie store to get the cookies from
     * @throws OXException In case the cookie is missing
     */
    public static void checkSecretCookie(CookieStore cookieStore) throws OXException {
        checkCookieSet(LoginServlet.SECRET_PREFIX, cookieStore);
    }

    /**
     * Checks that the public session cookie is set
     *
     * @param cookieStore The cookie store to get the cookies from
     * @throws OXException In case the cookie is missing
     */
    public static void checkPublicSessionCookie(CookieStore cookieStore) throws OXException {
        checkCookieSet(LoginServlet.PUBLIC_SESSION_PREFIX, cookieStore);
    }

    /**
     * Checks that a certain cookie with a dedicated name is set
     *
     * @param cookiePrefix The name prefix of the cookie, e.g. {@value LoginServlet#SECRET_PREFIX}
     * @param cookieStore The cookie store to get the cookies from
     * @throws OXException In case the cookie is missing
     */
    public static void checkCookieSet(String cookiePrefix, CookieStore cookieStore) throws OXException {
        List<Cookie> cookies = cookieStore.getCookies();
        if (null == cookies || cookies.isEmpty()) {
            throw ApiClientExceptions.MISSING_COOKIE.create();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith(cookiePrefix)) {
                return;
            }
        }
        throw ApiClientExceptions.MISSING_COOKIE.create();
    }

}
