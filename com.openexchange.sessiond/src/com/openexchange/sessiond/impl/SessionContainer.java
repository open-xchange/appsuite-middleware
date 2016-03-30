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

package com.openexchange.sessiond.impl;

import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    private final SessionMap sessionMap;
    private final Lock sessionIdMapLock = new ReentrantLock();
    private final ConcurrentTIntObjectHashMap<ConcurrentTIntObjectHashMap<Map<String, Object>>> userSessions;

    /**
     * Initializes a new {@link SessionContainer}.
     */
    protected SessionContainer() {
        super();
        sessionMap = new SessionMap();
        userSessions = new ConcurrentTIntObjectHashMap<ConcurrentTIntObjectHashMap<Map<String, Object>>>(32);
    }

    /**
     * Gets the current number of sessions held by this container
     *
     * @return The current number of sessions held by this container
     */
    protected int size() {
        return sessionMap.size();
    }

    /**
     * Checks if this container contains an entry for specified session ID
     *
     * @param sessionId The session ID
     * @return <code>true</code> if this container contains an entry for specified session ID; otherwise <code>false</code>
     */
    protected boolean containsSessionId(final String sessionId) {
        return sessionMap.containsBySessionId(sessionId);
    }

    /**
     * Checks if this container contains an entry for specified alternative identifier.
     *
     * @param altId The alternative identifier
     * @return <code>true</code> if this container contains an entry for specified alternative identifier; otherwise <code>false</code>
     */
    protected boolean containsAlternativeId(final String altId) {
        return sessionMap.containsByAlternativeId(altId);
    }

    /**
     * Checks if this container contains a session for specified user in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return <code>true</code> if this container contains an entry for specified user; otherwise <code>false</code>
     */
    protected boolean containsUser(final int userId, final int contextId) {
        final ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
        return null != map && map.contains(userId);
    }

    /**
     * Gets the number of sessions bound to specified user in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The number of sessions bound to specified user in specified context
     */
    protected int numOfUserSessions(final int userId, final int contextId) {
        final ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
        if (null == map) {
            return 0;
        }
        final Map<String, Object> sessionIds = map.get(userId);
        return null == sessionIds ? 0 : sessionIds.size();
    }

    /**
     * Gets the session bound to specified session ID.
     *
     * @param sessionId The session ID
     * @return The session bound to specified session ID, or <code>null</code> if there's no session for specified session ID.
     */
    protected SessionControl getSessionById(final String sessionId) {
        return sessionMap.getBySessionId(sessionId);
    }

    /**
     * Gets the session bound to specified alternative identifier.
     *
     * @param altId The alternative identifier
     * @return The session bound to specified alternative identifier, or <code>null</code> if there's no session for specified alternative identifier.
     */
    protected SessionControl getSessionByAlternativeId(final String altId) {
        return sessionMap.getByAlternativeId(altId);
    }

    /**
     * Gets the sessions bound to specified user ID and context ID.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The sessions bound to specified user ID and context ID
     */
    protected SessionControl[] getSessionsByUser(final int userId, final int contextId) {
        final ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
        if (null == map) {
            return new SessionControl[0];
        }
        final Map<String, Object> sessionIds = map.get(userId);
        if (null == sessionIds) {
            return new SessionControl[0];
        }
        final List<SessionControl> l = new ArrayList<SessionControl>(sessionIds.size());
        for (final String sessionId : sessionIds.keySet()) {
            l.add(sessionMap.getBySessionId(sessionId));
        }
        return l.toArray(new SessionControl[sessionIds.size()]);
    }

    /**
     * Gets any session associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return An arbitrary session or <code>null</code>
     */
    public SessionControl getAnySessionByUser(final int userId, final int contextId) {
        final ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
        if (null == map) {
            return null;
        }
        final Map<String, Object> sessionIds = map.get(userId);
        if (sessionIds == null) {
            return null;
        }
        for (final String sessionId : sessionIds.keySet()) {
            return sessionMap.getBySessionId(sessionId);
        }
        return null;
    }


    /**
     * Wraps specified session by a newly created {@link SessionControl} object and puts it into this container
     *
     * @param session The session to put
     * @param addIfAbsent <code>true</code> to perform an add-if-absent operation; otherwise <code>false</code>
     * @return The wrapping {@link SessionControl session control}.
     */
    protected SessionControl put(final SessionImpl session, final boolean addIfAbsent) throws OXException {
        final String sessionId = session.getSessionID();
        // Add session
        SessionControl sessionControl;
        sessionIdMapLock.lock();
        try {
            final SessionControl newSessionControl = new SessionControl(session);
            sessionControl = sessionMap.putIfAbsentBySessionId(sessionId, newSessionControl);
            if (null == sessionControl) {
                // Insert succeeded
                sessionControl = newSessionControl;
            } else {
                // Another session associated with that session identifier
                if (addIfAbsent) {
                    return sessionControl;
                }
                final SessionImpl ole = sessionControl.getSession();
                if (!ole.consideredEqual(session)) {
                    ole.logDiff(session, org.slf4j.LoggerFactory.getLogger(SessionContainer.class));
                    throw SessionExceptionCodes.SESSIONID_COLLISION.create(ole.getLogin(), session.getLogin());
                }
            }
        } finally {
            sessionIdMapLock.unlock();
        }
        // Add session ID to user-sessions-map
        final int contextId = session.getContextId();
        ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
        if (null == map) {
            final ConcurrentTIntObjectHashMap<Map<String, Object>> newMap = new ConcurrentTIntObjectHashMap<Map<String,Object>>(32);
            map = userSessions.putIfAbsent(contextId, newMap);
            if (null == map) {
                map = newMap;
            }
        }
        final int userId = session.getUserId();
        Map<String, Object> sessionIds = map.get(userId);
        if (sessionIds == null) {
            final Map<String, Object> newSet = new ConcurrentHashMap<String, Object>();
            sessionIds = map.putIfAbsent(userId, newSet);
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
     * @throws OXException If put operation fails because of conflict with an existing session
     */
    protected void putSessionControl(final SessionControl sessionControl) throws OXException {
        if (null == sessionControl) {
            return;
        }
        final Session session = sessionControl.getSession();
        final String sessionId = session.getSessionID();
        final SessionControl oldSessionControl = sessionMap.putIfAbsentBySessionId(sessionId, sessionControl);
        if (null != oldSessionControl) {
            final String login1 = oldSessionControl.getSession().getLogin();
            final String login2 = sessionControl.getSession().getLogin();
            throw SessionExceptionCodes.SESSIONID_COLLISION.create(login1, login2);
        }
        // Add session ID to user-sessions-map
        final int contextId = session.getContextId();
        ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
        if (null == map) {
            final ConcurrentTIntObjectHashMap<Map<String, Object>> newMap = new ConcurrentTIntObjectHashMap<Map<String,Object>>(32);
            map = userSessions.putIfAbsent(contextId, newMap);
            if (null == map) {
                map = newMap;
            }
        }
        final int userId = session.getUserId();
        Map<String, Object> sessionIds = map.get(userId);
        if (sessionIds == null) {
            final Map<String, Object> newSet = new ConcurrentHashMap<String, Object>();
            sessionIds = map.putIfAbsent(userId, newSet);
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
     * @return The {@link SessionControl session control} previously associated with specified session identifier, or <code>null</code>.
     */
    protected SessionControl removeSessionById(final String sessionId) {
        if (null == sessionId) {
            return null;
        }
        final SessionControl sessionControl = sessionMap.removeBySessionId(sessionId);
        if (sessionControl != null) {
            final Session session = sessionControl.getSession();
            final int contextId = session.getContextId();
            ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
            if (null == map) {
                return sessionControl;
            }
            if (map.isEmpty()) {
                final Lock l = userSessions.getReadWriteLock().writeLock();
                l.lock();
                try {
                    map = userSessions.get(contextId);
                    if (null == map) {
                        return sessionControl;
                    }
                    if (map.isEmpty()) {
                        userSessions.remove(contextId);
                        return sessionControl;
                    }
                } finally {
                    l.unlock();
                }
            }
            final int userId = session.getUserId();
            Map<String, Object> sessionIds = map.get(userId);
            if (sessionIds == null) {
                return sessionControl;
            }
            sessionIds.remove(sessionId);
            if (sessionIds.isEmpty()) {
                final Lock l = map.getReadWriteLock().writeLock();
                l.lock();
                try {
                    sessionIds = map.get(userId);
                    if (null != sessionIds && sessionIds.isEmpty()) {
                        map.remove(userId);
                    }
                } finally {
                    l.unlock();
                }
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
    protected SessionControl[] removeSessionsByUser(final int userId, final int contextId) {
        ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
        if (null == map) {
            return new SessionControl[0];
        }
        if (map.isEmpty()) {
            final Lock l = userSessions.getReadWriteLock().writeLock();
            l.lock();
            try {
                map = userSessions.get(contextId);
                if (null == map) {
                    return new SessionControl[0];
                }
                if (map.isEmpty()) {
                    userSessions.remove(contextId);
                    return new SessionControl[0];
                }
            } finally {
                l.unlock();
            }
        }
        final Map<String, Object> sessionIds = map.remove(userId);
        if (sessionIds == null) {
            return new SessionControl[0];
        }
        if (sessionIds.isEmpty()) {
            return new SessionControl[0];
        }
        final List<SessionControl> l = new ArrayList<SessionControl>(sessionIds.size());
        for (final String sessionId : sessionIds.keySet()) {
            final SessionControl sc = sessionMap.removeBySessionId(sessionId);
            if (sc != null) {
                l.add(sc);
            }
        }
        return l.toArray(new SessionControl[l.size()]);
    }

    /**
     * Removes the sessions bound to specified context ID.
     *
     * @param contextId The context ID
     * @return The {@link SessionControl session controls} previously associated with specified user ID and context ID.
     */
    protected SessionControl[] removeSessionsByContext(final int contextId) {
        final ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.remove(contextId);
        if (null == map) {
            return new SessionControl[0];
        }
        if (map.isEmpty()) {
            return new SessionControl[0];
        }
        final List<SessionControl> l = new ArrayList<SessionControl>(128);
        final SessionMap sessionMap = this.sessionMap;
        map.forEachValue(new TObjectProcedure<Map<String, Object>>() {

            @Override
            public boolean execute(final Map<String, Object> sessionIds) {
                for (final String sessionId : sessionIds.keySet()) {
                    final SessionControl sc = sessionMap.removeBySessionId(sessionId);
                    if (sc != null) {
                        l.add(sc);
                    }
                }
                return true;
            }
        });
        return l.toArray(new SessionControl[l.size()]);
    }

    /**
     * Removes the sessions bound to the given contextIds.
     *
     * @param contextId Set of context ids
     * @return The {@link SessionControl session controls} previously associated with specified user ID and context ID.
     */
    protected List<SessionControl> removeSessionsByContexts(final Set<Integer> contextIds) {
        List<SessionControl> removedSessionsByContexts = new ArrayList<SessionControl>();
        for (int contextId : contextIds) {
            SessionControl[] removeSessionsByContext = this.removeSessionsByContext(contextId);
            removedSessionsByContexts.addAll(Arrays.asList(removeSessionsByContext));
        }
        return removedSessionsByContexts;
    }

    /**
     * Checks if there is any session for given context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if there is such a session; otherwise <code>false</code>
     */
    protected boolean hasForContext(final int contextId) {
        final ConcurrentTIntObjectHashMap<Map<String, Object>> map = userSessions.get(contextId);
        return null != map && !map.isEmpty();
    }

    /**
     * Returns a collection view of the {@link SessionControl} objects contained in this container. The collection is
     * <b><small>not</small></b> backed by the container, so changes to the map are not reflected in the container, but changes made to any
     * {@link SessionControl} object is reflected in this container.
     *
     * @return A collection view of the {@link SessionControl} objects contained in this container.
     */
    protected Collection<SessionControl> getSessionControls() {
        return sessionMap.values();
    }

    /**
     * Returns a collection view of the session identifiers contained in this container. The collection is <b><small>not</small></b> 
     * backed by the container, so changes to the map are not reflected in the container.
     *
     * @return A collection view of the session identifiers contained in this container.
     */
    protected Collection<String> getSessionIDs() {
        return sessionMap.keys();
    }

}
