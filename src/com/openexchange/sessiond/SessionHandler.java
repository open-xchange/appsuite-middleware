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



package com.openexchange.sessiond;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.contexts.ContextNotFoundException;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.contexts.LoginInfo;
import com.openexchange.groupware.imap.IMAPException;
import com.openexchange.groupware.imap.IMAPPropertiesFactory;
import com.openexchange.groupware.ldap.Authentication;
import com.openexchange.groupware.ldap.Credentials;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.update.Updater;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.server.ServerTimer;

/**
 * SessionHandler
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class SessionHandler extends TimerTask {
	
	protected static int numberOfSessionContainers = 4;
	
	protected static int maxSessions = 5000;
	
	protected static LinkedList<Map<String, SessionObject>> sessionList = new LinkedList<Map<String, SessionObject>>();
	
	protected static LinkedList<Map<String, String>> userList = new LinkedList<Map<String, String>>();
	
	protected static LinkedList<Map<String, String>> randomList = new LinkedList<Map<String, String>>();
	
	protected static SessionIdGenerator sessionIdGenerator = null;
	
	private static SessiondConfig config = null;
	
	private static int lifeTime = 360000;
	
	private static int randomTokenTimeout = 60000;
	
	private static int containerTimeout = 420000;
	
	private static boolean noLimit = false;
	
	private static boolean isInit = false;
	
	private static int[] numberOfSessionsInContainer = null;
	
	private static final Log LOG = LogFactory.getLog(SessionHandler.class);
	
	public SessionHandler(SessiondConfig config) {
		this.config = config;
	}
	
	public void init() {
		numberOfSessionContainers = config.getNumberOfSessionContainers();
		maxSessions = config.getMaxSessions();
		lifeTime = config.getLifeTime();
		randomTokenTimeout = config.getRandomTokenTimeout();
		
		if (!isInit) {
			for (int a = 0; a < numberOfSessionContainers; a++) {
				addContainer();
			}
			
			numberOfSessionsInContainer = new int[numberOfSessionContainers];
			
			try {
				sessionIdGenerator = SessionIdGenerator.getInstance();
			} catch (SessiondException exc) {
				LOG.error("create instance of SessionIdGenerator", exc);
			}
			
			noLimit = (maxSessions == 0);
			
			Timer t = ServerTimer.getTimer();
			t.schedule(this, containerTimeout, containerTimeout);
			
			isInit = true;
		}
	}
	
	private static void addContainer() {
		sessionList.add(new Hashtable<String, SessionObject>(maxSessions));
		userList.add(new Hashtable<String, String>(maxSessions));
		randomList.add(new Hashtable<String, String>(maxSessions));
	}
	
	protected static SessionObject addSession(final String loginName, final String password, final String client_ip,
			final String host) throws LoginException, InvalidCredentialsException, UserNotFoundException, UserNotActivatedException,
			PasswordExpiredException, ContextNotFoundException, MaxSessionLimitException, SessiondException {
		final String sessionId = sessionIdGenerator.createSessionId(loginName, client_ip);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("addSession <" + sessionId + '>');
		}
		
		final LoginInfo li = LoginInfo.getInstance();
		String[] login_infos = null;
		try {
			login_infos = li.handleLoginInfo(loginName, password);
		} catch (LoginException e) {
			switch (e.getCategory()) {
				case SUBSYSTEM_OR_SERVICE_DOWN:
				case PROGRAMMING_ERROR:
				case SETUP_ERROR:
				case SOCKET_CONNECTION:
					throw new SessiondException(e);
				default:
					throw e;
			}
		}
		
		final String contextname = login_infos[0];
		final String username = login_infos[1];
		
		final ContextStorage cs = ContextStorage.getInstance();
		Context context = null;
		try {
			final int contextId = cs.getContextId(contextname);
			if (ContextStorage.NOT_FOUND == contextId) {
				throw new ContextNotFoundException("Can't find context.");
			}
			context = cs.getContext(contextId);
		} catch (ContextException e) {
			throw new SessiondException("", e);
		}
		
		if (context == null) {
			throw new ContextNotFoundException("can't find context with the given name (" + contextname + ")");
		}

        update(context);
        
		Credentials cred = null;
		User u = null;
		
		try {
			final Authentication auth = Authentication.getInstance(context);
			cred = auth.authenticate(username, password);
			
			if (cred != null) {
				final UserStorage us = UserStorage.getInstance(context);
				u = us.getUser(Integer.parseInt(cred.getValue(Credentials.USER_ID)));
			} else {
				throw new InvalidCredentialsException("invalid credentials!");
			}
		} catch (LdapException ex) {
			switch (ex.getDetail()) {
				case ERROR:
					throw new SessiondException(
							"Problem while authenticating user.", ex);
				case NOT_FOUND:
					throw new UserNotFoundException("User not found.", ex);
			}
		}
		
		String lang = u.getPreferredLanguage();
		
		if (lang == null || lang.length() == 0) {
			lang = "EN";
		}
		
		// is user active
		if (u.isMailEnabled()) {
			if (u.getShadowLastChange() == 0) {
				throw new PasswordExpiredException("user password is expired!");
			}
		} else {
			throw new UserNotActivatedException("user is not activated!");
		}
		
		Map<String, SessionObject> sessions = null;
		Map<String, String> userMap = null;
		Map<String, String> randomMap = null;
		
		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);
			userMap = userList.get(a);
			randomMap = randomList.get(a);
			
			if (!noLimit && sessions.size() > maxSessions) {
				throw new MaxSessionLimitException("max session limit reached");
			}
		}
		
		final String randomId = sessionIdGenerator.createRandomId();
		
		final SessionObject sessionobject = new SessionObject(sessionId);
		sessionobject.setUsername(cred.getValue(Credentials.USER_ID));
		sessionobject.setUserlogin(username);
		sessionobject.setLoginName(loginName);
		sessionobject.setPassword(password);
		sessionobject.setLanguage(lang);
		sessionobject.setLocalIp(client_ip);
		sessionobject.setHost(host);
		sessionobject.setCreationtime(new Date());
		sessionobject.setTimestamp(new Date());
		sessionobject.setLifetime(lifeTime);
		sessionobject.setContext(context);
		sessionobject.setCredentials(cred);
		sessionobject.setUserObject(u);
		
		sessionobject.setRandomToken(randomId);
		sessionobject.setSecret(sessionIdGenerator.createSecretId(loginName, client_ip));
		
		// Load IMAP Info
		try {
			sessionobject.setIMAPProperties(IMAPPropertiesFactory.getImapProperties(context, sessionobject));
		} catch (IMAPException e) {
			LOG.error("ERROR! IMAPException OCCURED " + e.getMessage());
		}
		
		if (sessions.containsKey(sessionId)) {
			LOG.debug("session REBORN sessionid=" + sessionId);
		}
		
		// Load user's configuration from db!
		try {
			sessionobject.setUserConfiguration(UserConfiguration.loadUserConfiguration(u.getId(), u.getGroups(),
					context));
		} catch (Exception exc) {
			throw new SessiondException(exc);
		}
		
		sessions.put(sessionId, sessionobject);
		randomMap.put(randomId, sessionId);
		userMap.put(loginName, sessionId);
		MonitoringInfo.incrementNumberOfActiveSessions();
		
		return sessionobject;
	}

    /**
     * @param context
     * @throws LoginException
     * @throws SessiondException
     */
    private static void update(Context context) throws LoginException, SessiondException {
        try {
            Updater updater = Updater.getInstance();
            if (updater.toUpdate(context)) {
                updater.startUpdate(context);
                throw new LoginException(LoginException.Code.UPDATE);
            }
            if (updater.isLocked(context)) {
                throw new LoginException(LoginException.Code.UPDATE);
            }
        } catch (UpdateException e) {
            throw new SessiondException(e.getMessage(), e);
        }
    }
	
	protected static boolean refreshSession(final String sessionid) {
		if (LOG.isDebugEnabled()) {
		LOG.debug("refreshSession <" + sessionid + ">");
		}
		
		Map<String, SessionObject> sessions = null;
		
		final Date timestamp = new Date();
		
		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);
			
			if (sessions.containsKey(sessionid)) {
				final SessionObject sessionobject = sessions.get(sessionid);
				if (isValid(sessionobject)) {
					sessionobject.setTimestamp(timestamp);
					
					sessionList.get(0).put(sessionid, sessionobject);
					if (a > 0) {
						sessions.remove(sessionid);
					}
					
					return true;
				}
				
				LOG.debug("session TIMEOUT sessionid=" + sessionid);
				sessions.remove(sessionid);
				
				return false;
			}
		}
		return false;
	}
	
	protected static boolean clearSession(final String sessionid) {
		LOG.debug("clearSession <" + sessionid + ">");
		
		Map<String, SessionObject> sessions = null;
		
		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);
			
			if (sessions.containsKey(sessionid)) {
				final SessionObject session = sessions.remove(sessionid);
				try {
					session.closingOperations();
				} catch (OXException e) {
					LOG.error(e);
				}
				MonitoringInfo.decrementNumberOfActiveSessions();
				
				return true;
			}
		}
		
		LOG.debug("can't find session id to remove session <" + sessionid + ">");
		
		return false;
	}
	
	protected static boolean setSession(final SessionObject sessionobject) {
		final String sessionid = sessionobject.getSessionID();
		
		LOG.debug("setSession <" + sessionid + ">");
		
		Map<String, SessionObject> sessions = null;
		
		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);
			
			if (sessions.containsKey(sessionid)) {
				sessions.put(sessionid, sessionobject);
				
				return true;
			}
		}
		return false;
	}
	
	protected static SessionObject getSessionByLoginName(final String loginName) {
		Map<String, String> userMap = null;
		
		for (int a = 0; a < numberOfSessionContainers; a++) {
			userMap = userList.get(a);
			
			if (userMap.containsKey(loginName)) {
				final String sessionId = userMap.get(loginName);
				return getSession(sessionId, true);
			}
		}
		return null;
	}
	
	protected static SessionObject getSessionByRandomToken(final String randomToken) {
		Map<String, String> random = null;
		
		for (int a = 0; a < numberOfSessionContainers; a++) {
			random = randomList.get(a);
			
			if (random.containsKey(randomToken)) {
				final String sessionId = random.get(randomToken);
				final SessionObject sessionObj = getSession(sessionId, true);
				
				final long now = System.currentTimeMillis();
				
				if (sessionObj.getCreationtime().getTime() + randomTokenTimeout < now) {
					sessionObj.setRandomToken(null);
					random.remove(randomToken);
					return sessionObj;
				}
			}
		}
		return null;
	}
	
	protected static SessionObject getSession(final String sessionid, final boolean refresh) {
		LOG.debug("getSession <" + sessionid + ">");
		
		Map<String, SessionObject> sessions = null;
		
		final Date timestamp = new Date();
		
		for (int a = 0; a < numberOfSessionContainers; a++) {
			sessions = sessionList.get(a);
			
			if (sessions.containsKey(sessionid)) {
				SessionObject sessionobject = sessions.get(sessionid);
				
				if (isValid(sessionobject)) {
					sessionobject.setTimestamp(timestamp);
					
					sessionList.get(0).put(sessionid, sessionobject);
					if (a > 0) {
						sessions.remove(sessionid);
					}
					
					return sessionobject;
				} else {
					return null;
				}
			}
		}
		return null;
	}
	
	protected static Iterator getSessions() {
		return new SessionIterator();
	}
	
	protected static void cleanUp() {
		LOG.debug("session cleanup");
		
		addContainer();
		MonitoringInfo.decrementNumberOfActiveSessions(sessionList.getLast().size());
		sessionList.removeLast();
		
		for (int a = 0; a < sessionList.size(); a++) {
			numberOfSessionsInContainer[a] = sessionList.get(a).size();
		}		
		
		MonitoringInfo.setNumberOfSessionsInContainer(numberOfSessionsInContainer);
	}
	
	protected static boolean isValid(final SessionObject sessionobject) {
		Context context = sessionobject.getContext();
		
		if (context == null) {
			LOG.error("context object is null!");
		}
		
		if ((sessionobject.getTimestamp().getTime() + sessionobject.getLifetime()) < System.currentTimeMillis()
		&& !context.isEnabled()) {
			return false;
		}
		return true;
	}
	
	public void run() {
		cleanUp();
	}
	
	protected static class SessionIterator implements Iterator {
		Map sessions = null;
		
		boolean hasnext = false;
		
		Iterator it = null;
		
		int pos = 0;
		
		public SessionIterator() {
			it = ((Map) sessionList.get(pos)).keySet().iterator();
			pos++;
		}
		
		public Object next() {
			if (it != null) {
				return it.next();
			} else {
				if (!hasnext && pos < numberOfSessionContainers) {
					it = ((Map) sessionList.get(pos)).keySet().iterator();
				}
			}
			
			return null;
		}
		
		public boolean hasNext() {
			if (it != null) {
				hasnext = it.hasNext();
				return hasnext;
			}
			return false;
		}
		
		public void remove() {
			if (it != null) {
				it.remove();
			}
		}
	}
}
