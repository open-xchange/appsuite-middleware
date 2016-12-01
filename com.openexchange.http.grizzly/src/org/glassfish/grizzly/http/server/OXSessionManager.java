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

package org.glassfish.grizzly.http.server;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.utils.DataStructures;
import org.slf4j.Logger;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.log.LogProperties;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.servlet.http.Cookies;


/**
 * {@link OXSessionManager} - Open-Xchange HTTP session manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OXSessionManager implements SessionManager {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OXSessionManager.class);

    private static final int MIN_PERIOD_SECONDS = 5;

    // -----------------------------------------------------------------------------------------------

    private final GrizzlyConfig grizzlyConfig;
    private final ConcurrentMap<String, Session> sessions;
    private final Random rnd;
    private final ScheduledTimerTask sessionExpirer;
    private final int max;
    private final boolean considerSessionCount;
    private final Lock lock;
    private int sessionsCount;  // protected by lock
    private long lastCleanUp;   // protected by lock

    /**
     * Initializes a new {@link OXSessionManager}.
     */
    public OXSessionManager(GrizzlyConfig grizzlyConfig, TimerService timerService) {
        super();
        this.grizzlyConfig = grizzlyConfig;
        int max = grizzlyConfig.getMaxNumberOfHttpSessions();
        this.max = max;
        this.considerSessionCount = max > 0;
        this.sessions = DataStructures.getConcurrentMap();
        this.rnd = new Random();
        this.sessionsCount = 0;
        final ReentrantLock lock = new ReentrantLock();
        this.lock = lock;
        this.lastCleanUp = 0;

        int configuredSessionTimeout = grizzlyConfig.getCookieMaxInactivityInterval();
        int periodSeconds = grizzlyConfig.getSessionExpiryCheckInterval();
        if (periodSeconds > configuredSessionTimeout) {
            periodSeconds = configuredSessionTimeout >> 2;
        }
        if (periodSeconds < MIN_PERIOD_SECONDS) {
            periodSeconds = MIN_PERIOD_SECONDS;
        }

        Runnable expirerTask = new Runnable() {

            @Override
            public void run() {
                lock.lock();
                try {
                    cleanUp(System.currentTimeMillis());
                } finally {
                    lock.unlock();
                }
            }
        };
        this.sessionExpirer = timerService.scheduleAtFixedRate(expirerTask, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    /**
     * Cleans-up the session collection. Drops invalid/expired sessions.
     * <p>
     * <b>Must only be called when holding lock.</b>
     *
     * @param currentTime The current time stamp
     */
    void cleanUp(long currentTime) {
        Session session;
        for (Iterator<Session> it = sessions.values().iterator(); it.hasNext();) {
            session = it.next();
            if (!session.isValid() || ((session.getSessionTimeout() > 0) && ((currentTime - session.getTimestamp()) > session.getSessionTimeout()))) {
                session.setValid(false);
                it.remove();
                sessionsCount--;
            }
        }

        lastCleanUp = System.currentTimeMillis();
    }

    /**
     * Disposes this instance
     */
    public void destroy() {
        lock.lock();
        try {
            sessionExpirer.cancel();
            sessions.clear();
            sessionsCount = 0;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Session getSession(Request request, String requestedSessionId) {
        if (requestedSessionId == null) {
            return null;
        }

        Session session = sessions.get(requestedSessionId);
        if (session == null) {
            return null;
        }

        if (session.isValid()) {
            if (request.isRequestedSessionIdFromURL() && !hasSessionCookie(request, requestedSessionId)) {
                Response response = request.response;
                response.addCookie(createSessionCookie(request, session.getIdInternal()));
            }
            return session;
        }

        removeInvalidSessionCookie(request, requestedSessionId);
        return null;
    }

    @Override
    public Session createSession(Request request) {
        lock.lock();
        try {
            if (considerSessionCount && sessionsCount >= max) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastCleanUp) < (MIN_PERIOD_SECONDS * 1000)) {
                    throw onMaxSessionCountExceeded();
                }

                // Clean-up & check again
                cleanUp(currentTime);
                if (sessionsCount >= max) {
                    throw onMaxSessionCountExceeded();
                }
            }

            Session session = new Session();

            String requestedSessionId;
            do {
                requestedSessionId = createSessionID(request);
                session.setIdInternal(requestedSessionId);
            } while (sessions.putIfAbsent(requestedSessionId, session) != null);
            sessionsCount++;

            LogProperties.put(LogProperties.Name.GRIZZLY_HTTP_SESSION, requestedSessionId);

            return session;
        } finally {
            lock.unlock();
        }
    }

    private IllegalStateException onMaxSessionCountExceeded() {
        String message = "Max. number of HTTP sessions (" + max + ") exceeded.";
        LOG.warn(message);
        return new IllegalStateException(message);
    }

    @Override
    public String changeSessionId(Request request, Session session) {
        lock.lock();
        try {
            final String oldSessionId = session.getIdInternal();
            final String newSessionId = String.valueOf(generateRandomLong());

            session.setIdInternal(newSessionId);

            sessions.remove(oldSessionId);
            sessions.put(newSessionId, session);
            return oldSessionId;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void configureSessionCookie(Request request, Cookie cookie) {
        cookie.setPath("/");

        String domain = Cookies.getDomainValue(request.getServerName());
        if (domain != null) {
            cookie.setDomain(domain);
        }

        /*
         * Toggle the security of the cookie on when we are dealing with a https request or the forceHttps config option is true e.g. when A
         * proxy in front of apache terminates ssl. The exception from forced https is a request from the local LAN.
         */
        boolean isCookieSecure = request.isSecure() || (grizzlyConfig.isForceHttps() && !Cookies.isLocalLan(request.getServerName()));
        cookie.setSecure(isCookieSecure);

        /*
         * If auto-login is enabled we transform the session cookie into a persistent cookie
         */
        if (grizzlyConfig.isSessionAutologin()) {
            cookie.setMaxAge(grizzlyConfig.getCookieMaxAge());
        } else {
            cookie.setMaxAge(-1); // cookies auto-expire
        }
    }

    /**
     * Create a new JSessioID String that consists of a:
     * <pre>
     *  &lt;random&gt; + ("-" + &lt;the urlencoded domain of this server&gt;)? + "." + &lt;backendRoute&gt;
     * </pre>
     * Example:
     * <pre>
     *  367190855121044669-open%2Dxchange%2Ecom.OX0
     * </pre>
     *
     * @return A new JSessionId value as String
     */
    private String createSessionID(Request request) {
        String backendRoute = grizzlyConfig.getBackendRoute();
        String domain = Cookies.getDomainValue(request.getServerName());
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
    private Cookie createSessionCookie(Request request, String sessionID) {
        Cookie jSessionIdCookie = new Cookie(request.obtainSessionCookieName(), sessionID);
        configureSessionCookie(request, jSessionIdCookie);
        return jSessionIdCookie;
    }

    /**
     * Checks if this request has a session cookie associated with specified session identifier.
     *
     * @param sessionID The session identifier to use for look-up
     * @return <code>true</code> if this request contains such a session cookie; otherwise <code>false</code>
     */
    private boolean hasSessionCookie(Request request, String sessionID) {
        Cookie[] cookies = request.getCookies();
        if (null == cookies || cookies.length <= 0) {
            return false;
        }

        String sessionCookieName = request.obtainSessionCookieName();
        for (Cookie cookie : cookies) {
            if (sessionCookieName.equals(cookie.getName()) && sessionID.equals(cookie.getValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove invalid JSession cookie used in the Request. Cookies are invalid when:
     *
     * @param invalidSessionId The invalid sessionId requested by the browser/cookie
     */
    private void removeInvalidSessionCookie(Request request, String invalidSessionId) {
        Cookie[] cookies = request.getCookies();
        if (null == cookies || cookies.length <= 0) {
            return;
        }

        String sessionCookieName = request.obtainSessionCookieName();
        Response response = request.response;
        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith(sessionCookieName)) {
                if (cookie.getValue().equals(invalidSessionId)) {
                    response.addCookie(createinvalidationCookie(cookie));
                    String domain = Cookies.extractDomainValue(cookie.getValue());
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
     * Returns pseudorandom positive long value.
     */
    private long generateRandomLong() {
        return (rnd.nextLong() & 0x7FFFFFFFFFFFFFFFL);
    }

}
