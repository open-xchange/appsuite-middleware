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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.logging.Log;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcasts;
import com.hazelcast.core.IMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageExceptionCodes;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.hazelcast.exceptions.OXHazelcastSessionStorageExceptionCodes;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link HazelcastSessionStorageService} - The {@link SessionStorageService} backed by {@link HazelcastInstance}.
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastSessionStorageService implements SessionStorageService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastSessionStorageService.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

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

    private static volatile Integer getSessionMapTimeout;
    private static int getSessionMapTimeout() {
        Integer tmp = getSessionMapTimeout;
        if (null == tmp) {
            synchronized (HazelcastSessionStorageService.class) {
                tmp = getSessionMapTimeout;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? 1000 : service.getIntProperty("com.openexchange.sessionstorage.hazelcast.getSessionMapTimeout", 1000));
                    getSessionMapTimeout = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private final String mapName;
    private final String encryptionKey;
    private final CryptoService cryptoService;
    private final RefusedExecutionBehavior<IMap<String, HazelcastStoredSession>> callerRunsBehavior;

    /**
     * Initializes a new {@link HazelcastSessionStorageService}.
     */
    public HazelcastSessionStorageService(final HazelcastSessionStorageConfiguration config, final HazelcastInstance hazelcast) {
        super();
        encryptionKey = config.getEncryptionKey();
        cryptoService = config.getCryptoService();
        final MapConfig mapConfig = config.getMapConfig();
        final String name = mapConfig.getName();
        mapName = name;
        final Config hzConfig = hazelcast.getConfig();
        if (null == hzConfig.getMapConfig(name)) {
            hzConfig.addMapConfig(mapConfig);
        }
        callerRunsBehavior = CallerRunsBehavior.<IMap<String, HazelcastStoredSession>> getInstance();
    }

    private IMap<String, HazelcastStoredSession> getMapFrom(final Future<IMap<String, HazelcastStoredSession>> f) throws OXException {
        try {
            return f.get(getSessionMapTimeout(), TimeUnit.MILLISECONDS);
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
            final HazelcastInstance hazelcastInstance = Services.optService(HazelcastInstance.class);
            if (null == hazelcastInstance) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
            }
            if (!failIfPaused) {
                return hazelcastInstance.getMap(mapName);
            }
            // Fail if paused
            if (Hazelcasts.isPaused()) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
            }
            final ThreadPoolService threadPool = ThreadPools.getThreadPool();
            if (null == threadPool) {
                return hazelcastInstance.getMap(mapName);
            }
            return getMapFrom(threadPool.submit(new GetSessionMapTask(hazelcastInstance, mapName), callerRunsBehavior));
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
            return sessions(failIfPaused);
        } catch (final OXException e) {
            throw new HazelcastException(e.getMessage(), e);
        }
    }

    @Override
    public Session lookupSession(final String sessionId) throws OXException {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessions(true);
            if (null != sessionId && sessions.containsKey(sessionId)) {
                final HazelcastStoredSession s = sessions.get(sessionId);
                s.setLastAccess(System.currentTimeMillis());
                s.setPassword(decrypt(s.getPassword()));
                sessions.replace(sessionId, s);
                return s;
            }
            throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
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
                    ss.setPassword(crypt(ss.getPassword()));
                    sessionsMap.putIfAbsent(session.getSessionID(), ss);
                } catch (final HazelcastException e) {
                    LOG.warn("Session "+ session.getSessionID() + " could not be added to session storage.", e);
                } catch (final OXException e) {
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
            ss.setPassword(crypt(ss.getPassword()));
            return null == sessions(false).putIfAbsent(session.getSessionID(), ss);
        } catch (final HazelcastException e) {
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_SAVE_FAILED.create(e, session.getSessionID());
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_SAVE_FAILED.create(e, session.getSessionID());
            }
            throw e;
        }
    }

    @Override
    public void addSession(final Session session) throws OXException {
        if (null != session) {
            try {
                final HazelcastStoredSession ss = new HazelcastStoredSession(session);
                ss.setPassword(crypt(ss.getPassword()));
                sessions(false).put(session.getSessionID(), ss);
            } catch (final HazelcastException e) {
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_SAVE_FAILED.create(e, session.getSessionID());
            } catch (final OXException e) {
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_SAVE_FAILED.create(e, session.getSessionID());
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
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_REMOVE_FAILED.create(e, sessionId);
            } catch (final OXException e) {
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_REMOVE_FAILED.create(e, sessionId);
                }
                throw e;
            }
        }
    }

    @Override
    public Session[] removeUserSessions(final int userId, final int contextId) throws OXException {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessions(false);
            final List<Session> removed = new ArrayList<Session>();
            for (final String sessionId : sessions.keySet()) {
                final Session s = sessions.get(sessionId);
                if (s.getUserId() == userId && s.getContextId() == contextId) {
                    removeSession(sessionId);
                    removed.add(s);
                }
            }
            final Session[] retval = new Session[removed.size()];
            int i = 0;
            for (final Session s : removed) {
                retval[i++] = s;
            }
            return retval;
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
            final IMap<String, HazelcastStoredSession> sessions = sessions(false);
            for (final String sessionId : sessions.keySet()) {
                final Session s = sessions.get(sessionId);
                if (s.getContextId() == contextId) {
                    removeSession(sessionId);
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
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked(true);
            for (final String sessionId : sessions.keySet()) {
                final Session s = sessions.get(sessionId);
                if (s.getContextId() == contextId) {
                    return true;
                }
            }
            return false;
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return false;
        }
    }

    @Override
    public Session[] getUserSessions(final int userId, final int contextId) {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked(true);
            final List<HazelcastStoredSession> found = new ArrayList<HazelcastStoredSession>();
            for (final String sessionId : sessions.keySet()) {
                final Session s = sessions.get(sessionId);
                if (s.getUserId() == userId && s.getContextId() == contextId) {
                    final HazelcastStoredSession ss = new HazelcastStoredSession(s);
                    ss.setLastAccess(System.currentTimeMillis());
                    found.add(ss);
                }
            }
            final Session[] retval = new Session[found.size()];
            int i = 0;
            for (final Session s : found) {
                retval[i++] = s;
            }
            return retval;
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return new Session[0];
        }
    }

    @Override
    public Session getAnyActiveSessionForUser(final int userId, final int contextId) {
        try {
            final Session[] userSessions = getUserSessions(userId, contextId);
            if (userSessions.length > 0) {
                return userSessions[0];
            }
            return null;
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public Session findFirstSessionForUser(final int userId, final int contextId) {
        return getAnyActiveSessionForUser(userId, contextId);
    }

    @Override
    public synchronized List<Session> getSessions() {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked(true);
            final List<Session> retval = new ArrayList<Session>();
            for (final String sessionId : sessions.keySet()) {
                retval.add(sessions.get(sessionId));
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
            for (final String sessionId : sessions(true).keySet()) {
                final Session s = lookupSession(sessionId);
                if (s.getRandomToken().equals(randomToken)) {
                    if (!s.getLocalIp().equals(newIP)) {
                        s.setLocalIp(newIP);
                    }
                    return s;
                }
            }
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND.create(randomToken);
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND.create(e, randomToken);
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND.create(e, randomToken);
            }
            throw e;
        }
    }

    @Override
    public Session getSessionByAlternativeId(final String altId) throws OXException {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessions(true);
            for (final String sessionId : sessions.keySet()) {
                final HazelcastStoredSession s = sessions.get(sessionId);
                if (s.getParameter(Session.PARAM_ALTERNATIVE_ID).equals(altId)) {
                    return s;
                }
            }
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND.create(altId);
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND.create(e, altId);
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND.create(e, altId);
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
            final Session s = lookupSession(sessionId);
            final HazelcastStoredSession ss = new HazelcastStoredSession(s);
            ss.setPassword(newPassword);
            ss.setLastAccess(System.currentTimeMillis());
            sessions(false).replace(sessionId, new HazelcastStoredSession(s), ss);
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void checkAuthId(final String login, final String authId) throws OXException {
        try {
            if (null != authId) {
                for (final Session session : getSessions()) {
                    if (authId.equals(session.getAuthId())) {
                        throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_DUPLICATE_AUTHID.create(
                            session.getLogin(),
                            login);
                    }
                }
            }
        } catch (final HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private String crypt(final String password) throws OXException {
        return cryptoService.encrypt(password, encryptionKey);
    }

    private String decrypt(final String encPassword) throws OXException {
        return cryptoService.decrypt(encPassword, encryptionKey);
    }

}
