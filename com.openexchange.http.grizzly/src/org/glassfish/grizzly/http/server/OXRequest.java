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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package org.glassfish.grizzly.http.server;

import static com.openexchange.tools.servlet.http.Cookies.extractDomainValue;
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.ThreadCache;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.Session;
import org.glassfish.grizzly.http.server.util.Globals;
import org.glassfish.grizzly.utils.Charsets;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.tools.servlet.http.Cookies;

/**
 * {@link OXRequest}
 * 
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class OXRequest extends Request {

    private static Log LOG = com.openexchange.log.Log.loggerFor(OXRequest.class);

    private static GrizzlyConfig grizzlyConfig = GrizzlyConfig.getInstance();

    private static final ThreadCache.CachedTypeIndex<Request> CACHE_IDX = ThreadCache.obtainIndex(Request.class, 16);

    public static Request create() {
        final Request request = ThreadCache.takeFromCache(CACHE_IDX);
        if (request != null) {
            return request;
        }

        OXRequest oxRequest = new OXRequest(new OXResponse());
        return oxRequest;
    }

    protected OXRequest(Response response) {
        super(response);
    }

    /**
     * <ol>
     * <li>Creates a new JSessionID with domain and backendroute appended</li>
     * <li>Sets a cookie for the new Session in the response</li>
     * <li>Removes invalid cookies found in the request</li>
     * </ol>
     */
    @Override
    protected Session doGetSession(final boolean create) {
        // Return the current session if it exists and is valid
        if ((session != null) && !session.isValid()) {
            session = null;
        }

        if (session != null) {
            return session;
        }

        // Session was null, check the known Sessions for the requested Session.
        if (requestedSessionId != null) {
            session = sessions.get(requestedSessionId);
            if ((session != null) && !session.isValid()) {
                // Session exists but is invalid. Remove the Session from known Sessions and the Session Cookies via response from browser.
                session = null;
                removeInvalidSessionCookie(requestedSessionId);
            }
            if (session != null) {
                return session;
            }
        }

        // Create a new session if requested and the response is not committed
        if (!create) {
            return null;
        }

        if (requestedSessionId != null && httpServerFilter.getConfiguration().isReuseSessionID()) {
            registerNewSession(requestedSessionId);
        } else {
            String sessionId = createSessionID();
            registerNewSession(sessionId);
            if (LOG.isInfoEnabled()) {
                LOG.info("Set new JSessionId Cookie: " + sessionId);
            }
        }
        return session;
    }

    /**
     * Register a new Session in the list of sessions, add it as Cookie to the Response and add the string value to the LogProperties.
     * 
     * @param sessionId The new SessionId that has to be registered
     */
    private void registerNewSession(String sessionId) {
        session = new Session(sessionId);
        session.setTimestamp(System.currentTimeMillis());
        session.setSessionTimeout(grizzlyConfig.getCookieMaxInactivityInterval()*1000);
        sessions.put(sessionId, session);
        response.addCookie(createSessionCookie(sessionId));
        if (LogProperties.isEnabled()) {
            Props logProperties = LogProperties.getLogProperties();
            logProperties.put("com.openexchange.http.grizzly.session", sessionId);
        }
    }

    /**
     * Remove invalid JSession cookie used in the Request. Cookies are invalid when:
     * 
     * @param invalidSessionId The invalid sessionId requested by the browser/cookie
     */
    private void removeInvalidSessionCookie(String invalidSessionId) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Removing invalid JSessionId Cookie: " + invalidSessionId);
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith(Globals.SESSION_COOKIE_NAME)) {
                if (cookie.getValue().equals(invalidSessionId)) {
                    response.addCookie(createinvalidationCookie(cookie));
                    String domain = extractDomainValue(cookie.getValue());
                    if (domain != null) {
                        response.addCookie(createinvalidationCookie(cookie, domain));
                    }
                    break;
                }
            }
        }
    }

    /**
     * Generate a invalidation Cookie that can be added to the response to prompt the browser to remove that cookie.
     * 
     * @param invalidCookie The invalid Cookie from the incoming request
     * @return an invalidation Cookie that can be added to the response to prompt the browser to remove that cookie.
     */
    private Cookie createinvalidationCookie(Cookie invalidCookie) {
        Cookie invalidationCookie = new Cookie(invalidCookie.getName(), invalidCookie.getValue());
        invalidationCookie.setPath("/");
        invalidationCookie.setMaxAge(0);
        return invalidationCookie;
    }

    /**
     * Generate a invalidation Cookie with domain that can be added to the response to prompt the browser to remove that cookie. The domain
     * is needed for IE to change/remove cookies.
     * 
     * @param invalidCookie The invalid Cookie from the incoming request
     * @param domain The domain to set in the invalidation cookie
     * @return an invalidation Cookie that can be added to the response to prompt the browser to remove that cookie.
     */
    private Cookie createinvalidationCookie(Cookie invalidCookie, String domain) {
        Cookie invalidationCookieWithDomain = createinvalidationCookie(invalidCookie);
        invalidationCookieWithDomain.setDomain(domain);
        return invalidationCookieWithDomain;
    }

    /**
     * Create a new JSessioID String that consists of a (random)-(the urlencoded domain of this server with dots and dashes
     * encoded).(backendRoute).
     * 
     * @return A new JSessionId value as String
     */
    private String createSessionID() {
        String backendRoute = grizzlyConfig.getBackendRoute();
        String domain = getDomainValue(getServerName());
        StringBuilder idBuilder = new StringBuilder(String.valueOf(generateRandomLong()));

        if (null != domain) {
            String encodedDomain = JSessionDomainEncoder.urlEncode(domain);
            idBuilder.append(encodedDomain);
        }
        idBuilder.append('.').append(backendRoute);

        return idBuilder.toString();
    }

    /**
     * Creates a new JSessionIdCookie based on a sessionID and the server configuration.
     * 
     * @param sessionID The sessionId to use for cookie generation
     * @return The new JSessionId Cookie
     */
    private Cookie createSessionCookie(String sessionID) {
        Cookie jSessionIdCookie = new Cookie(Globals.SESSION_COOKIE_NAME, sessionID);

        jSessionIdCookie.setPath("/");

        String domain = getDomainValue(getServerName());
        if (domain != null) {
            jSessionIdCookie.setDomain(domain);
        }

        /*
         * Toggle the security of the cookie on when we are dealing with a https request or the forceHttps config option is true e.g. when A
         * proxy like apache terminates ssl in front of the backend. The exception from forced https is a request from the local LAN.
         */
        boolean isCookieSecure = request.isSecure() || (grizzlyConfig.isCookieForceHttps() && !Cookies.isLocalLan(getServerName()));
        jSessionIdCookie.setSecure(isCookieSecure);

        /*
         * If auto-login is enabled we transform the session cookie into a persistent cookie
         */
        if (grizzlyConfig.isSessionAutologin()) {
            jSessionIdCookie.setMaxAge(grizzlyConfig.getCookieMaxAge());
        } else {
            jSessionIdCookie.setMaxAge(-1); // cookies auto-expire
        }

        return jSessionIdCookie;
    }

    /**
     * Parse request parameters. This differs from the original implementation in a way that we don't use ISO-8859-1 as fallback encoding
     * but the one configured in server.properties via "DefaultEncoding".
     */
    @Override
    protected void parseRequestParameters() {

        // getCharacterEncoding() may have been overridden to search for
        // hidden form field containing request encoding
        final String enc = getCharacterEncoding();

        // Delay updating requestParametersParsed to TRUE until
        // after getCharacterEncoding() has been called, because
        // getCharacterEncoding() may cause setCharacterEncoding() to be
        // called, and the latter will ignore the specified encoding if
        // requestParametersParsed is TRUE
        requestParametersParsed = true;

        Charset charset;

        if (enc != null) {
            try {
                charset = Charsets.lookupCharset(enc);
            } catch (Exception e) {
                charset = Charsets.lookupCharset(grizzlyConfig.getDefaultEncoding());
            }
        } else {
            charset = Charsets.lookupCharset(grizzlyConfig.getDefaultEncoding());
        }

        parameters.setEncoding(charset);
        parameters.setQueryStringEncoding(charset);

        parameters.handleQueryParameters();

        if (usingInputStream || usingReader) {
            return;
        }

        if (!Method.POST.equals(getMethod())) {
            return;
        }

        final int len = getContentLength();

        if (len > 0) {

            if (!checkPostContentType(getContentType()))
                return;

            try {
                final Buffer formData = getPostBody(len);
                parameters.processParameters(formData, formData.position(), len);
            } catch (Exception ignored) {
            } finally {
                try {
                    skipPostBody(len);
                } catch (Exception e) {
                    LOG.warn("Exception occurred during body skip", e);
                }
            }
        }

    }

}
