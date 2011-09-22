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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.sessiond.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;

/**
 * {@link SessionContainer} - A thread-safe container for {@link Session} objects wrapped by a {@link SessionControl} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class SessionContainer {

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<String, SessionControl> sessionIdMap;

    private final Lock sessionIdMapLock = new ReentrantLock();

    private final ConcurrentMap<UserKey, Map<String, Object>> userSessions;

    SessionContainer() {
        super();
        sessionIdMap = new ConcurrentHashMap<String, SessionControl>();
        userSessions = new ConcurrentHashMap<UserKey, Map<String, Object>>();
    }

    /**
     * Gets the current number of sessions held by this container
     *
     * @return The current number of sessions held by this container
     */
    int size() {
        return sessionIdMap.size();
    }

    /**
     * Checks if this container contains an entry for specified session ID
     *
     * @param sessionId The session ID
     * @return <code>true</code> if this container contains an entry for specified session ID; otherwise <code>false</code>
     */
    boolean containsSessionId(final String sessionId) {
        return sessionIdMap.containsKey(sessionId);
    }

    /**
     * Checks if this container contains a session for specified user in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return <code>true</code> if this container contains an entry for specified user; otherwise <code>false</code>
     */
    boolean containsUser(final int userId, final int contextId) {
        return userSessions.containsKey(new UserKey(userId, contextId));
    }

    /**
     * Gets the number of sessions bound to specified user in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The number of sessions bound to specified user in specified context
     */
    int numOfUserSessions(final int userId, final int contextId) {
        final Map<String, Object> sessionIds = userSessions.get(new UserKey(userId, contextId));
        return null == sessionIds ? 0 : sessionIds.size();
    }

    /**
     * Gets the session bound to specified session ID.
     *
     * @param sessionId The session ID
     * @return The session bound to specified session ID, or <code>null</code> if there's no session for specified session ID.
     */
    SessionControl getSessionById(final String sessionId) {
        return sessionIdMap.get(sessionId);
    }

    /**
     * Gets the sessions bound to specified user ID and context ID.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The sessions bound to specified user ID and context ID
     */
    SessionControl[] getSessionsByUser(final int userId, final int contextId) {
        final Map<String, Object> sessionIds = userSessions.get(new UserKey(userId, contextId));
        if (null == sessionIds) {
            return new SessionControl[0];
        }
        final List<SessionControl> l = new ArrayList<SessionControl>(sessionIds.size());
        for (final String sessionId : sessionIds.keySet()) {
            l.add(sessionIdMap.get(sessionId));
        }
        return l.toArray(new SessionControl[sessionIds.size()]);
    }
    
    public SessionControl getAnySessionByUser(int userId, int contextId) {
        final Map<String, Object> sessionIds = userSessions.get(new UserKey(userId, contextId));
        for (final String sessionId : sessionIds.keySet()) {
            return sessionIdMap.get(sessionId);
        }
        return null;
    }


    /**
     * Wraps specified session by a newly created {@link SessionControl} object and puts it into this container
     *
     * @param session The session to put
     * @return The wrapping {@link SessionControl session control}.
     */
    SessionControl put(final SessionImpl session) throws OXException {
        final String sessionId = session.getSessionID();
        // Add session
        SessionControl sessionControl;
        sessionIdMapLock.lock();
        try {
            sessionControl = sessionIdMap.get(sessionId);
            if (null == sessionControl) {
                final SessionControl newSessionControl = new SessionControl(session);
                sessionControl = sessionIdMap.putIfAbsent(sessionId, newSessionControl);
                if (null == sessionControl) {
                    sessionControl = newSessionControl;
                } else {
                    final String login1 = sessionControl.getSession().getLogin();
                    final String login2 = session.getLogin();
                    throw SessionExceptionCodes.SESSIONID_COLLISION.create(login1, login2);
                }
            } else {
                final String login1 = sessionControl.getSession().getLogin();
                final String login2 = session.getLogin();
                throw SessionExceptionCodes.SESSIONID_COLLISION.create(login1, login2);
            }
        } finally {
            sessionIdMapLock.unlock();
        }
        // Add session ID to user-sessions-map
        final UserKey key = new UserKey(session.getUserId(), session.getContextId());
        Map<String, Object> sessionIds = userSessions.get(key);
        if (sessionIds == null) {
            final Map<String, Object> newSet = new ConcurrentHashMap<String, Object>();
            sessionIds = userSessions.putIfAbsent(key, newSet);
            if (null == sessionIds) {
                sessionIds = newSet;
            }
        }
        sessionIds.put(sessionId, PRESENT);
        return sessionControl;
    }

    /**
     * Puts specified {@link SessionControl} object into this container
     *
     * @param sessionControl The session control to put
     * @throws OXException
     */
    void putSessionControl(final SessionControl sessionControl) throws OXException {
        final Session session = sessionControl.getSession();
        final String sessionId = session.getSessionID();
        final SessionControl oldSessionControl = sessionIdMap.putIfAbsent(sessionId, sessionControl);
        if (null != oldSessionControl) {
            final String login1 = oldSessionControl.getSession().getLogin();
            final String login2 = sessionControl.getSession().getLogin();
            throw SessionExceptionCodes.SESSIONID_COLLISION.create(login1, login2);
        }
        final UserKey key = new UserKey(session.getUserId(), session.getContextId());
        Map<String, Object> sessionIds = userSessions.get(key);
        if (sessionIds == null) {
            final Map<String, Object> newSet = new ConcurrentHashMap<String, Object>();
            sessionIds = userSessions.putIfAbsent(key, newSet);
            if (null == sessionIds) {
                sessionIds = newSet;
            }
        }
        sessionIds.put(sessionId, PRESENT);
    }

    /**
     * Removes the session bound to specified session ID.
     *
     * @param sessionId The session Id
     * @return The {@link SessionControl session control} previously associated with specified session ID, or <code>null</code>.
     */
    SessionControl removeSessionById(final String sessionId) {
        final SessionControl sessionControl = sessionIdMap.remove(sessionId);
        if (sessionControl != null) {
            final Session session = sessionControl.getSession();
            final UserKey key = new UserKey(session.getUserId(), session.getContextId());
            final Map<String, Object> sessionIds = userSessions.get(key);
            sessionIds.remove(sessionId);
            if (sessionIds.isEmpty()) {
                userSessions.remove(key);
            }
        }
        return sessionControl;
    }

    /**
     * Removes the sessions bound to specified user ID and context ID.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The {@link SessionControl session controls} previously associated with specified user ID and context ID.
     */
    SessionControl[] removeSessionsByUser(final int userId, final int contextId) {
        final UserKey key = new UserKey(userId, contextId);
        final Map<String, Object> sessionIds = userSessions.remove(key);
        if (sessionIds == null) {
            return new SessionControl[0];
        }
        final List<SessionControl> l = new ArrayList<SessionControl>(sessionIds.size());
        for (final String sessionId : sessionIds.keySet()) {
            final SessionControl sc = sessionIdMap.remove(sessionId);
            if (sc != null) {
                l.add(sc);
            }
        }
        return l.toArray(new SessionControl[l.size()]);
    }

    /**
     * Returns a collection view of the {@link SessionControl} objects contained in this container. The collection is
     * <b><small>not</small></b> backed by the container, so changes to the map are not reflected in the container, but changes made to any
     * {@link SessionControl} object is reflected in this container.
     *
     * @return A collection view of the {@link SessionControl} objects contained in this container.
     */
    Collection<SessionControl> getSessionControls() {
        return new ArrayList<SessionControl>(sessionIdMap.values());
    }

}
