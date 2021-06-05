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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.container.SessionControl;

/**
 * {@link SessionMap} - The thread-safe map for session identifier mappings and more.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionMap<S extends SessionControl> {

    private final ConcurrentMap<String, S> sessionIdMap;
    private final ConcurrentMap<String, S> alternativeIdMap;

    /**
     * Initializes a new {@link SessionMap}.
     */
    public SessionMap() {
        this(1024);
    }

    /**
     * Initializes a new {@link SessionMap}.
     *
     * @param initialCapacity The initial map's capacity
     */
    public SessionMap(final int initialCapacity) {
        super();
        sessionIdMap = new ConcurrentHashMap<String, S>(initialCapacity, 0.75f, 1);
        alternativeIdMap = new ConcurrentHashMap<String, S>(initialCapacity, 0.75f, 1);
    }

    // -------------------------------------------------------------------------------

    /**
     * Gets the map's size.
     *
     * @return The size
     */
    public int size() {
        return sessionIdMap.size();
    }

    // -------------------------------------------------------------------------------

    /**
     * Checks for presence of a session associated with given session identifier.
     *
     * @param sessionId The session identifier
     * @return <code>true</code> if such a session is present; otherwise <code>false</code>
     */
    public boolean containsBySessionId(final String sessionId) {
        return sessionIdMap.containsKey(sessionId);
    }

    /**
     * Checks for presence of a session associated with given alternative identifier.
     *
     * @param altId The alternative identifier
     * @return <code>true</code> if such a session is present; otherwise <code>false</code>
     */
    public boolean containsByAlternativeId(final String altId) {
        return alternativeIdMap.containsKey(altId);
    }

    // -------------------------------------------------------------------------------

    /**
     * Gets the session associated with specified session identifier.
     *
     * @param sessionId The session identifier
     * @return The associated session or <code>null</code> if absent
     */
    public S getBySessionId(final String sessionId) {
        return sessionIdMap.get(sessionId);
    }

    /**
     * Gets the session associated with specified alternative identifier.
     *
     * @param altId The alternative identifier
     * @return The associated session or <code>null</code> if absent
     */
    public S getByAlternativeId(final String altId) {
        return alternativeIdMap.get(altId);
    }

 // -------------------------------------------------------------------------------

    /**
     * Puts specified session into this map.
     *
     * @param sessionId The session identifier
     * @param session The session to put
     * @return The session already associated with given session identifier that has been replaced or <code>null</code> if nothing replaced
     */
    public S putBySessionId(final String sessionId, final S session) {
        S prev = sessionIdMap.put(sessionId, session);
        if (null != prev) {
            String prevAltId = (String) prev.getSession().getParameter(Session.PARAM_ALTERNATIVE_ID);
            if (null != prevAltId) {
                alternativeIdMap.remove(prevAltId);
            }
        }

        String altId = (String) session.getSession().getParameter(Session.PARAM_ALTERNATIVE_ID);
        if (null != altId) {
            alternativeIdMap.put(altId, session);
        }

        return prev;
    }

    // -------------------------------------------------------------------------------

    /**
     * Puts specified session into this map if no session is already associated with given session identifier.
     *
     * @param sessionId The session identifier
     * @param session The session to put
     * @return The session already associated with given session identifier or <code>null</code> on successful put
     */
    public S putIfAbsentBySessionId(final String sessionId, final S session) {
        S prev = sessionIdMap.putIfAbsent(sessionId, session);
        if (null != prev) {
            return prev;
        }

        String altId = (String) session.getSession().getParameter(Session.PARAM_ALTERNATIVE_ID);
        if (null != altId) {
            alternativeIdMap.put(altId, session);
        }

        return null;
    }

    // -------------------------------------------------------------------------------

    /**
     * Removes the session associated with specified session identifier.
     *
     * @param sessionId The session identifier
     * @return The possibly removed session or <code>null</code>
     */
    public S removeBySessionId(final String sessionId) {
        S session = sessionIdMap.remove(sessionId);
        if (null != session) {
            final String altId = (String) session.getSession().getParameter(Session.PARAM_ALTERNATIVE_ID);
            if (null != altId) {
                alternativeIdMap.remove(altId);
            }
        }
        return session;
    }

    // -------------------------------------------------------------------------------

    /**
     * Returns an unmodifable {@link Collection} view of the sessions contained in this map.
     *
     * @return The unmodifable {@link Collection} view of the sessions contained in this map
     */
    public Collection<S> values() {
        return Collections.unmodifiableCollection(sessionIdMap.values());
    }

    /**
     * Returns an unmodifable {@link Collection} view of the session identifiers contained in this map.
     *
     * @return The unmodifable {@link Collection} view of the sessions identifiers contained in this map
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(sessionIdMap.keySet());
    }

}
