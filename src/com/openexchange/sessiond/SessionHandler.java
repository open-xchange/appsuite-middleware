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

import java.lang.reflect.UndeclaredThrowableException;
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
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.contexts.LoginInfo;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.IMAPPropertyException;
import com.openexchange.imap.IMAPPropertiesFactory;
import com.openexchange.imap.UserSettingMailStorage;
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

    protected static SessionIdGenerator sessionIdGenerator;

    private static SessiondConfig config;

    private static int lifeTime = 360000;

    private static int randomTokenTimeout = 60000;

    private static int containerTimeout = 420000;

    private static boolean noLimit;

    private static boolean isInit;

    private static int[] numberOfSessionsInContainer;

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
                prependContainer();
            }

            numberOfSessionsInContainer = new int[numberOfSessionContainers];

            try {
                sessionIdGenerator = SessionIdGenerator.getInstance();
            } catch (SessiondException exc) {
                LOG.error("create instance of SessionIdGenerator", exc);
            }

            noLimit = (maxSessions == 0);

            final Timer t = ServerTimer.getTimer();
            t.schedule(this, containerTimeout, containerTimeout);

            isInit = true;
        }
    }

    private static void prependContainer() {
        sessionList.add(0, new Hashtable<String, SessionObject>(maxSessions));
        userList.add(0, new Hashtable<String, String>(maxSessions));
        randomList.add(0, new Hashtable<String, String>(maxSessions));
    }

    private static void removeContainer() {
        sessionList.removeLast();
        userList.removeLast();
        randomList.removeLast();
    }

    protected static SessionObject addSession(final String loginName, final String password, final String client_ip,
            final String host) throws LoginException, InvalidCredentialsException, UserNotFoundException, UserNotActivatedException,
            PasswordExpiredException, MaxSessionLimitException, SessiondException, ContextException {
        final String sessionId = sessionIdGenerator.createSessionId(loginName, client_ip);

        if (LOG.isDebugEnabled()) {
            LOG.debug("addSession <" + sessionId + '>');
        }

        final LoginInfo li = LoginInfo.getInstance();
        final String[] login_infos = li.handleLoginInfo(loginName, password);

        final String contextname = login_infos[0];
        final String username = login_infos[1];

        final ContextStorage contextStor = ContextStorage.getInstance();
        final int contextId = contextStor.getContextId(contextname);
        if (ContextStorage.NOT_FOUND == contextId) {
            throw new ContextException(ContextException.Code.NO_MAPPING,
                contextname);
        }
        final Context context = contextStor.getContext(contextId);
        if (null == context) {
            throw new ContextException(ContextException.Code.NOT_FOUND,
                contextId);
        }

        int userId = -1;
        User u = null;

        try {
            final UserStorage us = UserStorage.getInstance(context);
            userId = us.getUserId(username);
            u = us.getUser(userId);
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
        sessionobject.setUsername(String.valueOf(userId));
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
        sessionobject.setUserObject(u);

        sessionobject.setRandomToken(randomId);
        sessionobject.setSecret(sessionIdGenerator.createSecretId(loginName, client_ip));

        // Load IMAP Info
        try {
            sessionobject.setIMAPProperties(IMAPPropertiesFactory.getImapProperties(sessionobject));
        } catch (final IMAPPropertyException e) {
            LOG.error("ERROR! IMAPPropertyException OCCURRED " + e.getMessage());
        }

        if (sessions.containsKey(sessionId) && LOG.isDebugEnabled()) {
            //if (LOG.isDebugEnabled()) {
                LOG.debug("session REBORN sessionid=" + sessionId);
            //}
        }

        sessions.put(sessionId, sessionobject);
        randomMap.put(randomId, sessionId);
        userMap.put(loginName, sessionId);
        MonitoringInfo.incrementNumberOfActiveSessions();

        return sessionobject;
    }

    protected static boolean refreshSession(final String sessionid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshSession <" + sessionid + '>');
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
                        // the session is only moved to the first container so a decrement is not nessesary
                        // MonitoringInfo.decrementNumberOfActiveSessions();
                    }

                    return true;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("session TIMEOUT sessionid=" + sessionid);
                }
                sessions.remove(sessionid);
                MonitoringInfo.decrementNumberOfActiveSessions();

                return false;
            }
        }
        return false;
    }

    protected static boolean clearSession(final String sessionid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearSession <" + sessionid + '>');
        }

        Map<String, SessionObject> sessions = null;

        for (int a = 0; a < numberOfSessionContainers; a++) {
            sessions = sessionList.get(a);

            if (sessions.containsKey(sessionid)) {
                final SessionObject session = sessions.remove(sessionid);
                session.closingOperations();
                MonitoringInfo.decrementNumberOfActiveSessions();

                return true;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Cannot find session id to remove session <" + sessionid + '>');
        }

        return false;
    }

    protected static boolean setSession(final SessionObject sessionobject) {
        final String sessionid = sessionobject.getSessionID();

        if (LOG.isDebugEnabled()) {
            LOG.debug("setSession <" + sessionid + '>');
        }

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

                if (sessionObj.getCreationtime().getTime() + randomTokenTimeout >= now) {
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
                }
                return null;
            }
        }
        return null;
    }

    protected static Iterator getSessions() {
        return new SessionIterator();
    }

    protected static void cleanUp() {
        LOG.debug("session cleanup");

        if (LOG.isDebugEnabled()) {
            final Map<String, SessionObject> hashMap = sessionList.getLast();
            final Iterator<String> iterator = hashMap.keySet().iterator();
            while (iterator.hasNext()) {
                LOG.debug("session timeout for id: " + iterator.next());
            }
        }

        prependContainer();
        MonitoringInfo.decrementNumberOfActiveSessions(sessionList.getLast().size());
        removeContainer();

        for (int a = 0; a < sessionList.size(); a++) {
            numberOfSessionsInContainer[a] = sessionList.get(a).size();
        }

        MonitoringInfo.setNumberOfSessionsInContainer(numberOfSessionsInContainer);
    }

    /**
     * Checks if a session is still valid. Therefore the maximum lifetime of a
     * session, a disabled context and a disabled user are checked.
     * @param session Session to check.
     * @return <code>true</code> if the session is still valid.
     */
    protected static boolean isValid(final SessionObject session) {
        final Context context = session.getContext();
        if (context == null) {
            LOG.error("context object is null!");
        }
        if ((session.getTimestamp().getTime() + session.getLifetime()) < System
            .currentTimeMillis()) {
            return false;
        }
        try {
            if (!context.isEnabled() || !session.getUserObject()
                .isMailEnabled()) {
                return false;
            }
        } catch (UndeclaredThrowableException e) {
            return false;
        }
        return true;
    }

    public void run() {
        cleanUp();
    }

    protected static class SessionIterator implements Iterator {
        Map sessions;

        boolean hasnext;

        Iterator it;

        int pos;

        public SessionIterator() {
            it = ((Map) sessionList.get(pos)).keySet().iterator();
            pos++;
        }

        public Object next() {
            if (it != null) {
                return it.next();
            }
            if (!hasnext && pos < numberOfSessionContainers) {
                it = ((Map) sessionList.get(pos)).keySet().iterator();
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
