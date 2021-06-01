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

package com.openexchange.sessiond.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.impl.SessionImpl;
import com.openexchange.sessiond.impl.container.ShortTermSessionControl;

/**
 * {@link SessionContainer} - A thread-safe container for {@link Session} objects wrapped by a {@link ShortTermSessionControl} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionContainer {

    private static final Object PRESENT = new Object();

    private final SessionMap<ShortTermSessionControl> sessionMap;
    private final ConcurrentMap<Integer, ConcurrentMap<Integer, Map<String, Object>>> userSessions;

    /**
     * Initializes a new {@link SessionContainer}.
     */
    public SessionContainer() {
        super();
        sessionMap = new SessionMap<ShortTermSessionControl>();
        userSessions = new ConcurrentHashMap<>(32, 0.9F, 1);
    }

    /**
     * Gets the current number of sessions held by this container
     *
     * @return The current number of sessions held by this container
     */
    public int size() {
        return sessionMap.size();
    }

    /**
     * Checks if this container contains an entry for specified session identifier
     *
     * @param sessionId The session identifier
     * @return <code>true</code> if this container contains an entry for specified session identifier; otherwise <code>false</code>
     */
    public boolean containsSessionId(final String sessionId) {
        return sessionMap.containsBySessionId(sessionId);
    }

    /**
     * Checks if this container contains an entry for specified alternative identifier.
     *
     * @param altId The alternative identifier
     * @return <code>true</code> if this container contains an entry for specified alternative identifier; otherwise <code>false</code>
     */
    public boolean containsAlternativeId(final String altId) {
        return sessionMap.containsByAlternativeId(altId);
    }

    /**
     * Checks if this container contains a session for specified user in specified context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if this container contains an entry for specified user; otherwise <code>false</code>
     */
    public boolean containsUser(final int userId, final int contextId) {
        final ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(Integer.valueOf(contextId));
        if (null == map) {
            return false;
        }

        Map<String, Object> sessionIds = map.get(Integer.valueOf(userId));
        return null != sessionIds && !sessionIds.isEmpty();
    }

    /**
     * Gets the number of sessions bound to specified user in specified context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The number of sessions bound to specified user in specified context
     */
    public int numOfUserSessions(final int userId, final int contextId) {
        final ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(Integer.valueOf(contextId));
        if (null == map) {
            return 0;
        }
        final Map<String, Object> sessionIds = map.get(Integer.valueOf(userId));
        return null == sessionIds ? 0 : sessionIds.size();
    }

    /**
     * Gets the session bound to specified session identifier.
     *
     * @param sessionId The session identifier
     * @return The session bound to specified session identifier, or <code>null</code> if there's no session for specified session identifier.
     */
    public ShortTermSessionControl getSessionById(final String sessionId) {
        return sessionMap.getBySessionId(sessionId);
    }

    /**
     * Gets the session bound to specified alternative identifier.
     *
     * @param altId The alternative identifier
     * @return The session bound to specified alternative identifier, or <code>null</code> if there's no session for specified alternative identifier.
     */
    public ShortTermSessionControl getSessionByAlternativeId(final String altId) {
        return sessionMap.getByAlternativeId(altId);
    }

    /**
     * Gets the sessions bound to specified user identifier and context identifier.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The sessions bound to specified user identifier and context identifier
     */
    public List<ShortTermSessionControl> getSessionsByUser(final int userId, final int contextId) {
        ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(Integer.valueOf(contextId));
        if (null == map) {
            return Collections.emptyList();
        }

        Map<String, Object> sessionIds = map.get(Integer.valueOf(userId));
        if (null == sessionIds) {
            return Collections.emptyList();
        }

        List<ShortTermSessionControl> sessions = new ArrayList<ShortTermSessionControl>(sessionIds.size());
        Set<String> idsToRemove = null;
        for (final String sessionId : sessionIds.keySet()) {
            ShortTermSessionControl control = sessionMap.getBySessionId(sessionId);
            if (null == control) {
                // Apparently such a session does no more exist
                if (null == idsToRemove) {
                    idsToRemove = new HashSet<>(2);
                }
                idsToRemove.add(sessionId);
            } else {
                sessions.add(control);
            }
        }
        if (null != idsToRemove) {
            for (String sessionIdToRemove : idsToRemove) {
                sessionIds.remove(sessionIdToRemove);
            }
        }
        return sessions;
    }

    /**
     * Gets any session associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return An arbitrary session or <code>null</code>
     */
    public ShortTermSessionControl getAnySessionByUser(final int userId, final int contextId) {
        final ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(Integer.valueOf(contextId));
        if (null == map) {
            return null;
        }
        final Map<String, Object> sessionIds = map.get(Integer.valueOf(userId));
        if (sessionIds == null) {
            return null;
        }
        for (final String sessionId : sessionIds.keySet()) {
            return sessionMap.getBySessionId(sessionId);
        }
        return null;
    }


    /**
     * Wraps specified session by a newly created {@link ShortTermSessionControl} object and puts it into this container
     *
     * @param session The session to put
     * @param addIfAbsent <code>true</code> to perform an add-if-absent operation; otherwise <code>false</code>
     * @return The wrapping {@link ShortTermSessionControl session control}.
     */
    public ShortTermSessionControl put(final SessionImpl session, final boolean addIfAbsent) throws OXException {
        final String sessionId = session.getSessionID();
        // Add session
        ShortTermSessionControl sessionControl;
        {
            final ShortTermSessionControl newSessionControl = new ShortTermSessionControl(session);
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
        }
        // Add session identifier to user-sessions-map
        Integer iContextId = Integer.valueOf(session.getContextId());
        ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(iContextId);
        if (null == map) {
            ConcurrentMap<Integer, Map<String, Object>> newMap = new ConcurrentHashMap<Integer, Map<String,Object>>(32, 0.9F, 1);
            map = userSessions.putIfAbsent(iContextId, newMap);
            if (null == map) {
                map = newMap;
            }
        }

        Integer iUserId = Integer.valueOf(session.getUserId());
        Map<String, Object> sessionIds = map.get(iUserId);
        if (sessionIds == null) {
            Map<String, Object> newSet = new ConcurrentHashMap<String, Object>(16, 0.9F, 1);
            sessionIds = map.putIfAbsent(iUserId, newSet);
            if (null == sessionIds) {
                sessionIds = newSet;
            }
        }
        sessionIds.put(sessionId, PRESENT);
        return sessionControl;
    }

    /**
     * Puts specified {@link ShortTermSessionControl} object into this container
     *
     * @param sessionControl The session control to put
     * @throws OXException If put operation fails because of conflict with an existing session
     */
    public void putSessionControl(final ShortTermSessionControl sessionControl) throws OXException {
        if (null == sessionControl) {
            return;
        }
        final Session session = sessionControl.getSession();
        final String sessionId = session.getSessionID();
        final ShortTermSessionControl oldSessionControl = sessionMap.putIfAbsentBySessionId(sessionId, sessionControl);
        if (null != oldSessionControl) {
            final String login1 = oldSessionControl.getSession().getLogin();
            final String login2 = sessionControl.getSession().getLogin();
            throw SessionExceptionCodes.SESSIONID_COLLISION.create(login1, login2);
        }
        // Add session identifier to user-sessions-map
        Integer iContextId = Integer.valueOf(session.getContextId());
        ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(iContextId);
        if (null == map) {
            ConcurrentMap<Integer, Map<String, Object>> newMap = new ConcurrentHashMap<Integer, Map<String,Object>>(32, 0.9F, 1);
            map = userSessions.putIfAbsent(iContextId, newMap);
            if (null == map) {
                map = newMap;
            }
        }

        Integer iUserId = Integer.valueOf(session.getUserId());
        Map<String, Object> sessionIds = map.get(iUserId);
        if (sessionIds == null) {
            Map<String, Object> newSet = new ConcurrentHashMap<String, Object>(16, 0.9F, 1);
            sessionIds = map.putIfAbsent(iUserId, newSet);
            if (null == sessionIds) {
                sessionIds = newSet;
            }
        }
        sessionIds.put(sessionId, PRESENT);
    }

    /**
     * Removes the session bound to specified session identifier.
     *
     * @param sessionId The session Id
     * @return The {@link ShortTermSessionControl session control} previously associated with specified session identifier, or <code>null</code>.
     */
    public ShortTermSessionControl removeSessionById(final String sessionId) {
        if (null == sessionId) {
            return null;
        }

        ShortTermSessionControl sessionControl = sessionMap.removeBySessionId(sessionId);
        if (sessionControl == null) {
            return null;
        }

        Integer iContextId = Integer.valueOf(sessionControl.getContextId());
        ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(iContextId);
        if (null != map) {
            Integer iUserId = Integer.valueOf(sessionControl.getUserId());
            Map<String, Object> sessionIds = map.get(iUserId);
            if (sessionIds != null) {
                sessionIds.remove(sessionId);
            }
        }

        return sessionControl;
    }

    /**
     * Removes the sessions bound to specified user identifier and context identifier.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The {@link ShortTermSessionControl session controls} previously associated with specified user identifier and context identifier.
     */
    public List<ShortTermSessionControl> removeSessionsByUser(final int userId, final int contextId) {
        Integer iContextId = Integer.valueOf(contextId);
        ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(iContextId);
        if (null == map) {
            return Collections.emptyList();
        }

        Integer iUserId = Integer.valueOf(userId);
        Map<String, Object> sessionIds = map.remove(iUserId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShortTermSessionControl> l = new ArrayList<ShortTermSessionControl>(sessionIds.size());
        for (String sessionId : sessionIds.keySet()) {
            ShortTermSessionControl sc = sessionMap.removeBySessionId(sessionId);
            if (sc != null) {
                l.add(sc);
            }
        }
        return l;
    }

    /**
     * Removes the sessions bound to specified context identifier.
     *
     * @param contextId The context identifier
     * @return The {@link ShortTermSessionControl session controls} previously associated with specified user identifier and context identifier.
     */
    public List<ShortTermSessionControl> removeSessionsByContext(final int contextId) {
        Integer iContextId = Integer.valueOf(contextId);
        ConcurrentMap<Integer, Map<String, Object>> map = userSessions.remove(iContextId);
        if (null == map || map.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShortTermSessionControl> l = new ArrayList<ShortTermSessionControl>(128);
        SessionMap<ShortTermSessionControl> sessionMap = this.sessionMap;
        for (Map<String, Object> sessionIds : map.values()) {
            for (String sessionId : sessionIds.keySet()) {
                ShortTermSessionControl sc = sessionMap.removeBySessionId(sessionId);
                if (sc != null) {
                    l.add(sc);
                }
            }
        }
        return l;
    }

    /**
     * Removes the sessions bound to the given contextIds.
     *
     * @param contextIds The context identifiers to remove
     * @return The {@link ShortTermSessionControl session controls} previously associated with specified user identifier and context identifier.
     */
    public List<ShortTermSessionControl> removeSessionsByContexts(final Set<Integer> contextIds) {
        List<ShortTermSessionControl> removedSessionsByContexts = new ArrayList<ShortTermSessionControl>();
        for (int contextId : contextIds) {
            removedSessionsByContexts.addAll(this.removeSessionsByContext(contextId));
        }
        return removedSessionsByContexts;
    }

    /**
     * Checks if there is any session for given context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if there is such a session; otherwise <code>false</code>
     */
    public boolean hasForContext(final int contextId) {
        ConcurrentMap<Integer, Map<String, Object>> map = userSessions.get(Integer.valueOf(contextId));
        if (null == map || map.isEmpty()) {
            return false;
        }

        for (Map<String, Object> sessionIds : map.values()) {
            if (false == sessionIds.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a collection view of the {@link ShortTermSessionControl} objects contained in this container. The collection is
     * <b><small>not</small></b> backed by the container, so changes to the map are not reflected in the container, but changes made to any
     * {@link ShortTermSessionControl} object is reflected in this container.
     *
     * @return A collection view of the {@link ShortTermSessionControl} objects contained in this container.
     */
    public Collection<ShortTermSessionControl> getSessionControls() {
        return sessionMap.values();
    }

    /**
     * Returns a collection view of the session identifiers contained in this container. The collection is <b><small>not</small></b>
     * backed by the container, so changes to the map are not reflected in the container.
     *
     * @return A collection view of the session identifiers contained in this container.
     */
    public Collection<String> getSessionIDs() {
        return sessionMap.keys();
    }

}
