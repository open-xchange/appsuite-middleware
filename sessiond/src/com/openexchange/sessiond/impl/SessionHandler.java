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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
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
import com.openexchange.timer.TimerService;

/**
 * {@link SessionHandler} - Provides access to sessions
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionHandler {

    private static SessionIdGenerator sessionIdGenerator;

    private static SessiondConfigInterface config;

    private static SessionData sessionData;

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
        sessionData = new SessionData(config.getNumberOfSessionContainers(), config.getMaxSessions());
        if (initialized.compareAndSet(false, true)) {
            try {
                sessionIdGenerator = SessionIdGenerator.getInstance();
            } catch (final SessiondException exc) {
                LOG.error("create instance of SessionIdGenerator", exc);
            }

            noLimit = (config.getMaxSessions() == 0);

            final TimerService timer = SessiondServiceRegistry.getServiceRegistry().getService(TimerService.class);
            if (timer != null) {
                sessiondTimer = timer.scheduleWithFixedDelay(
                    new SessiondTimer(),
                    config.getSessionContainerTimeout(),
                    config.getSessionContainerTimeout());
            }
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
    public static SessionControl[] removeUserSessions(final int userId, final int contextId, final boolean propagate) {
        final SessionControl[] retval = sessionData.removeUserSessions(userId, contextId);
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
        return retval;
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
            final int count = sessionData.getNumOfUserSessions(userId, context);
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

        sessionData.addSession(session, config.getLifeTime(), noLimit);

        return sessionId;
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
        return sessionData.getSession(sessionid) != null;
    }

    /**
     * Clears the session denoted by given session ID from session container(s)
     * 
     * @param sessionid The session ID
     * @return <code>true</code> if a session could be removed; otherwise <code>false</code>
     */
    protected static boolean clearSession(final String sessionid) {
        final SessionControl sessionControl = sessionData.clearSession(sessionid);
        if (null == sessionControl) {
            LOG.debug("Cannot find session id to remove session <" + sessionid + '>');
            return false;
        } else {
            postSessionRemoval(sessionControl.getSession());
            LOG.info("Session closed. ID: " + sessionid);
            return true;
        }
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
        final SessionControl sessionControl = sessionData.getSession(sessionid);
        if (null == sessionControl) {
            throw new SessiondException(SessiondException.Code.PASSWORD_UPDATE_FAILED);
        }
        // TODO: Check permission via security service
        ((SessionImpl) sessionControl.getSession()).setPassword(newPassword);
    }

    protected static Session getSessionByRandomToken(final String randomToken, final String localIp) {
        final SessionControl sessionControl = sessionData.getSessionByRandomToken(randomToken, config.getRandomTokenTimeout(), localIp);
        if (null == sessionControl) {
            return null;
        }
        return sessionControl.getSession();
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
        final SessionControl sessionControl = sessionData.getSession(sessionid);
        if (null == sessionControl) {
            return null;
        }
        /*
         * Look-up cache if current session wrapped by session-control is marked for removal
         */
        try {
            final SessionCache cache = SessionCache.getInstance();
            final Session s = sessionControl.getSession();
            final CachedSession cachedSession = cache.getCachedSessionByUser(s.getUserId(), s.getContextId());
            if (null != cachedSession) {
                if (cachedSession.isMarkedAsRemoved()) {
                    cache.removeCachedSession(cachedSession.getSecret());
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
                    LOG.info("Cached session found. ID: " + cachedSession.getSessionId());
                    return sessionData.addSession(new SessionImpl(cachedSession, localIP), config.getLifeTime(), noLimit);
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
        return sessionData.getSessions();
    }

    protected static void cleanUp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("session cleanup");
        }
        final List<SessionControl> sessionControls = sessionData.rotate();
        if (LOG.isInfoEnabled()) {
            for (final SessionControl sessionControl : sessionControls) {
                LOG.info("Session timed out. ID: " + sessionControl.getSession().getSessionID());
            }
        }
        postContainerRemoval(sessionControls);
    }

    public static void close() {
        if (initialized.compareAndSet(true, false)) {
            postContainerRemoval(sessionData.getSessions());
            sessionData.clear();
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
        return sessionData.countSessions();
    }

    private static void postSessionRemoval(final Session session) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<Object, Object> dic = new Hashtable<Object, Object>();
            dic.put(SessiondEventConstants.PROP_SESSION, session);
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_SESSION, dic);
            eventAdmin.postEvent(event);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Posted event for removed session");
            }
        }
    }

    private static void postContainerRemoval(final List<SessionControl> sessionControls) {
        final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
        if (eventAdmin != null) {
            final Dictionary<Object, Object> dic = new Hashtable<Object, Object>();
            final Map<String, Session> eventMap = new HashMap<String, Session>();
            for (final SessionControl sessionControl : sessionControls) {
                final Session session = sessionControl.getSession();
                eventMap.put(session.getSessionID(), session);
            }
            dic.put(SessiondEventConstants.PROP_CONTAINER, eventMap);
            final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_CONTAINER, dic);
            eventAdmin.postEvent(event);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Posted event for removed session container");
            }
        }
    }
}
