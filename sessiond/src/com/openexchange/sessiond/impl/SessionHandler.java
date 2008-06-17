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

import static com.openexchange.sessiond.services.SessiondServiceRegistry.getServiceRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.openexchange.caching.CacheException;
import com.openexchange.caching.objects.CachedSession;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServerTimer;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.cache.SessionCache;
import com.openexchange.sessiond.event.SessiondEventConstants;
import com.openexchange.sessiond.exception.SessiondException;
import com.openexchange.sessiond.exception.SessiondException.Code;

/**
 * {@link SessionHandler}
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionHandler {

	private static int numberOfSessionContainers = 4;

	private static LinkedList<Map<String, SessionControl>> sessionList = new LinkedList<Map<String, SessionControl>>();

	private static LinkedList<Map<String, String>> userList = new LinkedList<Map<String, String>>();

	private static LinkedList<Map<String, String>> randomList = new LinkedList<Map<String, String>>();

	private static SessionIdGenerator sessionIdGenerator;

	private static SessiondConfigInterface config;

	private static boolean noLimit = false;

	private static boolean isInit = false;

	private static final Log LOG = LogFactory.getLog(SessionHandler.class);

	private static final AtomicInteger numberOfActiveSessions = new AtomicInteger();

	/**
	 * Initializes a new {@link SessionHandler session-handler}
	 */
	private SessionHandler() {
		super();
	}

	/**
	 * Initializes the {@link SessionHandler session handler}
	 * 
	 * @param config
	 *            The appropriate configuration
	 */
	public static void init(final SessiondConfigInterface config) {
		SessionHandler.config = config;

		if (!isInit) {
			try {
				sessionIdGenerator = SessionIdGenerator.getInstance();
			} catch (final SessiondException exc) {
				LOG.error("create instance of SessionIdGenerator", exc);
			}

			numberOfSessionContainers = config.getNumberOfSessionContainers();

			for (int a = 0; a < numberOfSessionContainers; a++) {
				prependContainer();
			}
		}

		noLimit = (config.getMaxSessions() == 0);

		final SessiondTimer sessiondTimer = new SessiondTimer();
		final Timer t = ServerTimer.getTimer();
		t.schedule(sessiondTimer, config.getSessionContainerTimeout(), config.getSessionContainerTimeout());
	}

	private static void prependContainer() {
		sessionList.add(0, new Hashtable<String, SessionControl>(config.getMaxSessions()));
		userList.add(0, new Hashtable<String, String>(config.getMaxSessions()));
		randomList.add(0, new Hashtable<String, String>(config.getMaxSessions()));
	}

	private static void removeContainer() {
		final Map<String, SessionControl> sessions = sessionList.removeLast();
		userList.removeLast();
		randomList.removeLast();
		postContainerRemoval(sessions);
	}

	protected static String addSession(final int userId, final String loginName, final String password,
			final Context context, final String clientHost) throws SessiondException {
		final String sessionId = sessionIdGenerator.createSessionId(loginName, clientHost);
		final String secret = sessionIdGenerator.createSecretId(loginName, String.valueOf(System.currentTimeMillis()));
		final String randomToken = sessionIdGenerator.createRandomId();

		final Session session = new SessionImpl(userId, loginName, password, context.getContextId(), sessionId, secret,
				randomToken, clientHost);

		if (LOG.isDebugEnabled()) {
			LOG.debug("addSession <" + sessionId + '>');
		}

		addSessionInternal(session);

		return sessionId;
	}

	private static SessionControl addSessionInternal(final Session session) throws SessiondException {
		final String sessionId = session.getSessionID();
		Map<String, SessionControl> sessions = null;
		Map<String, String> userMap = null;
		Map<String, String> randomMap = null;

		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);
			userMap = userList.get(a);
			randomMap = randomList.get(a);

			if (!noLimit && sessions.size() > config.getMaxSessions()) {
				throw new SessiondException(Code.MAX_SESSION_EXCEPTION);
			}
		}

		final SessionControl sessionControlObject = new SessionControl(session, config.getLifeTime());
		if (sessions.containsKey(sessionId) && LOG.isDebugEnabled()) {
			LOG.debug("session REBORN sessionid=" + sessionId);
		}

		sessions.put(sessionId, sessionControlObject);
		randomMap.put(session.getRandomToken(), sessionId);
		userMap.put(session.getLoginName(), sessionId);
		numberOfActiveSessions.incrementAndGet();
		return sessionControlObject;
	}

	protected static boolean refreshSession(final String sessionid) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("refreshSession <" + sessionid + '>');
		}

		Map<String, SessionControl> sessions = null;

		// final Date timestamp = new Date();

		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);

			if (sessions.containsKey(sessionid)) {
				final SessionControl sessionIdInterface = sessions.get(sessionid);
				if (isValid(sessionIdInterface)) {
					sessionIdInterface.updateTimestamp();

					sessionList.get(0).put(sessionid, sessionIdInterface);
					if (a > 0) {
						sessions.remove(sessionid);
						// the session is only moved to the first container so a
						// decrement is not necessary
					}

					return true;
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("session TIMEOUT sessionid=" + sessionid);
				}
				sessions.remove(sessionid);
				numberOfActiveSessions.decrementAndGet();

				return false;
			}
		}
		return false;
	}

	protected static boolean clearSession(final String sessionid) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("clearSession <" + sessionid + '>');
		}

		Map<String, SessionControl> sessions = null;

		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);

			if (sessions.containsKey(sessionid)) {
				final SessionControl session = sessions.remove(sessionid);
				numberOfActiveSessions.decrementAndGet();
				postSessionRemoval(session.getSession());
				return true;
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Cannot find session id to remove session <" + sessionid + '>');
		}

		return false;
	}

	protected static void changeSessionPassword(final String sessionid, final String newPassword)
			throws SessiondException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("changeSessionPassword <" + sessionid + '>');
		}

		// TODO: Check permission via security service

		for (int a = 0; a < numberOfSessionContainers; a++) {
			final Map<String, SessionControl> sessionContainer = sessionList.get(a);

			if (sessionContainer.containsKey(sessionid)) {
				final SessionControl sessionControl = sessionContainer.get(sessionid);
				if (isValid(sessionControl)) {
					sessionControl.updateTimestamp();
					/*
					 * Set new password
					 */
					if (!SessionImpl.class.isInstance(sessionControl.getSession())) {
						throw new SessiondException(SessiondException.Code.PASSWORD_UPDATE_FAILED);
					}
					((SessionImpl) sessionControl.getSession()).setPassword(newPassword);

					/*
					 * Move to first container
					 */
					sessionList.get(0).put(sessionid, sessionControl);
					if (a > 0) {
						sessionContainer.remove(sessionid);
						// the session is only moved to the first container so a
						// decrement is not necessary
					}
					return;
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("session TIMEOUT sessionid=" + sessionid);
				}
				sessionContainer.remove(sessionid);
				numberOfActiveSessions.decrementAndGet();

				throw new SessiondException(SessiondException.Code.SESSIOND_EXCEPTION);
			}
		}
		throw new SessiondException(SessiondException.Code.SESSIOND_EXCEPTION);

	}

	protected static Session getSessionByRandomToken(final String randomToken) {
		Map<String, String> random = null;

		for (int a = 0; a < numberOfSessionContainers; a++) {
			random = randomList.get(a);

			if (random.containsKey(randomToken)) {
				final String sessionId = random.get(randomToken);
				final SessionControl sessionControlObject = getSession(sessionId, true);

				final long now = System.currentTimeMillis();

				if (sessionControlObject.getCreationTime() + config.getRandomTokenTimeout() >= now) {
					final Session session = sessionControlObject.getSession();
					session.removeRandomToken();
					random.remove(randomToken);
					return session;
				}
			}
		}
		return null;
	}

	protected static SessionControl getSession(final String sessionid, final boolean refresh) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getSession <" + sessionid + '>');
		}

		// Map<String, SessionControlObject> sessions = null;

		// final Date timestamp = new Date();

		for (int a = 0; a < numberOfSessionContainers; a++) {
			final Map<String, SessionControl> sessions = sessionList.get(a);

			if (sessions.containsKey(sessionid)) {
				final SessionControl sessionControlObject = sessions.get(sessionid);

				if (sessionControlObject != null && isValid(sessionControlObject)) {
					sessionControlObject.updateTimestamp();

					if (a > 0) {
						/*
						 * Put into first container and remove from latter one
						 */
						sessionList.get(0).put(sessionid, sessionControlObject);
						sessions.remove(sessionid);
					}

					return sessionControlObject;
				}
				return null;
			}
		}
		return null;
	}

	/**
	 * Gets (and removes) the session bound to given secret cookie identifier in
	 * cache.
	 * <p>
	 * Session is going to be added to local session containers on a cache hit.
	 * 
	 * @param secret
	 *            The secret cookie identifier
	 * @param localIP
	 *            The host's local IP
	 * @return A wrapping instance of {@link SessionControl} or
	 *         <code>null</code>
	 */
	public static SessionControl getCachedSession(final String secret, final String localIP) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getCachedSession <" + secret + '>');
		}
		try {
			final CachedSession cachedSession = SessionCache.getInstance().removeCachedSession(secret);
			if (null != cachedSession) {
				/*
				 * A cache hit! Add to local session containers
				 */
				return addSessionInternal(new SessionImpl(cachedSession, localIP));
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
		final List<SessionControl> retval = new ArrayList<SessionControl>(numberOfActiveSessions.get());
		for (int a = 0; a < numberOfSessionContainers; a++) {
			retval.addAll(sessionList.get(a).values());
		}
		return retval;
	}

	protected static void cleanUp() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("session cleanup");
		}

		if (LOG.isDebugEnabled()) {
			final Map<String, SessionControl> hashMap = sessionList.getLast();
			final Iterator<String> iterator = hashMap.keySet().iterator();
			while (iterator.hasNext()) {
				LOG.debug("session timeout for id: " + iterator.next());
			}
		}
		prependContainer();
		decrementNumberOfActiveSessions(sessionList.getLast().size());
		removeContainer();
	}

	/**
	 * Checks if a session is still valid. Therefore the maximum lifetime of a
	 * session, a disabled context and a disabled user are checked.
	 * 
	 * @param session
	 *            Session to check.
	 * @return <code>true</code> if the session is still valid.
	 */
	protected static boolean isValid(final SessionControl session) {
		return ((session.getTimestamp() + session.getLifetime()) >= System.currentTimeMillis());
	}

	public static void close() {
		numberOfSessionContainers = 4;
		postContainersRemoval();
		sessionList = new LinkedList<Map<String, SessionControl>>();
		userList = new LinkedList<Map<String, String>>();
		randomList = new LinkedList<Map<String, String>>();
		sessionIdGenerator = null;
		config = null;
		noLimit = false;
		isInit = false;
	}

	public static int getNumberOfActiveSessions() {
		return numberOfActiveSessions.get();
	}

	protected static void decrementNumberOfActiveSessions(final int amount) {
		for (int a = 0; a < amount; a++) {
			numberOfActiveSessions.decrementAndGet();
		}
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

	private static void postContainerRemoval(final Map<String, SessionControl> sessions) {
		final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
		if (eventAdmin != null) {
			final Hashtable<Object, Object> dic = new Hashtable<Object, Object>();
			dic.put(SessiondEventConstants.PROP_CONTAINER, convert(sessions));
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
			for (final Map<String, SessionControl> sessions : sessionList) {
				final Hashtable<Object, Object> dic = new Hashtable<Object, Object>();
				dic.put(SessiondEventConstants.PROP_CONTAINER, convert(sessions));
				final Event event = new Event(SessiondEventConstants.TOPIC_REMOVE_CONTAINER, dic);
				eventAdmin.postEvent(event);
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Posted events for removed session containers");
		}
	}

	private static Map<String, Session> convert(final Map<String, SessionControl> sessions) {
		final int size = sessions.size();
		final Map<String, Session> retval = new HashMap<String, Session>(size);
		final Iterator<Map.Entry<String, SessionControl>> iter = sessions.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, SessionControl> entry = iter.next();
			retval.put(entry.getKey(), entry.getValue().getSession());
		}
		return retval;
	}
}
