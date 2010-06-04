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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.sessiond.exception.SessiondException;
import com.openexchange.sessiond.exception.SessiondException.Code;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * Object handling the multi threaded access to session container. Excessive locking is used to secure container data structures.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class SessionData {

    static final Log LOG = LogFactory.getLog(SessionData.class);

    private final LinkedList<SessionContainer> sessionList;

    private final LinkedList<Map<String, String>> userList;

    private final LinkedList<Map<String, String>> randomList;

    private final Lock rlock;

    private final Lock wlock;

    private final int maxSessions;

    SessionData(final int containerCount, final int maxSessions) {
        super();
        sessionList = new LinkedList<SessionContainer>();
        userList = new LinkedList<Map<String, String>>();
        randomList = new LinkedList<Map<String, String>>();
        final ReadWriteLock rwlock = new ReentrantReadWriteLock(true);
        rlock = rwlock.readLock();
        wlock = rwlock.writeLock();
        this.maxSessions = maxSessions;
        for (int i = 0; i < containerCount; i++) {
            sessionList.add(0, new SessionContainer(maxSessions));
            userList.add(0, new ConcurrentHashMap<String, String>(maxSessions));
            randomList.add(0, new ConcurrentHashMap<String, String>(maxSessions));
        }
    }

    void clear() {
        // A write access to lists
        wlock.lock();
        try {
            sessionList.clear();
            userList.clear();
            randomList.clear();
        } finally {
            wlock.unlock();
        }
    }

    List<SessionControl> rotate() {
        // A write access to lists
        wlock.lock();
        try {
            sessionList.addFirst(new SessionContainer(maxSessions));
            userList.addFirst(new ConcurrentHashMap<String, String>(maxSessions));
            randomList.addFirst(new ConcurrentHashMap<String, String>(maxSessions));
            userList.removeLast();
            randomList.removeLast();
            final List<SessionControl> retval = new ArrayList<SessionControl>(maxSessions);
            retval.addAll(sessionList.removeLast().getSessionControls());
            return retval;
        } finally {
            wlock.unlock();
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
        return false;
    }

    SessionControl[] removeUserSessions(final int userId, final int contextId) {
        // Removing sessions is a write operation.
        wlock.lock();
        try {
            final List<SessionControl> retval = new ArrayList<SessionControl>();
            for (final SessionContainer container : sessionList) {
                retval.addAll(Arrays.asList(container.removeSessionsByUser(userId, contextId)));
            }
            for (SessionControl control : retval) {
                unscheduleTask2MoveSession2FirstContainer(control.getSession().getSessionID());
            }
            return retval.toArray(new SessionControl[retval.size()]);
        } finally {
            wlock.unlock();
        }
    }

    SessionControl[] getUserSessions(final int userId, final int contextId) {
        // A read-only access to session list
        rlock.lock();
        try {
            final List<SessionControl> retval = new ArrayList<SessionControl>();
            for (final SessionContainer container : sessionList) {
                retval.addAll(Arrays.asList(container.getSessionsByUser(userId, contextId)));
            }
            return retval.toArray(new SessionControl[retval.size()]);
        } finally {
            rlock.unlock();
        }
    }

    int getNumOfUserSessions(final int userId, final Context context) {
        // A read-only access to session list
        int count = 0;
        rlock.lock();
        try {
            for (final SessionContainer container : sessionList) {
                count += container.numOfUserSessions(userId, context.getContextId());
            }
        } finally {
            rlock.unlock();
        }
        return count;
    }

    void checkAuthId(String login, String authId) throws SessiondException {
        rlock.lock();
        try {
            for (SessionContainer container : sessionList) {
                for (SessionControl sc : container.getSessionControls()) {
                    if (null != authId && authId.equals(sc.getSession().getAuthId())) {
                        throw new SessiondException(Code.DUPLICATE_AUTHID, sc.getSession().getLogin(), login);
                    }
                }
            }
        } finally {
            rlock.unlock();
        }
    }

    SessionControl addSession(final Session session, final int lifeTime, final boolean noLimit) throws SessiondException {
        if (!noLimit && countSessions() > maxSessions) {
            throw new SessiondException(Code.MAX_SESSION_EXCEPTION);
        }
        // Adding a session is a writing operation. Other threads requesting a session should be blocked.
        wlock.lock();
        try {
            final SessionControl control = sessionList.getFirst().put(session, lifeTime);
            userList.getFirst().put(session.getLoginName(), session.getSessionID());
            randomList.getFirst().put(session.getRandomToken(), session.getSessionID());
            return control;
        } finally {
            wlock.unlock();
        }
    }

    int countSessions() {
        // A read-only access to session list
        rlock.lock();
        try {
            int count = 0;
            for (final SessionContainer container : sessionList) {
                count += container.size();
            }
            return count;
        } finally {
            rlock.unlock();
        }
    }

    SessionControl getSession(final String sessionId) {
        SessionControl control = null;
        int i = 0;
        // Read-only access
        rlock.lock();
        try {
            for (i = 0; i < sessionList.size(); i++) {
                SessionContainer container = sessionList.get(i);
                if (container.containsSessionId(sessionId)) {
                    control = container.getSessionById(sessionId);
                    if (i > 0) {
                        // Schedule task to put session into first container and remove from latter one. This requires a write lock.
                        // See bug 16158.
                        scheduleTask2MoveSession2FirstContainer(sessionId);
                    }
                    break;
                }
            }
        } finally {
            rlock.unlock();
        }
        if (null == control) {
            return null;
        }
        if (!control.isValid()) {
            LOG.info("Session timed out. ID: " + sessionId);
            LOG.info("Session timestamp " + control.getLastAccessed() + ", lifeTime: " + control.getLifetime());
            wlock.lock();
            try {
                sessionList.get(i).removeSessionById(sessionId);
                Session session = control.getSession();
                userList.get(i).remove(session.getLoginName());
                randomList.get(i).remove(session.getRandomToken());
            } finally {
                wlock.unlock();
            }
            return null;
        }
        control.updateLastAccessed();
        return control;
    }

    SessionControl getSessionByRandomToken(final String randomToken, final long randomTimeout, final String localIp) {
        // A read-only access to session & random list
        rlock.lock();
        try {
            for (int i = 0; i < randomList.size(); i++) {
                final Map<String, String> random = randomList.get(i);
                if (random.containsKey(randomToken)) {
                    final String sessionId = random.get(randomToken);
                    final SessionControl sessionControl = sessionList.get(i).getSessionById(sessionId);
                    if (sessionControl.getCreationTime() + randomTimeout >= System.currentTimeMillis()) {
                        final Session session = sessionControl.getSession();
                        session.removeRandomToken();
                        random.remove(randomToken);
                        // Set local IP
                        ((SessionImpl) session).setLocalIp(localIp);
                        return sessionControl;
                    }
                }
            }
        } finally {
            rlock.unlock();
        }
        return null;
    }

    SessionControl clearSession(final String sessionId) {
        // A write access
        wlock.lock();
        try {
            for (int i = 0; i < sessionList.size(); i++) {
                final SessionContainer container = sessionList.get(i);
                if (container.containsSessionId(sessionId)) {
                    final SessionControl sessionControl = container.removeSessionById(sessionId);
                    final Session session = sessionControl.getSession();
                    userList.get(i).remove(session.getLoginName());
                    String random = session.getRandomToken();
                    if (null != random) {
                        // If session is access through random token, random token is removed in the session.
                        randomList.get(i).remove(random);
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

    List<SessionControl> getSessions() {
        // A read.only access
        rlock.lock();
        try {
            final List<SessionControl> retval = new ArrayList<SessionControl>();
            for (final SessionContainer container : sessionList) {
                retval.addAll(container.getSessionControls());
            }
            return retval;
        } finally {
            rlock.unlock();
        }
    }

    void move2FirstContainer(String sessionId) {
        wlock.lock();
        try {
            boolean movedSession = false;
            for (int i = 1; i < sessionList.size(); i++) {
                SessionContainer container = sessionList.get(i);
                if (container.containsSessionId(sessionId)) {
                    SessionControl sessionControl = container.removeSessionById(sessionId);
                    sessionList.getFirst().putSessionControl(sessionControl);
                    Session session = sessionControl.getSession();
                    userList.get(i).remove(session.getLoginName());
                    userList.getFirst().put(session.getLoginName(), session.getSessionID());
                    randomList.get(i).remove(session.getRandomToken());
                    randomList.getFirst().put(session.getRandomToken(), session.getSessionID());
                    LOG.trace("Moved from container " + i + " to first one.");
                    movedSession = true;
                }
            }
            if (!movedSession) {
                if (sessionList.getFirst().containsSessionId(sessionId)) {
                    LOG.warn("Somebody else moved session to most actual container.");
                } else {
                    LOG.warn("Was not able to move the session into the most actual container.");
                }
            }
        } finally {
            wlock.unlock();
        }
        unscheduleTask2MoveSession2FirstContainer(sessionId);
    }

    private final Map<String, Move2FirstContainerTask> tasks = new ConcurrentHashMap<String, Move2FirstContainerTask>();

    private final Lock tasksLock = new ReentrantLock();

    private ThreadPoolService threadPoolService;

    public void addThreadPoolService(ThreadPoolService service) {
        this.threadPoolService = service;
    }

    public void removeThreadPoolService() {
        threadPoolService = null; 
    }

    private void scheduleTask2MoveSession2FirstContainer(String sessionId) {
        final Move2FirstContainerTask task;
        tasksLock.lock();
        try {
            if (tasks.containsKey(sessionId)) {
                LOG.trace("Found an already existing task to move session to first container.");
                return;
            }
            task = new Move2FirstContainerTask(sessionId);
            tasks.put(sessionId, task);
        } finally {
            tasksLock.unlock();
        }
        if (null == threadPoolService) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        task.call();
                    } catch (Exception e) {
                        LOG.error("Moving session to first container failed.", e);
                    }
                }
            }).start();
        } else {
            threadPoolService.submit(task);
        }
    }

    private void unscheduleTask2MoveSession2FirstContainer(String sessionId) {
        tasksLock.lock();
        try {
            tasks.remove(sessionId);
        } finally {
            tasksLock.unlock();
        }
    }

    private class Move2FirstContainerTask extends AbstractTask<Void> {

        private final String sessionId;

        Move2FirstContainerTask(String sessionId) {
            super();
            this.sessionId = sessionId;
        }

        public Void call() {
            move2FirstContainer(sessionId);
            return null;
        }
    }
}
