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

package com.openexchange.sessiond.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.sessiond.impl.TimeoutTaskWrapper.submit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.session.SessionSerializationInterceptor;
import com.openexchange.sessiond.SessionCounter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.osgi.Services;
import com.openexchange.sessiond.serialization.PortableContextSessionsCleaner;
import com.openexchange.sessiond.serialization.PortableSessionFilterApplier;
import com.openexchange.sessiond.serialization.PortableSessionFilterApplier.Action;
import com.openexchange.sessiond.serialization.PortableUserSessionsCleaner;
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

    public static final SessionCounter SESSION_COUNTER = new SessionCounter() {

        @Override
        public int getNumberOfSessions(int userId, final int contextId) {
            return SESSION_DATA_REF.get().getNumOfUserSessions(userId, contextId, true);
        }
    };

    /** The session identifier generator */
    private static volatile SessionIdGenerator sessionIdGenerator;

    /** The applied configuration */
    static volatile SessiondConfigInterface config;

    /** The {@link SessionData} reference */
    protected static final AtomicReference<SessionData> SESSION_DATA_REF = new AtomicReference<SessionData>();

    /** Whether there is no limit when adding a new session */
    private static volatile boolean noLimit;

    /** Whether to put session s to central session storage asynchronously (default) or synchronously */
    private static volatile boolean asyncPutToSessionStorage;

    /** The obfuscator */
    protected static volatile Obfuscator obfuscatr;

    /** The initialized flag */
    private static final AtomicBoolean initialized = new AtomicBoolean();

    /** Logger */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionHandler.class);

    private static volatile ScheduledTimerTask shortSessionContainerRotator;

    private static volatile ScheduledTimerTask longSessionContainerRotator;

    private static List<SessionSerializationInterceptor> interceptors = Collections.synchronizedList(new LinkedList<SessionSerializationInterceptor>());

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
        SESSION_DATA_REF.set(sessionData);
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
                obfuscatr = new Obfuscator(config.getObfuscationKey().toCharArray());
            }
        }
    }

    /**
     * Gets the configuration
     *
     * @return The configuration
     */
    public static SessiondConfigInterface getConfig() {
        return config;
    }

    /**
     * Gets the session obfuscator that performs the conversion into/from a stored session
     *
     * @return The session obfuscator instance
     */
    public static Obfuscator getObfuscator() {
        return obfuscatr;
    }

    /**
     * Gets the names of such parameters that are supposed to be taken over from session to stored session representation.
     *
     * @return The parameter names
     */
    public static List<String> getRemoteParameterNames() {
        SessiondConfigInterface conf = config;
        return null == conf ? Collections.<String> emptyList() : conf.getRemoteParameterNames();
    }

    /**
     * Removes all sessions associated with given user in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The wrapper objects for removed sessions
     */
    public static Session[] removeUserSessions(final int userId, final int contextId) {
        SessionData sessionData = SESSION_DATA_REF.get();
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
            SessionImpl removedSession = control[i].getSession();
            retval[i] = removedSession;
            postSessionRemoval(removedSession);
        }
        /*
         * remove local sessions from storage (if available), too
         */
        final SessionStorageService storageService = Services.optService(SessionStorageService.class);
        if (null == storageService) {
            LOG.info("Local removal of user sessions: User={}, Context={}", Integer.valueOf(userId), Integer.valueOf(contextId));
            return retval;
        }
        Session[] retval2 = null;
        try {
            Task<Session[]> c = new AbstractTask<Session[]>() {

                @Override
                public Session[] call() throws Exception {
                    return storageService.removeLocalUserSessions(userId, contextId);
                }
            };
            retval2 = getFrom(c, new Session[0]);
        } catch (RuntimeException e) {
            LOG.error("", e);
        }
        LOG.info("Remote removal of user sessions: User={}, Context={}", Integer.valueOf(userId), Integer.valueOf(contextId));
        return merge(retval, retval2);
    }

    /**
     * Globally removes sessions associated to the given contexts. 'Globally' means sessions on all cluster nodes
     *
     * @param contextIds - Set with context ids to be removed
     * @throws OXException
     */
    public static void removeContextSessionsGlobal(final Set<Integer> contextIds) throws OXException {
        SessionHandler.removeRemoteContextSessions(contextIds);
        SessionHandler.removeContextSessions(contextIds);
    }

    /**
     * Globally removes sessions associated to the given contexts. 'Globally' means sessions on all cluster nodes
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @throws OXException If operation fails
     */
    public static void removeUserSessionsGlobal(int userId, int contextId) throws OXException {
        SessionHandler.removeRemoteUserSessions(userId, contextId);
        SessionHandler.removeUserSessions(userId, contextId);
    }

    /**
     * Triggers removing sessions for given context ids on remote cluster nodes
     *
     * @param contextIds - Set with context ids to be removed
     */
    private static void removeRemoteContextSessions(final Set<Integer> contextIds) throws OXException {
        HazelcastInstance hazelcastInstance = Services.optService(HazelcastInstance.class);

        if (hazelcastInstance != null) {
            LOG.debug("Trying to remove sessions for context ids {} from remote nodes", Strings.concat(", ", contextIds));

            Member localMember = hazelcastInstance.getCluster().getLocalMember();
            Set<Member> clusterMembers = new HashSet<Member>(hazelcastInstance.getCluster().getMembers());
            if (!clusterMembers.remove(localMember)) {
                LOG.warn("Couldn't remove local member from cluster members.");
            }
            if (!clusterMembers.isEmpty()) {
                Map<Member, Future<Set<Integer>>> submitToMembers = hazelcastInstance.getExecutorService("default").submitToMembers(new PortableContextSessionsCleaner(contextIds), clusterMembers);
                for (Entry<Member, Future<Set<Integer>>> memberEntry : submitToMembers.entrySet()) {
                    Future<Set<Integer>> future = memberEntry.getValue();
                    int hzExecutionTimeout = getRemoteContextSessionsExecutionTimeout();

                    Member member = memberEntry.getKey();
                    try {
                        Set<Integer> contextIdsSessionsHaveBeenRemovedFor = null;
                        if (hzExecutionTimeout > 0) {
                            contextIdsSessionsHaveBeenRemovedFor = future.get(hzExecutionTimeout, TimeUnit.SECONDS);
                        } else {
                            contextIdsSessionsHaveBeenRemovedFor = future.get();
                        }
                        if ((contextIdsSessionsHaveBeenRemovedFor != null) && (future.isDone())) {
                            LOG.info("Removed sessions for context ids {} on remote node {}", Strings.concat(", ", contextIdsSessionsHaveBeenRemovedFor), member.getSocketAddress().toString());
                        } else {
                            LOG.warn("No sessions for context ids {} removed on node {}.", Strings.concat(", ", contextIds), member.getSocketAddress().toString());
                        }
                    } catch (TimeoutException e) {
                        // Wait time elapsed; enforce cancelation
                        future.cancel(true);
                        LOG.error("Removing sessions for context ids {} on remote node {} took to longer than {} seconds and was aborted!", Strings.concat(", ", contextIds), member.getSocketAddress().toString(), hzExecutionTimeout, e);
                    } catch (InterruptedException e) {
                        future.cancel(true);
                        LOG.error("Removing sessions for context ids {} on remote node {} took to longer than {} seconds and was aborted!", Strings.concat(", ", contextIds), member.getSocketAddress().toString(), hzExecutionTimeout, e);
                    } catch (ExecutionException e) {
                        future.cancel(true);
                        LOG.error("Removing sessions for context ids {} on remote node {} took to longer than {} seconds and was aborted!", Strings.concat(", ", contextIds), member.getSocketAddress().toString(), hzExecutionTimeout, e.getCause());
                    } catch (Exception e) {
                        LOG.error("Failed to issue remote session removal for contexts {} on remote node {}.", Strings.concat(", ", contextIds), member.getSocketAddress().toString(), e.getCause());
                        throw SessionExceptionCodes.REMOTE_SESSION_REMOVAL_FAILED.create(Strings.concat(", ", contextIds), member.getSocketAddress().toString(), e.getCause());
                    }
                }
            } else {
                LOG.debug("No other cluster members besides the local member. No further clean up necessary.");
            }
        } else {
            LOG.warn("Cannot find HazelcastInstance for remote execution of session removing for context ids {}. Only local sessions will be removed.", Strings.concat(", ", contextIds));
        }
    }

    private static void removeRemoteUserSessions(int userId, int contextId) throws OXException {
        LOG.debug("Trying to remove sessions for user {} in context {} from remote nodes", userId, contextId);
        Map<Member, Integer> results = executeGlobalTask(new PortableUserSessionsCleaner(userId, contextId));
        for (Entry<Member, Integer> memberEntry : results.entrySet()) {
            Member member = memberEntry.getKey();
            Integer numOfRemovedSessions = memberEntry.getValue();
            if (numOfRemovedSessions == null) {
                LOG.warn("No sessions removed for user {} in context {} on remote node {}.", userId, contextId, member.getSocketAddress().toString());
            } else {
                LOG.info("Removed {} sessions for user {} in context {} on remote node {}", numOfRemovedSessions, userId, contextId, member.getSocketAddress().toString());
            }
        }
    }

    /**
     * Removes all sessions from remote nodes that match the given filter (excluding this node).
     *
     * @param filter The filter
     * @return The session IDs of all removed sessions
     */
    public static List<String> removeRemoteSessions(SessionFilter filter) throws OXException {
        LOG.debug("Trying to remove sessions from remote nodes by filter '{}'", filter);
        Map<Member, Collection<String>> results = executeGlobalTask(new PortableSessionFilterApplier(filter, Action.REMOVE));
        List<String> sessionIds = new ArrayList<String>();
        for (Entry<Member, Collection<String>> memberEntry : results.entrySet()) {
            Collection<String> memberSessionIds = memberEntry.getValue();
            if (memberSessionIds != null) {
                LOG.debug("Removed {} sessions on node {} for filter '{}'", memberSessionIds.size(), memberEntry.getKey().getSocketAddress().toString(), filter);
                sessionIds.addAll(memberSessionIds);
            }
        }

        return sessionIds;
    }

    /**
     * Finds all sessions on remote nodes that match the given filter (excluding this node).
     *
     * @param filter The filter
     * @return The session IDs of all found sessions
     */
    public static List<String> findRemoteSessions(SessionFilter filter) throws OXException {
        LOG.debug("Trying to find sessions on remote nodes by filter '{}'", filter);
        Map<Member, Collection<String>> results = executeGlobalTask(new PortableSessionFilterApplier(filter, Action.GET));
        List<String> sessionIds = new ArrayList<String>();
        for (Entry<Member, Collection<String>> memberEntry : results.entrySet()) {
            Collection<String> memberSessionIds = memberEntry.getValue();
            if (memberSessionIds != null) {
                LOG.debug("Found {} sessions on node {} for filter '{}'", memberSessionIds.size(), memberEntry.getKey().getSocketAddress().toString(), filter);
                sessionIds.addAll(memberSessionIds);
            }
        }

        return sessionIds;
    }

    /**
     * Finds all local sessions that match the given filter and returns their IDs.
     *
     * @param filter The filter
     * @return The found session IDs
     */
    public static List<String> findLocalSessions(SessionFilter filter) {
        final SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return Collections.emptyList();
        }

        List<Session> sessions = sessionData.filterSessions(filter);
        List<String> sessionIds = new ArrayList<String>(sessions.size());
        for (Session session : sessions) {
            sessionIds.add(session.getSessionID());
        }

        return sessionIds;
    }

    /**
     * Removes all local sessions that match the given filter and returns their IDs.
     *
     * @param filter The filter
     * @return The session IDs
     */
    public static List<String> removeLocalSessions(SessionFilter filter) {
        List<String> sessionIds = findLocalSessions(filter);
        for (String sessionId : sessionIds) {
            clearSession(sessionId);
        }

        return sessionIds;
    }

    /**
     * Executes a callable on all hazelcast members but this one. The given callable must be a portable or in other ways
     * serializable by hazelcast.
     *
     * @param callable The callable
     * @return A map containing the result for each member. If execution failed or timed out, the entry for a member will be <code>null</code>!
     */
    private static <T> Map<Member, T> executeGlobalTask(Callable<T> callable) {
        HazelcastInstance hazelcastInstance = Services.optService(HazelcastInstance.class);
        if (hazelcastInstance == null) {
            LOG.warn("Cannot find HazelcastInstance for remote execution of callable {}.", callable);
            return Collections.emptyMap();
        }

        Member localMember = hazelcastInstance.getCluster().getLocalMember();
        Set<Member> clusterMembers = new HashSet<Member>(hazelcastInstance.getCluster().getMembers());
        if (!clusterMembers.remove(localMember)) {
            LOG.warn("Couldn't remove local member from cluster members.");
        }

        if (clusterMembers.isEmpty()) {
            LOG.debug("No other cluster members besides the local member. Execution of callable {} not necessary.");
            return Collections.emptyMap();
        }

        int hzExecutionTimeout = getRemoteSessionTaskTimeout();
        Map<Member, Future<T>> submitToMembers = hazelcastInstance.getExecutorService("default").submitToMembers(callable, clusterMembers);
        Map<Member, T> results = new HashMap<Member, T>(submitToMembers.size(), 1.0f);
        for (Entry<Member, Future<T>> memberEntry : submitToMembers.entrySet()) {
            Member member = memberEntry.getKey();
            Future<T> future = memberEntry.getValue();
            T result = null;
            try {
                if (hzExecutionTimeout > 0) {
                    result = future.get(hzExecutionTimeout, TimeUnit.SECONDS);
                } else {
                    result = future.get();
                }
            } catch (TimeoutException e) {
                future.cancel(true);
                LOG.error("Executing callable {} on remote node {} took to longer than {} seconds and was aborted!", callable, member.getSocketAddress().toString(), Integer.valueOf(hzExecutionTimeout), e);
            } catch (InterruptedException e) {
                future.cancel(true);
                LOG.error("Executing callable {} on remote node {} took to longer than {} seconds and was aborted!", callable, member.getSocketAddress().toString(), Integer.valueOf(hzExecutionTimeout), e);
            } catch (ExecutionException e) {
                future.cancel(true);
                LOG.error("Executing callable {} on remote node {} failed!", callable, member.getSocketAddress().toString(), e.getCause());
            } finally {
                results.put(member, result);
            }
        }

        return results;
    }

    /**
     * Returns the timeout (in seconds) configured to wait for remote invalidation of context sessions. Default value 0 means "no timeout"
     *
     * @return timeout (in seconds) or 0 for no timeout
     */
    private static int getRemoteContextSessionsExecutionTimeout() {
        final ConfigurationService configurationService = Services.optService(ConfigurationService.class);
        if (configurationService == null) {
            LOG.info("ConfigurationService not available. No execution timeout for remote processing of context sessions invalidation available. Fallback to no timeout.");
            return 0;
        }
        return configurationService.getIntProperty("com.openexchange.remote.context.sessions.invalidation.timeout", 0);
    }

    /**
     * Returns the timeout (in seconds) configured to wait for remote execution of session tasks. Default value 0 means "no timeout"
     *
     * @return timeout (in seconds) or 0 for no timeout
     */
    private static int getRemoteSessionTaskTimeout() {
        int defaultValue = 300;
        ConfigurationService configurationService = Services.optService(ConfigurationService.class);
        if (configurationService == null) {
            LOG.info("ConfigurationService not available. No execution timeout for remote processing of session tasks available. Fallback to no timeout.");
            return defaultValue;
        }
        return configurationService.getIntProperty("com.openexchange.remote.session.task.timeout", defaultValue);
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
            ContextService cs = Services.optService(ContextService.class);
            if (null != cs) {
                try {
                    cs.loadContext(contextId);
                } catch (OXException e) {
                    if (2 == e.getCode() && "CTX".equals(e.getPrefix())) { // See com.openexchange.groupware.contexts.impl.ContextExceptionCodes.NOT_FOUND
                        LOG.info("No such context {}", Integer.valueOf(contextId));
                        return;
                    }
                }
            }
        }
        /*
         * Continue...
         */
        removeContextSessions(Collections.singleton(contextId));
    }

    /**
     * Removes all sessions associated to the given contexts.
     *
     * @param contextId Set with contextIds to remove session for.
     */
    public static Set<Integer> removeContextSessions(final Set<Integer> contextIds) {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        /*
         * remove from session data
         */
        List<SessionControl> removeContextSessions = sessionData.removeContextSessions(contextIds);
        postContainerRemoval(removeContextSessions, true);

        Set<Integer> processedContexts = new HashSet<Integer>(removeContextSessions.size());
        for (SessionControl control : removeContextSessions) {
            processedContexts.add(control.getSession().getContextId());
        }

        LOG.info("Removed {} sessions for {} contexts", removeContextSessions.size(), processedContexts.size());
        return processedContexts;
    }

    /**
     * Checks for any active session for specified context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if at least one active session is found; otherwise <code>false</code>
     */
    public static boolean hasForContext(final int contextId, boolean considerSessionStorage) {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        boolean hasForContext = sessionData.hasForContext(contextId);
        if (!hasForContext && considerSessionStorage) {
            final SessionStorageService storageService = Services.optService(SessionStorageService.class);
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
     * @param considerSessionStorage <code>true</code> to also consider session storage; otherwise <code>false</code>
     * @return The wrapper objects for sessions
     */
    public static List<SessionControl> getUserSessions(final int userId, final int contextId, boolean considerSessionStorage) {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return new LinkedList<SessionControl>();
        }
        List<SessionControl> retval = sessionData.getUserSessions(userId, contextId);
        if (considerSessionStorage) {
            final SessionStorageService storageService = Services.optService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Session[]> c = new AbstractTask<Session[]>() {

                        @Override
                        public Session[] call() throws Exception {
                            Session[] userSessions = storageService.getUserSessions(userId, contextId);
                            if (null == userSessions) {
                                return new Session[0];
                            }

                            int length = userSessions.length;
                            if (length == 0) {
                                return userSessions;
                            }

                            // Unwrap
                            Session[] retval = new Session[length];
                            List<String> remoteParameterNames = getRemoteParameterNames();
                            for (int i = length; i-- > 0;) {
                                retval[i] = getObfuscator().unwrap(userSessions[i], remoteParameterNames);
                            }
                            return retval;
                        }
                    };
                    Session[] sessions = getFrom(c, new Session[0]);
                    for (int i = 0; i < sessions.length; i++) {
                        retval.add(sessionToSessionControl(sessions[i]));
                    }
                } catch (RuntimeException e) {
                    LOG.error("", e);
                }
            }
        }
        return retval;
    }

    /**
     * Gets the number of <b>local-only</b> sessions associated with specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param considerLongTerm <code>true</code> to also consider long-term sessions; otherwise <code>false</code>
     * @return The number of sessions
     */
    public static int getNumOfUserSessions(int userId, int contextId, boolean considerLongTerm) {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return 0;
        }

        return sessionData.getNumOfUserSessions(userId, contextId, considerLongTerm);
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl retval = sessionData.getAnyActiveSessionForUser(userId, contextId, includeLongTerm);
        if (retval == null && includeStorage) {
            final SessionStorageService storageService = Services.optService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return storageService.getAnyActiveSessionForUser(userId, contextId);
                        }
                    };
                    SessionImpl unwrappedSession = getObfuscator().unwrap(getFrom(c, null), getRemoteParameterNames());
                    if (null != unwrappedSession) {
                        retval = new SessionControl(unwrappedSession);
                    }
                } catch (RuntimeException e) {
                    LOG.error("", e);
                }
            }
        }
        return retval;
    }

    public static Session findFirstSessionForUser(final int userId, final int contextId, final SessionMatcher matcher, final boolean ignoreLongTerm, final boolean ignoreStorage) {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        Session retval = sessionData.findFirstSessionForUser(userId, contextId, matcher, ignoreLongTerm);
        if (null == retval && !ignoreStorage) {
            final SessionStorageService storageService = Services.optService(SessionStorageService.class);
            if (null != storageService) {
                try {
                    Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return getObfuscator().unwrap(storageService.findFirstSessionForUser(userId, contextId), getRemoteParameterNames());
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
     * Stores the session (if available) into session storage.
     *
     * @param sessionId The session identifier
     * @return <code>true</code> if stored; otherwise <code>false</code>
     */
    protected static boolean storeSession(String sessionId) {
        if (null == sessionId) {
            return false;
        }

        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        SessionControl sessionControl = sessionData.getSession(sessionId);
        if (null == sessionControl) {
            return false;
        }

        List<String> remotes = getRemoteParameterNames();
        return putIntoSessionStorage(sessionControl.getSession(), true, null == remotes || remotes.isEmpty());
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
     * @param enhancement after creating the session, this callback will be called when not <code>null</code> for extending the session.
     * @return The created session
     * @throws OXException If creating a new session fails
     */
    protected static SessionImpl addSession(int userId, String loginName, String password, int contextId, String clientHost, String login, String authId, String hash, String client, String clientToken, boolean tranzient, SessionEnhancement enhancement) throws OXException {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            throw SessionExceptionCodes.NOT_INITIALIZED.create();
        }

        // Various checks
        checkMaxSessPerUser(userId, contextId, false);
        checkMaxSessPerClient(client, userId, contextId, false);
        checkAuthId(login, authId);

        // Create new session instance
        SessionImpl newSession = createNewSession(userId, loginName, password, contextId, clientHost, login, authId, hash, client, tranzient);
        if (null != enhancement) {
            enhancement.enhanceSession(newSession);
        }

        // Either add session or yield short-time token for it
        SessionImpl addedSession;
        if (null == clientToken) {
            addedSession = sessionData.addSession(newSession, noLimit).getSession();

            // Store session if not marked as transient and associated client is applicable
            List<String> remotes = getRemoteParameterNames();
            putIntoSessionStorage(addedSession, null == remotes || remotes.isEmpty());

            // Post event for created session
            postSessionCreation(addedSession);
        } else {
            String serverToken = sessionIdGenerator.createRandomId();
            // TODO change return type and return an interface that allows to dynamically add additional return values.
            newSession.setParameter("serverToken", serverToken);
            TokenSessionContainer.getInstance().addSession(newSession, clientToken, serverToken);
            addedSession = newSession;
        }

        // Return added  session
        return addedSession;
    }

    /**
     * Creates a new instance of {@code SessionImpl} from specified arguments
     *
     * @param userId The user identifier
     * @param loginName The login name
     * @param password The password
     * @param contextId The context identifier
     * @param clientHost The client host name or IP address
     * @param login The login; e.g. <code>"someone@invalid.com"</code>
     * @param authId The authentication identifier
     * @param hash The hash string
     * @param client The client identifier
     * @param tranzient Whether the session is meant to be transient/volatile; typically the session gets dropped soon
     * @return The newly created {@code SessionImpl} instance
     * @throws OXException If create attempt fails
     */
    private static SessionImpl createNewSession(int userId, String loginName, String password, int contextId, String clientHost, String login, String authId, String hash, String client, boolean tranzient) throws OXException {
        // Generate identifier, secret, and random
        SessionIdGenerator sessionIdGenerator = SessionHandler.sessionIdGenerator;
        String sessionId = sessionIdGenerator.createSessionId(loginName, clientHost);
        String secret = sessionIdGenerator.createSecretId(loginName, Long.toString(System.currentTimeMillis()));
        String randomToken = sessionIdGenerator.createRandomId();

        // Create the instance
        SessionImpl newSession = new SessionImpl(userId, loginName, password, contextId, sessionId, secret, randomToken, clientHost, login, authId, hash, client, tranzient);

        // Return...
        return newSession;
    }

    /**
     * Puts the given session into session storage if possible
     *
     * @param session The session
     * @return <code>true</code> if put into session storage; otherwise <code>false</code>
     */
    public static boolean putIntoSessionStorage(SessionImpl session) {
        return putIntoSessionStorage(session, asyncPutToSessionStorage);
    }

    /**
     * Puts the given session into session storage if possible
     *
     * @param session The session
     * @param asyncPutToSessionStorage Whether to perform put asynchronously or not
     * @return <code>true</code> if put into session storage; otherwise <code>false</code>
     */
    public static boolean putIntoSessionStorage(SessionImpl session, boolean asyncPutToSessionStorage) {
        return putIntoSessionStorage(session, false, asyncPutToSessionStorage);
    }

    /**
     * Puts the given session into session storage if possible
     *
     * @param session The session
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly replacing put
     * @param asyncPutToSessionStorage Whether to perform put asynchronously or not
     * @return <code>true</code> if put into session storage; otherwise <code>false</code>
     */
    public static boolean putIntoSessionStorage(SessionImpl session, boolean addIfAbsent, boolean asyncPutToSessionStorage) {
        if (useSessionStorage(session)) {
            SessionStorageService sessionStorageService = Services.optService(SessionStorageService.class);
            if (sessionStorageService != null) {
                for (SessionSerializationInterceptor interceptor : interceptors) {
                    interceptor.serialize(session);
                }
                if (asyncPutToSessionStorage) {
                    // Enforced asynchronous put
                    storeSessionAsync(session, sessionStorageService, addIfAbsent);
                } else {
                    storeSessionSync(session, sessionStorageService, addIfAbsent);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * (Synchronously) Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly
     *            replacing put
     */
    public static void storeSessionSync(SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent) {
        storeSession(session, sessionStorageService, addIfAbsent, false);
    }

    /**
     * (Asynchronously) Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly
     *            replacing put
     */
    public static void storeSessionAsync(SessionImpl session, final SessionStorageService sessionStorageService, final boolean addIfAbsent) {
        storeSession(session, sessionStorageService, addIfAbsent, true);
    }

    /**
     * Stores specified session.
     *
     * @param session The session to store
     * @param sessionStorageService The storage service
     * @param addIfAbsent <code>true</code> to perform add-if-absent store operation; otherwise <code>false</code> to perform a possibly
     *            replacing put
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        int maxSessPerUser = config.getMaxSessionsPerUser();
        if (maxSessPerUser > 0) {
            int count = sessionData.getNumOfUserSessions(userId, contextId, true);
            if (count >= maxSessPerUser) {
                throw SessionExceptionCodes.MAX_SESSION_PER_USER_EXCEPTION.create(I(userId), I(contextId));
            }
            if (considerSessionStorage) {
                final SessionStorageService storageService = Services.optService(SessionStorageService.class);
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
            SessionData sessionData = SESSION_DATA_REF.get();
            List<SessionControl> userSessions = null == sessionData ? new LinkedList<SessionControl>() : sessionData.getUserSessions(userId, contextId);
            int cnt = 1; // We have at least one
            for (SessionControl sessionControl : userSessions) {
                if (client.equals(sessionControl.getSession().getClient()) && ++cnt > maxSessPerClient) {
                    throw SessionExceptionCodes.MAX_SESSION_PER_CLIENT_EXCEPTION.create(client, I(userId), I(contextId));
                }
            }
            if (considerSessionStorage) {
                final SessionStorageService storageService = Services.optService(SessionStorageService.class);
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        sessionData.checkAuthId(login, authId);
        /*
        SessionStorageService storageService = Services.optService(SessionStorageService.class);
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        SessionControl sessionControl = sessionData.clearSession(sessionid);
        if (null == sessionControl) {
            LOG.debug("Cannot find session for given identifier to remove session <{}>", sessionid);
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
        SessionData sessionData = SESSION_DATA_REF.get();
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
        int userId = currentSession.getUserId();
        int contextId = currentSession.getContextId();
        currentSession.setPassword(newPassword);
        final SessionStorageService sessionStorage = Services.optService(SessionStorageService.class);
        if (null != sessionStorage && useSessionStorage(currentSession)) {
            Task<Void> c = new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    Session wrappedSession = getObfuscator().wrap(currentSession, getRemoteParameterNames());
                    sessionStorage.changePassword(sessionid, wrappedSession.getPassword());
                    return null;
                }
            };
            submitAndIgnoreRejection(c);
        }
        /*
         * Invalidate all other user sessions known by local session containers
         */
        List<SessionControl> userSessionControls = sessionData.getUserSessions(currentSession.getUserId(), currentSession.getContextId());
        if (null != userSessionControls) {
            for (SessionControl userSessionControl : userSessionControls) {
                String otherSessionID = userSessionControl.getSession().getSessionID();
                if (null != otherSessionID && false == otherSessionID.equals(sessionid)) {
                    clearSession(otherSessionID);
                }
            }
        }
        /*
         * Invalidate all further user sessions known on remote cluster members
         */
        removeRemoteUserSessions(userId, contextId);
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
                    final SessionStorageService sessionStorageService = Services.optService(SessionStorageService.class);
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
                    final SessionStorageService sessionStorageService = Services.optService(SessionStorageService.class);
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
                    final SessionStorageService sessionStorageService = Services.optService(SessionStorageService.class);
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl sessionControl = sessionData.getSessionByRandomToken(randomToken);
        if (null == sessionControl) {
            final SessionStorageService storageService = Services.optService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return storageService.getSessionByRandomToken(randomToken, newIP);
                        }
                    };
                    Session unwrappedSession = getObfuscator().unwrap(getFrom(c, null), getRemoteParameterNames());
                    if (null != unwrappedSession) {
                        return unwrappedSession;
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            throw SessionExceptionCodes.NOT_INITIALIZED.create();
        }
        // find session matching to tokens
        TokenSessionControl tokenControl = TokenSessionContainer.getInstance().getSession(clientToken, serverToken);
        SessionImpl activatedSession = tokenControl.getSession();

        // Put this session into the normal session container
        SessionControl sessionControl = sessionData.addSession(activatedSession, noLimit);
        SessionImpl addedSession = sessionControl.getSession();
        putIntoSessionStorage(addedSession);
        // Post event for created session
        postSessionCreation(addedSession);

        return activatedSession;
    }

    /**
     * Gets the session associated with given session ID
     *
     * @param sessionId The session ID
     * @param considerSessionStorage <code>true</code> to consider session storage for possible distributed session; otherwise
     *            <code>false</code>
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
     * @param considerSessionStorage <code>true</code> to consider session storage for possible distributed session; otherwise
     *            <code>false</code>
     * @return The session associated with given session ID; otherwise <code>null</code> if expired or none found
     */
    protected static SessionControl getSession(String sessionId, final boolean considerLocalStorage, final boolean considerSessionStorage) {
        LOG.debug("getSession <{}>", sessionId);
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl sessionControl = considerLocalStorage ? sessionData.getSession(sessionId) : null;
        if (considerSessionStorage && null == sessionControl) {
            SessionStorageService storageService = Services.optService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    SessionImpl unwrappedSession = getSessionFrom(sessionId, timeout(), storageService);
                    if (null != unwrappedSession) {
                        SessionControl sc = sessionData.addSession(unwrappedSession, noLimit, true);
                        if (unwrappedSession == sc.getSession()) {
                            // we restored the session first
                            for (SessionSerializationInterceptor interceptor : interceptors) {
                                interceptor.deserialize(unwrappedSession);
                            }
                        }
                        SessionControl retval = null == sc ? new SessionControl(unwrappedSession) : sc;

                        // Post event for restored session
                        postSessionRestauration(retval.getSession());

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
            storeSession(sessionControl.getSession(), Services.optService(SessionStorageService.class), true);
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return false;
        }
        return null != sessionData.optShortTermSession(sessionId);
    }

    protected static List<String> getActiveSessionIDs() {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return Collections.emptyList();
        }
        return sessionData.getShortTermSessionIDs();
    }

    /**
     * Gets the session associated with given alternative identifier
     *
     * @param alternative identifier The alternative identifier
     * @return The session associated with given alternative identifier; otherwise <code>null</code> if expired or none found
     */
    protected static SessionControl getSessionByAlternativeId(final String altId, boolean lookupSessionStorage) {
        LOG.debug("getSessionByAlternativeId <{}>", altId);
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return null;
        }
        SessionControl sessionControl = sessionData.getSessionByAlternativeId(altId);
        if (null == sessionControl && lookupSessionStorage) {
            final SessionStorageService storageService = Services.optService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Task<Session> c = new AbstractTask<Session>() {

                        @Override
                        public Session call() throws Exception {
                            return storageService.getSessionByAlternativeId(altId);
                        }
                    };
                    SessionImpl unwrappedSession = getObfuscator().unwrap(getFrom(c, null), getRemoteParameterNames());
                    if (null != unwrappedSession) {
                        return new SessionControl(unwrappedSession);
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
        final SessionStorageService storageService = Services.optService(SessionStorageService.class);
        if (storageService != null) {
            try {
                Task<Session> c = new AbstractTask<Session>() {

                    @Override
                    public Session call() throws Exception {
                        return storageService.getCachedSession(sessionId);
                    }
                };
                SessionImpl unwrappedSession = getObfuscator().unwrap(getFrom(c, null), getRemoteParameterNames());
                if (null != unwrappedSession) {
                    return new SessionControl(unwrappedSession);
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return Collections.emptyList();
        }
        List<SessionControl> retval = sessionData.getShortTermSessions();
        if (retval == null) {
            final SessionStorageService storageService = Services.optService(SessionStorageService.class);
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
                    Obfuscator obfuscator = getObfuscator();
                    for (Session s : list) {
                        SessionImpl unwrappedSession = obfuscator.unwrap(s, getRemoteParameterNames());
                        if (null != unwrappedSession) {
                            result.add(new SessionControl(unwrappedSession));
                        }
                    }
                    return result;
                }
            }
        }
        return retval;
    }

    protected static void cleanUp() {
        LOG.debug("session cleanup");
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null == sessionData) {
            LOG.warn("\tSessionData instance is null.");
            return;
        }
        List<SessionControl> controls = sessionData.rotateShort();
        if (config.isAutoLogin()) {
            for (final SessionControl sessionControl : controls) {
                LOG.info("Session is moved to long life time container. All temporary session data will be cleaned up. ID: {}", sessionControl.getSession().getSessionID());
            }
            postSessionDataRemoval(controls);
        } else {
            for (final SessionControl sessionControl : controls) {
                LOG.info("Session timed out. ID: {}", sessionControl.getSession().getSessionID());
            }
            postContainerRemoval(controls, true);
        }
    }

    protected static void cleanUpLongTerm() {
        SessionData sessionData = SESSION_DATA_REF.get();
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
            SessionData sd = SESSION_DATA_REF.get();
            if (null != sd) {
                postContainerRemoval(sd.getShortTermSessions(), false);
                sd.clear();
                SESSION_DATA_REF.set(null);
            } else {
                LOG.warn("\tSessionData instance is null.");
            }
            Obfuscator o = obfuscatr;
            if (null != o) {
                obfuscatr = null;
                o.destroy();
            }
            sessionIdGenerator = null;
            config = null;
            noLimit = false;
        }
    }

    public static int getNumberOfActiveSessions() {
        SessionData sessionData = SESSION_DATA_REF.get();
        return null == sessionData ? 0 : sessionData.countSessions();
    }

    public static int[] getNumberOfLongTermSessions() {
        SessionData sessionData = SESSION_DATA_REF.get();
        return null == sessionData ? new int[0]: sessionData.getLongTermSessionsPerContainer();
    }

    public static int[] getNumberOfShortTermSessions() {
        SessionData sessionData = SESSION_DATA_REF.get();
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
        EventAdmin eventAdmin = optEventAdmin == null ? Services.optService(EventAdmin.class) : optEventAdmin;
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_STORED_SESSION, dic));
            LOG.debug("Posted event for added session");
        }
    }

    private static void postSessionCreation(Session session) {
        EventAdmin eventAdmin = Services.optService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_ADD_SESSION, dic));
            LOG.debug("Posted event for added session");
        }
    }

    private static void postSessionRestauration(Session session) {
        EventAdmin eventAdmin = Services.optService(EventAdmin.class);
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
            final SessionStorageService sessionStorageService = Services.optService(SessionStorageService.class);
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
        EventAdmin eventAdmin = Services.optService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_REMOVE_SESSION, dic));
            LOG.debug("Posted event for removed session");

            SessionData sessionData = SESSION_DATA_REF.get();
            if (null != sessionData) {
                int contextId = session.getContextId();
                int userId = session.getUserId();
                if (false == sessionData.isUserActive(userId, contextId, false)) {
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

            SessionData sessionData = SESSION_DATA_REF.get();
            if (null != sessionData) {
                if (false == sessionData.hasForContext(contextId)) {
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

    protected static void postContainerRemoval(List<SessionControl> sessionControls, boolean removeFromSessionStorage) {
        if (removeFromSessionStorage) {
            // Asynchronous remove from session storage
            final SessionStorageService sessionStorageService = Services.optService(SessionStorageService.class);
            if (sessionStorageService != null) {
                final List<SessionControl> tSessionControls = new ArrayList<SessionControl>(sessionControls);
                ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() {
                        try {
                            List<String> sessionsToRemove = new ArrayList<String>();
                            for (final SessionControl sessionControl : tSessionControls) {
                                SessionImpl session = sessionControl.getSession();
                                if (useSessionStorage(session)) {
                                    sessionsToRemove.add(session.getSessionID());
                                }
                            }
                            sessionStorageService.removeSessions(sessionsToRemove);
                        } catch (final RuntimeException e) {
                            LOG.error("", e);
                        } catch (OXException e) {
                            LOG.error("", e);
                        }
                        return null;
                    }
                });
            }
        }

        // Asynchronous post of event
        EventAdmin eventAdmin = Services.optService(EventAdmin.class);
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

            SessionData sessionData = SESSION_DATA_REF.get();
            if (null != sessionData) {
                for (UserKey userKey : users) {
                    if (false == sessionData.isUserActive(userKey.userId, userKey.contextId, false)) {
                        postLastSessionGone(userKey.userId, userKey.contextId, eventAdmin);
                    }
                }
            }
        }
    }

    private static void postSessionDataRemoval(List<SessionControl> controls) {
        // Post event
        EventAdmin eventAdmin = Services.optService(EventAdmin.class);
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

            SessionData sessionData = SESSION_DATA_REF.get();
            if (null != sessionData) {
                for (UserKey userKey : users) {
                    if (false == sessionData.isUserActive(userKey.userId, userKey.contextId, false)) {
                        postLastSessionGone(userKey.userId, userKey.contextId, eventAdmin);
                    }
                }
            }
        }
    }

    static void postSessionReactivation(Session session) {
        EventAdmin eventAdmin = Services.optService(EventAdmin.class);
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
        EventAdmin eventAdmin = Services.optService(EventAdmin.class);
        if (eventAdmin != null) {
            Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            eventAdmin.postEvent(new Event(SessiondEventConstants.TOPIC_TOUCH_SESSION, dic));
            LOG.debug("Posted event for touched session");
        }
    }

    public static void addThreadPoolService(ThreadPoolService service) {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null != sessionData) {
            sessionData.addThreadPoolService(service);
        }
    }

    public static void removeThreadPoolService() {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null != sessionData) {
            sessionData.removeThreadPoolService();
        }
    }

    public static void addTimerService(TimerService service) {
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null != sessionData) {
            sessionData.addTimerService(service);
        }
        long containerTimeout = config.getSessionContainerTimeout();
        shortSessionContainerRotator = service.scheduleWithFixedDelay(new ShortSessionContainerRotator(), containerTimeout, containerTimeout);
        if (config.isAutoLogin()) {
            long longContainerTimeout = config.getLongTermSessionContainerTimeout();
            longSessionContainerRotator = service.scheduleWithFixedDelay(new LongSessionContainerRotator(), longContainerTimeout, longContainerTimeout);
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
        SessionData sessionData = SESSION_DATA_REF.get();
        if (null != sessionData) {
            sessionData.removeTimerService();
        }
    }

    public static void addSessionSerializationInterceptor(SessionSerializationInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public static void removeSessionSerializationInterceptor(SessionSerializationInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    private static SessionControl sessionToSessionControl(Session session) {
        if (session == null) {
            return null;
        }
        return new SessionControl(session instanceof SessionImpl ? (SessionImpl) session : new SessionImpl(session));
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

        protected StoreSessionTask(SessionImpl session, SessionStorageService sessionStorageService, boolean addIfAbsent) {
            super();
            this.sessionStorageService = sessionStorageService;
            this.addIfAbsent = addIfAbsent;
            this.session = session;
        }

        @Override
        public Void call() {
            try {
                if (addIfAbsent) {
                    if (sessionStorageService.addSessionIfAbsent(getObfuscator().wrap(session, config.getRemoteParameterNames()))) {
                        LOG.info("Put session {} with auth Id {} into session storage.", session.getSessionID(), session.getAuthId());
                        postSessionStored(session);
                    }
                } else {
                    sessionStorageService.addSession(getObfuscator().wrap(session, config.getRemoteParameterNames()));
                    LOG.info("Put session {} with auth Id {} into session storage.", session.getSessionID(), session.getAuthId());
                    postSessionStored(session);
                }
            } catch (final Exception e) {
                LOG.warn("Failed to put session {} with Auth-Id {} into session storage (user={}, context={})", session.getSessionID(), session.getAuthId(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
            }
            return null;
        }
    }

    private static volatile Integer timeout;

    /**
     * Gets the default timeout for session-storage operations.
     *
     * @return The default timeout in milliseconds
     */
    public static int timeout() {
        Integer tmp = timeout;
        if (null == tmp) {
            synchronized (SessionHandler.class) {
                tmp = timeout;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    int defaultTimeout = 3000;
                    tmp = Integer.valueOf(null == service ? defaultTimeout : service.getIntProperty("com.openexchange.sessiond.sessionstorage.timeout", defaultTimeout));
                    timeout = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    /**
     * Gets the denoted session from session storage using given timeout.
     *
     * @param sessionId The session identifier
     * @param timeoutMillis The timeout in milliseconds; a value lower than or equal to zero is a synchronous call
     * @param storageService The session storage instance
     * @return The unwrapped session or <code>null</code> if timeout elapsed
     * @throws OXException If fetching session from session storage fails
     */
    private static SessionImpl getSessionFrom(String sessionId, long timeoutMillis, SessionStorageService storageService) throws OXException {
        try {
            return getObfuscator().unwrap(storageService.lookupSession(sessionId, timeoutMillis), getRemoteParameterNames());
        } catch (OXException e) {
            if (SessionStorageExceptionCodes.INTERRUPTED.equals(e)) {
                // Expected...
                return null;
            }
            throw e;
        }
    }

    /**
     * Submits given task to thread pool while ignoring a possible {@link RejectedExecutionException} in case thread pool refuses its
     * execution.
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
    public static boolean useSessionStorage(Session session) {
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
