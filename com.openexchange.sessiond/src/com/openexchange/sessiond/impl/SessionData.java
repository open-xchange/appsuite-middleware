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

package com.openexchange.sessiond.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
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

    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessionData.class));

    private final int maxSessions;
    private final long randomTokenTimeout;
    private final boolean autoLogin;

    private final LinkedList<SessionContainer> sessionList;
    private final Map<String, String> randoms;
    private final Lock rlock;
    private final Lock wlock;

    private final LinkedList<SessionMap> longTermList;

    // The LongTermUserGuardian contains an entry for a given UserKey if the longTermList contains a session for the user
    // This is used to guard against potentially slow serial searches of the long term sessions
    private final UserRefCounter longTermUserGuardian = new UserRefCounter();

    private final Lock wlongTermLock;
    private final Lock rlongTermLock;

    /**
     * Map to remember if there is already a task that should move the session to the first container.
     */
    private final ConcurrentMap<String, Move2FirstContainerTask> tasks = new ConcurrentHashMap<String, Move2FirstContainerTask>();
    private final AtomicReference<ThreadPoolService> threadPoolService;

    private final AtomicReference<TimerService> timerService;
    protected Map<String, ScheduledTimerTask> removers = new ConcurrentHashMap<String, ScheduledTimerTask>();

    SessionData(final long containerCount, final int maxSessions, final long randomTokenTimeout, final long longTermContainerCount, final boolean autoLogin) {
        super();
        threadPoolService = new AtomicReference<ThreadPoolService>();
        timerService = new AtomicReference<TimerService>();
        this.maxSessions = maxSessions;
        this.randomTokenTimeout = randomTokenTimeout;
        this.autoLogin = autoLogin;

        sessionList = new LinkedList<SessionContainer>();
        randoms = new ConcurrentHashMap<String, String>();
        ReadWriteLock rwlock = new ReentrantReadWriteLock(true);
        rlock = rwlock.readLock();
        wlock = rwlock.writeLock();
        rwlock = new ReentrantReadWriteLock(true);
        wlongTermLock = rwlock.writeLock();
        rlongTermLock = rwlock.readLock();
        for (int i = 0; i < containerCount; i++) {
            sessionList.add(0, new SessionContainer());
        }

        longTermList = new LinkedList<SessionMap>();
        for (int i = 0; i < longTermContainerCount; i++) {
            longTermList.add(0, new SessionMap(256));
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
            sessionList.addFirst(new SessionContainer());
            final List<SessionControl> retval = new ArrayList<SessionControl>(maxSessions);
            retval.addAll(sessionList.removeLast().getSessionControls());
            if (autoLogin) {
                wlongTermLock.lock();
                try {
                    final SessionMap first = longTermList.getFirst();
                    for (final SessionControl control : retval) {
                        final SessionImpl session = control.getSession();
                        first.putBySessionId(session.getSessionID(), control);
                        longTermUserGuardian.add(session.getUserId(), session.getContextId());
                    }
                } finally {
                    wlongTermLock.unlock();
                }
            }
            return retval;
        } finally {
            wlock.unlock();
        }
    }

    List<SessionControl> rotateLongTerm() {
        wlongTermLock.lock();
        try {
            longTermList.addFirst(new SessionMap(256));
            final List<SessionControl> retval = new ArrayList<SessionControl>();
            retval.addAll(longTermList.removeLast().values());
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
     * @param context The user's context
     * @return <code>true</code> if given user in specified context has an active session; otherwise <code>false</code>
     */
    boolean isUserActive(final int userId, final Context context) {
        return isUserActive(userId, context.getContextId());
    }

    /**
     * Checks if given user in specified context has an active session kept in session container(s)
     *
     * @param userId The user ID
     * @param contextId The user's context ID
     * @return <code>true</code> if given user in specified context has an active session; otherwise <code>false</code>
     */
    boolean isUserActive(final int userId, final int contextId) {
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
        final List<SessionControl> retval = new ArrayList<SessionControl>();
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
        final List<SessionControl> list = new ArrayList<SessionControl>();
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

    public Session findFirstSessionForUser(final int userId, final int contextId, final SessionMatcher matcher, final boolean ignoreLongTerm) {
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
        if (!ignoreLongTerm) {
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

    SessionControl[] getUserSessions(final int userId, final int contextId) {
        // A read-only access to session list
        final List<SessionControl> retval = new ArrayList<SessionControl>();
        // Short term ones
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                retval.addAll(Arrays.asList(container.getSessionsByUser(userId, contextId)));
            }
        } finally {
            rlock.unlock();
        }
        rlongTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return retval.toArray(new SessionControl[retval.size()]);
            }
            for (final SessionMap longTermMap : longTermList) {
                for (final SessionControl control : longTermMap.values()) {
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId && session.getUserId() == userId) {
                        retval.add(control);
                    }
                }
            }
        } finally {
            rlongTermLock.unlock();
        }
        return retval.toArray(new SessionControl[retval.size()]);
    }

    int getNumOfUserSessions(final int userId, final int contextId) {
        // A read-only access to session list
        int count = 0;
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                count += container.numOfUserSessions(userId, contextId);
            }
        } finally {
            rlock.unlock();
        }
        rlongTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return count;
            }
            for (final SessionMap longTermMap : longTermList) {
                for (final SessionControl control : longTermMap.values()) {
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId && session.getUserId() == userId) {
                        count++;
                    }
                }
            }
        } finally {
            rlongTermLock.unlock();
        }
        return count;
    }

    void checkAuthId(final String login, final String authId) throws OXException {
        if (null != authId) {
            rlock.lock();
            try {
                for (final SessionContainer container : sessionList) {
                    for (final SessionControl sc : container.getSessionControls()) {
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
            for (final SessionMap longTermMap : longTermList) {
                for (final SessionControl control : longTermMap.values()) {
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
            control = sessionList.getFirst().put(session, addIfAbsent);
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
            final int size = sessionList.size();
            for (int i = 0; i < size; i++) {
                if ((control = sessionList.get(i).getSessionById(sessionId)) != null) {
                    if (i > 0) {
                        // Schedule task to put session into first container and remove from latter one. This requires a write lock.
                        // See bug 16158.
                        scheduleTask2MoveSession2FirstContainer(sessionId, false);
                    }
                    return control;
                }
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
            final int size = sessionList.size();
            for (int i = 0; i < size; i++) {
                if ((control = sessionList.get(i).getSessionById(sessionId)) != null) {
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
            LOG.error(e.getMessage(), e);
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
        final List<SessionControl> retval = new ArrayList<SessionControl>();
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

    List<SessionControl> getLongTermSessions() {
        final List<SessionControl> retval = new ArrayList<SessionControl>();
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
            for (int i = 1; i < sessionList.size() && null == control; i++) {
                final SessionContainer container = sessionList.get(i);
                if (container.containsSessionId(sessionId)) {
                    // Remove from current container & put into first one
                    control = container.removeSessionById(sessionId);
                    if (null != control) {
                        sessionList.getFirst().putSessionControl(control);
                        LOG.trace("Moved from container " + i + " to first one.");
                    }
                }
            }
            if (null == control) {
                if (sessionList.getFirst().containsSessionId(sessionId)) {
                    LOG.warn("Somebody else moved session to most up-to-date container.");
                } else {
                    LOG.warn("Was not able to move the session " + sessionId + " into the most up-to-date container.");
                }
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
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
            for (int i = 0; i < longTermList.size() && !movedSession; i++) {
                final SessionMap longTermMap = longTermList.get(i);
                control = longTermMap.removeBySessionId(sessionId);
                if (null == control) {
                    continue;
                }
                sessionList.getFirst().putSessionControl(control);
                final SessionImpl session = control.getSession();
                longTermUserGuardian.remove(session.getUserId(), session.getContextId());
                LOG.trace("Moved from long term container " + i + " to first one.");
                movedSession = true;
            }
            if (!movedSession) {
                if (sessionList.getFirst().containsSessionId(sessionId)) {
                    LOG.warn("Somebody else moved session to most actual container.");
                } else {
                    LOG.warn("Was not able to move the session into the most actual container.");
                }
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
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
                LOG.error(t.getMessage(), t);
            }
        }
    }
}
