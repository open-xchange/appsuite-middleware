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

package org.glassfish.grizzly.http.server;

import static com.openexchange.tools.servlet.http.Cookies.extractDomainValue;
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import java.nio.charset.Charset;
import org.glassfish.grizzly.ThreadCache;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.util.Globals;
import org.glassfish.grizzly.utils.Charsets;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.log.LogProperties;
import com.openexchange.tools.servlet.http.Cookies;

/**
 * {@link OXRequest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class OXRequest extends Request {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXRequest.class);

    private static final GrizzlyConfig grizzlyConfig = GrizzlyConfig.getInstance();

    private static final ThreadCache.CachedTypeIndex<Request> CACHE_IDX = ThreadCache.obtainIndex(Request.class, 16);

    private static final boolean SIZE_AWARE;

    static {
        int max = grizzlyConfig.getMaxNumberOfHttpSessions();
        if (max > 0) {
            sessions = new BoundedConcurrentHashMap<String, Session>(1024, max);
        }
        SIZE_AWARE = true;
    }

    private static final String SESSION_COOKIE_NAME = Globals.SESSION_COOKIE_NAME;

    // ---------------------------- Members ---------------------------- //

    public static Request create() {
        final Request request = ThreadCache.takeFromCache(CACHE_IDX);
        if (request != null) {
            return request;
        }

        return new OXRequest(new OXResponse());
    }

    private String XForwardProto = null;
    private int XForwardPort = 0;
    private boolean isConsiderXForwards = grizzlyConfig.isConsiderXForwards();
    private boolean isForcedSecurity = false;

    protected OXRequest(Response response) {
        super(response);
    }

    @Override
    protected void recycle() {
        XForwardProto = null;
        XForwardPort = 0;
        isConsiderXForwards = grizzlyConfig.isConsiderXForwards();
        isForcedSecurity = false;
        super.recycle();
    }

    /**
     * Gets the XForwardProto e.g. http/s
     *
     * @return The XForwardProto
     */
    public String getXForwardProto() {
        return XForwardProto;
    }

    /**
     * Sets the xForwardProto e.g. http/s
     *
     * @param XForwardProto The XForwardProto to set
     */
    public void setxForwardProto(String XForwardProto) {
        this.XForwardProto = XForwardProto;
    }

    /**
     * Gets the XForwardPort
     *
     * @return The XForwardPort
     */
    public int getXForwardPort() {
        return XForwardPort;
    }

    /**
     * Sets the XForwardPort
     *
     * @param XForwardPort The XForwardPort to set
     */
    public void setXForwardPort(int XForwardPort) {
        this.XForwardPort = XForwardPort;
    }

    @Override
    public String getScheme() {
        if (isConsiderXForwards && XForwardProto != null) {
            return XForwardProto;
        }
        return super.getScheme();
    }

    @Override
    public int getServerPort() {
        if (isConsiderXForwards && XForwardPort > 0) {
            return XForwardPort;
        }
        return super.getServerPort();
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
                if (isRequestedSessionIdFromURL() && !hasSessionCookie(requestedSessionId)) {
                    response.addCookie(createSessionCookie(session.getIdInternal()));
                }
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
            LOG.debug("Set new JSessionId Cookie: {}", sessionId);
        }
        return session;
    }

    /**
     * Register a new Session in the list of sessions, add it as Cookie to the Response and add the string value to the LogProperties.
     *
     * @param sessionId The new SessionId that has to be registered
     */
    private void registerNewSession(String sessionId) {
        if (false == SIZE_AWARE) {
            // Check possible limitation
            final int max = grizzlyConfig.getMaxNumberOfHttpSessions();
            if (max > 0 && sessions.size() >= max) {
                final String message = "Max. number of HTTP session (" + max + ") exceeded.";
                LOG.warn(message);
                throw new IllegalStateException(message);
            }
        }

        // Proceed creating that session
        session = new Session(sessionId);
        session.setTimestamp(System.currentTimeMillis());
        session.setSessionTimeout(grizzlyConfig.getCookieMaxInactivityInterval() * 1000);
        sessions.put(sessionId, session);
        response.addCookie(createSessionCookie(sessionId));
        LogProperties.put(LogProperties.Name.GRIZZLY_HTTP_SESSION, sessionId);
    }

    /**
     * Remove invalid JSession cookie used in the Request. Cookies are invalid when:
     *
     * @param invalidSessionId The invalid sessionId requested by the browser/cookie
     */
    private void removeInvalidSessionCookie(String invalidSessionId) {
        Cookie[] cookies = getCookies();
        if (null == cookies || cookies.length <= 0) {
            return;
        }

        LOG.debug("Removing invalid JSessionId Cookie: {}", invalidSessionId);
        String sessionCookieName = SESSION_COOKIE_NAME;
        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith(sessionCookieName)) {
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
        Cookie jSessionIdCookie = new Cookie(SESSION_COOKIE_NAME, sessionID);

        jSessionIdCookie.setPath("/");

        String domain = getDomainValue(getServerName());
        if (domain != null) {
            jSessionIdCookie.setDomain(domain);
        }

        /*
         * Toggle the security of the cookie on when we are dealing with a https request or the forceHttps config option is true e.g. when A
         * proxy in front of apache terminates ssl. The exception from forced https is a request from the local LAN.
         */
        boolean isCookieSecure = isSecure() || (grizzlyConfig.isForceHttps() && !Cookies.isLocalLan(getServerName()));
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
     * Checks if this request has a session cookie associated with specified session identifier.
     *
     * @param sessionID The session identifier to use for look-up
     * @return <code>true</code> if this request contains such a session cookie; otherwise <code>false</code>
     */
    private boolean hasSessionCookie(String sessionID) {
        Cookie[] cookies = getCookies();
        if (null == cookies || cookies.length <= 0) {
            return false;
        }

        String sessionCookieName = SESSION_COOKIE_NAME;
        for (Cookie cookie : cookies) {
            if (sessionCookieName.equals(cookie.getName()) && sessionID.equals(cookie.getValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Override isSecure by first checking the X-Forward-Proto Header. Fallback is the original implementation of the header wasn't present.
     *
     * @return True if the X-Forward-Proto header indicates a secure connection or a real https connection was used.
     */
    @Override
    public boolean isSecure() {
        if (isConsiderXForwards && XForwardProto != null) {
            return XForwardProto.equals("https");
        }
        return super.isSecure();
    }

    /**
     * Lookup a Charset based on a String value.
     * If the lookup fails try to fall back to the default encoding from GrizzlyConfig. If that fails, too use UTF-8.
     * The resulting Charset is used to e.g decode parameters sent via request bodies.
     *
     * @param enc The String representing the encoding
     * @return The Charset loked up, or the default encoding specified in GrizzlyConfig, or UTF-8
     */
    @Override
    protected Charset lookupCharset(final String enc) {
        Charset charset;

        if (enc != null) {
            try {
                charset = Charsets.lookupCharset(enc);
            } catch (Exception e) {
                try {
                    String defaultEncoding = GrizzlyConfig.getInstance().getDefaultEncoding();
                    charset = Charsets.lookupCharset(defaultEncoding);
                } catch (Exception ex) {
                    charset = Charsets.UTF8_CHARSET;
                }
            }
        } else {
            try {
                String defaultEncoding = GrizzlyConfig.getInstance().getDefaultEncoding();
                charset = Charsets.lookupCharset(defaultEncoding);
            } catch (Exception ex) {
                charset = Charsets.UTF8_CHARSET;
            }
        }

        return charset;
    }

    /*
     * We can't look up certificate or ssl attributes as long as the http balancer terminates ssl in our setup. Yet we have to set the
     * Request to secure for e.g. using the correct schema(http/https) when generating urls and the request reached the balancer via https.
     * To circumvent problems arising when clients ask for e.g. certificate or ssl attributes on an unsecure Request that is marked as
     * secure we'll return null.
     */
    @Override
    public Object getAttribute(String name) {
        if( Globals.SSL_CERTIFICATE_ATTR.equals(name) || isSSLAttribute(name)) {
            //Did we force a secure request although the balancer terminated https
            Object attribute = getAttribute("com.openexchange.http.isForcedSecurity");
            if(attribute != null) {
                isForcedSecurity = (Boolean)attribute;
            }
            if(isForcedSecurity) {
                return null;
            }
        }
        return super.getAttribute(name);
    }

}
