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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.tools.servlet.http;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpSession;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link HttpSessionManagement} - Management for HTTP sessions
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpSessionManagement {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(HttpSessionManagement.class);

    private static final AtomicBoolean initialized = new AtomicBoolean();

    private static Map<String, HttpSessionWrapper> sessions;

    private static ScheduledTimerTask sessionRemover;

    /**
     * Initializes HTTP session management
     */
    static void init() {
        if (!initialized.get()) {
            synchronized (initialized) {
                if (!initialized.get()) {
                    sessions = new ConcurrentHashMap<String, HttpSessionWrapper>();
                    final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
                    if (null != timer) {
                        sessionRemover = timer.scheduleWithFixedDelay(new SessionRemover(), 100, 3600000);
                    }
                    initialized.set(true);
                }
            }
        }
    }

    /**
     * Resets HTTP session management
     */
    static void reset() {
        if (initialized.get()) {
            synchronized (initialized) {
                if (initialized.get()) {
                    sessions.clear();
                    sessions = null;
                    sessionRemover.cancel(false);
                    final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
                    if (null != timer) {
                        timer.purge();
                    }
                    sessionRemover = null;
                    initialized.set(false);
                }
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
     */
    public static void putHttpSession(final HttpSessionWrapper httpSession) {
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
     */
    public static HttpSession createAndGetHttpSession(final String uniqueId) {
        final HttpSessionWrapper httpSession;
        if (sessions.containsKey(uniqueId)) {
            httpSession = sessions.get(uniqueId);
        } else {
            httpSession = new HttpSessionWrapper(uniqueId);
            sessions.put(uniqueId, httpSession);
        }
        return httpSession;
    }

    /**
     * Creates a new HTTP session with given unique ID
     * 
     * @param uniqueId The unique ID to apply to HTTP session
     */
    public static void createHttpSession(final String uniqueId) {
        HttpSessionWrapper httpSession = sessions.get(uniqueId);
        if (null != httpSession) {
            return;
        }
        httpSession = new HttpSessionWrapper(uniqueId);
        sessions.put(uniqueId, httpSession);
    }

    /**
     * Creates a new HTTP session
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
    public static boolean isHttpSessionExpired(final HttpSessionWrapper httpSession) {
        return httpSession.getMaxInactiveInterval() > 0 ? ((System.currentTimeMillis() - httpSession.getLastAccessedTime()) / 1000) > httpSession.getMaxInactiveInterval() : false;
    }

    /**
     * Checks if HTTP session referenced by specified sessionId is valid
     * 
     * @param sessionId The HTTP session ID
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    public static boolean isHttpSessionValid(final String sessionId) {
        final HttpSessionWrapper httpSession = sessions.get(sessionId);
        return ((httpSession == null) || (!isHttpSessionExpired(httpSession)));
    }

    /**
     * Creates a unique ID
     * 
     * @return The unique ID
     */
    public static String getNewUniqueId() {
        final StringBuilder s = new StringBuilder(36).append(UUID.randomUUID());
        s.deleteCharAt(23);
        s.deleteCharAt(18);
        s.deleteCharAt(13);
        s.deleteCharAt(8);
        return s.toString();
    }

    private static final class SessionRemover implements Runnable {

        public SessionRemover() {
            super();
        }

        public void run() {
            try {
                for (final Iterator<Map.Entry<String, HttpSessionWrapper>> iter = sessions.entrySet().iterator(); iter.hasNext();) {
                    final Map.Entry<String, HttpSessionWrapper> entry = iter.next();
                    if (isHttpSessionExpired(entry.getValue())) {
                        entry.getValue().invalidate();
                        iter.remove();
                    }
                }
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

}
