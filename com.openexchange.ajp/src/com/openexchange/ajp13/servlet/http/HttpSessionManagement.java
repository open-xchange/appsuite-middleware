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

package com.openexchange.ajp13.servlet.http;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link HttpSessionManagement} - Management for HTTP sessions
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpSessionManagement {

    protected static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(HttpSessionManagement.class));

    private static volatile Map<String, HttpSessionWrapper> sessions;

    private static volatile ScheduledTimerTask sessionRemover;

    private static volatile int maxActiveSessions = -1;

    /**
     * Initializes HTTP session management
     */
    static void init() {
        synchronized (HttpSessionManagement.class) {
            if (null == sessions) {
                sessions = new NonBlockingHashMap<String, HttpSessionWrapper>();
                final ConfigurationService service = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
                maxActiveSessions = null == service ? -1 : service.getIntProperty("com.openexchange.servlet.maxActiveSessions", 250000);
                final TimerService timer = AJPv13ServiceRegistry.getInstance().getService(TimerService.class);
                if (null != timer) {
                    sessionRemover = timer.scheduleWithFixedDelay(new SessionRemover(sessions), 300000, 300000); // Every 5 minutes
                }
            }
        }
    }

    /**
     * Resets HTTP session management
     */
    static void reset() {
        synchronized (HttpSessionManagement.class) {
            if (null != sessions) {
                sessions.clear();
                sessions = null;
                maxActiveSessions = -1;
                sessionRemover.cancel(false);
                final TimerService timer = AJPv13ServiceRegistry.getInstance().getService(TimerService.class);
                if (null != timer) {
                    timer.purge();
                }
                sessionRemover = null;
            }
        }
    }

    /**
     * Initializes a new {@link HttpSessionManagement}
     */
    private HttpSessionManagement() {
        super();
    }

    /**
     * Gets the HTTP session whose unique ID matches given <code>sessionId</code>.
     * <p>
     * Don't forget to touch last-accessed time stamp if returned by {@link HttpServletRequest#getSession()} via {@link HttpSessionWrapper#touch()}.
     *
     * @param sessionId The session ID
     * @return The HTTP session whose unique ID matches given <code>sessionId</code>.
     */
    public static HttpSessionWrapper getHttpSession(final String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Puts specified HTTP session into this management
     *
     * @param httpSession The HTTP session to add
     * @throws IllegalStateException If max. number of HTTP session is exceeded
     */
    public static void putHttpSession(final HttpSessionWrapper httpSession) {
        final int max = maxActiveSessions;
        if (max > 0 && sessions.size() >= max) {
            final String message = "Max. number of HTTP session (" + max + ") exceeded.";
            LOG.warn(message);
            throw new IllegalStateException(message);
        }
        sessions.put(httpSession.getId(), httpSession);
    }

    /**
     * Checks if this management contains a HTTP session whose unique ID matches given <code>sessionId</code>.
     *
     * @param sessionId The session ID
     * @return <code>true</code> if this management contains a HTTP session whose unique ID matches given <code>sessionId</code>, otherwise
     *         <code>false</code>.
     */
    public static boolean containsHttpSession(final String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * Removes HTTP session whose unique ID matches given <code>sessionId</code> .
     *
     * @param sessionId The session ID
     */
    public static void removeHttpSession(final String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * Creates and gets a new HTTP session with given unique ID
     *
     * @param uniqueId The unique ID to apply to HTTP session
     * @return The new HTTP session
     * @throws IllegalStateException If max. number of HTTP session is exceeded
     */
    public static HttpSession createAndGetHttpSession(final String uniqueId) {
        final HttpSessionWrapper httpSession;
        final Map<String, HttpSessionWrapper> sessions = HttpSessionManagement.sessions;
        if (sessions.containsKey(uniqueId)) {
            httpSession = sessions.get(uniqueId);
            httpSession.touch(); // Touch last-accessed time stamp
        } else {
            httpSession = new HttpSessionWrapper(uniqueId);
            final int max = maxActiveSessions;
            if (max > 0 && sessions.size() >= max) {
                final String message = "Max. number of HTTP session (" + max + ") exceeded.";
                LOG.warn(message);
                throw new IllegalStateException(message);
            }
            sessions.put(uniqueId, httpSession);
        }
        return httpSession;
    }

    /**
     * Creates a new HTTP session with given unique ID
     *
     * @param uniqueId The unique ID to apply to HTTP session
     * @throws IllegalStateException If max. number of HTTP session is exceeded
     */
    public static void createHttpSession(final String uniqueId) {
        final Map<String, HttpSessionWrapper> sessions = HttpSessionManagement.sessions;
        HttpSessionWrapper httpSession = sessions.get(uniqueId);
        if (null != httpSession) {
            return;
        }
        httpSession = new HttpSessionWrapper(uniqueId);
        final int max = maxActiveSessions;
        if (max > 0 && sessions.size() >= max) {
            final String message = "Max. number of HTTP session (" + max + ") exceeded.";
            LOG.warn(message);
            throw new IllegalStateException(message);
        }
        sessions.put(uniqueId, httpSession);
    }

    /**
     * Creates a new HTTP session
     *
     * @throws IllegalStateException If max. number of HTTP session is exceeded
     */
    public static void createHttpSession() {
        createHttpSession(getNewUniqueId());
    }

    /**
     * Checks if given HTTP session has expired; meaning its last accessed timestamp exceeds max. inactive interval
     *
     * @param httpSession The HTTP session to check
     * @return <code>true</code> if given HTTP session has expired; otherwise <code>false</code>
     */
    public static boolean isHttpSessionExpired(final HttpSession httpSession) {
        final int maxInactiveInterval = httpSession.getMaxInactiveInterval();
        if (maxInactiveInterval > 0) {
            return ((System.currentTimeMillis() - httpSession.getLastAccessedTime()) / 1000) > maxInactiveInterval;
        }
        /*
         * No inactive interval set
         */
        return false;
    }

    /**
     * Checks if HTTP session referenced by specified sessionId is valid
     *
     * @param sessionId The HTTP session ID
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    public static boolean isHttpSessionValid(final String sessionId) {
        final Map<String, HttpSessionWrapper> sessionMap = sessions;
        final HttpSessionWrapper httpSession = sessionMap.get(sessionId);
        if (null == httpSession) {
            /*
             * HTTP session must be present
             */
            return false;
        }
        if (isHttpSessionExpired(httpSession)) {
            /*
             * Expired
             */
            sessionMap.remove(sessionId);
            return false;
        }
        /*
         * Non-null and not expired
         */
        return true;
    }

    /**
     * Creates a unique ID
     *
     * @return The unique ID
     */
    public static String getNewUniqueId() {
        final com.openexchange.java.StringAllocator s = new com.openexchange.java.StringAllocator(36).append(UUID.randomUUID());
        s.deleteCharAt(23);
        s.deleteCharAt(18);
        s.deleteCharAt(13);
        s.deleteCharAt(8);
        return s.toString();
    }

    private static final class SessionRemover implements Runnable {

        private final Map<String, HttpSessionWrapper> sessionsMap;

        public SessionRemover(final Map<String, HttpSessionWrapper> sessionsMap) {
            super();
            this.sessionsMap = sessionsMap;
        }

        @Override
        public void run() {
            try {
                for (final Iterator<HttpSessionWrapper> iter = sessionsMap.values().iterator(); iter.hasNext();) {
                    final HttpSessionWrapper session = iter.next();
                    if (isHttpSessionExpired(session)) {
                        session.invalidate();
                        iter.remove();
                    }
                }
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

}
