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

package com.openexchange.sessionstorage.hazelcast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageExceptionCodes;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSession;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link HazelcastSessionStorageService} - The {@link SessionStorageService} backed by {@link HazelcastInstance}.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastSessionStorageService implements SessionStorageService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastSessionStorageService.class);

    private static final AtomicReference<HazelcastInstance> REFERENCE = new AtomicReference<HazelcastInstance>();

    /**
     * Sets specified {@link HazelcastInstance}.
     *
     * @param hazelcast The {@link HazelcastInstance}
     */
    public static void setHazelcastInstance(final HazelcastInstance hazelcast) {
        REFERENCE.set(hazelcast);
    }

    private static interface RemovedSessionHandler {

        void handleRemovedSession(PortableSession removedSession);
    }

    private static final RemovedSessionHandler NOOP_REMOVED_SESSION_HANDLER = new RemovedSessionHandler() {

        @Override
        public void handleRemovedSession(PortableSession removedSession) {
            // Nothing
        }
    };

    private static final class CollectingRemovedSessionHandler implements RemovedSessionHandler {

        final List<Session> removedSessions;

        CollectingRemovedSessionHandler(int capacity) {
            super();
            removedSessions = new ArrayList<Session>(capacity);
        }

        @Override
        public void handleRemovedSession(PortableSession removedSession) {
            removedSessions.add(removedSession);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------- //

    private final String sessionsMapName;
    private final Unregisterer unregisterer;
    private final AtomicBoolean inactive;
    private final ConcurrentMap<String, AcquiredLatch> synchronizer;

    /**
     * Initializes a new {@link HazelcastSessionStorageService}.
     *
     * @param sessionsMapName The name of the distributed 'sessions' map
     * @param unregisterer The unregisterer
     */
    public HazelcastSessionStorageService(String sessionsMapName, Unregisterer unregisterer) {
        super();
        this.sessionsMapName = sessionsMapName;
        this.unregisterer = unregisterer;
        inactive = new AtomicBoolean(false);
        synchronizer = new ConcurrentHashMap<String, AcquiredLatch>(256);
    }

    private AcquiredLatch acquireFor(String sessionId) {
        AcquiredLatch latch = synchronizer.get(sessionId);
        if (null == latch) {
            AcquiredLatch newLatch = new AcquiredLatch(Thread.currentThread(), new CountDownLatch(1));
            latch = synchronizer.putIfAbsent(sessionId, newLatch);
            if (null == latch) {
                latch = newLatch;
            }
        }
        return latch;
    }

    private void releaseFor(String sessionId) {
        synchronizer.remove(sessionId);
    }

    private void ensureActive() throws OXException {
        if (inactive.get()) {
            throw SessionStorageExceptionCodes.SESSION_STORAGE_DOWN.create(HazelcastSessionStorageService.class);
        }
    }

    private OXException handleNotActiveException(HazelcastInstanceNotActiveException e) {
        LOG.warn("Encountered a {} error. {} will be shut-down!", HazelcastInstanceNotActiveException.class.getSimpleName(), HazelcastSessionStorageService.class);
        unregisterer.propagateNotActive(e);
        unregisterer.unregisterSessionStorage();
        inactive.set(true);
        return SessionStorageExceptionCodes.SESSION_STORAGE_DOWN.create(e, HazelcastSessionStorageService.class.getName());
    }

    @Override
    public Session lookupSession(String sessionId) throws OXException {
        return lookupSession(sessionId, 0L);
    }

    @Override
    public Session lookupSession(String sessionId, long timeoutMillis) throws OXException {
        if (null == sessionId) {
            return null;
        }

        AcquiredLatch acquiredLatch = acquireFor(sessionId);
        CountDownLatch latch = acquiredLatch.latch;
        if (Thread.currentThread() == acquiredLatch.owner) {
            // Look-up that session in Hazelcast
            try {
                Session session = fetchFromHz(sessionId, timeoutMillis);
                acquiredLatch.result.set(session);
                return session;
            } catch (OXException e) {
                acquiredLatch.result.set(e);
                throw e;
            } finally {
                latch.countDown();
                releaseFor(sessionId);
            }
        }

        try {
            // Need to await 'til fetched from Hazelcast by concurrent thread
            latch.await();

            // Check if already locally available...
            Object result = acquiredLatch.result.get();
            if (result instanceof Session) {
                return (Session) result;
            }

            throw ((result instanceof OXException) ? (OXException) result : SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId));
        } catch (InterruptedException e) {
            throw SessionStorageExceptionCodes.INTERRUPTED.create(e, new Object[0]);
        }
    }

    private Session fetchFromHz(String sessionId, long timeoutMillis) throws OXException {
        ensureActive();
        try {
            // Fetch either synchronously or asynchronously
            PortableSession storedSession;
            if (timeoutMillis <= 0) {
                storedSession = sessions().get(sessionId);
            } else {
                Future<PortableSession> f = sessions().getAsync(sessionId);
                storedSession = getFrom(f, timeoutMillis);
                if (null == storedSession && f.isCancelled()) {
                    LOG.warn("Session {} could not be retrieved from session storage within {}msec.", sessionId, Long.valueOf(timeoutMillis));
                }
            }

            // Check if not null
            if (null != storedSession) {
                return storedSession;
            }

            // Throw exception
            throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            throw handleHazelcastException(e, SessionStorageExceptionCodes.NO_SESSION_FOUND.create(e, sessionId));
        } catch (RuntimeException e) {
            throw handleRuntimeException(e, SessionStorageExceptionCodes.NO_SESSION_FOUND.create(e, sessionId));
        } catch (OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(e, sessionId);
            }
            throw e;
        }
    }

    private <V> V getFrom(Future<V> f, long timeoutMillis) throws OXException {
        try {
            return f.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SessionStorageExceptionCodes.INTERRUPTED.create(e, e.getMessage());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HazelcastInstanceNotActiveException) {
                throw (HazelcastInstanceNotActiveException) cause;
            }
            if (cause instanceof HazelcastException) {
                throw (HazelcastException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException(cause);
        } catch (TimeoutException e) {
            f.cancel(true);
            return null;
        }
    }

    @Override
    public void addSessionsIfAbsent(final Collection<Session> sessions) throws OXException {
        if (null == sessions || sessions.isEmpty()) {
            return;
        }
        ensureActive();
        try {
            IMap<String, PortableSession> sessionsMap = sessions();
            for (Session session : sessions) {
                sessionsMap.putIfAbsent(session.getSessionID(), new PortableSession(session));
            }
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (RuntimeException e) {
            handleRuntimeException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    @Override
    public boolean addSessionIfAbsent(final Session session) throws OXException {
        if (null == session) {
            return false;
        }
        ensureActive();
        try {
            return null == sessions().putIfAbsent(session.getSessionID(), new PortableSession(session));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            throw handleHazelcastException(e, SessionStorageExceptionCodes.SAVE_FAILED.create(e, session.getSessionID()));
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                throw SessionStorageExceptionCodes.SAVE_FAILED.create(e, session.getSessionID());
            }
            throw e;
        }
    }

    @Override
    public void addSession(final Session session) throws OXException {
        if (null != session) {
            ensureActive();
            try {
                sessions().set(session.getSessionID(), new PortableSession(session), 0, TimeUnit.SECONDS);
            } catch (HazelcastInstanceNotActiveException e) {
                throw handleNotActiveException(e);
            } catch (HazelcastException e) {
                throw handleHazelcastException(e, SessionStorageExceptionCodes.SAVE_FAILED.create(e, session.getSessionID()));
            }
        }
    }

    @Override
    public void removeSession(final String sessionId) throws OXException {
        if (null != sessionId) {
            ensureActive();
            try {
                PortableSession removedSession = sessions().remove(sessionId);
                if (null == removedSession) {
                    LOG.debug("Session with ID '{}' not found, unable to remove from storage.", sessionId);
                }
            } catch (HazelcastInstanceNotActiveException e) {
                throw handleNotActiveException(e);
            } catch (HazelcastException e) {
                throw handleHazelcastException(e, SessionStorageExceptionCodes.REMOVE_FAILED.create(e, sessionId));
            } catch (OXException e) {
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    throw SessionStorageExceptionCodes.REMOVE_FAILED.create(e, sessionId);
                }
                throw e;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Session> removeSessions(List<String> sessionIds) throws OXException {
        int size;
        if (null == sessionIds || 0 == (size = sessionIds.size())) {
            return Collections.emptyList();
        }
        /*
         * check if still active
         */
        ensureActive();
        /*
         * remove sessions...
         */
        IMap<String, PortableSession> sessions = sessions();
        /*
         * schedule remove operations
         */
        return removeSessionsByIds(sessionIds, size, sessions);
    }

    private List<Session> removeSessionsByIds(Collection<String> sessionIds, int size, IMap<String, PortableSession> sessions) throws OXException {
        CollectingRemovedSessionHandler removedSessionHandler = new CollectingRemovedSessionHandler(size);
        removeSessionsByIds(sessionIds, size, removedSessionHandler, sessions);
        return removedSessionHandler.removedSessions;
    }

    private void removeSessionsByIds(Collection<String> sessionIds, int size, RemovedSessionHandler removedSessionHandler, IMap<String, PortableSession> sessions) throws OXException {
        /*
         * schedule remove operations
         */
        Map<String, Future<PortableSession>> futures = new HashMap<String, Future<PortableSession>>(size);
        for (String sessionID : sessionIds) {
            futures.put(sessionID, sessions.removeAsync(sessionID));
        }
        /*
         * collect removed sessions
         */
        for (Entry<String, Future<PortableSession>> future : futures.entrySet()) {
            try {
                PortableSession removedSession = future.getValue().get();
                if (null == removedSession) {
                    LOG.debug("Session with ID '{}' not found, unable to remove from storage.", future.getKey());
                } else {
                    removedSessionHandler.handleRemovedSession(removedSession);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw SessionStorageExceptionCodes.REMOVE_FAILED.create(e, future.getKey());
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof HazelcastInstanceNotActiveException) {
                    throw handleNotActiveException((HazelcastInstanceNotActiveException) cause);
                }
                if (cause instanceof HazelcastException) {
                    throw handleHazelcastException((HazelcastException) cause, SessionStorageExceptionCodes.REMOVE_FAILED.create(ThreadPools.launderThrowable(e, HazelcastException.class), future.getKey()));
                }

                // Launder...
                throw SessionStorageExceptionCodes.REMOVE_FAILED.create(ThreadPools.launderThrowable(e, HazelcastException.class), future.getKey());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session[] removeLocalUserSessions(int userId, int contextId) throws OXException {
        ensureActive();
        /*
         * search sessions by context- and user-ID
         */
        IMap<String, PortableSession> sessions = sessions();
        Set<String> sessionIDs = sessions.localKeySet(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId + " AND " + PortableSession.PARAMETER_USER_ID + " = " + userId));
        int size;
        if (null == sessionIDs || 0 == (size = sessionIDs.size())) {
            return new Session[0];
        }
        List<Session> removedSessions = removeSessionsByIds(sessionIDs, size, sessions);
        return removedSessions.toArray(new Session[removedSessions.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session[] removeUserSessions(final int userId, final int contextId) throws OXException {
        ensureActive();
        /*
         * search sessions by context- and user-ID
         */
        IMap<String, PortableSession> sessions = sessions();
        Set<String> sessionIDs = sessions.keySet(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId + " AND " + PortableSession.PARAMETER_USER_ID + " = " + userId));
        int size;
        if (null == sessionIDs || 0 == (size = sessionIDs.size())) {
            return new Session[0];
        }
        List<Session> removedSessions = removeSessionsByIds(sessionIDs, size, sessions);
        return removedSessions.toArray(new Session[removedSessions.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLocalContextSessions(int contextId) throws OXException {
        ensureActive();
        /*
         * search sessions by context ID
         */
        IMap<String, PortableSession> sessions = sessions();
        Set<String> sessionIDs = sessions.localKeySet(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId));
        int size;
        if (null == sessionIDs || 0 == (size = sessionIDs.size())) {
            return;
        }
        /*
         * schedule remove operations
         */
        removeSessionsByIds(sessionIDs, size, NOOP_REMOVED_SESSION_HANDLER, sessions);
    }

    @Override
    public void removeContextSessions(final int contextId) throws OXException {
        ensureActive();
        /*
         * search sessions by context ID
         */
        IMap<String, PortableSession> sessions = sessions();
        Set<String> sessionIDs = sessions.keySet(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId));
        int size;
        if (null == sessionIDs || 0 == (size = sessionIDs.size())) {
            return;
        }
        /*
         * schedule remove operations
         */
        removeSessionsByIds(sessionIDs, size, NOOP_REMOVED_SESSION_HANDLER, sessions);
    }

    @Override
    public boolean hasForContext(final int contextId) throws OXException {
        ensureActive();
        try {
            Session session = findSession(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId));
            if (null != session && session.getContextId() == contextId) {
                return true;
            }
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            LOG.debug("", e);
        }
        /*
         * none found
         */
        return false;
    }

    @Override
    public Session[] getUserSessions(final int userId, final int contextId) throws OXException {
        ensureActive();
        try {
            /*
             * find sessions by context- and user-ID
             */
            Collection<PortableSession> sessions =
                sessions().values(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId + " AND " + PortableSession.PARAMETER_USER_ID + " = " + userId));
            return null == sessions ? new Session[0] : sessions.toArray(new Session[sessions.size()]);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (RuntimeException e) {
            throw handleRuntimeException(e, null);
        }
    }

    @Override
    public Session getAnyActiveSessionForUser(final int userId, final int contextId) throws OXException {
        ensureActive();
        try {
            /*
             * find session by context- and user-ID
             */
            return findSession(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId + " AND " + PortableSession.PARAMETER_USER_ID + " = " + userId));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (RuntimeException e) {
            throw handleRuntimeException(e, null);
        }
    }

    @Override
    public Session findFirstSessionForUser(final int userId, final int contextId) throws OXException {
        return getAnyActiveSessionForUser(userId, contextId);
    }

    @Override
    public List<Session> getSessions() {
        if (inactive.get()) {
            return Collections.emptyList();
        }
        try {
            return new ArrayList<Session>(sessions().values());
        } catch (HazelcastInstanceNotActiveException e) {
            inactive.set(true);
        } catch (final HazelcastException e) {
            LOG.debug("", e);
        } catch (OXException e) {
            LOG.debug("", e);

        }
        return Collections.emptyList();
    }

    @Override
    public int getNumberOfActiveSessions() {
        if (inactive.get()) {
            return 0;
        }
        try {
            return sessions().size();
        } catch (HazelcastInstanceNotActiveException e) {
            inactive.set(true);
        } catch (OXException e) {
            LOG.debug("", e);

        }
        return 0;
    }

    @Override
    public Session getSessionByRandomToken(final String randomToken, final String newIP) throws OXException {
        ensureActive();
        try {
            if (null != randomToken) {
                PortableSession session = findSession(new SqlPredicate(PortableSession.PARAMETER_RANDOM_TOKEN + " = '" + randomToken + "'"));
                if (null != session && randomToken.equals(session.getRandomToken())) {
                    if (false == session.getLocalIp().equals(newIP)) {
                        session.setLocalIp(newIP);
                        sessions().set(session.getSessionId(), session, 0, TimeUnit.SECONDS);
                    }
                    return session;
                }
            }
            throw SessionStorageExceptionCodes.RANDOM_NOT_FOUND.create(randomToken);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            LOG.debug("", e);
            throw handleHazelcastException(e, SessionStorageExceptionCodes.RANDOM_NOT_FOUND.create(e, randomToken));
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                LOG.debug("", e);

                throw SessionStorageExceptionCodes.RANDOM_NOT_FOUND.create(e, randomToken);
            }
            throw e;
        }
    }

    @Override
    public Session getSessionByAlternativeId(final String altId) throws OXException {
        if (null == altId) {
            throw new NullPointerException("altId is null.");
        }

        ensureActive();
        try {
            // Session session = findSession(new AltIdPredicate(altId));
            Session session = findSession(new SqlPredicate(PortableSession.PARAMETER_ALT_ID + " = '" + altId + "'"));
            if (null != session && altId.equals(session.getParameter(Session.PARAM_ALTERNATIVE_ID))) {
                return session;
            }
            throw SessionStorageExceptionCodes.ALTID_NOT_FOUND.create(altId);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            LOG.debug("", e);
            throw handleHazelcastException(e, SessionStorageExceptionCodes.ALTID_NOT_FOUND.create(e, altId));
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                LOG.debug("", e);
                throw SessionStorageExceptionCodes.ALTID_NOT_FOUND.create(e, altId);
            }
            throw e;
        }
    }

    @Override
    public Session getCachedSession(final String sessionId) throws OXException {
        return lookupSession(sessionId);
    }

    @Override
    public void cleanUp() throws OXException {
        ensureActive();
        try {
            sessions().clear();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public void changePassword(final String sessionId, final String newPassword) throws OXException {
        ensureActive();
        try {
            IMap<String, PortableSession> sessions = sessions();
            PortableSession storedSession = sessions.get(sessionId);
            if (null == storedSession) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
            }
            storedSession.setPassword(newPassword);
            sessions.set(sessionId, storedSession, 0, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            LOG.debug("", e);
            throw handleHazelcastException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    @Override
    public void setLocalIp(String sessionId, String localIp) throws OXException {
        ensureActive();
        try {
            IMap<String, PortableSession> sessions = sessions();
            PortableSession storedSession = sessions.get(sessionId);
            if (null == storedSession) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
            }
            storedSession.setLocalIp(localIp);
            sessions.set(sessionId, storedSession, 0, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            LOG.debug("", e);
            throw handleHazelcastException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    @Override
    public void setClient(String sessionId, String client) throws OXException {
        ensureActive();
        try {
            IMap<String, PortableSession> sessions = sessions();
            PortableSession storedSession = sessions.get(sessionId);
            if (null == storedSession) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
            }
            storedSession.setClient(client);
            sessions.set(sessionId, storedSession, 0, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            LOG.debug("", e);
            throw handleHazelcastException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    @Override
    public void setHash(String sessionId, String hash) throws OXException {
        ensureActive();
        try {
            IMap<String, PortableSession> sessions = sessions();
            PortableSession storedSession = sessions.get(sessionId);
            if (null == storedSession) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
            }
            storedSession.setHash(hash);
            sessions.set(sessionId, storedSession, 0, TimeUnit.SECONDS);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            LOG.debug("", e);
            throw handleHazelcastException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    @Override
    public void checkAuthId(final String login, final String authId) throws OXException {
        if (null != authId) {
            ensureActive();
            try {
                Session session = findSession(new SqlPredicate(PortableSession.PARAMETER_AUTH_ID + " = '" + authId + "'"));
                if (null != session && authId.equals(session.getAuthId())) {
                    throw SessionStorageExceptionCodes.DUPLICATE_AUTHID.create(session.getLogin(), login);
                }
            } catch (HazelcastInstanceNotActiveException e) {
                throw handleNotActiveException(e);
            } catch (final HazelcastException e) {
                LOG.debug("", e);
                throw handleHazelcastException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
            }
        }
    }

    @Override
    public int getUserSessionCount(int userId, int contextId) throws OXException {
        ensureActive();
        try {
            /*
             * search sessions by context- and user-ID
             */
            IMap<String, PortableSession> sessions = sessions();
            Set<String> sessionIDs =
                sessions.keySet(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId + " AND " + PortableSession.PARAMETER_USER_ID + " = " + userId));
            return null != sessionIDs ? sessionIDs.size() : 0;
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (final HazelcastException e) {
            LOG.debug("", e);
            throw handleHazelcastException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    /**
     * 'Touches' multiple sessions in the storage causing the map entry's idle time being reseted.
     *
     * @param sessionIDs The session ID to touch
     * @return The number of successfully touched sessions
     */
    public int touch(List<String> sessionIDs) throws OXException {
        int touched = 0;
        ensureActive();
        try {
            /*
             * calling containsKey resets map entries idle-time
             */
            IMap<String, PortableSession> sessions = sessions();
            for (String sessionID : sessionIDs) {
                if (false == sessions.containsKey(sessionID)) {
                    LOG.debug("Ignoring keep-alive even for not found session ID: {}", sessionID);
                } else {
                    touched++;
                    LOG.debug("Received keep-alive for '{}'.", sessionID);
                }
            }
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            LOG.debug("", e);
            throw handleHazelcastException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
        return touched;
    }

    /**
     * Gets a session by the supplied predicate, trying to query the local map entries first, and the whole distributed map afterwards if
     * needed.
     *
     * @param predicate The predicate to use
     * @return The first found matching session, or <code>null</code> if not found
     * @throws OXException
     */
    private PortableSession findSession(Predicate<?, ?> predicate) throws OXException, HazelcastException {
        IMap<String, PortableSession> sessions = sessions();
        if (null == sessions) {
            return null;
        }
        /*
         * try to lookup session from local keyset first
         */
        Set<String> localKeySet = sessions.localKeySet(predicate);
        if (null != localKeySet && 0 < localKeySet.size()) {
            for (String key : localKeySet) {
                PortableSession storedSession = sessions.get(key);
                if (null != storedSession) {
                    return storedSession;
                }
            }
        }
        /*
         * also query cluster if not yet found
         */
        Set<String> keySet = sessions.keySet(predicate);
        if (null != keySet && 0 < keySet.size()) {
            for (String key : keySet) {
                PortableSession storedSession = sessions.get(key);
                if (null != storedSession) {
                    return storedSession;
                }
            }
        }
        /*
         * not found
         */
        return null;
    }

    /**
     * Gets the 'sessions' map that maps session-IDs to stored sessions.
     *
     * @return The 'sessions' map
     * @throws OXException
     */
    private IMap<String, PortableSession> sessions() throws OXException {
        try {
            HazelcastInstance hazelcastInstance = REFERENCE.get();
            if (null == hazelcastInstance) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
            }
            return hazelcastInstance.getMap(sessionsMapName);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            throw handleHazelcastException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        } catch (RuntimeException e) {
            throw handleRuntimeException(e, SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
        }
    }

    // ------------------------------------ Exception handling ------------------------------------------------------------ //

    private OXException handleRuntimeException(RuntimeException e, OXException optRegular) {
        if (e instanceof HazelcastException) {
            return handleHazelcastException((HazelcastException) e, optRegular);
        }
        return handleRegular0(optRegular, e);
    }

    private OXException handleHazelcastException(HazelcastException e, OXException optRegular) {
        /*
         * Due to handling in 'com.hazelcast.util.ExceptionUtil.rethrow(Throwable)' a possible InterruptedException gets rethrown as a new
         * HazelcastException wrapping that InterruptedException instance
         */
        return isInterruptedException(e) ? SessionStorageExceptionCodes.INTERRUPTED.create(e, new Object[0]) : handleRegular0(optRegular, e);
    }

    private OXException handleRegular0(OXException optRegular, Exception cause) {
        return null == optRegular ? SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage()) : optRegular;
    }

    private boolean isInterruptedException(HazelcastException e) {
        return null == e ? false : isInterruptedException0(e.getCause());
    }

    private boolean isInterruptedException0(Throwable t) {
        if (null == t) {
            return false;
        }
        if (t instanceof InterruptedException) {
            return true;
        }

        return isInterruptedException0(t.getCause());
    }

}
