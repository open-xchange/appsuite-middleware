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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.caching.objects.CachedSession;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionCounter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.cache.SessionCache;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.threadpool.ThreadPoolService;
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
        public int getNumberOfSessions(final int userId, final int contextId) {
            return sessionDataRef.get().getNumOfUserSessions(userId, contextId);
        }
    };

    private static SessionIdGenerator sessionIdGenerator;

    static SessiondConfigInterface config;

    protected static final AtomicReference<SessionData> sessionDataRef = new AtomicReference<SessionData>();

    private static boolean noLimit;

    private static final AtomicBoolean initialized = new AtomicBoolean();

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessionHandler.class));

    private static final boolean INFO = LOG.isInfoEnabled();

    private static final boolean DEBUG = LOG.isDebugEnabled();

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
    public static void init(final SessiondConfigInterface newConfig) {
        SessionHandler.config = newConfig;
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
            noLimit = (newConfig.getMaxSessions() == 0);
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
        Session[] retval = new Session[control.length];
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
        SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                retval2 = storageService.removeUserSessions(userId, contextId);
            } catch (OXException e) {
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
        SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                storageService.removeContextSessions(contextId);
            } catch (OXException e) {
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
        SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null && hasForContext == false) {
            try {
                hasForContext = storageService.hasForContext(contextId);
            } catch (OXException e) {
                LOG.error(e.getMessage(), e);
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
            SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Session[] sessions = storageService.getUserSessions(userId, contextId);
                    retval = new SessionControl[sessions.length];
                    for (int i = 0; i < sessions.length; i++) {
                        retval[i] = sessionToSessionControl(sessions[i]);
                    }
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return retval;
    }

    public static SessionControl getAnyActiveSessionForUser(final int userId, final int contextId, final boolean includeLongTerm) {
        SessionControl retval = sessionDataRef.get().getAnyActiveSessionForUser(userId, contextId, includeLongTerm);
        if (retval == null) {
            SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    retval = sessionToSessionControl(storageService.getAnyActiveSessionForUser(userId, contextId));
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return retval;
    }

    public static Session findFirstSessionForUser(final int userId, final int contextId, final SessionMatcher matcher) {
        Session retval = sessionDataRef.get().findFirstSessionForUser(userId, contextId, matcher);
        if (retval == null) {
            SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    retval = storageService.findFirstSessionForUser(userId, contextId);
                } catch (OXException e) {
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
    protected static SessionImpl addSession(final int userId, final String loginName, final String password, final int contextId, final String clientHost, final String login, final String authId, final String hash, final String client, final boolean isVolatile) throws OXException {
        checkMaxSessPerUser(userId, contextId);
        checkMaxSessPerClient(client, userId, contextId);
        checkAuthId(login, authId);
        final String sessionId = sessionIdGenerator.createSessionId(loginName, clientHost);
        final SessionImpl session = new SessionImpl(userId, loginName, password, contextId, sessionId, sessionIdGenerator.createSecretId(
            loginName,
            Long.toString(System.currentTimeMillis())), sessionIdGenerator.createRandomId(), clientHost, login, authId, hash, client);
        session.setVolatile(isVolatile);
        // Add session
        sessionDataRef.get().addSession(session, noLimit);
        final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
        if (sessionStorageService != null) {
            sessionStorageService.addSession(session);
        }
        // Post event for created session
        postSessionCreation(session);
        // Return session ID
        return session;
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
                int count = storageService.getUserSessions(userId, contextId).length;
                if (maxSessPerUser > 0 && count >= maxSessPerUser) {
                    throw SessionExceptionCodes.MAX_SESSION_PER_USER_EXCEPTION.create(I(userId), I(contextId));
                }
            } catch (OXException e) {
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
        SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            if (maxSessPerClient > 0) {
                try {
                    Session[] userSessions = storageService.getUserSessions(userId, contextId);
                    int cnt = 0;
                    for (final Session session : userSessions) {
                        if (client.equals(session.getClient()) && ++cnt > maxSessPerClient) {
                            throw SessionExceptionCodes.MAX_SESSION_PER_CLIENT_EXCEPTION.create(client, I(userId), I(contextId));
                        }
                    }
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private static void checkAuthId(final String login, final String authId) throws OXException {
        sessionDataRef.get().checkAuthId(login, authId);
        SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                storageService.checkAuthId(login, authId);
            } catch (OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
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
        try {
            final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                sessionStorageService.removeSession(sessionControl.getSession().getSessionID());
            }
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
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
            sessionStorageService.changePassword(sessionid, newPassword);
        }
    }

    protected static Session getSessionByRandomToken(final String randomToken, final String newIP) {
        SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                return storageService.getSessionByRandomToken(randomToken, newIP);
            } catch (OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        final SessionControl sessionControl = sessionDataRef.get().getSessionByRandomToken(randomToken);
        if (null == sessionControl) {
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
            SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    Session s = storageService.lookupSession(sessionId);
                    sessionData.addSession(
                        new SessionImpl(
                            s.getUserId(),
                            s.getLoginName(),
                            s.getPassword(),
                            s.getContextId(),
                            s.getSessionID(),
                            s.getSecret(),
                            s.getRandomToken(),
                            s.getLocalIp(),
                            s.getLogin(),
                            s.getAuthId(),
                            s.getHash(),
                            s.getClient()),
                        noLimit);
                    return sessionToSessionControl(s);
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
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
            SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                try {
                    return sessionToSessionControl(storageService.getSessionByAlternativeId(altId));
                } catch (OXException e) {
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
                    return sessionDataRef.get().addSession(new SessionImpl(cachedSession), noLimit);
                }
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
        if (storageService != null) {
            try {
                return sessionToSessionControl(storageService.getCachedSession(sessionId));
            } catch (OXException e) {
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
        List<SessionControl> retval = sessionDataRef.get().getShortTermSessions();
        if (retval == null) {
            SessionStorageService storageService = getServiceRegistry().getService(SessionStorageService.class);
            if (storageService != null) {
                List<Session> list = storageService.getSessions();
                List<SessionControl> result = new ArrayList<SessionControl>();
                for (Session s : list) {
                    result.add(sessionToSessionControl(s));
                }
                return result;
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
            try {
                final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                if (sessionStorageService != null) {
                    sessionStorageService.removeSession(sessionControl.getSession().getSessionID());
                }
            } catch (OXException e) {
                LOG.error(e.getMessage(), e);
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
            try {
                final SessionStorageService sessionStorageService = getServiceRegistry().getService(SessionStorageService.class);
                if (sessionStorageService != null) {
                    sessionStorageService.removeSession(control.getSession().getSessionID());
                }
            } catch (OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        postContainerRemoval(controls);
    }

    public static void close() {
        if (initialized.compareAndSet(true, false)) {
            final SessionData sd = sessionDataRef.get();
            if (null != sd) {
                postContainerRemoval(sd.getShortTermSessions());
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
        }
    }

    private static void postContainerRemoval(final List<SessionControl> sessionControls) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            final Map<String, Session> eventMap = new HashMap<String, Session>();
            for (final SessionControl sessionControl : sessionControls) {
                final Session session = sessionControl.getSession();
                eventMap.put(session.getSessionID(), session);
            }
            dic.put(SessiondEventConstants.PROP_CONTAINER, eventMap);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_CONTAINER, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for removed session container");
            }
        }
    }

    private static void postSessionDataRemoval(final List<SessionControl> controls) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<String, Object> dic = new Hashtable<String, Object>(2);
            final Map<String, Session> eventMap = new HashMap<String, Session>();
            for (final SessionControl sessionControl : controls) {
                final Session session = sessionControl.getSession();
                eventMap.put(session.getSessionID(), session);
            }
            dic.put(SessiondEventConstants.PROP_CONTAINER, eventMap);
            dic.put(SessiondEventConstants.PROP_COUNTER, SESSION_COUNTER);
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_DATA, dic);
            eventAdmin.postEvent(event);
            if (DEBUG) {
                LOG.debug("Posted event for removing temporary session data.");
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

    private static SessionControl sessionToSessionControl(Session session) {
        if (session != null) {
            SessionImpl impl = new SessionImpl(
                session.getUserId(),
                session.getLoginName(),
                session.getPassword(),
                session.getContextId(),
                session.getSessionID(),
                session.getSecret(),
                session.getRandomToken(),
                session.getLocalIp(),
                session.getLogin(),
                session.getAuthId(),
                session.getHash(),
                session.getClient());
            SessionControl control = new SessionControl(impl);
            return control;
        }
        return null;
    }

    private static Session[] merge(Session[] array1, Session[] array2) {
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
}
