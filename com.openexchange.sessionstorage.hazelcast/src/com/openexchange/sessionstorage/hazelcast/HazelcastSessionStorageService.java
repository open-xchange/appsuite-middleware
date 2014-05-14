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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import com.openexchange.sessionstorage.hazelcast.portable.PortableSession;
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

    // ----------------------------------------------------------------------------------------------------------------------------------------------- //

    private final String sessionsMapName;
    private final AtomicBoolean inactive;

    /**
     * Initializes a new {@link HazelcastSessionStorageService}.
     *
     * @param sessionsMapName The name of the distributed 'sessions' map
     */
    public HazelcastSessionStorageService(String sessionsMapName) {
        super();
        this.sessionsMapName = sessionsMapName;
        inactive = new AtomicBoolean(false);
    }

    private void ensureActive() throws OXException {
        if (inactive.get()) {
            throw SessionStorageExceptionCodes.SESSION_STORAGE_DOWN.create(HazelcastSessionStorageService.class);
        }
    }

    private OXException handleNotActiveException(HazelcastInstanceNotActiveException e) {
        inactive.set(true);
        return SessionStorageExceptionCodes.SESSION_STORAGE_DOWN.create(e, HazelcastSessionStorageService.class.getName());
    }

    @Override
    public Session lookupSession(final String sessionId) throws OXException {
        ensureActive();
        try {
            PortableSession storedSession = sessions().get(sessionId);
            if (null == storedSession) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
            }
            return storedSession;
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        } catch (HazelcastException e) {
            throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(e, sessionId);
        } catch (OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(e, sessionId);
            }
            throw e;
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
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
        } catch (final HazelcastException e) {
            throw SessionStorageExceptionCodes.SAVE_FAILED.create(e, session.getSessionID());
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
                throw SessionStorageExceptionCodes.SAVE_FAILED.create(e, session.getSessionID());
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
                throw SessionStorageExceptionCodes.REMOVE_FAILED.create(e, sessionId);
            } catch (OXException e) {
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    throw SessionStorageExceptionCodes.REMOVE_FAILED.create(e, sessionId);
                }
                throw e;
            }
        }
    }

    @Override
    public Session[] removeUserSessions(final int userId, final int contextId) throws OXException {
        ensureActive();
        /*
         * search sessions by context- and user-ID
         */
        IMap<String, PortableSession> sessions = sessions();
        Set<String> sessionIDs =
            sessions.keySet(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId + " AND " + PortableSession.PARAMETER_USER_ID + " = " + userId));
        if (null == sessionIDs || 0 == sessionIDs.size()) {
            return new Session[0];
        }
        /*
         * schedule remove operations
         */
        Map<String, Future<PortableSession>> futures = new HashMap<String, Future<PortableSession>>(sessionIDs.size());
        for (String sessionID : sessionIDs) {
            futures.put(sessionID, sessions.removeAsync(sessionID));
        }
        /*
         * collect removed sessions
         */
        List<Session> removedSessions = new ArrayList<Session>(sessionIDs.size());
        for (Entry<String, Future<PortableSession>> future : futures.entrySet()) {
            try {
                PortableSession removedSession = future.getValue().get();
                if (null != removedSession) {
                    removedSessions.add(removedSession);
                } else {
                    LOG.debug("Session with ID '{}' not found, unable to remove from storage.", future.getKey());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw SessionStorageExceptionCodes.REMOVE_FAILED.create(e, future.getKey());
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof HazelcastInstanceNotActiveException) {
                    throw handleNotActiveException((HazelcastInstanceNotActiveException) cause);
                }

                // Launder...
                throw SessionStorageExceptionCodes.REMOVE_FAILED.create(ThreadPools.launderThrowable(e, HazelcastException.class), future.getKey());
            }
        }
        return removedSessions.toArray(new Session[removedSessions.size()]);
    }

    @Override
    public void removeContextSessions(final int contextId) throws OXException {
        ensureActive();
        /*
         * search sessions by context ID
         */
        IMap<String, PortableSession> sessions = sessions();
        Set<String> sessionIDs = sessions.keySet(new SqlPredicate(PortableSession.PARAMETER_CONTEXT_ID + " = " + contextId));
        if (null == sessionIDs || 0 == sessionIDs.size()) {
            return;
        }
        /*
         * schedule remove operations
         */
        Map<String, Future<PortableSession>> futures = new HashMap<String, Future<PortableSession>>(sessionIDs.size());
        for (String sessionID : sessionIDs) {
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
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw SessionStorageExceptionCodes.REMOVE_FAILED.create(e, future.getKey());
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof HazelcastInstanceNotActiveException) {
                    throw handleNotActiveException((HazelcastInstanceNotActiveException) cause);
                }

                // Launder...
                throw SessionStorageExceptionCodes.REMOVE_FAILED.create(ThreadPools.launderThrowable(e, HazelcastException.class), future.getKey());
            }
        }
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
            return null != sessions ? sessions.toArray(new Session[sessions.size()]) : new Session[0];
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
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
        } catch (final HazelcastException e) {
            LOG.debug("", e);
            throw SessionStorageExceptionCodes.RANDOM_NOT_FOUND.create(e, randomToken);
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
        } catch (final HazelcastException e) {
            LOG.debug("", e);
            throw SessionStorageExceptionCodes.ALTID_NOT_FOUND.create(e, altId);
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
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
                throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
        }
    }

    /**
     * 'Touches' a session in the storage causing the map entry's idle time being reseted.
     *
     * @param sessionID The session ID
     * @throws OXException
     */
    public void touch(String sessionID) throws OXException {
        ensureActive();
        try {
            /*
             * calling containsKey resets map entries idle-time
             */
            if (false == sessions().containsKey(sessionID)) {
                LOG.debug("Ignoring keep-alive even for not found session ID: {}", sessionID);
            } else {
                LOG.debug("Received keep-alive for '{}'.", sessionID);
            }
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
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
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
