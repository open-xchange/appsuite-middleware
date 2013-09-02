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
import java.util.concurrent.CountDownLatch;
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
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionCounter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionMatcher;
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
        public int getNumberOfSessions(final int userId, final int contextId) {
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

    /** Whether there is no limit when adding a new session */
    private static volatile boolean asyncPutToSessionStorage;

    /** The obfuscator */
    protected static Obfuscator obfuscator;

    /** The initialized flag */
    private static final AtomicBoolean initialized = new AtomicBoolean();

    /** Logger */
    protected static final Log LOG = com.openexchange.log.Log.loggerFor(SessionHandler.class);

    /** If INFO logging is enabled for this class */
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
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return new Session[0];
        }
        /*
         * remove from session data
         */
        final SessionControl[] control = sessionData.removeUserSessions(userId, contextId);
        final Session[] retval = new Session[control.length];
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
            LOG.info(new StringAllocator(64).append(null != storageService ? "Remote" : "Local")
                .append(" removal of user sessions: User=").append(userId).append(", Context=").append(contextId).toString());
        }
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
            final ContextService cs = getServiceRegistry().getService(ContextService.class);
            if (null != cs) {
                try {
                    cs.loadContext(contextId);
                } catch (final OXException e) {
                    if (2 == e.getCode() && "CTX".equals(e.getPrefix())) { // See com.openexchange.groupware.contexts.impl.ContextExceptionCodes.NOT_FOUND
                        if (INFO) {
                            LOG.info(new StringAllocator(64).append("No such context ").append(contextId).toString());
                        }
                        return;
                    }
                }
            }
        }
        /*
         * Continue...
         */
        final SessionData sessionData = sessionDataRef.get();
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
                final Task<Void> c = new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        storageService.removeContextSessions(contextId);
                        return null;
                    }
                };
                submitAndIgnoreRejection(c);
            } catch (final RuntimeException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (INFO) {
            LOG.info(new StringAllocator(64).append(null != storageService ? "Remote" : "Local")
                .append(" removal of sessions: Context=").append(contextId).toString());
        }
    }

    /**
     * Checks for any active session for specified context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if at least one active session is found; otherwise <code>false</code>
     */
    public static boolean hasForContext(final int contextId, final boolean considerSessionStorage) {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        boolean hasForContext = sessionData.hasForContext(contextId);
        if (!hasForContext && considerSessionStorage) {
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
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return new SessionControl[0];
        }
        SessionControl[] retval = sessionData.getUserSessions(userId, contextId);
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
    public static SessionControl getAnyActiveSessionForUser(final int userId, final int contextId, final boolean includeLongTerm, final boolean includeStorage) {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl retval = sessionData.getAnyActiveSessionForUser(userId, contextId, includeLongTerm);
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
                    final Session storedSession = obfuscator.unwrap(getFrom(c, null));
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

    public static Session findFirstSessionForUser(final int userId, final int contextId, final SessionMatcher matcher, final boolean ignoreLongTerm, final boolean ignoreStorage) {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        Session retval = sessionData.findFirstSessionForUser(userId, contextId, matcher, ignoreLongTerm);
        if (null == retval && !ignoreStorage) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (null != storageService) {
                try {
                    final Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return obfuscator.unwrap(storageService.findFirstSessionForUser(userId, contextId));
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
     * @param tranzient <code>true</code> if the session should be transient, <code>false</code>, otherwise
     * @return The created session
     * @throws OXException If creating a new session fails
     */
    protected static SessionImpl addSession(final int userId, final String loginName, final String password, final int contextId, final String clientHost, final String login, final String authId, final String hash, final String client, final String clientToken, final boolean tranzient) throws OXException {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            throw SessionExceptionCodes.NOT_INITIALIZED.create();
        }
        checkMaxSessPerUser(userId, contextId, false);
        checkMaxSessPerClient(client, userId, contextId, false);
        checkAuthId(login, authId);
        final SessionIdGenerator sessionIdGenerator = SessionHandler.sessionIdGenerator;
        final String sessionId = sessionIdGenerator.createSessionId(loginName, clientHost);
        final SessionImpl session = new SessionImpl(userId, loginName, password, contextId, sessionId, sessionIdGenerator.createSecretId(
            loginName, Long.toString(System.currentTimeMillis())), sessionIdGenerator.createRandomId(), clientHost, login, authId, hash,
            client, tranzient);
        // Add session
        final SessionImpl addedSession;
        if (null == clientToken) {
            addedSession = sessionData.addSession(session, noLimit).getSession();
            // store session if not marked as transient
            if (false == session.isTransient()) {
                final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                if (sessionStorageService != null) {
                    if (asyncPutToSessionStorage) {
                        // Enforced asynchronous put
                        storeSessionAsync(addedSession, sessionStorageService, false, null);
                    } else {
                        storeSessionSync(addedSession, sessionStorageService, false);
                    }
                }
            }
            // Post event for created session
            postSessionCreation(addedSession);
        } else {
            final String serverToken = sessionIdGenerator.createRandomId();
            // TODO change return type and return an interface that allows to dynamically add additional return values.
            session.setParameter("serverToken", serverToken);
            final TokenSessionControl control = TokenSessionContainer.getInstance().addSession(session, clientToken, serverToken);
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
    public static void storeSessionSync(final SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent) {
        storeSession(session, sessionStorageService, addIfAbsent, false, null);
    }

    /**
     * (Asynchronously) Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly replacing put
     * @param latch The latch needed to signal completion of invocation
     */
    public static void storeSessionAsync(final SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent, final CountDownLatch latch) {
        storeSession(session, sessionStorageService, addIfAbsent, true, latch);
    }

    /**
     * Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly replacing put
     * @param async Whether to perform task asynchronously or not
     * @param latch The latch needed when invoked asynchronously; otherwise simply pass <code>null</code>
     */
    public static void storeSession(final SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent, final boolean async, final CountDownLatch latch) {
        if (null == session || null == sessionStorageService) {
            return;
        }
        if (async) {
            ThreadPools.getThreadPool().submit(new StoreSessionTask(session, sessionStorageService, addIfAbsent, latch));
        } else {
            final StoreSessionTask task = new StoreSessionTask(session, sessionStorageService, addIfAbsent, latch);
            final Thread thread = Thread.currentThread();
            boolean ran = false;
            task.beforeExecute(thread);
            try {
                task.call();
                ran = true;
                task.afterExecute(null);
            } catch (final Exception ex) {
                if (!ran) {
                    task.afterExecute(ex);
                }
                // Else the exception occurred within
                // afterExecute itself in which case we don't
                // want to call it again.
                final OXException oxe = (ex instanceof OXException ? (OXException) ex : SessionExceptionCodes.SESSIOND_EXCEPTION.create(ex, ex.getMessage()));
                LOG.warn(oxe.getMessage(), oxe);
            }
        }
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
        if (asyncPutToSessionStorage) {
            for (final SessionImpl session : sessions) {
                storeSessionAsync(session, sessionStorageService, true, null);
            }
        } else {
            for (final SessionImpl session : sessions) {
                storeSessionSync(session, sessionStorageService, true);
            }
        }
    }

    private static void checkMaxSessPerUser(final int userId, final int contextId, boolean considerSessionStorage) throws OXException {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        final int maxSessPerUser = config.getMaxSessionsPerUser();
        if (maxSessPerUser > 0) {
            int count = sessionData.getNumOfUserSessions(userId, contextId);
            if (count >= maxSessPerUser) {
                throw SessionExceptionCodes.MAX_SESSION_PER_USER_EXCEPTION.create(I(userId), I(contextId));
            }
            if (considerSessionStorage) {
                final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
                if (storageService != null) {
                    try {
                        final Task<Integer> c = new AbstractTask<Integer>() {

                            @Override
                            public Integer call() throws Exception {
                                return Integer.valueOf(storageService.getUserSessionCount(userId, contextId));
                            }
                        };
                        count = getFrom(c, Integer.valueOf(0)).intValue();
                        if (count >= maxSessPerUser) {
                            throw SessionExceptionCodes.MAX_SESSION_PER_USER_EXCEPTION.create(I(userId), I(contextId));
                        }
                    } catch (final OXException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private static void checkMaxSessPerClient(final String client, final int userId, final int contextId, boolean considerSessionStorage) throws OXException {
        if (null == client) {
            // Nothing to check against
            return;
        }
        final int maxSessPerClient = config.getMaxSessionsPerClient();
        if (maxSessPerClient > 0) {
            final SessionData sessionData = sessionDataRef.get();
            final SessionControl[] userSessions = null == sessionData ? new SessionControl[0] : sessionData.getUserSessions(userId, contextId);
            int cnt = 0;
            for (final SessionControl sessionControl : userSessions) {
                if (client.equals(sessionControl.getSession().getClient()) && ++cnt > maxSessPerClient) {
                    throw SessionExceptionCodes.MAX_SESSION_PER_CLIENT_EXCEPTION.create(client, I(userId), I(contextId));
                }
            }
            if (considerSessionStorage) {
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
                            final Session[] storedSessions = getFrom(c, new Session[0]);
                            cnt = 0;
                            for (final Session session : storedSessions) {
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
        }
    }

    private static void checkAuthId(final String login, final String authId) throws OXException {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        sessionData.checkAuthId(login, authId);
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
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        final SessionControl sessionControl = sessionData.clearSession(sessionid);
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
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        if (DEBUG) {
            LOG.debug(new StringBuilder("changeSessionPassword <").append(sessionid).append('>').toString());
        }
        final SessionControl sessionControl = sessionData.getSession(sessionid);
        if (null == sessionControl) {
            throw SessionExceptionCodes.PASSWORD_UPDATE_FAILED.create();
        }
        /*
         * Change password in current session
         */
        final SessionImpl currentSession = sessionControl.getSession();
        currentSession.setPassword(newPassword);
        final SessionStorageService sessionStorage = getServiceRegistry().getService(SessionStorageService.class);
        if (null != sessionStorage) {
            final Task<Void> c = new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    sessionStorage.changePassword(sessionid, newPassword);
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
            final Task<Void> c = new AbstractTask<Void>() {

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
                final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                if (sessionStorageService != null) {
                    final AbstractTask<Void> c = new AbstractTask<Void>() {

                        @Override
                        public Void call() throws Exception {
                            sessionStorageService.setLocalIp(session.getSessionID(), localIp);
                            return null;
                        }
                    };
                    submit(c);
                }
            } catch (final RuntimeException e) {
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
                final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                if (sessionStorageService != null) {
                    final AbstractTask<Void> c = new AbstractTask<Void>() {

                        @Override
                        public Void call() throws Exception {
                            sessionStorageService.setClient(session.getSessionID(), client);
                            return null;
                        }
                    };
                    submit(c);
                }
            } catch (final RuntimeException e) {
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
                final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                if (sessionStorageService != null) {
                    final AbstractTask<Void> c = new AbstractTask<Void>() {

                        @Override
                        public Void call() throws Exception {
                            sessionStorageService.setHash(session.getSessionID(), hash);
                            return null;
                        }
                    };
                    submit(c);
                }
            } catch (final RuntimeException e) {
                throw SessionExceptionCodes.SESSIOND_EXCEPTION.create(e, e.getMessage());
            }
        }
    }

    protected static Session getSessionByRandomToken(final String randomToken, final String newIP) {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        final SessionControl sessionControl = sessionData.getSessionByRandomToken(randomToken);
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
                    final Session s = obfuscator.unwrap(getFrom(c, null));
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

    static Session getSessionWithTokens(final String clientToken, final String serverToken) throws OXException {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            throw SessionExceptionCodes.NOT_INITIALIZED.create();
        }
        // find session matching to tokens
        final TokenSessionControl tokenControl = TokenSessionContainer.getInstance().getSession(clientToken, serverToken);
        final SessionImpl activatedSession = tokenControl.getSession();

        // Put this session into the normal session container
        final SessionControl sessionControl = sessionData.addSession(activatedSession, noLimit);
        final SessionImpl addedSession = sessionControl.getSession();
        final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
        if (sessionStorageService != null) {
            if (asyncPutToSessionStorage) {
                storeSessionAsync(addedSession, sessionStorageService, false, null);
            } else {
                storeSessionSync(addedSession, sessionStorageService, false);
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
    protected static SessionControl getSession(final String sessionId, final boolean considerSessionStorage) {
        if (DEBUG) {
            LOG.debug(new StringBuilder("getSession <").append(sessionId).append('>').toString());
        }
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        final SessionControl sessionControl = sessionData.getSession(sessionId);
        if (considerSessionStorage && null == sessionControl) {
            final SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    final Session storedSession = getSessionFrom(sessionId, storageService);
                    if (null != storedSession) {
                        final SessionControl sc = sessionData.addSession(new SessionImpl(storedSession), noLimit, true);
                        return null == sc ? sessionToSessionControl(storedSession) : sc;
                    }
                } catch (final OXException e) {
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
    protected static boolean isActive(final String sessionId) {
        final SessionData sessionData = sessionDataRef.get();
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
    protected static SessionControl getSessionByAlternativeId(final String altId) {
        if (DEBUG) {
            LOG.debug(new StringBuilder("getSessionByAlternativeId <").append(altId).append('>').toString());
        }
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        final SessionControl sessionControl = sessionData.getSessionByAlternativeId(altId);
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
                    final Session session = obfuscator.unwrap(getFrom(c, null));
                    if (null != session) {
                        return sessionToSessionControl(session);
                    }
                } catch (final RuntimeException e) {
                    LOG.error(e.getMessage(), e);
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
        if (DEBUG) {
            LOG.debug(new StringBuilder("getCachedSession <").append(sessionId).append('>').toString());
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
                final Session session = obfuscator.unwrap(getFrom(c, null));
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
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return Collections.emptyList();
        }
        final List<SessionControl> retval = sessionData.getShortTermSessions();
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
                        result.add(sessionToSessionControl(obfuscator.unwrap(s)));
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
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        final List<SessionControl> controls = sessionData.rotateShort();
        final String message;
        if (config.isAutoLogin()) {
            message = "Session is moved to long life time container. All temporary session data will be cleaned up. ID: ";
        } else {
            message = "Session timed out. ID: ";
        }
        for (final SessionControl sessionControl : controls) {
            LOG.info(message + sessionControl.getSession().getSessionID());
        }
        postSessionDataRemoval(controls);
    }

    protected static void cleanUpLongTerm() {
        final SessionData sessionData = sessionDataRef.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        final List<SessionControl> controls = sessionData.rotateLongTerm();
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
            } else {
                LOG.warn("\tSessionData instance is null.");
            }
            sessionIdGenerator = null;
            config = null;
            noLimit = false;
        }
    }

    public static int getNumberOfActiveSessions() {
        final SessionData sessionData = sessionDataRef.get();
        return null == sessionData ? 0 : sessionData.countSessions();
    }

    public static int[] getNumberOfLongTermSessions() {
        final SessionData sessionData = sessionDataRef.get();
        return null == sessionData ? new int[0]: sessionData.getLongTermSessionsPerContainer();
    }

    public static int[] getNumberOfShortTermSessions() {
        final SessionData sessionData = sessionDataRef.get();
        return null == sessionData ? new int[0] : sessionData.getShortTermSessionsPerContainer();
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

    static void postSessionRemoval(final SessionImpl session) {
        if (false == session.isTransient()) {
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
            final SessionData sessionData = sessionDataRef.get();
            if (null != sessionData) {
                if (sessionData.isUserActive(session.getUserId(), session.getContextId())) {
                    postLastSessionGone(session.getUserId(), session.getContextId(), eventAdmin);
                }
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
                final List<SessionControl> tSessionControls = new ArrayList<SessionControl>(sessionControls);
                ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() {
                        try {
                            for (final SessionControl sessionControl : tSessionControls) {
                                SessionImpl session = sessionControl.getSession();
                                if (null != session && false == session.isTransient()) {
                                    try {
                                        sessionStorageService.removeSession(session.getSessionID());
                                    } catch (final OXException e) {
                                        LOG.error(e.getMessage(), e);
                                    } catch (final RuntimeException e) {
                                        LOG.error(e.getMessage(), e);
                                    }
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
            final SessionData sessionData = sessionDataRef.get();
            if (null != sessionData) {
                for (final UserKey userKey : users) {
                    if (sessionData.isUserActive(userKey.userId, userKey.contextId)) {
                        postLastSessionGone(userKey.userId, userKey.contextId, eventAdmin);
                    }
                }
            }
        }
    }

    private static void postSessionDataRemoval(final List<SessionControl> controls) {
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
            final SessionData sessionData = sessionDataRef.get();
            if (null != sessionData) {
                for (final UserKey userKey : users) {
                    if (sessionData.isUserActive(userKey.userId, userKey.contextId)) {
                        postLastSessionGone(userKey.userId, userKey.contextId, eventAdmin);
                    }
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
            final Event event = new Event(SessiondEventConstants.TOPIC_REACTIVATE_SESSION, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for reactivated session");
            }
        }
    }

    /**
     * Broadcasts the {@link SessiondEventConstants#TOPIC_TOUCH_SESSION} event, usually after the session has been moved to the first
     * container.
     *
     * @param session The session that was touched
     */
    static void postSessionTouched(final Session session) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_TOUCH_SESSION, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for touched session");
            }
        }
    }

    public static void addThreadPoolService(final ThreadPoolService service) {
        final SessionData sessionData = sessionDataRef.get();
        if (null != sessionData) {
            sessionData.addThreadPoolService(service);
        }
    }

    public static void removeThreadPoolService() {
        final SessionData sessionData = sessionDataRef.get();
        if (null != sessionData) {
            sessionData.removeThreadPoolService();
        }
    }

    public static void addTimerService(final TimerService service) {
        final SessionData sessionData = sessionDataRef.get();
        if (null != sessionData) {
            sessionData.addTimerService(service);
        }
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
        final SessionData sessionData = sessionDataRef.get();
        if (null != sessionData) {
            sessionData.removeTimerService();
        }
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
        private final CountDownLatch optLatch;

        protected StoreSessionTask(final SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent, final CountDownLatch optLatch) {
            super();
            this.sessionStorageService = sessionStorageService;
            this.addIfAbsent = addIfAbsent;
            this.session = session;
            this.optLatch = optLatch;
        }

        @Override
        public Void call() {
            try {
                if (addIfAbsent) {
                    if (sessionStorageService.addSessionIfAbsent(obfuscator.wrap(session))) {
                        LOG.info("Put session " + session.getSessionID() + " with auth Id " + session.getAuthId() + " into session storage.");
                        postSessionStored(session);
                    }
                } else {
                    sessionStorageService.addSession(obfuscator.wrap(session));
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
            } finally {
                final CountDownLatch latch = optLatch;
                if (null != latch) {
                    latch.countDown();
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
            return obfuscator.unwrap(storageService.lookupSession(sessionId));
        }
    }

    private static volatile Integer timeout;
    private static int timeout() {
        Integer tmp = timeout;
        if (null == tmp) {
            synchronized (SessionHandler.class) {
                tmp = timeout;
                if (null == tmp) {
                    final ConfigurationService service = getServiceRegistry().getService(ConfigurationService.class);
                    final int defaultTimeout = 3000;
                    tmp = Integer.valueOf(null == service ? defaultTimeout : service.getIntProperty("com.openexchange.sessiond.sessionstorage.timeout", defaultTimeout));
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

    private static <V> void submit(final AbstractTask<V> c) {
        try {
            ThreadPools.getThreadPool().submit(c);
        } catch (final RejectedExecutionException e) {
            c.execute();
        }
    }

    private static <V> void submitAndIgnoreRejection(final Task<V> c) {
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
