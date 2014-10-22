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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import static com.openexchange.sessiond.impl.TimeoutTaskWrapper.submit;
import static com.openexchange.sessiond.services.SessiondServiceRegistry.getServiceRegistry;
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
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionCounter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessionModifyCallback;
import com.openexchange.sessiond.SessiondEventConstants;
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
        public int getNumberOfSessions(int userId, final int contextId) {
            return sessionDataRef.get().getNumOfUserSessions(userId, contextId);
        }
    };

    /** The session identifier generator */
    private static volatile SessionIdGenerator sessionIdGenerator;

    /** The applied configuration */
    static volatile SessiondConfigInterface config;

    /** The {@link SessionData} reference */
    protected static final AtomicReference<SessionData> sessionDataRef = new AtomicReference<SessionData>();

    /** Whether there is no limit when adding a new session */
    private static volatile boolean noLimit;

    /** Whether to put session s to central session storage asynchronously (default) or synchronously */
    private static volatile boolean asyncPutToSessionStorage;

    /** The obfuscator */
    protected static Obfuscator obfuscator;

    /** The initialized flag */
    private static final AtomicBoolean initialized = new AtomicBoolean();

    /** Logger */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionHandler.class);

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
    public static void init(SessiondConfigInterface config) {
        SessionHandler.config = config;
        SessionData sessionData = new SessionData(
            config.getNumberOfSessionContainers(),
            config.getMaxSessions(),
            config.getRandomTokenTimeout(),
            config.getNumberOfLongTermSessionContainers(),
            config.isAutoLogin());
        sessionDataRef.set(sessionData);
        if (initialized.compareAndSet(false, true)) {
            try {
                sessionIdGenerator = SessionIdGenerator.getInstance();
            } catch (OXException exc) {
                LOG.error("create instance of SessionIdGenerator", exc);
            }
            noLimit = (config.getMaxSessions() == 0);
            asyncPutToSessionStorage = config.isAsyncPutToSessionStorage();
            synchronized (SessionHandler.class) {
                // Make it visible to other threads, too
                obfuscator = new Obfuscator(config.getObfuscationKey());
            }
        }
    }

    /**
     * Removes all sessions associated with given user in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The wrapper objects for removed sessions
     */
    public static Session[] removeUserSessions(final int userId, final int contextId) {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return new Session[0];
        }
        /*
         * remove from session data
         */
        SessionControl[] control = sessionData.removeUserSessions(userId, contextId);
        Session[] retval = new Session[control.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = control[i].getSession();
        }
        /*
         * remove from storage if available, too
         */
        Session[] retval2 = null;
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                Task<Session[]> c = new AbstractTask<Session[]>() {

                    @Override
                    public Session[] call() throws Exception {
                        return storageService.removeUserSessions(userId, contextId);
                    }
                };
                retval2 = getFrom(c, new Session[0]);
            } catch (RuntimeException e) {
                LOG.error("", e);
            }
        }
        LOG.info("{} removal of user sessions: User={}, Context={}", (null != storageService ? "Remote" : "Local"), userId, contextId);
        return merge(retval, retval2);
    }

    /**
     * Removes all sessions associated with given context.
     *
     * @param contextId The context ID
     */
    public static void removeContextSessions(final int contextId) {
        /*
         * Check context existence
         */
        {
            ContextService cs = getServiceRegistry().getService(ContextService.class);
            if (null != cs) {
                try {
                    cs.loadContext(contextId);
                } catch (OXException e) {
                    if (2 == e.getCode() && "CTX".equals(e.getPrefix())) { // See com.openexchange.groupware.contexts.impl.ContextExceptionCodes.NOT_FOUND
                        LOG.info("No such context {}", contextId);
                        return;
                    }
                }
            }
        }
        /*
         * Continue...
         */
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        /*
         * remove from session data
         */
        sessionData.removeContextSessions(contextId);
        /*
         * remove from storage if available, too
         */
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                Task<Void> c = new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        storageService.removeContextSessions(contextId);
                        return null;
                    }
                };
                submitAndIgnoreRejection(c);
            } catch (RuntimeException e) {
                LOG.error("", e);
            }
        }
        LOG.info("{} removal of sessions: Context={}", (null != storageService ? "Remote" : "Local"), contextId);
    }

    /**
     * Checks for any active session for specified context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if at least one active session is found; otherwise <code>false</code>
     */
    public static boolean hasForContext(final int contextId, boolean considerSessionStorage) {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        boolean hasForContext = sessionData.hasForContext(contextId);
        if (!hasForContext && considerSessionStorage) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Boolean> c = new AbstractTask<Boolean>() {

                        @Override
                        public Boolean call() throws Exception {
                            return Boolean.valueOf(storageService.hasForContext(contextId));
                        }
                    };
                    hasForContext = getFrom(c, Boolean.FALSE).booleanValue();
                } catch (RuntimeException e) {
                    LOG.error("", e);
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
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return new SessionControl[0];
        }
        SessionControl[] retval = sessionData.getUserSessions(userId, contextId);
        if (retval == null) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Session[]> c = new AbstractTask<Session[]>() {

                        @Override
                        public Session[] call() throws Exception {
                            return storageService.getUserSessions(userId, contextId);
                        }
                    };
                    Session[] sessions = getFrom(c, new Session[0]);
                    retval = new SessionControl[sessions.length];
                    for (int i = 0; i < sessions.length; i++) {
                        retval[i] = sessionToSessionControl(sessions[i]);
                    }
                } catch (RuntimeException e) {
                    LOG.error("", e);
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
    public static SessionControl getAnyActiveSessionForUser(final int userId, final int contextId, final boolean includeLongTerm, final boolean includeStorage) {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl retval = sessionData.getAnyActiveSessionForUser(userId, contextId, includeLongTerm);
        if (retval == null && includeStorage) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return storageService.getAnyActiveSessionForUser(userId, contextId);
                        }
                    };
                    Session storedSession = obfuscator.unwrap(getFrom(c, null));
                    if (null != storedSession) {
                        retval = sessionToSessionControl(storedSession);
                    }
                } catch (RuntimeException e) {
                    LOG.error("", e);
                }
            }
        }
        return retval;
    }

    public static Session findFirstSessionForUser(final int userId, final int contextId, final SessionMatcher matcher, final boolean ignoreLongTerm, final boolean ignoreStorage) {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        Session retval = sessionData.findFirstSessionForUser(userId, contextId, matcher, ignoreLongTerm);
        if (null == retval && !ignoreStorage) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (null != storageService) {
                try {
                    Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return obfuscator.unwrap(storageService.findFirstSessionForUser(userId, contextId));
                        }
                    };
                    retval = getFrom(c, null);
                } catch (RuntimeException e) {
                    LOG.error("", e);
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
     * @param tranzient <code>true</code> if the session should be transient, <code>false</code>, otherwise
     * @param callback after creating the session, this callback will be called when not <code>null</code> for extending the session.
     * @return The created session
     * @throws OXException If creating a new session fails
     */
    protected static SessionImpl addSession(final int userId, final String loginName, final String password, final int contextId, final String clientHost, final String login, final String authId, final String hash, final String client, final String clientToken, final boolean tranzient, SessionModifyCallback callback) throws OXException {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            throw SessionExceptionCodes.NOT_INITIALIZED.create();
        }
        checkMaxSessPerUser(userId, contextId, false);
        checkMaxSessPerClient(client, userId, contextId, false);
        checkAuthId(login, authId);
        SessionIdGenerator sessionIdGenerator = SessionHandler.sessionIdGenerator;
        String sessionId = sessionIdGenerator.createSessionId(loginName, clientHost);
        SessionImpl session = new SessionImpl(userId, loginName, password, contextId, sessionId, sessionIdGenerator.createSecretId(
            loginName, Long.toString(System.currentTimeMillis())), sessionIdGenerator.createRandomId(), clientHost, login, authId, hash,
            client, tranzient);
        if (null != callback) {
            callback.modify(session);
        }
        // Add session
        SessionImpl addedSession;
        if (null == clientToken) {
            addedSession = sessionData.addSession(session, noLimit).getSession();
            // store session if not marked as transient
            if (useSessionStorage(session)) {
                SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                if (sessionStorageService != null) {
                    if (asyncPutToSessionStorage) {
                        // Enforced asynchronous put
                        storeSessionAsync(addedSession, sessionStorageService, false);
                    } else {
                        storeSessionSync(addedSession, sessionStorageService, false);
                    }
                }
            }
            // Post event for created session
            postSessionCreation(addedSession);
        } else {
            String serverToken = sessionIdGenerator.createRandomId();
            // TODO change return type and return an interface that allows to dynamically add additional return values.
            session.setParameter("serverToken", serverToken);
            TokenSessionControl control = TokenSessionContainer.getInstance().addSession(session, clientToken, serverToken);
            addedSession = control.getSession();
        }
        // Return session ID
        return addedSession;
    }

    /**
     * (Synchronously) Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly replacing put
     */
    public static void storeSessionSync(SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent) {
        storeSession(session, sessionStorageService, addIfAbsent, false);
    }

    /**
     * (Asynchronously) Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly replacing put
     */
    public static void storeSessionAsync(SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent) {
        storeSession(session, sessionStorageService, addIfAbsent, true);
    }

    /**
     * Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly replacing put
     * @param async Whether to perform task asynchronously or not
     */
    public static void storeSession(SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent, final boolean async) {
        if (null == session || null == sessionStorageService) {
            return;
        }
        if (async) {
            ThreadPools.getThreadPool().submit(new StoreSessionTask(session, sessionStorageService, addIfAbsent));
        } else {
            StoreSessionTask task = new StoreSessionTask(session, sessionStorageService, addIfAbsent);
            Thread thread = Thread.currentThread();
            boolean ran = false;
            task.beforeExecute(thread);
            try {
                task.call();
                ran = true;
                task.afterExecute(null);
            } catch (Exception ex) {
                if (!ran) {
                    task.afterExecute(ex);
                }
                // Else the exception occurred within
                // afterExecute itself in which case we don't
                // want to call it again.
                OXException oxe = (ex instanceof OXException ? (OXException) ex : SessionExceptionCodes.SESSIOND_EXCEPTION.create(ex, ex.getMessage()));
                LOG.warn("", oxe);
            }
        }
    }

    /**
     * Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     */
    public static void storeSessions(Collection<SessionImpl> sessions, final SessionStorageService sessionStorageService) {
        if (null == sessions || sessions.isEmpty() || null == sessionStorageService) {
            return;
        }
        if (asyncPutToSessionStorage) {
            for (SessionImpl session : sessions) {
                storeSessionAsync(session, sessionStorageService, true);
            }
        } else {
            for (SessionImpl session : sessions) {
                storeSessionSync(session, sessionStorageService, true);
            }
        }
    }

    private static void checkMaxSessPerUser(final int userId, final int contextId, boolean considerSessionStorage) throws OXException {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        int maxSessPerUser = config.getMaxSessionsPerUser();
        if (maxSessPerUser > 0) {
            int count = sessionData.getNumOfUserSessions(userId, contextId);
            if (count >= maxSessPerUser) {
                throw SessionExceptionCodes.MAX_SESSION_PER_USER_EXCEPTION.create(I(userId), I(contextId));
            }
            if (considerSessionStorage) {
                final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
                if (storageService != null) {
                    try {
                        Task<Integer> c = new AbstractTask<Integer>() {

                            @Override
                            public Integer call() throws Exception {
                                return Integer.valueOf(storageService.getUserSessionCount(userId, contextId));
                            }
                        };
                        count = getFrom(c, Integer.valueOf(0)).intValue();
                        if (count >= maxSessPerUser) {
                            throw SessionExceptionCodes.MAX_SESSION_PER_USER_EXCEPTION.create(I(userId), I(contextId));
                        }
                    } catch (OXException e) {
                        LOG.error("", e);
                    }
                }
            }
        }
    }

    private static void checkMaxSessPerClient(String client, final int userId, final int contextId, boolean considerSessionStorage) throws OXException {
        if (null == client) {
            // Nothing to check against
            return;
        }
        int maxSessPerClient = config.getMaxSessionsPerClient();
        if (maxSessPerClient > 0) {
            SessionData sessionData = sessionDataRef.get();
            SessionControl[] userSessions = null == sessionData ? new SessionControl[0] : sessionData.getUserSessions(userId, contextId);
            int cnt = 1; // We have at least one
            for (SessionControl sessionControl : userSessions) {
                if (client.equals(sessionControl.getSession().getClient()) && ++cnt > maxSessPerClient) {
                    throw SessionExceptionCodes.MAX_SESSION_PER_CLIENT_EXCEPTION.create(client, I(userId), I(contextId));
                }
            }
            if (considerSessionStorage) {
                final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
                if (storageService != null) {
                    if (maxSessPerClient > 0) {
                        try {
                            Task<Session[]> c = new AbstractTask<Session[]>() {

                                @Override
                                public Session[] call() throws Exception {
                                    return storageService.getUserSessions(userId, contextId);
                                }
                            };
                            Session[] storedSessions = getFrom(c, new Session[0]);
                            cnt = 0;
                            for (Session session : storedSessions) {
                                if (client.equals(session.getClient()) && ++cnt > maxSessPerClient) {
                                    throw SessionExceptionCodes.MAX_SESSION_PER_CLIENT_EXCEPTION.create(client, I(userId), I(contextId));
                                }
                            }
                        } catch (OXException e) {
                            LOG.error("", e);
                        }
                    }
                }
            }
        }
    }

    private static void checkAuthId(String login, final String authId) throws OXException {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        sessionData.checkAuthId(login, authId);
        /*
        SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                storageService.checkAuthId(login, authId);
            } catch (OXException e) {
                LOG.error("", e);
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
    protected static boolean clearSession(String sessionid) {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        SessionControl sessionControl = sessionData.clearSession(sessionid);
        if (null == sessionControl) {
            LOG.debug("Cannot find session for given identifier to remove session <{}{}", sessionid, '>');
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
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        LOG.debug("changeSessionPassword <{}>", sessionid);
        SessionControl sessionControl = sessionData.getSession(sessionid);
        if (null == sessionControl) {
            throw SessionExceptionCodes.PASSWORD_UPDATE_FAILED.create();
        }
        /*
         * Change password in current session
         */
        final SessionImpl currentSession = sessionControl.getSession();
        currentSession.setPassword(newPassword);
        final SessionStorageService sessionStorage = getServiceRegistry().getService(SessionStorageService.class);
        if (null != sessionStorage && useSessionStorage(currentSession)) {
            Task<Void> c = new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    Session wrappedSession = obfuscator.wrap(currentSession);
                    sessionStorage.changePassword(sessionid, wrappedSession.getPassword());
                    return null;
                }
            };
            submitAndIgnoreRejection(c);
        }
        /*
         * Invalidate all other user sessions known by local session containers
         */
        SessionControl[] userSessionControls = sessionData.getUserSessions(currentSession.getUserId(), currentSession.getContextId());
        if (null != userSessionControls && 0 < userSessionControls.length) {
            for (SessionControl userSessionControl : userSessionControls) {
                String otherSessionID = userSessionControl.getSession().getSessionID();
                if (null != otherSessionID && false == otherSessionID.equals(sessionid)) {
                    clearSession(otherSessionID);
                }
            }
        }
        /*
         * Invalidate all further user sessions in session storage if needed
         */
        if (null != sessionStorage) {
            Task<Void> c = new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    Session[] sessions = sessionStorage.getUserSessions(currentSession.getUserId(), currentSession.getContextId());
                    if (null != sessions && 0 < sessions.length) {
                        for (Session session : sessions) {
                            String otherSessionID = session.getSessionID();
                            if (null != otherSessionID && false == otherSessionID.equals(sessionid)) {
                                sessionStorage.removeSession(otherSessionID);
                            }
                        }
                    }
                    return null;
                }
            };
            submitAndIgnoreRejection(c);
        }
    }

    /**
     * Sets the local IP address for given session.
     *
     * @param session The session
     * @param localIp The new local IP address
     * @throws OXException If changing local IP address fails or any reason
     */
    protected static void setLocalIp(final SessionImpl session, final String localIp) throws OXException {
        if (null != session) {
            try {
                session.setLocalIp(localIp, false);
                if (useSessionStorage(session)) {
                    final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                    if (sessionStorageService != null) {
                        AbstractTask<Void> c = new AbstractTask<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    sessionStorageService.setLocalIp(session.getSessionID(), localIp);
                                } catch (OXException e) {
                                    if (SessionStorageExceptionCodes.NO_SESSION_FOUND.equals(e)) {
                                        // No such session held in session storage
                                        LOG.debug("Session {} not available in session storage.", session.getSessionID(), e);
                                    } else {
                                        LOG.warn("Failed to set local IP address", e);
                                    }
                                } catch (Exception e) {
                                    if (e.getCause() instanceof InterruptedException) {
                                        // Timed out
                                        LOG.warn("Failed to set local IP address in time");
                                    } else {
                                        LOG.warn("Failed to set local IP address", e);
                                    }
                                }
                                return null;
                            }
                        };
                        submit(c);
                    }
                }
            } catch (RuntimeException e) {
                throw SessionExceptionCodes.SESSIOND_EXCEPTION.create(e, e.getMessage());
            }
        }
    }

    /**
     * Sets the client identifier for given session.
     *
     * @param session The session
     * @param client The new client identifier
     * @throws OXException If changing client identifier fails or any reason
     */
    protected static void setClient(final SessionImpl session, final String client) throws OXException {
        if (null != session) {
            try {
                session.setClient(client, false);
                if (useSessionStorage(session)) {
                    final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                    if (sessionStorageService != null) {
                        AbstractTask<Void> c = new AbstractTask<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    sessionStorageService.setClient(session.getSessionID(), client);
                                } catch (OXException e) {
                                    if (SessionStorageExceptionCodes.NO_SESSION_FOUND.equals(e)) {
                                        // No such session held in session storage
                                        LOG.debug("Session {} not available in session storage.", session.getSessionID(), e);
                                    } else {
                                        LOG.warn("Failed to set client", e);
                                    }
                                } catch (Exception e) {
                                    if (e.getCause() instanceof InterruptedException) {
                                        // Timed out
                                        LOG.warn("Failed to set client in time");
                                    } else {
                                        LOG.warn("Failed to set client", e);
                                    }
                                }
                                return null;
                            }
                        };
                        submit(c);
                    }
                }
            } catch (RuntimeException e) {
                throw SessionExceptionCodes.SESSIOND_EXCEPTION.create(e, e.getMessage());
            }
        }
    }

    /**
     * Sets the hash identifier for given session.
     *
     * @param session The session
     * @param client The new hash identifier
     * @throws OXException If changing hash identifier fails or any reason
     */
    protected static void setHash(final SessionImpl session, final String hash) throws OXException {
        if (null != session) {
            try {
                session.setHash(hash, false);
                if (useSessionStorage(session)) {
                    final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                    if (sessionStorageService != null) {
                        AbstractTask<Void> c = new AbstractTask<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    sessionStorageService.setHash(session.getSessionID(), hash);
                                } catch (OXException e) {
                                    if (SessionStorageExceptionCodes.NO_SESSION_FOUND.equals(e)) {
                                        // No such session held in session storage
                                        LOG.debug("Session {} not available in session storage.", session.getSessionID(), e);
                                    } else {
                                        LOG.warn("Failed to set hash", e);
                                    }
                                } catch (Exception e) {
                                    if (e.getCause() instanceof InterruptedException) {
                                        // Timed out
                                        LOG.warn("Failed to set hash in time");
                                    } else {
                                        LOG.warn("Failed to set hash", e);
                                    }
                                }
                                return null;
                            }
                        };
                        submit(c);
                    }
                }
            } catch (RuntimeException e) {
                throw SessionExceptionCodes.SESSIOND_EXCEPTION.create(e, e.getMessage());
            }
        }
    }

    protected static Session getSessionByRandomToken(final String randomToken, final String newIP) {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl sessionControl = sessionData.getSessionByRandomToken(randomToken);
        if (null == sessionControl) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return storageService.getSessionByRandomToken(randomToken, newIP);
                        }
                    };
                    Session s = obfuscator.unwrap(getFrom(c, null));
                    if (null != s) {
                        return s;
                    }
                } catch (RuntimeException e) {
                    LOG.error("", e);
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
            Session session = sessionControl.getSession();
            String oldIP = session.getLocalIp();
            if (!newIP.equals(oldIP)) {
                LOG.info("Changing IP of session {} with authID: {} from {} to {}{}", session.getSessionID(), session.getAuthId(), oldIP, newIP, '.');
                session.setLocalIp(newIP);
            }
        }
        return sessionControl.getSession();
    }

    static Session getSessionWithTokens(String clientToken, final String serverToken) throws OXException {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            throw SessionExceptionCodes.NOT_INITIALIZED.create();
        }
        // find session matching to tokens
        TokenSessionControl tokenControl = TokenSessionContainer.getInstance().getSession(clientToken, serverToken);
        SessionImpl activatedSession = tokenControl.getSession();

        // Put this session into the normal session container
        SessionControl sessionControl = sessionData.addSession(activatedSession, noLimit);
        SessionImpl addedSession = sessionControl.getSession();
        if (useSessionStorage(addedSession)) {
            SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                if (asyncPutToSessionStorage) {
                    storeSessionAsync(addedSession, sessionStorageService, false);
                } else {
                    storeSessionSync(addedSession, sessionStorageService, false);
                }
            }
        }
        // Post event for created session
        postSessionCreation(addedSession);

        return activatedSession;
    }

    /**
     * Gets the session associated with given session ID
     *
     * @param sessionId The session ID
     * @param considerSessionStorage <code>true</code> to consider session storage for possible distributed session; otherwise <code>false</code>
     * @return The session associated with given session ID; otherwise <code>null</code> if expired or none found
     */
    protected static SessionControl getSession(String sessionId, final boolean considerSessionStorage) {
        return getSession(sessionId, true, considerSessionStorage);
    }

    /**
     * Gets the session associated with given session ID
     *
     * @param sessionId The session ID
     * @param considerLocalStorage <code>true</code> to consider local storage; otherwise <code>false</code>
     * @param considerSessionStorage <code>true</code> to consider session storage for possible distributed session; otherwise <code>false</code>
     * @return The session associated with given session ID; otherwise <code>null</code> if expired or none found
     */
    protected static SessionControl getSession(String sessionId, final boolean considerLocalStorage, final boolean considerSessionStorage) {
        LOG.debug("getSession <{}>", sessionId);
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl sessionControl = considerLocalStorage ? sessionData.getSession(sessionId) : null;
        if (considerSessionStorage && null == sessionControl) {
            SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Session storedSession = getSessionFrom(sessionId, storageService);
                    if (null != storedSession) {
                        SessionControl sc = sessionData.addSession(new SessionImpl(storedSession), noLimit, true);
                        SessionControl retval = null == sc ? sessionToSessionControl(storedSession) : sc;
                        if (null != retval) {
                            // Post event for restored session
                            postSessionRestauration(retval.getSession());
                        }
                        return retval;
                    }
                } catch (OXException e) {
                    if (!SessionStorageExceptionCodes.NO_SESSION_FOUND.equals(e)) {
                        LOG.warn("Session look-up failed in session storage.", e);
                    }
                }
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
     * Checks if denoted session is <code>locally</code> available and located in short-term container.
     *
     * @param sessionId The session identifier
     * @return <code>true</code> if <code>locally</code> active; otherwise <code>false</code>
     */
    protected static boolean isActive(String sessionId) {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        return null != sessionData.optShortTermSession(sessionId);
    }

    /**
     * Gets the session associated with given alternative identifier
     *
     * @param alternative identifier The alternative identifier
     * @return The session associated with given alternative identifier; otherwise <code>null</code> if expired or none found
     */
    protected static SessionControl getSessionByAlternativeId(final String altId, boolean lookupSessionStorage) {
        LOG.debug("getSessionByAlternativeId <{}>", altId);
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl sessionControl = sessionData.getSessionByAlternativeId(altId);
        if (null == sessionControl && lookupSessionStorage) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return storageService.getSessionByAlternativeId(altId);
                        }
                    };
                    Session session = obfuscator.unwrap(getFrom(c, null));
                    if (null != session) {
                        return sessionToSessionControl(session);
                    }
                } catch (RuntimeException e) {
                    LOG.error("", e);
                }
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
        LOG.debug("getCachedSession <{}>", sessionId);
        final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                Task<Session> c = new AbstractTask<Session>() {

                    @Override
                    public Session call() throws Exception {
                        return storageService.getCachedSession(sessionId);
                    }
                };
                Session session = obfuscator.unwrap(getFrom(c, null));
                if (null != session) {
                    return sessionToSessionControl(session);
                }
            } catch (RuntimeException e) {
                LOG.error("", e);
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
        LOG.debug("getSessions");
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return Collections.emptyList();
        }
        List<SessionControl> retval = sessionData.getShortTermSessions();
        if (retval == null) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                Task<List<Session>> c = new AbstractTask<List<Session>>() {

                    @Override
                    public List<Session> call() throws Exception {
                        return storageService.getSessions();
                    }
                };
                List<Session> list = getFrom(c, Collections.<Session> emptyList());
                if (null != list && !list.isEmpty()) {
                    List<SessionControl> result = new ArrayList<SessionControl>();
                    for (Session s : list) {
                        result.add(sessionToSessionControl(obfuscator.unwrap(s)));
                    }
                    return result;
                }
            }
        }
        return retval;
    }

    protected static void cleanUp() {
        LOG.debug("session cleanup");
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        List<SessionControl> controls = sessionData.rotateShort();
        if (config.isAutoLogin()) {
            for (final SessionControl sessionControl : controls) {
                LOG.info("Session is moved to long life time container. All temporary session data will be cleaned up. ID: {}", new Object() { @Override public String toString() { return sessionControl.getSession().getSessionID();}});
            }
            postSessionDataRemoval(controls);
        } else {
            for (final SessionControl sessionControl : controls) {
                LOG.info("Session timed out. ID: {}", new Object() { @Override public String toString() { return sessionControl.getSession().getSessionID();}});
            }
            postContainerRemoval(controls, true);
        }
    }

    protected static void cleanUpLongTerm() {
        SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        List<SessionControl> controls = sessionData.rotateLongTerm();
        for (SessionControl control : controls) {
            LOG.info("Session timed out. ID: {}", control.getSession().getSessionID());
        }
        postContainerRemoval(controls, true);
    }

    public static void close() {
        if (initialized.compareAndSet(true, false)) {
            SessionData sd = sessionDataRef.get();
            if (null != sd) {
                postContainerRemoval(sd.getShortTermSessions(), false);
                sd.clear();
                sessionDataRef.set(null);
            } else {
                LOG.warn("\tSessionData instance is null.");
            }
            sessionIdGenerator = null;
            config = null;
            noLimit = false;
        }
    }

    public static int getNumberOfActiveSessions() {
        SessionData sessionData = sessionDataRef.get();
        return null == sessionData ? 0 : sessionData.countSessions();
    }

    public static int[] getNumberOfLongTermSessions() {
        SessionData sessionData = sessionDataRef.get();
        return null == sessionData ? new int[0]: sessionData.getLongTermSessionsPerContainer();
    }

    public static int[] getNumberOfShortTermSessions() {
        SessionData sessionData = sessionDataRef.get();
        return null == sessionData ? new int[0] : sessionData.getShortTermSessionsPerContainer();
    }

    /**
     * Post event that a single session has been put into {@link SessionStorageService session storage}.
     *
     * @param session The stored session
     */
    protected static void postSessionStored(Session session) {
        postSessionStored(session, null);
    }

    /**
     * Post event that a single session has been put into {@link SessionStorageService session storage}.
     *
     * @param session The stored session
     * @param optEventAdmin The optional {@link EventAdmin} instance
     */
    public static void postSessionStored(Session session, final EventAdmin optEventAdmin) {
        EventAdmin eventAdmin = optEventAdmin == null ? getServiceRegistry().getService(EventAdmin.class) : optEventAdmin;
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_STORED_SESSION, dic));
            LOG.debug("Posted event for added session");
        }
    }

    private static void postSessionCreation(Session session) {
        EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_ADD_SESSION, dic));
            LOG.debug("Posted event for added session");
        }
    }

    private static void postSessionRestauration(Session session) {
        EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_RESTORED_SESSION, dic));
            LOG.debug("Posted event for restored session");
        }
    }

    private static void postSessionRemoval(final SessionImpl session) {
        if (useSessionStorage(session)) {
            // Asynchronous remove from session storage
            final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() {
                        try {
                            sessionStorageService.removeSession(session.getSessionID());
                        } catch (OXException e) {
                            LOG.warn("Session could not be removed from session storage: {}", session.getSessionID(), e);
                        } catch (RuntimeException e) {
                            LOG.warn("Session could not be removed from session storage: {}", session.getSessionID(), e);
                        }
                        return null;
                    }
                });
            }
        }

        // Asynchronous post of event
        EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_REMOVE_SESSION, dic));
            LOG.debug("Posted event for removed session");

            SessionData sessionData = sessionDataRef.get();
            if (null != sessionData) {
                int contextId = session.getContextId();
                int userId = session.getUserId();
                if (sessionData.isUserActive(userId, contextId, false)) {
                    postLastSessionGone(userId, contextId, eventAdmin);
                }
            }
        }
    }

    private static void postLastSessionGone(int userId, int contextId, EventAdmin eventAdmin) {
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_USER_ID, Integer.valueOf(userId));
            dic.put(SessiondEventConstants.PROP_CONTEXT_ID, Integer.valueOf(contextId));
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_LAST_SESSION, dic));
            LOG.debug("Posted event for last removed session for user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId));

            SessionData sessionData = sessionDataRef.get();
            if (null != sessionData) {
                if (sessionData.hasForContext(contextId)) {
                    postContextLastSessionGone(contextId, eventAdmin);
                }
            }
        }
    }

    private static void postContextLastSessionGone(int contextId, EventAdmin eventAdmin) {
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_CONTEXT_ID, Integer.valueOf(contextId));
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_LAST_SESSION_CONTEXT, dic));
            LOG.debug("Posted event for last removed session for context {}", Integer.valueOf(contextId));
        }
    }

    protected static void postContainerRemoval(List<SessionControl> sessionControls, final boolean removeFromSessionStorage) {
        if (removeFromSessionStorage) {
            // Asynchronous remove from session storage
            final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                final List<SessionControl> tSessionControls = new ArrayList<SessionControl>(sessionControls);
                ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() {
                        try {
                            for (SessionControl sessionControl : tSessionControls) {
                                SessionImpl session = sessionControl.getSession();
                                if (useSessionStorage(session)) {
                                    try {
                                        sessionStorageService.removeSession(session.getSessionID());
                                    } catch (OXException e) {
                                        LOG.error("", e);
                                    } catch (RuntimeException e) {
                                        LOG.error("", e);
                                    }
                                }
                            }
                        } catch (RuntimeException e) {
                            LOG.error("", e);
                        }
                        return null;
                    }
                });
            }
        }

        // Asynchronous post of event
        EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            Map<String, Session> eventMap = new HashMap<String, Session>();
            Set<UserKey> users = new HashSet<UserKey>(sessionControls.size());
            for (SessionControl sessionControl : sessionControls) {
                Session session = sessionControl.getSession();
                eventMap.put(session.getSessionID(), session);
                users.add(new UserKey(session.getUserId(), session.getContextId()));
            }
            dic.put(SessiondEventConstants.PROP_CONTAINER, eventMap);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_REMOVE_CONTAINER, dic));
            LOG.debug("Posted event for removed session container");

            SessionData sessionData = sessionDataRef.get();
            if (null != sessionData) {
                for (UserKey userKey : users) {
                    if (sessionData.isUserActive(userKey.userId, userKey.contextId, false)) {
                        postLastSessionGone(userKey.userId, userKey.contextId, eventAdmin);
                    }
                }
            }
        }
    }

    private static void postSessionDataRemoval(List<SessionControl> controls) {
        // Post event
        EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            Map<String, Session> eventMap = new HashMap<String, Session>();
            Set<UserKey> users = new HashSet<UserKey>(controls.size());
            for (SessionControl sessionControl : controls) {
                Session session = sessionControl.getSession();
                eventMap.put(session.getSessionID(), session);
                users.add(new UserKey(session.getUserId(), session.getContextId()));
            }
            dic.put(SessiondEventConstants.PROP_CONTAINER, eventMap);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_REMOVE_DATA, dic));
            LOG.debug("Posted event for removing temporary session data.");

            SessionData sessionData = sessionDataRef.get();
            if (null != sessionData) {
                for (UserKey userKey : users) {
                    if (sessionData.isUserActive(userKey.userId, userKey.contextId, false)) {
                        postLastSessionGone(userKey.userId, userKey.contextId, eventAdmin);
                    }
                }
            }
        }
    }

    static void postSessionReactivation(Session session) {
        EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_REACTIVATE_SESSION, dic));
            LOG.debug("Posted event for reactivated session");
        }
    }

    /**
     * Broadcasts the {@link SessiondEventConstants#TOPIC_TOUCH_SESSION} event, usually after the session has been moved to the first
     * container.
     *
     * @param session The session that was touched
     */
    static void postSessionTouched(Session session) {
        EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_TOUCH_SESSION, dic));
            LOG.debug("Posted event for touched session");
        }
    }

    public static void addThreadPoolService(ThreadPoolService service) {
        SessionData sessionData = sessionDataRef.get();
        if (null != sessionData) {
            sessionData.addThreadPoolService(service);
        }
    }

    public static void removeThreadPoolService() {
        SessionData sessionData = sessionDataRef.get();
        if (null != sessionData) {
            sessionData.removeThreadPoolService();
        }
    }

    public static void addTimerService(TimerService service) {
        SessionData sessionData = sessionDataRef.get();
        if (null != sessionData) {
            sessionData.addTimerService(service);
        }
        long containerTimeout = config.getSessionContainerTimeout();
        shortSessionContainerRotator = service.scheduleWithFixedDelay(
            new ShortSessionContainerRotator(),
            containerTimeout,
            containerTimeout);
        if (config.isAutoLogin()) {
            long longContainerTimeout = config.getLongTermSessionContainerTimeout();
            longSessionContainerRotator = service.scheduleWithFixedDelay(
                new LongSessionContainerRotator(),
                longContainerTimeout,
                longContainerTimeout);
        }
    }

    public static void removeTimerService() {
        ScheduledTimerTask longSessionContainerRotator = SessionHandler.longSessionContainerRotator;
        if (longSessionContainerRotator != null) {
            longSessionContainerRotator.cancel(false);
            SessionHandler.longSessionContainerRotator = null;
        }
        ScheduledTimerTask shortSessionContainerRotator = SessionHandler.shortSessionContainerRotator;
        if (shortSessionContainerRotator != null) {
            shortSessionContainerRotator.cancel(false);
            SessionHandler.shortSessionContainerRotator = null;
        }
        SessionData sessionData = sessionDataRef.get();
        if (null != sessionData) {
            sessionData.removeTimerService();
        }
    }

    private static SessionControl sessionToSessionControl(Session session) {
        if (session == null) {
            return null;
        }
        return new SessionControl(new SessionImpl(session));
    }

    private static Session[] merge(Session[] array1, final Session[] array2) {
        int lenghtArray1 = 0, lengthArray2 = 0;
        if (array1 != null) {
            lenghtArray1 = array1.length;
        }
        if (array2 != null) {
            lengthArray2 = array2.length;
        }
        Session[] retval = new Session[lenghtArray1 + lengthArray2];
        int i = 0;
        if (array1 != null) {
            for (Session s : array1) {
                retval[i++] = s;
            }
        }
        if (array2 != null) {
            for (Session s : array2) {
                retval[i++] = s;
            }
        }
        return retval;
    }

    private static final class StoreSessionTask extends AbstractTask<Void> {

        private final SessionStorageService sessionStorageService;
        private final boolean addIfAbsent;
        private final SessionImpl session;

        protected StoreSessionTask(SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent) {
            super();
            this.sessionStorageService = sessionStorageService;
            this.addIfAbsent = addIfAbsent;
            this.session = session;
        }

        @Override
        public Void call() {
            try {
                if (addIfAbsent) {
                    if (sessionStorageService.addSessionIfAbsent(obfuscator.wrap(session))) {
                        LOG.info("Put session {} with auth Id {} into session storage.", session.getSessionID(), session.getAuthId());
                        postSessionStored(session);
                    }
                } else {
                    sessionStorageService.addSession(obfuscator.wrap(session));
                    LOG.info("Put session {} with auth Id {} into session storage.", session.getSessionID(), session.getAuthId());
                    postSessionStored(session);
                }
            } catch (Exception e) {
                LOG.warn("Failed to put session {} with Auth-Id {} into session storage (user={}, context={})",session.getSessionID(),session.getAuthId(),Integer.valueOf(session.getUserId()),Integer.valueOf(session.getContextId()), e);
            }
            return null;
        }
    }

    private static final class GetStoredSessionTask extends AbstractTask<Session> {

        private final SessionStorageService storageService;
        private final String sessionId;

        protected GetStoredSessionTask(String sessionId, final SessionStorageService storageService) {
            super();
            this.storageService = storageService;
            this.sessionId = sessionId;
        }

        @Override
        public Session call() throws Exception {
            try {
                return obfuscator.unwrap(storageService.lookupSession(sessionId));
            } catch (OXException e) {
                if (SessionStorageExceptionCodes.INTERRUPTED.equals(e)) {
                    // Expected...
                    return null;
                }
                throw e;
            }
        }
    }

    private static volatile Integer timeout;

    /**
     * Gets the timeout for session-storage operations.
     *
     * @return The timeout in milliseconds
     */
    public static int timeout() {
        Integer tmp = timeout;
        if (null == tmp) {
            synchronized (SessionHandler.class) {
                tmp = timeout;
                if (null == tmp) {
                    ConfigurationService service = getServiceRegistry().getService(ConfigurationService.class);
                    int defaultTimeout = 3000;
                    tmp = Integer.valueOf(null == service ? defaultTimeout : service.getIntProperty("com.openexchange.sessiond.sessionstorage.timeout", defaultTimeout));
                    timeout = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static Session getSessionFrom(String sessionId, final SessionStorageService storageService) throws OXException {
        Future<Session> f;
        try {
            f = ThreadPools.getThreadPool().submit(new GetStoredSessionTask(sessionId, storageService));
        } catch (Exception e) {
            return null;
        }

        int tout = timeout();
        try {
            return f.get(tout, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            return storageService.lookupSession(sessionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
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
        } catch (TimeoutException e) {
            LOG.warn("Session {} could not be retrieved from session storage within {}msec.", sessionId, tout);
            f.cancel(true);
            return null;
        } catch (CancellationException e) {
            return null;
        }
    }

    /**
     * Submits given task to thread pool while ignoring a possible {@link RejectedExecutionException} in case thread pool refuses its execution.
     *
     * @param task The task to submit
     */
    private static <V> void submitAndIgnoreRejection(Task<V> task) {
        try {
            ThreadPools.getThreadPool().submit(task);
        } catch (RejectedExecutionException e) {
            // Ignore
        }
    }

    private static <V> V getFrom(Task<V> c, V defaultValue) {
        Future<V> f;
        try {
            f = ThreadPools.getThreadPool().submit(c);
        } catch (Exception e) {
            // Failed to submit to thread pool
            return defaultValue;
        }

        // Await task completion
        try {
            return f.get(timeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return defaultValue;
        } catch (ExecutionException e) {
            ThreadPools.launderThrowable(e, OXException.class);
            return defaultValue;
        } catch (TimeoutException e) {
            f.cancel(true);
            return defaultValue;
        } catch (CancellationException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a value indicating whether a session qualifies for being put in the distributed session storage or not. This includes a check
     * for the "transient" flag, as well as other relevant session properties.
     *
     * @param session The session to check
     * @return <code>true</code> if session should be put to storage, <code>false</code>, otherwise
     */
    static boolean useSessionStorage(SessionImpl session) {
        return null != session && false == session.isTransient() && false == isUsmEas(session.getClient());
    }

    /**
     * Gets a value indicating whether the supplied client identifier indicates an USM session or not.
     *
     * @param clientId the client ID to check
     * @return <code>true</code> if the client denotes an USM client, <code>false</code>, otherwise
     */
    private static boolean isUsmEas(String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }
        String uc = Strings.toUpperCase(clientId);
        return uc.startsWith("USM-EAS") || uc.startsWith("USM-JSON");
    }

    private static final class UserKey {
        protected final int contextId;
        protected final int userId;
        private final int hash;

        protected UserKey(int userId, final int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            int prime = 31;
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
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof UserKey)) {
                return false;
            }
            UserKey other = (UserKey) obj;
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
