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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServerTimer;
import com.openexchange.session.Session;
import com.openexchange.sessiond.exception.SessiondException;
import com.openexchange.sessiond.exception.SessiondException.Code;

/**
 * SessionHandler
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class SessionHandler {

	protected static int numberOfSessionContainers = 4;

	protected static LinkedList<Map<String, SessionControlObject>> sessionList = new LinkedList<Map<String, SessionControlObject>>();

	protected static LinkedList<Map<String, String>> userList = new LinkedList<Map<String, String>>();

	protected static LinkedList<Map<String, String>> randomList = new LinkedList<Map<String, String>>();

	protected static SessionIdGenerator sessionIdGenerator;

	private static SessiondConfigInterface config;

	private static boolean noLimit = false;

	private static boolean isInit = false;

	private static int[] numberOfSessionsInContainer;

	private static final Log LOG = LogFactory.getLog(SessionHandler.class);

	public SessionHandler() {

	}

	public static void init(final SessiondConfigInterface config) {
		SessionHandler.config = config;

		if (!isInit) {
			try {
				sessionIdGenerator = SessionIdGenerator.getInstance();
			} catch (SessiondException exc) {
				LOG.error("create instance of SessionIdGenerator", exc);
			}
			
			numberOfSessionContainers = config.getNumberOfSessionContainers();

			for (int a = 0; a < numberOfSessionContainers; a++) {
				prependContainer();
			}
		}

		numberOfSessionsInContainer = new int[numberOfSessionContainers];

		noLimit = (config.getMaxSessions() == 0);

		final SessiondTimer sessiondTimer = new SessiondTimer();
		final Timer t = ServerTimer.getTimer();
		t.schedule(sessiondTimer, config.getSessionContainerTimeout(), config
				.getSessionContainerTimeout());
	}

	private static void prependContainer() {
		sessionList.add(0, new Hashtable<String, SessionControlObject>(config.getMaxSessions()));
		userList.add(0, new Hashtable<String, String>(config.getMaxSessions()));
		randomList.add(0, new Hashtable<String, String>(config.getMaxSessions()));
	}

	private static void removeContainer() {
		sessionList.removeLast();
		userList.removeLast();
		randomList.removeLast();
	}

	protected static String addSession(final int userId, String loginName, String password,
			final Context context, final String clientHost) throws SessiondException {
		final String sessionId = sessionIdGenerator.createSessionId(loginName, clientHost);
		final String secret = sessionIdGenerator.createSecretId(loginName, String.valueOf(System
				.currentTimeMillis()));
		final String randomToken = sessionIdGenerator.createRandomId();

		final Session session = new SessionImpl(userId, loginName, password, context, sessionId,
				secret, randomToken, clientHost);

		if (LOG.isDebugEnabled()) {
			LOG.debug("addSession <" + sessionId + '>');
		}

		Map<String, SessionControlObject> sessions = null;
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

		final SessionControlObject sessionControlObject = new SessionControlObject(session, config
				.getLifeTime());
		if (sessions.containsKey(sessionId) && LOG.isDebugEnabled()) {
			LOG.debug("session REBORN sessionid=" + sessionId);
		}

		sessions.put(sessionId, sessionControlObject);
		randomMap.put(randomToken, sessionId);
		userMap.put(loginName, sessionId);
		LOG.warn("TODO: MonitoringInfo still missing");
		// MonitoringInfo.incrementNumberOfActiveSessions();

		return sessionId;
	}

	protected static boolean refreshSession(final String sessionid) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("refreshSession <" + sessionid + '>');
		}

		Map<String, SessionControlObject> sessions = null;

		final Date timestamp = new Date();

		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);

			if (sessions.containsKey(sessionid)) {
				final SessionControlObject sessionIdInterface = sessions.get(sessionid);
				if (isValid(sessionIdInterface)) {
					sessionIdInterface.updateTimestamp();

					sessionList.get(0).put(sessionid, sessionIdInterface);
					if (a > 0) {
						sessions.remove(sessionid);
						// the session is only moved to the first container so a
						// decrement is not nessesary
						// MonitoringInfo.decrementNumberOfActiveSessions();
						LOG.warn("TODO: MonitoringInfo still missing");
					}

					return true;
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("session TIMEOUT sessionid=" + sessionid);
				}
				sessions.remove(sessionid);
				LOG.warn("TODO: MonitoringInfo still missing");
				// MonitoringInfo.decrementNumberOfActiveSessions();

				return false;
			}
		}
		return false;
	}

	protected static boolean clearSession(final String sessionid) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("clearSession <" + sessionid + '>');
		}

		Map<String, SessionControlObject> sessions = null;

		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);

			if (sessions.containsKey(sessionid)) {
				final SessionControlObject session = sessions.remove(sessionid);
				// session.closingOperations();
				// MonitoringInfo.decrementNumberOfActiveSessions();
				LOG.warn("TODO: MonitoringInfo still missing");

				return true;
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Cannot find session id to remove session <" + sessionid + '>');
		}

		return false;
	}

	protected static Session getSessionByRandomToken(final String randomToken) {
		Map<String, String> random = null;

		for (int a = 0; a < numberOfSessionContainers; a++) {
			random = randomList.get(a);

			if (random.containsKey(randomToken)) {
				final String sessionId = random.get(randomToken);
				final SessionControlObject sessionControlObject = getSession(sessionId, true);

				final long now = System.currentTimeMillis();

				if (sessionControlObject.getCreationTime().getTime()
						+ config.getRandomTokenTimeout() >= now) {
					final Session session = sessionControlObject.getSession();
					session.removeRandomToken();
					random.remove(randomToken);
					return session;
				}
			}
		}
		return null;
	}

	protected static SessionControlObject getSession(final String sessionid, final boolean refresh) {
		LOG.debug("getSession <" + sessionid + ">");

		Map<String, SessionControlObject> sessions = null;

		final Date timestamp = new Date();

		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);

			if (sessions.containsKey(sessionid)) {
				SessionControlObject sessionControlObject = sessions.get(sessionid);

				if (isValid(sessionControlObject)) {
					sessionControlObject.updateTimestamp();

					sessionList.get(0).put(sessionid, sessionControlObject);
					if (a > 0) {
						sessions.remove(sessionid);
					}

					return sessionControlObject;
				}
				return null;
			}
		}
		return null;
	}

	protected static void cleanUp() {
		LOG.debug("session cleanup");

		if (LOG.isDebugEnabled()) {
			final Map<String, SessionControlObject> hashMap = sessionList.getLast();
			final Iterator<String> iterator = hashMap.keySet().iterator();
			while (iterator.hasNext()) {
				LOG.debug("session timeout for id: " + iterator.next());
			}
		}
		prependContainer();
		// MonitoringInfo.decrementNumberOfActiveSessions(sessionList.getLast().size());
		removeContainer();

		for (int a = 0; a < sessionList.size(); a++) {
			numberOfSessionsInContainer[a] = sessionList.get(a).size();
		}

		// MonitoringInfo.setNumberOfSessionsInContainer(numberOfSessionsInContainer);
		LOG.warn("TODO: MonitoringInfo still missing");
	}

	/**
	 * Checks if a session is still valid. Therefore the maximum lifetime of a
	 * session, a disabled context and a disabled user are checked.
	 * 
	 * @param session
	 *            Session to check.
	 * @return <code>true</code> if the session is still valid.
	 */
	protected static boolean isValid(final SessionControlObject session) {
		if ((session.getTimestamp().getTime() + session.getLifetime()) < System.currentTimeMillis()) {
			return false;
		}
		try {
			// if (!session.getSession().getContext().isEnabled() ||
			// !session.getSession().getUserObject()
			// .isMailEnabled()) {
			// return false;
			// }
			LOG.warn("TODO: enabled check not missing");
		} catch (UndeclaredThrowableException e) {
			return false;
		}
		return true;
	}
	
	public static void close() {
		numberOfSessionContainers = 4;
		sessionList = new LinkedList<Map<String, SessionControlObject>>();
		userList = new LinkedList<Map<String, String>>();
		randomList = new LinkedList<Map<String, String>>();
		sessionIdGenerator = null;
		config = null;
		noLimit = false;
		isInit = false;
		numberOfSessionsInContainer = null;
	}
}
