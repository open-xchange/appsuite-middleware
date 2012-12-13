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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.sessiond.services.SessiondServiceRegistry.getServiceRegistry;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.caching.objects.CachedSession;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionCounter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.cache.SessionCache;
import com.openexchange.sessiond.services.SessiondServiceRegistry;
import com.openexchange.sessionstorage.SessionStorageExceptionCodes;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link SessionHandler} - Provides access to sessions
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionHandler {

    /**
     * The parameter name for session storage's {@link Future add task}.
     */
    //private static final String PARAM_SST_FUTURE = StoredSession.PARAM_SST_FUTURE;

    public static final SessionCounter SESSION_COUNTER = new SessionCounter() {

        @Override
        public int getNumberOfSessions(final int userId, final int contextId) {
            return sessionDataRef.get().getNumOfUserSessions(userId, contextId);
        }
    };

    private static volatile SessionIdGenerator sessionIdGenerator;

    static volatile SessiondConfigInterface config;

    protected static final AtomicReference<SessionData> sessionDataRef = new AtomicReference<SessionData>();

    private static volatile boolean noLimit;

    private static final AtomicBoolean initialized = new AtomicBoolean();

    /** Logger */
    protected static final Log LOG = com.openexchange.log.Log.loggerFor(SessionHandler.class);

    private static final boolean INFO = LOG.isInfoEnabled();

    /** Whether debug log level is enabled */
    protected static final boolean DEBUG = LOG.isDebugEnabled();

    private static volatile ScheduledTimerTask shortSessionContainerRotator;

    private static volatile ScheduledTimerTask longSessionContainerRotator;

    /**
     * Initializes a new {@link SessionHandler session handler}
     */
    private SessionHandler() {
        super();
    }

    /**
     * Initializes the {@link SessionHandler session handler}
     * 
     * @param newConfig The appropriate configuration
     */
    public static void init(final SessiondConfigInterface config) {
        SessionHandler.config = config;
        final SessionData sessionData = new SessionData(
            config.getNumberOfSessionContainers(),
            config.getMaxSessions(),
            config.getRandomTokenTimeout(),
            config.getNumberOfLongTermSessionContainers(),
            config.isAutoLogin());
        sessionDataRef.set(sessionData);
        if (initialized.compareAndSet(false, true)) {
            try {
                sessionIdGenerator = SessionIdGenerator.getInstance();
            } catch (final OXException exc) {
                LOG.error("create instance of SessionIdGenerator", exc);
            }
            noLimit = (config.getMaxSessions() == 0);
        }
    }

    /**
     * Removes all sessions associated with given user in specified context
     * 
     * @param userId The user ID
     * @param contextId The context ID
     * @param propagate <code>true</code> for remote removal; otherwise <code>false</code>
     * @return The wrapper objects for removed sessions
     */
    public static Session[] removeUserSessions(final int userId, final int contextId, final boolean propagate) {
        final SessionControl[] control = sessionDataRef.get().removeUserSessions(userId, contextId);
        final Session[] retval = new Session[control.length];
        Session[] retval2 = null;
        int i = 0;
        if (propagate) {
            for (final SessionControl sessionControl : control) {
                try {
                    SessionCache.getInstance().putCachedSessionForRemoteRemoval(sessionControl.getSession().createCachedSession());
                    retval[i++] = sessionControl.getSession();
                } catch (final OXException e) {
                    LOG.error("Remote removal failed for session " + sessionControl.getSession().getSecret(), e);
                }
            }
        }
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                final Task<Session[]> c = new AbstractTask<Session[]>() {
                    
                    @Override
                    public Session[] call() throws Exception {
                        return storageService.removeUserSessions(userId, contextId);
                    }
                };
                retval2 = getFrom(c, new Session[0]);
            } catch (final RuntimeException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (INFO) {
            LOG.info(new StringBuilder(64).append(propagate ? "Remote" : "Local").append(" removal of user sessions: User=").append(userId).append(
                ", Context=").append(contextId).toString());
        }
        return merge(retval, retval2);
    }

    /**
     * Removes all sessions associated with given context.
     * 
     * @param contextId The context ID
     * @param propagate <code>true</code> for remote removal; otherwise <code>false</code>
     */
    public static void removeContextSessions(final int contextId, final boolean propagate) {
        final List<SessionControl> list = sessionDataRef.get().removeContextSessions(contextId);
        if (propagate) {
            for (final SessionControl sessionControl : list) {
                try {
                    SessionCache.getInstance().putCachedSessionForRemoteRemoval(sessionControl.getSession().createCachedSession());
                } catch (final OXException e) {
                    LOG.warn("Remote removal failed for session " + sessionControl.getSession().getSecret(), e);
                }
            }
        }
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                final Task<Void> c = new AbstractTask<Void>() {
                    
                    @Override
                    public Void call() throws Exception {
                        storageService.removeContextSessions(contextId);
                        return null;
                    }
                };
                submitSafe(c);
            } catch (final RuntimeException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (INFO) {
            LOG.info(new StringBuilder(64).append(propagate ? "Remote" : "Local").append(" removal of sessions: Context=").append(contextId).toString());
        }
    }

    /**
     * Checks for any active session for specified context.
     * 
     * @param contextId The context identifier
     * @return <code>true</code> if at least one active session is found; otherwise <code>false</code>
     */
    public static boolean hasForContext(final int contextId) {
        boolean hasForContext = sessionDataRef.get().hasForContext(contextId);
        if (!hasForContext) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    final Task<Boolean> c = new AbstractTask<Boolean>() {
                        
                        @Override
                        public Boolean call() throws Exception {
                            return Boolean.valueOf(storageService.hasForContext(contextId));
                        }
                    };
                    hasForContext = getFrom(c, Boolean.FALSE).booleanValue();
                } catch (final RuntimeException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return hasForContext;
    }

    /**
     * Gets all sessions associated with given user in specified context
     * 
     * @param userId The user ID
     * @param contextId The context ID
     * @return The wrapper objects for sessions
     */
    public static SessionControl[] getUserSessions(final int userId, final int contextId) {
        SessionControl[] retval = sessionDataRef.get().getUserSessions(userId, contextId);
        if (retval == null) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    final Task<Session[]> c = new AbstractTask<Session[]>() {
                        
                        @Override
                        public Session[] call() throws Exception {
                            return storageService.getUserSessions(userId, contextId);
                        }
                    };
                    final Session[] sessions = getFrom(c, new Session[0]);
                    retval = new SessionControl[sessions.length];
                    for (int i = 0; i < sessions.length; i++) {
                        retval[i] = sessionToSessionControl(sessions[i]);
                    }
                } catch (final RuntimeException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return retval;
    }

    /**
     * Gets an active session of an user if available.
     * 
     * @param userId The user ID
     * @param contextId The context ID
     * @param includeLongTerm <code>true</code> to also lookup the long term sessions, <code>false</code>, otherwise
     * @param includeStorage <code>true</code> to also lookup the distributed session storage, <code>false</code>, otherwise
     * @return
     */
    public static SessionControl getAnyActiveSessionForUser(final int userId, final int contextId, final boolean includeLongTerm, 
        final boolean includeStorage) {
        SessionControl retval = sessionDataRef.get().getAnyActiveSessionForUser(userId, contextId, includeLongTerm);
        if (retval == null && includeStorage) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    final Task<Session> c = new AbstractTask<Session>() {
                        
                        @Override
                        public Session call() throws Exception {
                            return storageService.getAnyActiveSessionForUser(userId, contextId);
                        }
                    };
                    final Session storedSession = getFrom(c, null);
                    if (null != storedSession) {
                        retval = sessionToSessionControl(storedSession);
                    }
                } catch (final RuntimeException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return retval;
    }

    public static Session findFirstSessionForUser(final int userId, final int contextId, final SessionMatcher matcher) {
        Session retval = sessionDataRef.get().findFirstSessionForUser(userId, contextId, matcher);
        if (null == retval) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (null != storageService) {
                try {
                    final Task<Session> c = new AbstractTask<Session>() {
                        
                        @Override
                        public Session call() throws Exception {
                            return storageService.findFirstSessionForUser(userId, contextId);
                        }
                    };
                    retval = getFrom(c, null);
                } catch (final RuntimeException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return retval;
    }

    /**
     * Adds a new session containing given attributes to session container(s)
     * 
     * @param userId The user ID
     * @param loginName The user's login name
     * @param password The user's password
     * @param contextId The context identifier
     * @param clientHost The client host name or IP address
     * @param login The full user's login; e.g. <i>test@foo.bar</i>
     * @return The session ID associated with newly created session
     * @throws OXException If creating a new session fails
     */
    protected static SessionImpl addSession(final int userId, final String loginName, final String password, final int contextId, final String clientHost, final String login, final String authId, final String hash, final String client, final VolatileParams volatileParams) throws OXException {
        checkMaxSessPerUser(userId, contextId);
        checkMaxSessPerClient(client, userId, contextId);
        checkAuthId(login, authId);
        final SessionIdGenerator sessionIdGenerator = SessionHandler.sessionIdGenerator;
        final String sessionId = sessionIdGenerator.createSessionId(loginName, clientHost);
        final SessionImpl session = new SessionImpl(userId, loginName, password, contextId, sessionId, sessionIdGenerator.createSecretId(
            loginName,
            Long.toString(System.currentTimeMillis())), sessionIdGenerator.createRandomId(), clientHost, login, authId, hash, client);
        session.setVolatile(null != volatileParams);
        // Add session
        final SessionImpl addedSession = sessionDataRef.get().addSession(session, noLimit, volatileParams).getSession();
        final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
        if (sessionStorageService != null) {
            storeSession(addedSession, sessionStorageService, false);
        }
        // Post event for created session
        postSessionCreation(addedSession);
        // Return session ID
        return addedSession;
    }

    /**
     * Stores specified session.
     * 
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code>
     */
    public static void storeSession(final SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent) {
        if (null == session || null == sessionStorageService) {
            return;
        }
        ThreadPools.getThreadPool().submit(new StoreSessionTask(session, sessionStorageService, addIfAbsent));
    }

    /**
     * Stores specified session.
     * 
     * @param session The session to store
     * @param sessionStorageService The storage service
     */
    public static void storeSessions(final Collection<SessionImpl> sessions, final SessionStorageService sessionStorageService) {
        if (null == sessions || sessions.isEmpty() || null == sessionStorageService) {
            return;
        }
        for (final SessionImpl session : sessions) {
            storeSession(session, sessionStorageService, true);
        }
    }

    private static void checkMaxSessPerUser(final int userId, final int contextId) throws OXException {
        final int maxSessPerUser = config.getMaxSessionsPerUser();
        if (maxSessPerUser > 0) {
            final int count = sessionDataRef.get().getNumOfUserSessions(userId, contextId);
            if (count >= maxSessPerUser) {
                throw SessionExceptionCodes.MAX_SESSION_PER_USER_EXCEPTION.create(I(userId), I(contextId));
            }
        }
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                final Task<Integer> c = new AbstractTask<Integer>() {
                    
                    @Override
                    public Integer call() throws Exception {
                        return Integer.valueOf(storageService.getUserSessions(userId, contextId).length);
                    }
                };
                final int count = getFrom(c, Integer.valueOf(0)).intValue();
                if (maxSessPerUser > 0 && count >= maxSessPerUser) {
                    throw SessionExceptionCodes.MAX_SESSION_PER_USER_EXCEPTION.create(I(userId), I(contextId));
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private static void checkMaxSessPerClient(final String client, final int userId, final int contextId) throws OXException {
        if (null == client) {
            // Nothing to check against
            return;
        }
        final int maxSessPerClient = config.getMaxSessionsPerClient();
        if (maxSessPerClient > 0) {
            final SessionControl[] userSessions = sessionDataRef.get().getUserSessions(userId, contextId);
            int cnt = 0;
            for (final SessionControl sessionControl : userSessions) {
                if (client.equals(sessionControl.getSession().getClient()) && ++cnt > maxSessPerClient) {
                    throw SessionExceptionCodes.MAX_SESSION_PER_CLIENT_EXCEPTION.create(client, I(userId), I(contextId));
                }
            }
        }
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            if (maxSessPerClient > 0) {
                try {
                    final Task<Session[]> c = new AbstractTask<Session[]>() {
                        
                        @Override
                        public Session[] call() throws Exception {
                            return storageService.getUserSessions(userId, contextId);
                        }
                    };
                    final Session[] userSessions = getFrom(c, new Session[0]);
                    int cnt = 0;
                    for (final Session session : userSessions) {
                        if (client.equals(session.getClient()) && ++cnt > maxSessPerClient) {
                            throw SessionExceptionCodes.MAX_SESSION_PER_CLIENT_EXCEPTION.create(client, I(userId), I(contextId));
                        }
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private static void checkAuthId(final String login, final String authId) throws OXException {
        sessionDataRef.get().checkAuthId(login, authId);
        /*
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                storageService.checkAuthId(login, authId);
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        */
    }

    /**
     * Clears the session denoted by given session ID from session container(s)
     * 
     * @param sessionid The session ID
     * @return <code>true</code> if a session could be removed; otherwise <code>false</code>
     */
    protected static boolean clearSession(final String sessionid) {
        final SessionControl sessionControl = sessionDataRef.get().clearSession(sessionid);
        if (null == sessionControl) {
            LOG.debug("Cannot find session for given identifier to remove session <" + sessionid + '>');
            return false;
        }
        postSessionRemoval(sessionControl.getSession());
        return true;
    }

    /**
     * Changes the password stored in session denoted by given session ID
     * 
     * @param sessionid The session ID
     * @param newPassword The new password
     * @throws OXException If changing the password fails
     */
    protected static void changeSessionPassword(final String sessionid, final String newPassword) throws OXException {
        if (DEBUG) {
            LOG.debug(new StringBuilder("changeSessionPassword <").append(sessionid).append('>').toString());
        }
        final SessionControl sessionControl = sessionDataRef.get().getSession(sessionid);
        if (null == sessionControl) {
            throw SessionExceptionCodes.PASSWORD_UPDATE_FAILED.create();
        }
        // TODO: Check permission via security service
        sessionControl.getSession().setPassword(newPassword);
        final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
        if (sessionStorageService != null) {
            final Task<Void> c = new AbstractTask<Void>() {
                
                @Override
                public Void call() throws Exception {
                    sessionStorageService.changePassword(sessionid, newPassword);
                    return null;
                }
            };
            submitSafe(c);
        }
    }

    protected static Session getSessionByRandomToken(final String randomToken, final String newIP) {
        final SessionControl sessionControl = sessionDataRef.get().getSessionByRandomToken(randomToken);
        if (null == sessionControl) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    final Task<Session> c = new AbstractTask<Session>() {
                        
                        @Override
                        public Session call() throws Exception {
                            return storageService.getSessionByRandomToken(randomToken, newIP);
                        }
                    };
                    final Session s = getFrom(c, null);
                    if (null != s) {
                        return s;
                    }
                } catch (final RuntimeException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            return null;
        }
        /*
         * Check if local IP should be replaced
         */
        if (null != newIP) {
            /*
             * Set local IP
             */
            final Session session = sessionControl.getSession();
            final String oldIP = session.getLocalIp();
            if (!newIP.equals(oldIP)) {
                LOG.info("Changing IP of session " + session.getSessionID() + " with authID: " + session.getAuthId() + " from " + oldIP + " to " + newIP + '.');
                session.setLocalIp(newIP);
            }
        }
        return sessionControl.getSession();
    }

    /**
     * Gets the session associated with given session ID
     * 
     * @param sessionId The session ID
     * @return The session associated with given session ID; otherwise <code>null</code> if expired or none found
     */
    protected static SessionControl getSession(final String sessionId) {
        if (DEBUG) {
            LOG.debug(new StringBuilder("getSession <").append(sessionId).append('>').toString());
        }
        final SessionData sessionData = sessionDataRef.get();
        final SessionControl sessionControl = sessionData.getSession(sessionId);
        if (null == sessionControl) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    final Session storedSession = getSessionFrom(sessionId, storageService);
                    if (null != storedSession) {
                        final SessionControl sc = sessionData.addSession(new SessionImpl(storedSession), noLimit, null, true);
                        return null == sc ? sessionToSessionControl(storedSession) : sc;
                    }
                } catch (final OXException e) {
                    if (!SessionStorageExceptionCodes.NO_SESSION_FOUND.equals(e)) {
                        LOG.warn("Session look-up failed in session storage.", e);
                    }
                }
            }
        } else {
            // Look-up cache if current session wrapped by session-control is marked for removal
            try {
                final SessionCache cache = SessionCache.getInstance();
                final Session session = sessionControl.getSession();
                final CachedSession cachedSession = cache.getCachedSessionByUser(session.getUserId(), session.getContextId());
                if (null != cachedSession && cachedSession.isMarkedAsRemoved()) {
                    final String cSessionId = cachedSession.getSessionId();
                    if (sessionId.equals(cSessionId)) {
                        cache.removeCachedSession(cSessionId);
                        sessionData.clearSession(sessionId);
                        return null;
                    }
                }
            } catch (final OXException e) {
                LOG.error("Unable to look-up session cache", e);
            }
        }
        /*-
         * Ensure session is available in session storage
        if (null != sessionControl) {
            storeSession(sessionControl.getSession(), getServiceRegistry().getService(SessionStorageService.class), true);
        }
        */
        return sessionControl;
    }

    /**
     * Gets the session associated with given alternative identifier
     * 
     * @param alternative identifier The alternative identifier
     * @return The session associated with given alternative identifier; otherwise <code>null</code> if expired or none found
     */
    protected static SessionControl getSessionByAlternativeId(final String altId) {
        if (DEBUG) {
            LOG.debug(new StringBuilder("getSessionByAlternativeId <").append(altId).append('>').toString());
        }
        final SessionControl sessionControl = sessionDataRef.get().getSessionByAlternativeId(altId);
        if (null == sessionControl) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    final Task<Session> c = new AbstractTask<Session>() {
                        
                        @Override
                        public Session call() throws Exception {
                            return storageService.getSessionByAlternativeId(altId);
                        }
                    };
                    final Session session = getFrom(c, null);
                    if (null != session) {
                        return sessionToSessionControl(session);
                    }
                } catch (final RuntimeException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } else {
            // Look-up cache if current session wrapped by session-control is marked for removal
            try {
                final SessionCache cache = SessionCache.getInstance();
                final Session session = sessionControl.getSession();
                final CachedSession cachedSession = cache.getCachedSessionByUser(session.getUserId(), session.getContextId());
                if (null != cachedSession) {
                    if (cachedSession.isMarkedAsRemoved()) {
                        cache.removeCachedSession(cachedSession.getSecret());
                        removeUserSessions(cachedSession.getUserId(), cachedSession.getContextId(), false);
                        return null;
                    }
                }
            } catch (final OXException e) {
                LOG.error("Unable to look-up session cache", e);
            }
        }
        return sessionControl;
    }

    /**
     * Gets (and removes) the session bound to given session identifier in cache.
     * <p>
     * Session is going to be added to local session containers on a cache hit.
     * 
     * @param sessionId The session identifier
     * @return A wrapping instance of {@link SessionControl} or <code>null</code>
     */
    public static SessionControl getCachedSession(final String sessionId) {
        if (DEBUG) {
            LOG.debug(new StringBuilder("getCachedSession <").append(sessionId).append('>').toString());
        }
        try {
            final CachedSession cachedSession = SessionCache.getInstance().removeCachedSession(sessionId);
            if (null != cachedSession) {
                if (cachedSession.isMarkedAsRemoved()) {
                    removeUserSessions(cachedSession.getUserId(), cachedSession.getContextId(), false);
                } else {
                    // A cache hit! Add to local session containers
                    LOG.info("Migrate session: " + cachedSession.getSessionId());
                    return sessionDataRef.get().addSession(new SessionImpl(cachedSession), noLimit, null);
                }
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                final Task<Session> c = new AbstractTask<Session>() {
                    
                    @Override
                    public Session call() throws Exception {
                        return storageService.getCachedSession(sessionId);
                    }
                };
                final Session session = getFrom(c, null);
                if (null != session) {
                    return sessionToSessionControl(session);
                }
            } catch (final RuntimeException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Gets all available instances of {@link SessionControl}
     * 
     * @return All available instances of {@link SessionControl}
     */
    public static List<SessionControl> getSessions() {
        if (DEBUG) {
            LOG.debug("getSessions");
        }
        final List<SessionControl> retval = sessionDataRef.get().getShortTermSessions();
        if (retval == null) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                final Task<List<Session>> c = new AbstractTask<List<Session>>() {
                    
                    @Override
                    public List<Session> call() throws Exception {
                        return storageService.getSessions();
                    }
                };
                final List<Session> list = getFrom(c, Collections.<Session> emptyList());
                if (null != list && !list.isEmpty()) {
                    final List<SessionControl> result = new ArrayList<SessionControl>();
                    for (final Session s : list) {
                        result.add(sessionToSessionControl(s));
                    }
                    return result;
                }
            }
        }
        return retval;
    }

    protected static void cleanUp() {
        if (DEBUG) {
            LOG.debug("session cleanup");
        }
        final List<SessionControl> controls = sessionDataRef.get().rotateShort();
        for (final SessionControl sessionControl : controls) {
            if (INFO) {
                LOG.info("Session timed out. ID: " + sessionControl.getSession().getSessionID());
            }
        }
        postSessionDataRemoval(controls);
    }

    protected static void cleanUpLongTerm() {
        final List<SessionControl> controls = sessionDataRef.get().rotateLongTerm();
        for (final SessionControl control : controls) {
            if (INFO) {
                LOG.info("Session timed out. ID: " + control.getSession().getSessionID());
            }
        }
        postContainerRemoval(controls, true);
    }

    public static void close() {
        if (initialized.compareAndSet(true, false)) {
            final SessionData sd = sessionDataRef.get();
            if (null != sd) {
                postContainerRemoval(sd.getShortTermSessions(), false);
                sd.clear();
                sessionDataRef.set(null);
            }
            sessionIdGenerator = null;
            config = null;
            noLimit = false;
        }
    }

    public static int getNumberOfActiveSessions() {
        return sessionDataRef.get().countSessions();
    }

    public static int[] getNumberOfLongTermSessions() {
        return sessionDataRef.get().getLongTermSessionsPerContainer();
    }

    public static int[] getNumberOfShortTermSessions() {
        return sessionDataRef.get().getShortTermSessionsPerContainer();
    }

    /**
     * Post event that a single session has been put into {@link SessionStorageService session storage}.
     * 
     * @param session The stored session
     */
    protected static void postSessionStored(final Session session) {
        postSessionStored(session, null);
    }

    /**
     * Post event that a single session has been put into {@link SessionStorageService session storage}.
     * 
     * @param session The stored session
     * @param optEventAdmin The optional {@link EventAdmin} instance
     */
    public static void postSessionStored(final Session session, final EventAdmin optEventAdmin) {
        final EventAdmin eventAdmin = optEventAdmin == null ? getServiceRegistry().getService(EventAdmin.class) : optEventAdmin;
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_STORED_SESSION, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for added session");
            }
        }
    }

    private static void postSessionCreation(final Session session) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_ADD_SESSION, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for added session");
            }
        }
    }

    static void postSessionRemoval(final Session session) {
        // Asynchronous remove from session storage
        final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
        if (sessionStorageService != null) {
            ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {
    
                @Override
                public Void call() {
                    try {
                        sessionStorageService.removeSession(session.getSessionID());
                    } catch (final OXException e) {
                        if (DEBUG) {
                            LOG.warn("Session could not be removed from session storage: " + session.getSessionID(), e);
                        } else {
                            LOG.warn("Session could not be removed from session storage: " + session.getSessionID());
                        }
                    } catch (final RuntimeException e) {
                        if (DEBUG) {
                            LOG.warn("Session could not be removed from session storage: " + session.getSessionID(), e);
                        } else {
                            LOG.warn("Session could not be removed from session storage: " + session.getSessionID());
                        }
                    }
                    return null;
                }
            });
        }
        // Asynchronous post of event
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_SESSION, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for removed session");
            }
            if (sessionDataRef.get().isUserActive(session.getUserId(), session.getContextId())) {
                postLastSessionGone(session.getUserId(), session.getContextId(), eventAdmin);
            }
        }
    }

    private static void postLastSessionGone(final int userId, final int contextId, final EventAdmin eventAdmin) {
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_USER_ID, Integer.valueOf(userId));
            dic.put(SessiondEventConstants.PROP_CONTEXT_ID, Integer.valueOf(contextId));
            final Event event = new Event(SessiondEventConstants.TOPIC_LAST_SESSION, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for last removed session");
            }
        }
    }

    private static void postContainerRemoval(final List<SessionControl> sessionControls, final boolean removeFromSessionStorage) {
        if (removeFromSessionStorage) {
            // Asynchronous remove from session storage
            final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() {
                        try {
                            for (final SessionControl sessionControl : sessionControls) {
                                try {
                                    sessionStorageService.removeSession(sessionControl.getSession().getSessionID());
                                } catch (final OXException e) {
                                    LOG.error(e.getMessage(), e);
                                } catch (final RuntimeException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        } catch (final RuntimeException e) {
                            LOG.error(e.getMessage(), e);
                        }
                        return null;
                    }
                });
            }
        }
        // Asynchronous post of event
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            final Map<String, Session> eventMap = new HashMap<String, Session>();
            final Set<UserKey> users = new HashSet<UserKey>(sessionControls.size());
            for (final SessionControl sessionControl : sessionControls) {
                final Session session = sessionControl.getSession();
                eventMap.put(session.getSessionID(), session);
                users.add(new UserKey(session.getUserId(), session.getContextId()));
            }
            dic.put(SessiondEventConstants.PROP_CONTAINER, eventMap);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_CONTAINER, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for removed session container");
            }
            for (final UserKey userKey : users) {
                if (sessionDataRef.get().isUserActive(userKey.userId, userKey.contextId)) {
                    postLastSessionGone(userKey.userId, userKey.contextId, eventAdmin);
                }
            }
        }
    }

    private static void postSessionDataRemoval(final List<SessionControl> controls) {
        // Asynchronous remove from session storage
        final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
        if (sessionStorageService != null) {
            ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {
    
                @Override
                public Void call() {
                    try {
                        for (final SessionControl sessionControl : controls) {
                            try {
                                sessionStorageService.removeSession(sessionControl.getSession().getSessionID());
                            } catch (final OXException e) {
                                LOG.error(e.getMessage(), e);
                            } catch (final RuntimeException e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                    } catch (final RuntimeException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    return null;
                }
            });
        }
        // Post event
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            final Map<String, Session> eventMap = new HashMap<String, Session>();
            final Set<UserKey> users = new HashSet<UserKey>(controls.size());
            for (final SessionControl sessionControl : controls) {
                final Session session = sessionControl.getSession();
                eventMap.put(session.getSessionID(), session);
                users.add(new UserKey(session.getUserId(), session.getContextId()));
            }
            dic.put(SessiondEventConstants.PROP_CONTAINER, eventMap);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_DATA, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for removing temporary session data.");
            }
            for (final UserKey userKey : users) {
                if (sessionDataRef.get().isUserActive(userKey.userId, userKey.contextId)) {
                    postLastSessionGone(userKey.userId, userKey.contextId, eventAdmin);
                }
            }
        }
    }

    static void postSessionReactivation(final Session session) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_ADD_SESSION, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for added session");
            }
        }
    }

    public static void addThreadPoolService(final ThreadPoolService service) {
        sessionDataRef.get().addThreadPoolService(service);
    }

    public static void removeThreadPoolService() {
        sessionDataRef.get().removeThreadPoolService();
    }

    public static void addTimerService(final TimerService service) {
        sessionDataRef.get().addTimerService(service);
        final long containerTimeout = config.getSessionContainerTimeout();
        shortSessionContainerRotator = service.scheduleWithFixedDelay(
            new ShortSessionContainerRotator(),
            containerTimeout,
            containerTimeout);
        if (config.isAutoLogin()) {
            final long longContainerTimeout = config.getLongTermSessionContainerTimeout();
            longSessionContainerRotator = service.scheduleWithFixedDelay(
                new LongSessionContainerRotator(),
                longContainerTimeout,
                longContainerTimeout);
        }
    }

    public static void removeTimerService() {
        final ScheduledTimerTask longSessionContainerRotator = SessionHandler.longSessionContainerRotator;
        if (longSessionContainerRotator != null) {
            longSessionContainerRotator.cancel(false);
            SessionHandler.longSessionContainerRotator = null;
        }
        final ScheduledTimerTask shortSessionContainerRotator = SessionHandler.shortSessionContainerRotator;
        if (shortSessionContainerRotator != null) {
            shortSessionContainerRotator.cancel(false);
            SessionHandler.shortSessionContainerRotator = null;
        }
        sessionDataRef.get().removeTimerService();
    }

    private static SessionControl sessionToSessionControl(final Session session) {
        if (session == null) {
            return null;
        }
        return new SessionControl(new SessionImpl(session));
    }

    private static Session[] merge(final Session[] array1, final Session[] array2) {
        int lenghtArray1 = 0, lengthArray2 = 0;
        if (array1 != null) {
            lenghtArray1 = array1.length;
        }
        if (array2 != null) {
            lengthArray2 = array2.length;
        }
        final Session[] retval = new Session[lenghtArray1 + lengthArray2];
        int i = 0;
        if (array1 != null) {
            for (final Session s : array1) {
                retval[i++] = s;
            }
        }
        if (array2 != null) {
            for (final Session s : array2) {
                retval[i++] = s;
            }
        }
        return retval;
    }

    private static final class StoreSessionTask extends AbstractTask<Void> {

        private final SessionStorageService sessionStorageService;
        private final boolean addIfAbsent;
        private final SessionImpl session;

        protected StoreSessionTask(SessionImpl session, SessionStorageService sessionStorageService, boolean addIfAbsent) {
            super();
            this.sessionStorageService = sessionStorageService;
            this.addIfAbsent = addIfAbsent;
            this.session = session;
        }

        @Override
        public Void call() throws Exception {
            try {
                if (addIfAbsent) {
                    if (sessionStorageService.addSessionIfAbsent(session)) {
                        LOG.info("Put session " + session.getSessionID() + " with auth Id " + session.getAuthId() + " into session storage.");
                        postSessionStored(session);
                    }
                } else {
                    sessionStorageService.addSession(session);
                    LOG.info("Put session " + session.getSessionID() + " with auth Id " + session.getAuthId() + " into session storage.");
                    postSessionStored(session);
                }
            } catch (final Exception e) {
                final String s =
                    MessageFormat.format(
                        "Failed to put session {0} with Auth-Id {1} into session storage (user={2}, context={3}): {4}",
                        session.getSessionID(),
                        session.getAuthId(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        e.getMessage());
                if (DEBUG) {
                    LOG.info(s, e);
                } else {
                    LOG.info(s);
                }
            }
            return null;
        }
    }

    private static final class GetStoredSessionTask extends AbstractTask<Session> {

        private final SessionStorageService storageService;
        private final String sessionId;

        protected GetStoredSessionTask(final String sessionId, final SessionStorageService storageService) {
            super();
            this.storageService = storageService;
            this.sessionId = sessionId;
        }

        @Override
        public Session call() throws Exception {
            return storageService.lookupSession(sessionId);
        }
    }

    private static volatile Integer timeout;
    private static int timeout() {
        Integer tmp = timeout;
        if (null == tmp) {
            synchronized (SessionHandler.class) {
                tmp = timeout;
                if (null == tmp) {
                    ConfigurationService service = SessiondServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? 250 : service.getIntProperty("com.openexchange.sessiond.sessionstorage.timeout", 250));
                    timeout = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static Session getSessionFrom(final String sessionId, final SessionStorageService storageService) throws OXException {
        final int tout = timeout();
        try {
            final GetStoredSessionTask task = new GetStoredSessionTask(sessionId, storageService);
            return ThreadPools.getThreadPool().submit(task).get(tout, TimeUnit.MILLISECONDS);
        } catch (final RejectedExecutionException e) {
            return storageService.lookupSession(sessionId);
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
            LOG.warn("Session " + sessionId + " could not be retrieved from session storage within " + tout + "msec.");
            return null;
        } catch (final CancellationException e) {
            return null;
        }
    }

    private static <V> void submitSafe(final Task<V> c) {
        try {
            ThreadPools.getThreadPool().submit(c);
        } catch (final RejectedExecutionException e) {
            // Ignore
        }
    }

    private static <V> V getFrom(final Task<V> c, final V defaultValue) {
        try {
            return ThreadPools.getThreadPool().submit(c).get(timeout(), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return defaultValue;
        } catch (final ExecutionException e) {
            ThreadPools.launderThrowable(e, OXException.class);
            return defaultValue;
        } catch (final TimeoutException e) {
            return defaultValue;
        } catch (final CancellationException e) {
            return defaultValue;
        }
    }

    private static final class UserKey {
        protected final int contextId;
        protected final int userId;
        private final int hash;

        protected UserKey(final int userId, final int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof UserKey)) {
                return false;
            }
            final UserKey other = (UserKey) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }
}
