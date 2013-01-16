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

package com.openexchange.sessionstorage.hazelcast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEntry;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.Hazelcasts;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageExceptionCodes;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.AbortBehavior;

/**
 * {@link HazelcastSessionStorageService} - The {@link SessionStorageService} backed by {@link HazelcastInstance}.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastSessionStorageService implements SessionStorageService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastSessionStorageService.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final AtomicReference<HazelcastInstance> REFERENCE = new AtomicReference<HazelcastInstance>();

    /**
     * Sets specified {@link HazelcastInstance}.
     *
     * @param hazelcast The {@link HazelcastInstance}
     */
    public static void setHazelcastInstance(final HazelcastInstance hazelcast) {
        REFERENCE.set(hazelcast);
    }

    private static final class GetSessionMapTask extends AbstractTask<IMap<String, HazelcastStoredSession>> {

        private final HazelcastInstance hazelcastInstance;
        private final String mapName;

        protected GetSessionMapTask(final HazelcastInstance hazelcastInstance, final String mapName) {
            super();
            this.hazelcastInstance = hazelcastInstance;
            this.mapName = mapName;
        }

        @Override
        public IMap<String, HazelcastStoredSession> call() throws Exception {
            return hazelcastInstance.getMap(mapName);
        }
    }

    private static volatile Integer timeout;
    private static int timeout() {
        Integer tmp = timeout;
        if (null == tmp) {
            synchronized (HazelcastSessionStorageService.class) {
                tmp = timeout;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? 250 : service.getIntProperty("com.openexchange.sessionstorage.hazelcast.timeout", 250));
                    timeout = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private final String mapName;
    private final RefusedExecutionBehavior<IMap<String, HazelcastStoredSession>> abortBehavior;
    private boolean allowFailIfPaused;

    /**
     * Initializes a new {@link HazelcastSessionStorageService}.
     *
     * @param mapName The name of the distributed sessions map
     */
    public HazelcastSessionStorageService(String mapName) {
        super();
        this.mapName = mapName;
        abortBehavior = AbortBehavior.<IMap<String, HazelcastStoredSession>> getInstance();
        allowFailIfPaused = false;
    }

    /**
     * Sets the fail-if-paused behavior.
     *
     * @param allowFailIfPaused <code>true</code> to set the fail-if-paused behavior; else <code>false</code>
     * @return This session storage with new behavior applied
     */
    public HazelcastSessionStorageService setAllowFailIfPaused(boolean allowFailIfPaused) {
        this.allowFailIfPaused = allowFailIfPaused;
        return this;
    }

    private IMap<String, HazelcastStoredSession> getMapFrom(final Future<IMap<String, HazelcastStoredSession>> f) throws OXException {
        try {
            return f.get(timeout(), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof OXException) {
                throw (OXException) t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw new IllegalStateException("Not unchecked", t);
        } catch (final TimeoutException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, HazelcastInstance.class.getName());
        } catch (final CancellationException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, HazelcastInstance.class.getName());
        }
    }

    /**
     * Gets the map
     *
     * @return The map
     * @throws OXException If service is unavailable or currently paused
     */
    private IMap<String, HazelcastStoredSession> sessions(final boolean failIfPaused) throws OXException {
        try {
            final HazelcastInstance hazelcastInstance = REFERENCE.get();
            if (null == hazelcastInstance) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
            }
            final IMap<String, HazelcastStoredSession> sessions;
            if (failIfPaused && allowFailIfPaused) {
                // Fail if paused
                if (Hazelcasts.isPaused()) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
                }
                final ThreadPoolService threadPool = ThreadPools.getThreadPool();
                if (null == threadPool) {
                    sessions = hazelcastInstance.getMap(mapName);
                } else {
                    final IMap<String, HazelcastStoredSession> map = getMapFrom(threadPool.submit(new GetSessionMapTask(hazelcastInstance, mapName), abortBehavior));
                    if (null == map) {
                        throw new HazelcastException("No such map: " + mapName);
                    }
                    sessions = new TimeoutAwareIMap(map, timeout());
                }
            } else {
                sessions = hazelcastInstance.getMap(mapName);
            }
            return sessions;
        } catch (final OXException e) {
            throw e;
        } catch (final HazelcastException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the map
     *
     * @return The map
     * @throws HazelcastException If service is unavailable or currently paused
     */
    private IMap<String, HazelcastStoredSession> sessionsUnchecked(final boolean failIfPaused) {
        try {
            final IMap<String, HazelcastStoredSession> map = sessions(failIfPaused);
            if (null == map) {
                throw new HazelcastException("No such map: " + mapName);
            }
            return map;
        } catch (final OXException e) {
            throw new HazelcastException(e.getDisplayMessage(Locale.US), e);
        }
    }

    @Override
    public Session lookupSession(final String sessionId) throws OXException {
        try {
            HazelcastStoredSession storedSession = sessions(true).get(sessionId);
            if (null == storedSession) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
            }
            return storedSession;
        } catch (final HazelcastException e) {
            throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(e, sessionId);
        } catch (final OXException e) {
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
        try {
            final IMap<String, HazelcastStoredSession> sessionsMap = sessions(false);
            for (final Session session : sessions) {
                try {
                    final HazelcastStoredSession ss = new HazelcastStoredSession(session);
                    sessionsMap.putIfAbsent(session.getSessionID(), ss);
                } catch (final HazelcastException e) {
                    LOG.warn("Session "+ session.getSessionID() + " could not be added to session storage.", e);
                }
            }
        } catch (final RuntimeException e) {
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean addSessionIfAbsent(final Session session) throws OXException {
        if (null == session) {
            return false;
        }
        try {
            final HazelcastStoredSession ss = new HazelcastStoredSession(session);
            return null == sessions(false).putIfAbsent(session.getSessionID(), ss);
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
            try {
                sessions(false).set(session.getSessionID(), new HazelcastStoredSession(session), 0, TimeUnit.SECONDS);
            } catch (final HazelcastException e) {
                throw SessionStorageExceptionCodes.SAVE_FAILED.create(e, session.getSessionID());
            } catch (final OXException e) {
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    throw SessionStorageExceptionCodes.SAVE_FAILED.create(e, session.getSessionID());
                }
                throw e;
            }
        }
    }

    @Override
    public void removeSession(final String sessionId) throws OXException {
        if (null != sessionId) {
            try {
                sessions(false).remove(sessionId);
            } catch (final HazelcastException e) {
                throw SessionStorageExceptionCodes.REMOVE_FAILED.create(e, sessionId);
            } catch (final OXException e) {
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    throw SessionStorageExceptionCodes.REMOVE_FAILED.create(e, sessionId);
                }
                throw e;
            }
        }
    }

    @Override
    public Session[] removeUserSessions(final int userId, final int contextId) throws OXException {
        try {
            List<Session> removed = new ArrayList<Session>();
            IMap<String, HazelcastStoredSession> sessions = sessions(false);
            if (null != sessions && 0 < sessions.size()) {
                Predicate<?, ?> predicate = new SqlPredicate("contextId = " + contextId + " AND userId = " + userId);
                for (Entry<String, HazelcastStoredSession> entry : sessions.entrySet(predicate)) {
                    removed.add(sessions.remove(entry.getKey()));
                }
            }
            return removed.toArray(new Session[removed.size()]);
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
                return new Session[0];
            }
            throw e;
        }
    }

    @Override
    public void removeContextSessions(final int contextId) throws OXException {
        try {
            IMap<String, HazelcastStoredSession> sessions = sessions(false);
            if (null != sessions && 0 < sessions.size()) {
                for (Entry<String, HazelcastStoredSession> entry : sessions.entrySet(new SqlPredicate("contextId = " + contextId))) {
                    sessions.remove(entry.getKey());
                }
            }
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
            }
            throw e;
        }
    }

    @Override
    public boolean hasForContext(final int contextId) {
        SqlPredicate predicate = new SqlPredicate("contextId = " + contextId);
        try {
            /*
             * try to lookup session from local keyset first
             */
            for (HazelcastStoredSession session : filterLocal(predicate, true)) {
                if (null != session && session.getContextId() == contextId) {
                    return true;
                }
            }
            /*
             * also query cluster if not yet found
             */
            for (HazelcastStoredSession session : filter(predicate, true)) {
                if (null != session && session.getContextId() == contextId) {
                    return true;
                }
            }
        } catch (HazelcastException e) {
            LOG.debug(e.getMessage(), e);
        }
        /*
         * none found
         */
        return false;
    }

    @Override
    public Session[] getUserSessions(final int userId, final int contextId) {
        try {
            List<HazelcastStoredSession> found = new ArrayList<HazelcastStoredSession>();
            for (HazelcastStoredSession session : filter(new SqlPredicate("contextId = " + contextId + " AND userId = " + userId), true)) {
                if (null != session && session.getUserId() == userId && session.getContextId() == contextId) {
                    found.add(session);
                }
            }
            return found.toArray(new Session[found.size()]);
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return new Session[0];
        }
    }

    @Override
    public Session getAnyActiveSessionForUser(final int userId, final int contextId) {
        SqlPredicate predicate = new SqlPredicate("contextId = " + contextId + " AND userId = " + userId);
        try {
            /*
             * try to lookup session from local keyset first
             */
            for (HazelcastStoredSession session : filterLocal(predicate, true)) {
                if (null != session && session.getUserId() == userId && session.getContextId() == contextId) {
                    return new HazelcastStoredSession(session);
                }
            }
            /*
             * also query cluster if not yet found
             */
            for (HazelcastStoredSession session : filter(predicate, true)) {
                if (null != session && session.getUserId() == userId && session.getContextId() == contextId) {
                    return new HazelcastStoredSession(session);
                }
            }
        } catch (HazelcastException e) {
            LOG.debug(e.getMessage(), e);
        }
        /*
         * not found
         */
        return null;
    }

    @Override
    public Session findFirstSessionForUser(final int userId, final int contextId) {
        return getAnyActiveSessionForUser(userId, contextId);
    }

    @Override
    public List<Session> getSessions() {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked(true);
            final List<Session> retval = new ArrayList<Session>();
            for (final String sessionId : sessions.keySet()) {
                final HazelcastStoredSession storedSession = sessions.get(sessionId);
                if (null != storedSession) {
                    retval.add(storedSession);
                }
            }
            return retval;
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public int getNumberOfActiveSessions() {
        return sessionsUnchecked(true).size();
    }

    @Override
    public Session getSessionByRandomToken(final String randomToken, final String newIP) throws OXException {
        try {
            if (null != randomToken) {
                for (HazelcastStoredSession session : filter(new SqlPredicate("randomToken = '" + randomToken + "'"), true)) {
                    if (null != session && randomToken.equals(session.getRandomToken())) {
                        if (false == session.getLocalIp().equals(newIP)) {
                            session.setLocalIp(newIP);
                        }
                        return session;
                    }
                }
            }
            throw SessionStorageExceptionCodes.RANDOM_NOT_FOUND.create(randomToken);
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.RANDOM_NOT_FOUND.create(e, randomToken);
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
                throw SessionStorageExceptionCodes.RANDOM_NOT_FOUND.create(e, randomToken);
            }
            throw e;
        }
    }

    @Override
    public Session getSessionByAlternativeId(final String altId) throws OXException {
        try {
            if (null == altId) {
                throw new NullPointerException("altId is null.");
            }
            Predicate<String, HazelcastStoredSession> altIdPredicate = new Predicate<String, HazelcastStoredSession>() {

                private static final long serialVersionUID = -4810797295980425113L;

                @Override
                public boolean apply(MapEntry<String, HazelcastStoredSession> mapEntry) {
                    return null != mapEntry && null != mapEntry.getValue() &&
                        altId.equals(mapEntry.getValue().getParameter(Session.PARAM_ALTERNATIVE_ID));
                }
            };
            for (HazelcastStoredSession session : filter(altIdPredicate, true)) {
                if (null != session && altId.equals(session.getParameter(Session.PARAM_ALTERNATIVE_ID))) {
                    return session;
                }
            }
            throw SessionStorageExceptionCodes.ALTID_NOT_FOUND.create(altId);
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.ALTID_NOT_FOUND.create(e, altId);
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
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
    public void cleanUp() {
        sessionsUnchecked(false).clear();
    }

    @Override
    public void changePassword(final String sessionId, final String newPassword) throws OXException {
        try {
            IMap<String, HazelcastStoredSession> sessions = sessions(true);
            HazelcastStoredSession storedSession = sessions.get(sessionId);
            if (null == storedSession) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
            }
            storedSession.setPassword(newPassword);
            sessions.set(sessionId, storedSession, 0, TimeUnit.SECONDS);
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void checkAuthId(final String login, final String authId) throws OXException {
        if (null != authId) {
            try {
                for (HazelcastStoredSession session : filter(new SqlPredicate("authId = '" + authId + "'"), true)) {
                    if (null != session && authId.equals(session.getAuthId())) {
                        throw SessionStorageExceptionCodes.DUPLICATE_AUTHID.create(session.getLogin(), login);
                    }
                }
            } catch (final HazelcastException e) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
                throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    /**
     * Filters the stored sessions by a {@link Predicate}.
     *
     * @param predicate The predicate to use for filtering
     * @param failIfPaused <code>true</code> to abort if the hazelcast instance is paused, <code>false</code>, otherwise
     * @return The stored sessions matching the predicate, or an empty collection if none were found
     */
    private Collection<HazelcastStoredSession> filter(Predicate<?, ?> predicate, boolean failIfPaused) {
        IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked(failIfPaused);
        if (null != sessions) {
            return sessions.values(predicate);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Filters the locally available stored sessions by a {@link Predicate}.
     *
     * @param predicate The predicate to use for filtering
     * @param failIfPaused <code>true</code> to abort if the hazelcast instance is paused, <code>false</code>, otherwise
     * @return The stored sessions matching the predicate, or an empty collection if none were found
     */
    private Collection<HazelcastStoredSession> filterLocal(Predicate<?, ?> predicate, boolean failIfPaused) {
        IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked(failIfPaused);
        if (null != sessions) {
            Collection<HazelcastStoredSession> values = new ArrayList<HazelcastStoredSession>();
            Set<String> localKeySet = sessions.localKeySet(predicate);
            if (null != localKeySet && 0 < localKeySet.size()) {
                for (String key : localKeySet) {
                    HazelcastStoredSession storedSession = sessions.get(key);
                    if (null != storedSession) {
                        values.add(storedSession);
                    }
                }
            }
            return values;
        } else {
            return Collections.emptyList();
        }
    }

}
