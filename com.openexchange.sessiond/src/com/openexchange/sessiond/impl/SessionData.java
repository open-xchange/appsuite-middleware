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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * Object handling the multi threaded access to session container. Excessive locking is used to secure container data structures.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class SessionData {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionData.class);

    private final int maxSessions;
    private final long randomTokenTimeout;
    private final boolean autoLogin;
    private final Map<String, String> randoms;

    private final ArrayList<SessionContainer> sessionList;
    private final Lock rlock;
    private final Lock wlock;

    /**
     * The LongTermUserGuardian contains an entry for a given UserKey if the longTermList contains a session for the user
     * <p>
     * This is used to guard against potentially slow serial searches of the long term sessions
     */
    private final UserRefCounter longTermUserGuardian = new UserRefCounter();

    private final ArrayList<SessionMap> longTermList;
    private final Lock wlongTermLock;
    private final Lock rlongTermLock;

    /**
     * Map to remember if there is already a task that should move the session to the first container.
     */
    private final ConcurrentMap<String, Move2FirstContainerTask> tasks = new ConcurrentHashMap<String, Move2FirstContainerTask>();

    private final AtomicReference<ThreadPoolService> threadPoolService;
    private final AtomicReference<TimerService> timerService;

    protected final Map<String, ScheduledTimerTask> removers = new ConcurrentHashMap<String, ScheduledTimerTask>();

    /**
     * Initializes a new {@link SessionData}.
     *
     * @param containerCount The container count for short-term sessions
     * @param maxSessions The max. number of total sessions
     * @param randomTokenTimeout The timeout for random tokens
     * @param longTermContainerCount The container count for long-term sessions
     * @param autoLogin Whether auto-login is enabled or not
     */
    SessionData(int containerCount, int maxSessions, long randomTokenTimeout, int longTermContainerCount, boolean autoLogin) {
        super();
        threadPoolService = new AtomicReference<ThreadPoolService>();
        timerService = new AtomicReference<TimerService>();
        this.maxSessions = maxSessions;
        this.randomTokenTimeout = randomTokenTimeout;
        this.autoLogin = autoLogin;

        randoms = new ConcurrentHashMap<String, String>();

        ReadWriteLock shortTermLock = new ReentrantReadWriteLock(true);
        rlock = shortTermLock.readLock();
        wlock = shortTermLock.writeLock();

        sessionList = new ArrayList<SessionContainer>(containerCount);
        for (int i = containerCount; i-- > 0;) {
            sessionList.add(new SessionContainer());
        }

        ReadWriteLock longTermLock = new ReentrantReadWriteLock(true);
        wlongTermLock = longTermLock.writeLock();
        rlongTermLock = longTermLock.readLock();

        longTermList = new ArrayList<SessionMap>(longTermContainerCount);
        for (int i = longTermContainerCount; i-- > 0;) {
            longTermList.add(new SessionMap(256));
        }
    }

    void clear() {
        wlock.lock();
        try {
            sessionList.clear();
            randoms.clear();
        } finally {
            wlock.unlock();
        }
        wlongTermLock.lock();
        try {
            longTermList.clear();
            longTermUserGuardian.clear();
        } finally {
            wlongTermLock.unlock();
        }
    }

    /**
     * Rotates the session containers. A new slot is added to head of each queue, while the last one is removed.
     *
     * @return The removed sessions
     */
    List<SessionControl> rotateShort() {
        // A write access to lists
        wlock.lock();
        try {
            List<SessionControl> removedSessions = new LinkedList<SessionControl>(sessionList.remove(sessionList.size() - 1).getSessionControls());
            sessionList.add(0, new SessionContainer());

            if (autoLogin && false == removedSessions.isEmpty()) {
                List<SessionControl> transientSessions = null;

                wlongTermLock.lock();
                try {
                    SessionMap first = longTermList.get(0);
                    for (Iterator<SessionControl> it = removedSessions.iterator(); it.hasNext();) {
                        final SessionControl control = it.next();
                        final SessionImpl session = control.getSession();
                        if (false == session.isTransient()) {
                            // A regular, non-transient session
                            first.putBySessionId(session.getSessionID(), control);
                            longTermUserGuardian.add(session.getUserId(), session.getContextId());
                        } else {
                            // A transient session -- do not move to long-term container
                            it.remove();
                            if (null == transientSessions) {
                                transientSessions = new LinkedList<SessionControl>();
                            }
                            transientSessions.add(control);
                        }
                    }
                } finally {
                    wlongTermLock.unlock();
                }

                if (null != transientSessions) {
                    SessionHandler.postContainerRemoval(transientSessions, true);
                }
            }

            return removedSessions;
        } finally {
            wlock.unlock();
        }
    }

    List<SessionControl> rotateLongTerm() {
        wlongTermLock.lock();
        try {
            longTermList.add(0, new SessionMap(256));
            final List<SessionControl> retval = new LinkedList<SessionControl>(longTermList.remove(longTermList.size() - 1).values());
            for (final SessionControl sessionControl : retval) {
                final SessionImpl session = sessionControl.getSession();
                longTermUserGuardian.remove(session.getUserId(), session.getContextId());
            }
            return retval;
        } finally {
            wlongTermLock.unlock();
        }
    }

    /**
     * Checks if given user in specified context has an active session kept in session container(s)
     *
     * @param userId The user ID
     * @param contextId The user's context ID
     * @param includeLongTerm <code>true</code> to also lookup the long term sessions, <code>false</code>, otherwise
     * @return <code>true</code> if given user in specified context has an active session; otherwise <code>false</code>
     */
    boolean isUserActive(int userId, int contextId, boolean includeLongTerm) {
        // A read-only access to session list
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                if (container.containsUser(userId, contextId)) {
                    return true;
                }
            }
        } finally {
            rlock.unlock();
        }

        // No need to check long-term container
        if (!includeLongTerm) {
            return false;
        }

        // Check long-term container, too
        rlongTermLock.lock();
        try {
            return hasLongTermSession(userId, contextId);
        } finally {
            rlongTermLock.unlock();
        }
    }

    private final boolean hasLongTermSession(final int userId, final int contextId) {
        return this.longTermUserGuardian.contains(userId, contextId);
    }

    private final boolean hasLongTermSession(final int contextId) {
        return this.longTermUserGuardian.contains(contextId);
    }

    SessionControl[] removeUserSessions(final int userId, final int contextId) {
        // Removing sessions is a write operation.
        final List<SessionControl> retval = new LinkedList<SessionControl>();
        wlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                retval.addAll(Arrays.asList(container.removeSessionsByUser(userId, contextId)));
            }
            for (final SessionControl control : retval) {
                unscheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID());
            }
        } finally {
            wlock.unlock();
        }
        wlongTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return retval.toArray(new SessionControl[retval.size()]);
            }
            for (final SessionMap longTerm : longTermList) {
                final Iterator<SessionControl> iter = longTerm.values().iterator();
                while (iter.hasNext()) {
                    final SessionControl control = iter.next();
                    final Session session = control.getSession();
                    if ((session.getContextId() == contextId) && (session.getUserId() == userId)) {
                        longTerm.removeBySessionId(session.getSessionID());
                        longTermUserGuardian.remove(userId, contextId);
                        retval.add(control);
                    }
                }
            }
        } finally {
            wlongTermLock.unlock();
        }
        return retval.toArray(new SessionControl[retval.size()]);
    }

    List<SessionControl> removeContextSessions(final int contextId) {
        // Removing sessions is a write operation.
        final List<SessionControl> list = new LinkedList<SessionControl>();
        wlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                list.addAll(Arrays.asList(container.removeSessionsByContext(contextId)));
            }
            for (final SessionControl control : list) {
                unscheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID());
            }
        } finally {
            wlock.unlock();
        }
        wlongTermLock.lock();
        try {
            if (!hasLongTermSession(contextId)) {
                return list;
            }
            for (final SessionMap longTerm : longTermList) {
                final Iterator<SessionControl> iter = longTerm.values().iterator();
                while (iter.hasNext()) {
                    final SessionControl control = iter.next();
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId) {
                        longTerm.removeBySessionId(session.getSessionID());
                        longTermUserGuardian.remove(session.getUserId(), contextId);
                        list.add(control);
                    }
                }
            }
        } finally {
            wlongTermLock.unlock();
        }
        return list;
    }

    /**
     * Removes all sessions belonging to given contexts out of long- and short-term container.
     *
     * @param contextIds - Set with the context identifiers to remove sessions for
     * @return List of {@link SessionControl} objects for each handled session
     */
    List<SessionControl> removeContextSessions(final Set<Integer> contextIds) {
        // Removing sessions is a write operation.
        final List<SessionControl> list = new ArrayList<SessionControl>();
        wlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                list.addAll(container.removeSessionsByContexts(contextIds));
            }
            for (final SessionControl control : list) {
                unscheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID());
            }
        } finally {
            wlock.unlock();
        }

        wlongTermLock.lock();
        for (int contextId : contextIds) {
            if (!hasLongTermSession(contextId)) {
                continue;
            }
            for (final SessionMap longTerm : longTermList) {
                final Iterator<SessionControl> iter = longTerm.values().iterator();
                while (iter.hasNext()) {
                    final SessionControl control = iter.next();
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId) {
                        longTerm.removeBySessionId(session.getSessionID());
                        longTermUserGuardian.remove(session.getUserId(), contextId);
                        list.add(control);
                    }
                }
            }
        }
        wlongTermLock.unlock();

        return list;
    }

    boolean hasForContext(final int contextId) {
        wlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                if (container.hasForContext(contextId)) {
                    return true;
                }
            }
        } finally {
            wlock.unlock();
        }
        return false;
    }

    /**
     * Gets the first session for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param includeLongTerm Whether long-term container should be considered or not
     * @return The first matching session or <code>null</code>
     */
    public SessionControl getAnyActiveSessionForUser(final int userId, final int contextId, final boolean includeLongTerm) {
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                final SessionControl control = container.getAnySessionByUser(userId, contextId);
                if (control != null) {
                    return control;
                }
            }
        } finally {
            rlock.unlock();
        }
        if (includeLongTerm) {
            rlongTermLock.lock();
            try {
                if (!hasLongTermSession(userId, contextId)) {
                    return null;
                }
                for (final SessionMap longTermMap : longTermList) {
                    for (final SessionControl control : longTermMap.values()) {
                        final Session session = control.getSession();
                        if ((session.getContextId() == contextId) && (session.getUserId() == userId)) {
                            return control;
                        }
                    }
                }
            } finally {
                rlongTermLock.unlock();
            }
        }
        return null;
    }

    /**
     * Finds the first session for given user that satisfies given matcher.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param matcher The matcher to satisfy
     * @param ignoreLongTerm Whether long-term container should be considered or not
     * @return The first matching session or <code>null</code>
     */
    public Session findFirstSessionForUser(int userId, int contextId, SessionMatcher matcher, boolean ignoreLongTerm) {
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                final SessionControl control = container.getAnySessionByUser(userId, contextId);
                if ((control != null) && matcher.accepts(control.getSession())) {
                    return control.getSession();
                }
            }
        } finally {
            rlock.unlock();
        }
        if (false == ignoreLongTerm) {
            rlongTermLock.lock();
            try {
                if (!hasLongTermSession(userId, contextId)) {
                    return null;
                }
                for (final SessionMap longTermMap : longTermList) {
                    for (final SessionControl control : longTermMap.values()) {
                        final Session session = control.getSession();
                        if (session.getContextId() == contextId && session.getUserId() == userId && matcher.accepts(control.getSession())) {
                            return control.getSession();
                        }
                    }
                }
            } finally {
                rlongTermLock.unlock();
            }
        }
        return null;
    }

    public List<Session> filterSessions(SessionFilter filter) {
        List<Session> sessions = new LinkedList<Session>();
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                collectSessions(filter, container.getSessionControls(), sessions);
            }
        } finally {
            rlock.unlock();
        }

        rlongTermLock.lock();
        try {
            for (final SessionMap longTermMap : longTermList) {
                collectSessions(filter, longTermMap.values(), sessions);
            }
        } finally {
            rlongTermLock.unlock();
        }

        return sessions;
    }

    private static void collectSessions(SessionFilter filter, Collection<SessionControl> sessionControls, List<Session> sessions) {
        for (SessionControl sessionControl : sessionControls) {
            SessionImpl session = sessionControl.getSession();
            if (filter.apply(session)) {
                sessions.add(session);
            }
        }
    }

    /**
     * Gets the <b>local-only</b> sessions associated with specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The <b>local-only</b> sessions or an empty list
     */
    List<SessionControl> getUserSessions(int userId, int contextId) {
        // A read-only access to session list
        List<SessionControl> retval = new LinkedList<SessionControl>();

        // Short term ones
        rlock.lock();
        try {
            for (SessionContainer container : sessionList) {
                retval.addAll(Arrays.asList(container.getSessionsByUser(userId, contextId)));
            }
        } finally {
            rlock.unlock();
        }

        // Long term ones
        rlongTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return retval;
            }
            for (SessionMap longTermMap : longTermList) {
                for (SessionControl control : longTermMap.values()) {
                    Session session = control.getSession();
                    if (session.getContextId() == contextId && session.getUserId() == userId) {
                        retval.add(control);
                    }
                }
            }
        } finally {
            rlongTermLock.unlock();
        }

        return retval;
    }

    /**
     * Gets the number of <b>local-only</b> sessions associated with specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param considerLongTerm <code>true</code> to also consider long-term sessions; otherwise <code>false</code>
     * @return The number of sessions
     */
    int getNumOfUserSessions(int userId, int contextId, boolean considerLongTerm) {
        // A read-only access to session list
        int count = 0;
        rlock.lock();
        try {
            for (SessionContainer container : sessionList) {
                count += container.numOfUserSessions(userId, contextId);
            }
        } finally {
            rlock.unlock();
        }
        if (considerLongTerm) {
            rlongTermLock.lock();
            try {
                if (!hasLongTermSession(userId, contextId)) {
                    return count;
                }
                for (SessionMap longTermMap : longTermList) {
                    for (SessionControl control : longTermMap.values()) {
                        Session session = control.getSession();
                        if (session.getContextId() == contextId && session.getUserId() == userId) {
                            count++;
                        }
                    }
                }
            } finally {
                rlongTermLock.unlock();
            }
        }
        return count;
    }

    /**
     * Checks validity/uniqueness of specified authentication identifier for given login
     *
     * @param login The login
     * @param authId The authentication identifier
     * @throws OXException If authentication identifier is invalid/non-unique
     */
    void checkAuthId(String login, String authId) throws OXException {
        if (null != authId) {
            rlock.lock();
            try {
                for (SessionContainer container : sessionList) {
                    for (SessionControl sc : container.getSessionControls()) {
                        if (authId.equals(sc.getSession().getAuthId())) {
                            throw SessionExceptionCodes.DUPLICATE_AUTHID.create(sc.getSession().getLogin(), login);
                        }
                    }
                }
            } finally {
                rlock.unlock();
            }
        }
        rlongTermLock.lock();
        try {
            for (SessionMap longTermMap : longTermList) {
                for (SessionControl control : longTermMap.values()) {
                    if (null != authId && authId.equals(control.getSession().getAuthId())) {
                        throw SessionExceptionCodes.DUPLICATE_AUTHID.create(control.getSession().getLogin(), login);
                    }
                }
            }
        } finally {
            rlongTermLock.unlock();
        }
    }

    /**
     * Adds specified session.
     *
     * @param session The session to add
     * @param noLimit <code>true</code> to add without respect to limitation; otherwise <code>false</code> to honor limitation
     * @return The associated {@link SessionControl} instance
     * @throws OXException If add operation fails
     */
    protected SessionControl addSession(final SessionImpl session, final boolean noLimit) throws OXException {
        return addSession(session, noLimit, false);
    }

    /**
     * Adds specified session.
     *
     * @param session The session to add
     * @param noLimit <code>true</code> to add without respect to limitation; otherwise <code>false</code> to honor limitation
     * @param addIfAbsent <code>true</code> to perform an add-if-absent operation; otherwise <code>false</code> to fail on duplicate session
     * @return The associated {@link SessionControl} instance
     * @throws OXException If add operation fails
     */
    protected SessionControl addSession(final SessionImpl session, final boolean noLimit, final boolean addIfAbsent) throws OXException {
        if (!noLimit && countSessions() > maxSessions) {
            throw SessionExceptionCodes.MAX_SESSION_EXCEPTION.create();
        }
        final SessionControl control;
        // Adding a session is a writing operation. Other threads requesting a session should be blocked.
        wlock.lock();
        try {
            control = sessionList.get(0).put(session, addIfAbsent);
            randoms.put(session.getRandomToken(), session.getSessionID());
        } finally {
            wlock.unlock();
        }
        scheduleRandomTokenRemover(session.getRandomToken());
        return control;
    }

    int countSessions() {
        // A read-only access to session list
        int count = 0;
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                count += container.size();
            }
        } finally {
            rlock.unlock();
        }
        rlongTermLock.lock();
        try {
            for (final SessionMap longTermMap : longTermList) {
                count += longTermMap.size();
            }
        } finally {
            rlongTermLock.unlock();
        }
        return count;
    }

    int[] getShortTermSessionsPerContainer() {
        // read-only access to short term sessions.
        final int[] retval;
        rlock.lock();
        try {
            retval = new int[sessionList.size()];
            int i = 0;
            for (final SessionContainer container : sessionList) {
                retval[i++] = container.size();
            }
        } finally {
            rlock.unlock();
        }
        return retval;
    }

    int[] getLongTermSessionsPerContainer() {
        // read-only access to long term sessions.
        final int[] retval;
        rlongTermLock.lock();
        try {
            retval = new int[longTermList.size()];
            int i = 0;
            for (final SessionMap longTermMap : longTermList) {
                retval[i++] = longTermMap.size();
            }
        } finally {
            rlongTermLock.unlock();
        }
        return retval;
    }

    SessionControl getSessionByAlternativeId(final String altId) {
        SessionControl control = null;
        int i = 0;
        // Read-only access
        rlock.lock();
        try {
            for (i = 0; i < sessionList.size(); i++) {
                final SessionContainer container = sessionList.get(i);
                if (container.containsAlternativeId(altId)) {
                    control = container.getSessionByAlternativeId(altId);
                    if (i > 0) {
                        // Schedule task to put session into first container and remove from latter one. This requires a write lock.
                        // See bug 16158.
                        scheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID(), false);
                    }
                    break;
                }
            }
        } finally {
            rlock.unlock();
        }
        if (null != control) {
            return control;
        }
        rlongTermLock.lock();
        try {
            for (final SessionMap longTermMap : longTermList) {
                if (longTermMap.containsByAlternativeId(altId)) {
                    control = longTermMap.getByAlternativeId(altId);
                    scheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID(), true);
                }
            }
        } finally {
            rlongTermLock.unlock();
        }
        return control;
    }

    SessionControl getSession(final String sessionId) {
        SessionControl control = null;
        // Read-only access
        rlock.lock();
        try {
            boolean first = true;
            for (SessionContainer container : sessionList) {
                if ((control = container.getSessionById(sessionId)) != null) {
                    if (false == first) {
                        // Schedule task to put session into first container and remove from latter one. This requires a write lock.
                        // See bug 16158.
                        scheduleTask2MoveSession2FirstContainer(sessionId, false);
                    }
                    return control;
                }

                first = false;
            }
        } catch (final IndexOutOfBoundsException e) {
            // For safety
        } finally {
            rlock.unlock();
        }
        // Check long-term container, too
        rlongTermLock.lock();
        try {
            for (final SessionMap longTermMap : longTermList) {
                if (longTermMap.containsBySessionId(sessionId)) {
                    control = longTermMap.getBySessionId(sessionId);
                    scheduleTask2MoveSession2FirstContainer(sessionId, true);
                }
            }
        } finally {
            rlongTermLock.unlock();
        }
        return control;
    }

    SessionControl optShortTermSession(final String sessionId) {
        SessionControl control = null;
        // Read-only access
        rlock.lock();
        try {
            for (SessionContainer container : sessionList) {
                if ((control = container.getSessionById(sessionId)) != null) {
                    return control;
                }
            }
        } catch (final IndexOutOfBoundsException e) {
            // For safety
        } finally {
            rlock.unlock();
        }
        return control;
    }

    SessionControl getSessionByRandomToken(final String randomToken) {
        // A read-only access to session and a write access to random list
        final String sessionId;
        wlock.lock();
        try {
            sessionId = randoms.remove(randomToken);
        } finally {
            wlock.unlock();
        }
        if (null == sessionId) {
            return null;
        }
        final SessionControl sessionControl = getSession(sessionId);
        final SessionImpl session = sessionControl.getSession();
        if (!randomToken.equals(session.getRandomToken())) {
            final OXException e = SessionExceptionCodes.WRONG_BY_RANDOM.create(session.getSessionID(), session.getRandomToken(), randomToken, sessionId);
            LOG.error("", e);
            SessionHandler.clearSession(sessionId);
            return null;
        }
        session.removeRandomToken();
        if (sessionControl.getCreationTime() + randomTokenTimeout < System.currentTimeMillis()) {
            SessionHandler.clearSession(sessionId);
            return null;
        }
        return sessionControl;
    }

    SessionControl clearSession(final String sessionId) {
        // A write access
        wlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                if (container.containsSessionId(sessionId)) {
                    final SessionControl sessionControl = container.removeSessionById(sessionId);
                    final Session session = sessionControl.getSession();
                    final String random = session.getRandomToken();
                    if (null != random) {
                        // If session is accessed through random token, random token is removed in the session.
                        randoms.remove(random);
                    }
                    unscheduleTask2MoveSession2FirstContainer(sessionId);
                    return sessionControl;
                }
            }
        } finally {
            wlock.unlock();
        }
        return null;
    }

    List<SessionControl> getShortTermSessions() {
        // A read.only access
        final List<SessionControl> retval = new LinkedList<SessionControl>();
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                retval.addAll(container.getSessionControls());
            }
        } finally {
            rlock.unlock();
        }
        return retval;
    }

    List<String> getShortTermSessionIDs() {
        // A read.only access
        List<String> retval = new LinkedList<String>();
        rlock.lock();
        try {
            for (SessionContainer container : sessionList) {
                retval.addAll(container.getSessionIDs());
            }
        } finally {
            rlock.unlock();
        }
        return retval;
    }

    List<SessionControl> getLongTermSessions() {
        final List<SessionControl> retval = new LinkedList<SessionControl>();
        rlongTermLock.lock();
        try {
            for (final SessionMap longTermMap : longTermList) {
                retval.addAll(longTermMap.values());
            }
        } finally {
            rlongTermLock.unlock();
        }
        return retval;
    }

    void move2FirstContainer(final String sessionId) {
        SessionControl control = null;
        wlock.lock();
        try {
            int size = sessionList.size();
            for (int i = 1; i < size && null == control; i++) {
                final SessionContainer container = sessionList.get(i);
                if (container.containsSessionId(sessionId)) {
                    // Remove from current container & put into first one
                    control = container.removeSessionById(sessionId);
                    if (null != control) {
                        sessionList.get(0).putSessionControl(control);
                    }
                }
            }
            if (null == control) {
                if (sessionList.get(0).containsSessionId(sessionId)) {
                    LOG.warn("Somebody else moved session to most up-to-date container.");
                } else {
                    LOG.debug("Was not able to move the session {} into the most up-to-date container since it has already been removed in the meantime", sessionId);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
        } finally {
            wlock.unlock();
        }
        unscheduleTask2MoveSession2FirstContainer(sessionId);
        if (null != control) {
            SessionHandler.postSessionTouched(control.getSession());
        }
    }

    void move2FirstContainerLongTerm(final String sessionId) {
        SessionControl control = null;
        wlock.lock();
        wlongTermLock.lock();
        try {
            boolean movedSession = false;
            int size = longTermList.size();
            for (int i = 0; i < size && !movedSession; i++) {
                final SessionMap longTermMap = longTermList.get(i);
                control = longTermMap.removeBySessionId(sessionId);
                if (null == control) {
                    continue;
                }
                sessionList.get(0).putSessionControl(control);
                final SessionImpl session = control.getSession();
                longTermUserGuardian.remove(session.getUserId(), session.getContextId());
                LOG.trace("Moved from long term container {} to first one.", i);
                movedSession = true;
            }
            if (!movedSession) {
                if (sessionList.get(0).containsSessionId(sessionId)) {
                    LOG.warn("Somebody else moved session to most actual container.");
                } else {
                    LOG.warn("Was not able to move the session into the most actual container.");
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
        } finally {
            wlongTermLock.unlock();
            wlock.unlock();
        }
        unscheduleTask2MoveSession2FirstContainer(sessionId);
        if (null != control) {
            SessionHandler.postSessionReactivation(control.getSession());
        }
    }

    void removeRandomToken(final String randomToken) {
        wlock.lock();
        try {
            randoms.remove(randomToken);
        } finally {
            wlock.unlock();
        }
    }

    public void addThreadPoolService(final ThreadPoolService service) {
        threadPoolService.set(service);
    }

    public void removeThreadPoolService() {
        threadPoolService.set(null);
    }

    private void scheduleTask2MoveSession2FirstContainer(final String sessionId, final boolean longTerm) {
        Move2FirstContainerTask task = tasks.get(sessionId);
        if (null != task) {
            LOG.trace("Found an already existing task to move session to first container.");
            return;
        }
        {
            final Move2FirstContainerTask ntask = new Move2FirstContainerTask(sessionId, longTerm);
            task = tasks.putIfAbsent(sessionId, ntask);
            if (null != task) {
                LOG.trace("Found an already existing task to move session to first container.");
                return;
            }
            task = ntask;
        }
        final ThreadPoolService threadPoolService = this.threadPoolService.get();
        if (null == threadPoolService) {
            final Move2FirstContainerTask tmp = task;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        tmp.call();
                    } catch (final Exception e) {
                        LOG.error("Moving session to first container failed.", e);
                    }
                }
            }, "Move2FirstContainer").start();
        } else {
            threadPoolService.submit(task);
        }
    }

    private void unscheduleTask2MoveSession2FirstContainer(final String sessionId) {
        final Move2FirstContainerTask task = tasks.remove(sessionId);
        if (null != task) {
            task.deactivate();
        }
    }

    private class Move2FirstContainerTask extends AbstractTask<Void> {

        private final String sessionId;

        private final boolean longTerm;

        private boolean deactivated = false;

        Move2FirstContainerTask(final String sessionId, final boolean longTerm) {
            super();
            this.sessionId = sessionId;
            this.longTerm = longTerm;
        }

        public void deactivate() {
            deactivated = true;
        }

        @Override
        public Void call() {
            if (deactivated) {
                return null;
            }
            if (longTerm) {
                move2FirstContainerLongTerm(sessionId);
            } else {
                move2FirstContainer(sessionId);
            }
            return null;
        }
    }

    /**
     * Adds the specified timer service.
     *
     * @param service The timer service
     */
    public void addTimerService(final TimerService service) {
        timerService.set(service);
    }

    /**
     * Removes the timer service
     */
    public void removeTimerService() {
        for (final ScheduledTimerTask timerTask : removers.values()) {
            timerTask.cancel();
        }
        timerService.set(null);
    }

    private void scheduleRandomTokenRemover(final String randomToken) {
        final RandomTokenRemover remover = new RandomTokenRemover(randomToken);
        final TimerService timerService = this.timerService.get();
        if (null == timerService) {
            remover.run();
        } else {
            final ScheduledTimerTask timerTask = timerService.schedule(remover, randomTokenTimeout, TimeUnit.MILLISECONDS);
            removers.put(randomToken, timerTask);
        }
    }

    private class RandomTokenRemover implements Runnable {

        private final String randomToken;

        RandomTokenRemover(final String randomToken) {
            super();
            this.randomToken = randomToken;
        }

        @Override
        public void run() {
            try {
                removers.remove(randomToken);
                removeRandomToken(randomToken);
            } catch (final Throwable t) {
                LOG.error("", t);
            }
        }
    }

}
