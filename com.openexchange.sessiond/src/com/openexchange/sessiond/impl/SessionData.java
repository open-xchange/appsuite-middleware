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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
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

    private final LinkedList<Map<String, SessionControl>> longTermList;

    // The LongTermUserGuardian contains an entry for a given UserKey if the longTermList contains a session for the user
    // This is used to guard against potentially slow serial searches of the long term sessions
    private final Set<UserKey> longTermUserGuardian = new HashSet<UserKey>();

    private final Lock longTermLock = new ReentrantLock();

    SessionData(final long containerCount, final int maxSessions, final long randomTokenTimeout, final long longTermContainerCount, final boolean autoLogin) {
        super();
        this.maxSessions = maxSessions;
        this.randomTokenTimeout = randomTokenTimeout;
        this.autoLogin = autoLogin;

        sessionList = new LinkedList<SessionContainer>();
        randoms = new ConcurrentHashMap<String, String>();
        final ReadWriteLock rwlock = new ReentrantReadWriteLock(true);
        rlock = rwlock.readLock();
        wlock = rwlock.writeLock();
        for (int i = 0; i < containerCount; i++) {
            sessionList.add(0, new SessionContainer());
        }

        longTermList = new LinkedList<Map<String, SessionControl>>();
        for (int i = 0; i < longTermContainerCount; i++) {
            longTermList.add(0, new HashMap<String, SessionControl>());
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
        longTermLock.lock();
        try {
            longTermList.clear();
            longTermUserGuardian.clear();
        } finally {
            longTermLock.unlock();
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
                longTermLock.lock();
                try {
                    final Map<String, SessionControl> first = longTermList.getFirst();
                    for (final SessionControl control : retval) {
                        first.put(control.getSession().getSessionID(), control);
                        longTermUserGuardian.add(new UserKey(control.getSession().getUserId(), control.getSession().getContextId()));
                    }
                } finally {
                    longTermLock.unlock();
                }
            }
            return retval;
        } finally {
            wlock.unlock();
        }
    }

    List<SessionControl> rotateLongTerm() {
        longTermLock.lock();
        try {
            longTermList.addFirst(new HashMap<String, SessionControl>());
            final List<SessionControl> retval = new ArrayList<SessionControl>();
            retval.addAll(longTermList.removeLast().values());
            for (final SessionControl sessionControl : retval) {
                longTermUserGuardian.remove(new UserKey(sessionControl.getSession().getUserId(), sessionControl.getSession().getContextId()));
            }
            return retval;
        } finally {
            longTermLock.unlock();
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
        // A read-only access to session list
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                if (container.containsUser(userId, context.getContextId())) {
                    return true;
                }
            }
        } finally {
            rlock.unlock();
        }
        longTermLock.lock();
        try {
            return hasLongTermSession(userId, context.getContextId());
        } finally {
            longTermLock.unlock();
        }
    }

    private final boolean hasLongTermSession(final int userId, final int contextId) {
        return this.longTermUserGuardian.contains(new UserKey(userId, contextId));
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
        longTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return retval.toArray(new SessionControl[retval.size()]);
            }
            for (final Map<String, SessionControl> longTerm : longTermList) {
                final Iterator<SessionControl> iter = longTerm.values().iterator();
                while (iter.hasNext()) {
                    final SessionControl control = iter.next();
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId && session.getUserId() == userId) {
                        iter.remove();
                        retval.add(control);
                    }
                }
            }
        } finally {
            longTermLock.unlock();
        }
        return retval.toArray(new SessionControl[retval.size()]);
    }

    public SessionControl getAnyActiveSessionForUser(final int userId, final int contextId) {
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                final SessionControl control = container.getAnySessionByUser(userId, contextId);
                if ( control != null ) {
                    return control;
                }
            }
        } finally {
            rlock.unlock();
        }
        longTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return null;
            }
            for (final Map<String, SessionControl> longTermMap : longTermList) {
                for (final SessionControl control : longTermMap.values()) {
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId && session.getUserId() == userId) {
                        return control;
                    }
                }
            }
        } finally {
            longTermLock.unlock();
        }
        return null;
    }

    public Session findFirstSessionForUser(final int userId, final int contextId, final SessionMatcher matcher) {
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                final SessionControl control = container.getAnySessionByUser(userId, contextId);
                if ( control != null && matcher.accepts(control.getSession())) {
                    return control.getSession();
                }
            }
        } finally {
            rlock.unlock();
        }
        longTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return null;
            }
            for (final Map<String, SessionControl> longTermMap : longTermList) {
                for (final SessionControl control : longTermMap.values()) {
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId && session.getUserId() == userId && matcher.accepts(control.getSession())) {
                        return control.getSession();
                    }
                }
            }
        } finally {
            longTermLock.unlock();
        }
        return null;
    }



    SessionControl[] getUserSessions(final int userId, final int contextId) {
        // A read-only access to session list
        final List<SessionControl> retval = new ArrayList<SessionControl>();
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                retval.addAll(Arrays.asList(container.getSessionsByUser(userId, contextId)));
            }
        } finally {
            rlock.unlock();
        }
        longTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return retval.toArray(new SessionControl[retval.size()]);
            }
            for (final Map<String, SessionControl> longTermMap : longTermList) {
                for (final SessionControl control : longTermMap.values()) {
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId && session.getUserId() == userId) {
                        retval.add(control);
                    }
                }
            }
        } finally {
            longTermLock.unlock();
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
        longTermLock.lock();
        try {
            if (!hasLongTermSession(userId, contextId)) {
                return count;
            }
            for (final Map<String, SessionControl> longTermMap : longTermList) {
                for (final SessionControl control : longTermMap.values()) {
                    final Session session = control.getSession();
                    if (session.getContextId() == contextId && session.getUserId() == userId) {
                        count++;
                    }
                }
            }
        } finally {
            longTermLock.unlock();
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
        longTermLock.lock();
        try {
            for (final Map<String, SessionControl> longTermMap : longTermList) {
                for (final SessionControl control : longTermMap.values()) {
                    if (null != authId && authId.equals(control.getSession().getAuthId())) {
                        throw SessionExceptionCodes.DUPLICATE_AUTHID.create(control.getSession().getLogin(), login);
                    }
                }
            }
        } finally {
            longTermLock.unlock();
        }
    }

    SessionControl addSession(final SessionImpl session, final boolean noLimit) throws OXException {
        if (!noLimit && countSessions() > maxSessions) {
            throw SessionExceptionCodes.MAX_SESSION_EXCEPTION.create();
        }
        // Adding a session is a writing operation. Other threads requesting a session should be blocked.
        final SessionControl control;
        wlock.lock();
        try {
            control = sessionList.getFirst().put(session);
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
        longTermLock.lock();
        try {
            for (final Map<String, SessionControl> longTermMap : longTermList) {
                count += longTermMap.size();
            }
        } finally {
            longTermLock.unlock();
        }
        return count;
    }

    SessionControl getSession(final String sessionId) {
        SessionControl control = null;
        int i = 0;
        // Read-only access
        rlock.lock();
        try {
            for (i = 0; i < sessionList.size(); i++) {
                final SessionContainer container = sessionList.get(i);
                if (container.containsSessionId(sessionId)) {
                    control = container.getSessionById(sessionId);
                    if (i > 0) {
                        // Schedule task to put session into first container and remove from latter one. This requires a write lock.
                        // See bug 16158.
                        scheduleTask2MoveSession2FirstContainer(sessionId, false);
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
        longTermLock.lock();
        try {
            for (final Map<String, SessionControl> longTermMap : longTermList) {
                if (longTermMap.containsKey(sessionId)) {
                    control = longTermMap.get(sessionId);
                    scheduleTask2MoveSession2FirstContainer(sessionId, true);
                }
            }
        } finally {
            longTermLock.unlock();
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
        longTermLock.lock();
        try {
            for (final Map<String, SessionControl> longTermMap : longTermList) {
                retval.addAll(longTermMap.values());
            }
        } finally {
            longTermLock.unlock();
        }
        return retval;
    }

    void move2FirstContainer(final String sessionId) {
        wlock.lock();
        try {
            boolean movedSession = false;
            for (int i = 1; i < sessionList.size() && !movedSession; i++) {
                final SessionContainer container = sessionList.get(i);
                if (container.containsSessionId(sessionId)) {
                    final SessionControl sessionControl = container.removeSessionById(sessionId);
                    sessionList.getFirst().putSessionControl(sessionControl);
                    LOG.trace("Moved from container " + i + " to first one.");
                    movedSession = true;
                }
            }
            if (!movedSession) {
                if (sessionList.getFirst().containsSessionId(sessionId)) {
                    LOG.warn("Somebody else moved session to most actual container.");
                } else {
                    LOG.warn("Was not able to move the session " + sessionId + " into the most actual container.");
                }
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            wlock.unlock();
        }
        unscheduleTask2MoveSession2FirstContainer(sessionId);
    }

    void move2FirstContainerLongTerm(final String sessionId) {
        SessionControl control = null;
        wlock.lock();
        longTermLock.lock();
        try {
            boolean movedSession = false;
            for (int i = 0; i < longTermList.size() && !movedSession; i++) {
                final Map<String, SessionControl> longTermMap = longTermList.get(i);
                control = longTermMap.remove(sessionId);
                if (null == control) {
                    continue;
                }
                sessionList.getFirst().putSessionControl(control);
                longTermUserGuardian.remove(new UserKey(control.getSession().getUserId(), control.getSession().getContextId()));
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
            longTermLock.unlock();
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

    /**
     * Map to remember if there is already a task that should move the session to the first container.
     */
    private final Map<String, Move2FirstContainerTask> tasks = new ConcurrentHashMap<String, Move2FirstContainerTask>();

    private final Lock tasksLock = new ReentrantLock();

    private ThreadPoolService threadPoolService;

    public void addThreadPoolService(final ThreadPoolService service) {
        threadPoolService = service;
    }

    public void removeThreadPoolService() {
        threadPoolService = null;
    }

    private void scheduleTask2MoveSession2FirstContainer(final String sessionId, final boolean longTerm) {
        final Move2FirstContainerTask task;
        tasksLock.lock();
        try {
            if (tasks.containsKey(sessionId)) {
                LOG.trace("Found an already existing task to move session to first container.");
                return;
            }
            task = new Move2FirstContainerTask(sessionId, longTerm);
            tasks.put(sessionId, task);
        } finally {
            tasksLock.unlock();
        }
        if (null == threadPoolService) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.call();
                    } catch (final Exception e) {
                        LOG.error("Moving session to first container failed.", e);
                    }
                }
            }).start();
        } else {
            threadPoolService.submit(task);
        }
    }

    private void unscheduleTask2MoveSession2FirstContainer(final String sessionId) {
        tasksLock.lock();
        try {
            final Move2FirstContainerTask task = tasks.remove(sessionId);
            if (null != task) {
                task.deactivate();
            }
        } finally {
            tasksLock.unlock();
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

    private TimerService timerService;
    Map<String, ScheduledTimerTask> removers = new ConcurrentHashMap<String, ScheduledTimerTask>();

    public void addTimerService(final TimerService service) {
        timerService = service;
    }

    public void removeTimerService() {
        for (final ScheduledTimerTask timerTask : removers.values()) {
            timerTask.cancel();
        }
        timerService = null;
    }

    private void scheduleRandomTokenRemover(final String randomToken) {
        final RandomTokenRemover remover = new RandomTokenRemover(randomToken);
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
