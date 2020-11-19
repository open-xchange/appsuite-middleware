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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.session.Session;

/**
 * {@link SessionMap} - The thread-safe map for session identifier mappings and more.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionMap {

    private final ConcurrentMap<String, SessionControl> sessionIdMap;
    private final ConcurrentMap<String, SessionControl> alternativeIdMap;

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
        sessionIdMap = new ConcurrentHashMap<String, SessionControl>(initialCapacity, 0.75f, 1);
        alternativeIdMap = new ConcurrentHashMap<String, SessionControl>(initialCapacity, 0.75f, 1);
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
    public SessionControl getBySessionId(final String sessionId) {
        return sessionIdMap.get(sessionId);
    }

    /**
     * Gets the session associated with specified alternative identifier.
     *
     * @param altId The alternative identifier
     * @return The associated session or <code>null</code> if absent
     */
    public SessionControl getByAlternativeId(final String altId) {
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
    public SessionControl putBySessionId(final String sessionId, final SessionControl session) {
        SessionControl prev = sessionIdMap.put(sessionId, session);
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
    public SessionControl putIfAbsentBySessionId(final String sessionId, final SessionControl session) {
        SessionControl prev = sessionIdMap.putIfAbsent(sessionId, session);
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
    public SessionControl removeBySessionId(final String sessionId) {
        SessionControl session = sessionIdMap.remove(sessionId);
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
     * Returns a {@link Collection} view of the sessions contained in this map.
     * <p>
     * The collection is <b>NOT</b> backed by the map.
     *
     * @return The {@link Collection} view of the sessions contained in this map
     */
    public Collection<SessionControl> values() {
        return new ArrayList<SessionControl>(sessionIdMap.values());
    }

    /**
     * Returns a {@link Collection} view of the session identifiers contained in this map.
     * <p>
     * The collection is <b>NOT</b> backed by the map.
     *
     * @return The {@link Collection} view of the sessions identifiers contained in this map
     */
    public ArrayList<String> keys() {
        return new ArrayList<String>(sessionIdMap.keySet());
    }

}
