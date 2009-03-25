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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.objects.CachedSession;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.cache.SessionCache;
import com.openexchange.sessiond.event.SessiondEventConstants;
import com.openexchange.sessiond.exception.SessiondException;
import com.openexchange.sessiond.exception.SessiondException.Code;
import com.openexchange.sessiond.services.SessiondServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.Timer;

/**
 * {@link SessionHandler} - Provides access to sessions
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionHandler {

    private static int numberOfSessionContainers = 4;

    private static LinkedList<SessionContainer> sessionList = new LinkedList<SessionContainer>();

    private static LinkedList<Map<String, String>> userList = new LinkedList<Map<String, String>>();

    private static LinkedList<Map<String, String>> randomList = new LinkedList<Map<String, String>>();

    private static SessionIdGenerator sessionIdGenerator;

    private static SessiondConfigInterface config;

    private static boolean noLimit;

    private static final AtomicBoolean initialized = new AtomicBoolean();

    private static final Log LOG = LogFactory.getLog(SessionHandler.class);

    private static ScheduledTimerTask sessiondTimer;

    /**
     * Initializes a new {@link SessionHandler session handler}
     */
    private SessionHandler() {
        super();
    }

    /**
     * Initializes the {@link SessionHandler session handler}
     * 
     * @param config The appropriate configuration
     */
    public static void init(final SessiondConfigInterface config) {
        SessionHandler.config = config;

        if (initialized.compareAndSet(false, true)) {
            try {
                sessionIdGenerator = SessionIdGenerator.getInstance();
            } catch (final SessiondException exc) {
                LOG.error("create instance of SessionIdGenerator", exc);
            }

            numberOfSessionContainers = config.getNumberOfSessionContainers();

            for (int a = 0; a < numberOfSessionContainers; a++) {
                prependContainer();
            }

            noLimit = (config.getMaxSessions() == 0);

            final Timer timer = SessiondServiceRegistry.getServiceRegistry().getService(Timer.class);
            if (timer != null) {
                sessiondTimer = timer.scheduleWithFixedDelay(
                    new SessiondTimer(),
                    config.getSessionContainerTimeout(),
                    config.getSessionContainerTimeout());
            }
        }
    }

    private static void prependContainer() {
        sessionList.add(0, new SessionContainer(config.getMaxSessions()));
        userList.add(0, new ConcurrentHashMap<String, String>(config.getMaxSessions()));
        randomList.add(0, new ConcurrentHashMap<String, String>(config.getMaxSessions()));
    }

    private static void removeContainer() {
        final SessionContainer sessions = sessionList.removeLast();
        userList.removeLast();
        randomList.removeLast();
        postContainerRemoval(sessions);
    }

    /**
     * Checks if given user in specified context has an active session kept in session container(s)
     * 
     * @param userId The user ID
     * @param context The user's context
     * @return <code>true</code> if given user in specified context has an active session; otherwise <code>false</code>
     */
    protected static boolean isUserActive(final int userId, final Context context) {
        final int size = sessionList.size();
        boolean active = false;
        for (int i = 0; i < size && !active; i++) {
            active |= sessionList.get(i).containsUser(userId, context.getContextId());
        }
        return active;
    }

    /**
     * Removes all sessions associated with given user in specified context
     * 
     * @param userId The user ID
     * @param contextId The context ID
     * @param propagate <code>true</code> for remote removal; otherwise <code>false</code>
     * @return The wrapper objects for removed sessions
     */
    public static SessionControl[] removeUserSessions(final int userId, final int contextId, final boolean propagate) {
        final int size = sessionList.size();
        final List<SessionControl> retval = new ArrayList<SessionControl>(config.getMaxSessionsPerUser());
        for (int i = 0; i < size; i++) {
            retval.addAll(Arrays.asList(sessionList.get(i).removeSessionsByUser(userId, contextId)));
        }
        if (propagate) {
            for (final SessionControl sessionControl : retval) {
                try {
                    SessionCache.getInstance().putCachedSessionForRemoteRemoval(
                        ((SessionImpl) sessionControl.getSession()).createCachedSession());
                } catch (final CacheException e) {
                    LOG.error("Remote removal failed for session " + sessionControl.getSession().getSecret(), e);
                } catch (final ServiceException e) {
                    LOG.error("Remote removal failed for session " + sessionControl.getSession().getSecret(), e);
                }
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuilder(64).append(propagate ? "Remote" : "Local").append(" removal of user sessions: User=").append(userId).append(
                ", Context=").append(contextId).toString());
        }
        return retval.toArray(new SessionControl[retval.size()]);
    }

    /**
     * Adds a new session containing given attributes to session container(s)
     * 
     * @param userId The user ID
     * @param loginName The user's login name
     * @param password The user's password
     * @param context The context
     * @param clientHost The client host name or IP address
     * @param login The full user's login; e.g. <i>test@foo.bar</i>
     * @return The session ID associated with newly created session
     * @throws SessiondException If creating a new session fails
     */
    protected static String addSession(final int userId, final String loginName, final String password, final Context context, final String clientHost, final String login) throws SessiondException {
        final int maxSessPerUser = config.getMaxSessionsPerUser();
        if (maxSessPerUser > 0) {
            final int size = sessionList.size();
            int count = 0;
            for (int i = 0; i < size && count <= maxSessPerUser; i++) {
                count += sessionList.get(i).numOfUserSessions(userId, context.getContextId());
            }
            if (count >= maxSessPerUser) {
                throw new SessiondException(Code.MAX_SESSION_PER_USER_EXCEPTION, null, I(userId), I(context.getContextId()));
            }
        }

        final String sessionId = sessionIdGenerator.createSessionId(loginName, clientHost);
        final String secret = sessionIdGenerator.createSecretId(loginName, String.valueOf(System.currentTimeMillis()));
        final String randomToken = sessionIdGenerator.createRandomId();

        final Session session = new SessionImpl(
            userId,
            loginName,
            password,
            context.getContextId(),
            sessionId,
            secret,
            randomToken,
            clientHost,
            login);

        LOG.info("Session created. ID: " + sessionId + ", Context: " + context.getContextId() + ", User: " + userId);

        addSessionInternal(session);

        return sessionId;
    }

    private static SessionControl addSessionInternal(final Session session) throws SessiondException {
        SessionContainer sessionContainer = null;
        Map<String, String> userMap = null;
        Map<String, String> randomMap = null;

        for (int a = 0; a < numberOfSessionContainers; a++) {
            sessionContainer = sessionList.get(a);
            userMap = userList.get(a);
            randomMap = randomList.get(a);

            if (!noLimit && sessionContainer.size() > config.getMaxSessions()) {
                throw new SessiondException(Code.MAX_SESSION_EXCEPTION);
            }
        }

        final SessionControl sessionControlObject = sessionContainer.put(session, config.getLifeTime());

        randomMap.put(session.getRandomToken(), session.getSessionID());
        userMap.put(session.getLoginName(), session.getSessionID());
        return sessionControlObject;
    }

    /**
     * Refreshes the session's last-accessed time stamp
     * 
     * @param sessionid The session ID denoting the session
     * @return <code>true</code> if a refreshing last-accessed time stamp was successful; otherwise <code>false</code>
     */
    protected static boolean refreshSession(final String sessionid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("refreshSession <").append(sessionid).append('>').toString());
        }

        for (int a = 0; a < numberOfSessionContainers; a++) {
            final SessionContainer sessionContainer = sessionList.get(a);

            if (sessionContainer.containsSessionId(sessionid)) {
                final SessionControl sessionControl = sessionContainer.getSessionById(sessionid);
                if (isValid(sessionControl)) {
                    sessionControl.updateTimestamp();

                    if (a > 0) {
                        /*
                         * Put into first container and remove from latter one
                         */
                        sessionList.get(0).putSessionControl(sessionControl);
                        sessionContainer.removeSessionById(sessionid);
                    }

                    return true;
                }
                LOG.info("Session timed out. ID: " + sessionid);
                sessionContainer.removeSessionById(sessionid);

                return false;
            }
        }
        return false;
    }

    /**
     * Clears the session denoted by given session ID from session container(s)
     * 
     * @param sessionid The session ID
     * @return <code>true</code> if a session could be removed; otherwise <code>false</code>
     */
    protected static boolean clearSession(final String sessionid) {
        for (int a = 0; a < numberOfSessionContainers; a++) {
            final SessionContainer sessionContainer = sessionList.get(a);

            if (sessionContainer.containsSessionId(sessionid)) {
                final SessionControl sessionControl = sessionContainer.removeSessionById(sessionid);
                postSessionRemoval(sessionControl.getSession());
                LOG.info("Session closed. ID: " + sessionid);
                return true;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Cannot find session id to remove session <" + sessionid + '>');
        }

        return false;
    }

    /**
     * Changes the password stored in session denoted by given session ID
     * 
     * @param sessionid The session ID
     * @param newPassword The new password
     * @throws SessiondException If changing the password fails
     */
    protected static void changeSessionPassword(final String sessionid, final String newPassword) throws SessiondException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("changeSessionPassword <").append(sessionid).append('>').toString());
        }

        // TODO: Check permission via security service

        for (int a = 0; a < numberOfSessionContainers; a++) {
            final SessionContainer sessionContainer = sessionList.get(a);

            if (sessionContainer.containsSessionId(sessionid)) {
                final SessionControl sessionControl = sessionContainer.getSessionById(sessionid);
                if (isValid(sessionControl)) {
                    sessionControl.updateTimestamp();
                    /*
                     * Set new password
                     */
                    ((SessionImpl) sessionControl.getSession()).setPassword(newPassword);

                    if (a > 0) {
                        /*
                         * Put into first container and remove from latter one
                         */
                        sessionList.get(0).putSessionControl(sessionControl);
                        sessionContainer.removeSessionById(sessionid);
                    }
                    return;
                }
                LOG.info("Session timed out. ID: " + sessionid);
                sessionContainer.removeSessionById(sessionid);
                throw new SessiondException(SessiondException.Code.PASSWORD_UPDATE_FAILED);
            }
        }
        throw new SessiondException(SessiondException.Code.PASSWORD_UPDATE_FAILED);
    }

    protected static Session getSessionByRandomToken(final String randomToken, final String localIp) {
        Map<String, String> random = null;

        for (int a = 0; a < numberOfSessionContainers; a++) {
            random = randomList.get(a);

            if (random.containsKey(randomToken)) {
                final String sessionId = random.get(randomToken);
                final SessionControl sessionControlObject = getSession(sessionId);

                final long now = System.currentTimeMillis();

                if (sessionControlObject.getCreationTime() + config.getRandomTokenTimeout() >= now) {
                    final Session session = sessionControlObject.getSession();
                    session.removeRandomToken();
                    random.remove(randomToken);
                    /*
                     * Set local IP
                     */
                    ((SessionImpl) session).setLocalIp(localIp);
                    return session;
                }
            }
        }
        return null;
    }

    /**
     * Gets the session associated with given session ID
     * 
     * @param sessionid The session ID
     * @return The session associated with given session ID; otherwise <code>null</code> if expired or none found
     */
    protected static SessionControl getSession(final String sessionid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("getSession <").append(sessionid).append('>').toString());
        }

        for (int a = 0; a < numberOfSessionContainers; a++) {
            final SessionContainer sessionContainer = sessionList.get(a);

            if (sessionContainer.containsSessionId(sessionid)) {
                final SessionControl sessionControl = sessionContainer.getSessionById(sessionid);

                if (sessionControl != null && isValid(sessionControl)) {
                    sessionControl.updateTimestamp();

                    if (a > 0) {
                        /*
                         * Put into first container and remove from latter one
                         */
                        sessionList.get(0).putSessionControl(sessionControl);
                        sessionContainer.removeSessionById(sessionid);
                    }

                    /*
                     * Look-up cache if current session wrapped by session-control is marked for removal
                     */
                    try {
                        final Session s = sessionControl.getSession();
                        final CachedSession cachedSession = SessionCache.getInstance().getCachedSessionByUser(
                            s.getUserId(),
                            s.getContextId());
                        if (null != cachedSession) {
                            if (cachedSession.isMarkedAsRemoved()) {
                                SessionCache.getInstance().removeCachedSession(cachedSession.getSecret());
                                removeUserSessions(cachedSession.getUserId(), cachedSession.getContextId(), false);
                                return null;
                            }
                        }
                    } catch (final CacheException e) {
                        LOG.error("Unable to look-up session cache", e);
                    } catch (final ServiceException e) {
                        LOG.error("Unable to look-up session cache", e);
                    }

                    return sessionControl;
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Gets (and removes) the session bound to given secret cookie identifier in cache.
     * <p>
     * Session is going to be added to local session containers on a cache hit.
     * 
     * @param secret The secret cookie identifier
     * @param localIP The host's local IP
     * @return A wrapping instance of {@link SessionControl} or <code>null</code>
     */
    public static SessionControl getCachedSession(final String secret, final String localIP) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("getCachedSession <").append(secret).append('>').toString());
        }
        try {
            final CachedSession cachedSession = SessionCache.getInstance().removeCachedSession(secret);
            if (null != cachedSession) {
                if (cachedSession.isMarkedAsRemoved()) {
                    removeUserSessions(cachedSession.getUserId(), cachedSession.getContextId(), false);
                } else {
                    /*
                     * A cache hit! Add to local session containers
                     */
                    return addSessionInternal(new SessionImpl(cachedSession, localIP));
                }
            }
        } catch (final CacheException e) {
            LOG.error(e.getMessage(), e);
        } catch (final SessiondException e) {
            LOG.error(e.getMessage(), e);
        } catch (final ServiceException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets all available instances of {@link SessionControl}
     * 
     * @return All available instances of {@link SessionControl}
     */
    public static List<SessionControl> getSessions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSessions");
        }
        final List<SessionControl> retval = new ArrayList<SessionControl>();
        for (int a = 0; a < numberOfSessionContainers; a++) {
            retval.addAll(sessionList.get(a).getSessionControls());
        }
        return retval;
    }

    protected static void cleanUp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("session cleanup");
        }

        if (LOG.isInfoEnabled()) {
            final SessionContainer sessionContainer = sessionList.getLast();
            for (final Iterator<String> iterator = sessionContainer.getSessionIds(); iterator.hasNext();) {
                LOG.info("Session timed out. ID: " + iterator.next());
            }
        }
        prependContainer();
        removeContainer();
    }

    /**
     * Checks if a session is still valid. Therefore the maximum lifetime of a session, a disabled context and a disabled user are checked.
     * 
     * @param session Session to check.
     * @return <code>true</code> if the session is still valid.
     */
    protected static boolean isValid(final SessionControl session) {
        return ((session.getTimestamp() + session.getLifetime()) >= System.currentTimeMillis());
    }

    public static void close() {
        if (initialized.compareAndSet(true, false)) {
            numberOfSessionContainers = 4;
            postContainersRemoval();
            sessionList = new LinkedList<SessionContainer>();
            userList = new LinkedList<Map<String, String>>();
            randomList = new LinkedList<Map<String, String>>();
            sessionIdGenerator = null;
            config = null;
            noLimit = false;
            if (sessiondTimer != null) {
                sessiondTimer.cancel(true);
                sessiondTimer = null;
            }
        }
    }

    public static int getNumberOfActiveSessions() {
        int numSessions = 0;
        for (final SessionContainer container : sessionList) {
            numSessions += container.size();
        }
        return numSessions;
    }

    private static void postSessionRemoval(final Session session) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Hashtable<Object, Object> dic = new Hashtable<Object, Object>();
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_SESSION, dic);
            eventAdmin.postEvent(event);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Posted event for removed session");
            }
        }
    }

    private static void postContainerRemoval(final SessionContainer sessionContainer) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Hashtable<Object, Object> dic = new Hashtable<Object, Object>();
            dic.put(SessiondEventConstants.PROP_CONTAINER, sessionContainer.convert());
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_CONTAINER, dic);
            eventAdmin.postEvent(event);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Posted event for removed session container");
            }
        }
    }

    private static void postContainersRemoval() {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            for (final SessionContainer sessionContainer : sessionList) {
                final Hashtable<Object, Object> dic = new Hashtable<Object, Object>();
                dic.put(SessiondEventConstants.PROP_CONTAINER, sessionContainer.convert());
                final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_CONTAINER, dic);
                eventAdmin.postEvent(event);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Posted events for removed session containers");
        }
    }
}
