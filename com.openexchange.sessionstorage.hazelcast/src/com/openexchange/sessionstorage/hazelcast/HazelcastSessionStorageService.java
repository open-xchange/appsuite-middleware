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

package com.openexchange.sessionstorage.hazelcast;

import java.util.ArrayList;
import java.util.List;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.hazelcast.exceptions.OXHazelcastSessionStorageExceptionCodes;
import com.openexchange.sessionstorage.hazelcast.osgi.HazelcastSessionStorageServiceRegistry;

/**
 * {@link HazelcastSessionStorageService}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class HazelcastSessionStorageService implements SessionStorageService {

    private final HazelcastInstance hazelcast;

    private final IMap<String, HazelcastStoredSession> sessions;

    private final String encryptionKey;

    private final CryptoService cryptoService;

    private final MapConfig mapConfig;

    private static volatile HazelcastSessionStorageService instance;

    /**
     * Initializes a new {@link HazelcastSessionStorageService}.
     */
    public HazelcastSessionStorageService(HazelcastSessionStorageConfiguration config) {
        super();
        encryptionKey = config.getEncryptionKey();
        mapConfig = config.getMapConfig();
        hazelcast = HazelcastSessionStorageServiceRegistry.getRegistry().getService(HazelcastInstance.class);
        hazelcast.getConfig().addMapConfig(mapConfig);
        sessions = hazelcast.getMap("sessions");
        cryptoService = config.getCryptoService();
        instance = this;
    }

    @Override
    public Session lookupSession(String sessionId) throws OXException {
        if (sessions.containsKey(sessionId)) {
            HazelcastStoredSession s = sessions.get(sessionId);
            s.setLastAccess(System.currentTimeMillis());
            s.setPassword(decrypt(s.getPassword()));
            sessions.replace(sessionId, s);
            return s;
        }
        throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_SESSION_NOT_FOUND.create(sessionId);
    }

    @Override
    public void addSession(Session session) throws OXException {
        try {
            HazelcastStoredSession ss = new HazelcastStoredSession(session);
            ss.setPassword(crypt(ss.getPassword()));
            sessions.put(session.getSessionID(), ss);
        } catch (Exception e) {
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_SAVE_FAILED.create(e, session.getSessionID());
        }
    }

    @Override
    public void removeSession(String sessionId) throws OXException {
        try {
            sessions.remove(sessionId);
        } catch (Exception e) {
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_REMOVE_FAILED.create(e, sessionId);
        }
    }

    @Override
    public Session[] removeUserSessions(int userId, int contextId) throws OXException {
        List<Session> removed = new ArrayList<Session>();
        for (String sessionId : sessions.keySet()) {
            Session s = sessions.get(sessionId);
            if (s.getUserId() == userId && s.getContextId() == contextId) {
                removeSession(sessionId);
                removed.add(s);
            }
        }
        Session[] retval = new Session[removed.size()];
        int i = 0;
        for (Session s : removed) {
            retval[i++] = s;
        }
        return retval;
    }

    @Override
    public void removeContextSessions(int contextId) throws OXException {
        for (String sessionId : sessions.keySet()) {
            Session s = sessions.get(sessionId);
            if (s.getContextId() == contextId) {
                removeSession(sessionId);
            }
        }
    }

    @Override
    public boolean hasForContext(int contextId) {
        for (String sessionId : sessions.keySet()) {
            Session s = sessions.get(sessionId);
            if (s.getContextId() == contextId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Session[] getUserSessions(int userId, int contextId) {
        List<HazelcastStoredSession> found = new ArrayList<HazelcastStoredSession>();
        for (String sessionId : sessions.keySet()) {
            Session s = sessions.get(sessionId);
            if (s.getUserId() == userId && s.getContextId() == contextId) {
                HazelcastStoredSession ss = new HazelcastStoredSession(s);
                ss.setLastAccess(System.currentTimeMillis());
                found.add(ss);
            }
        }
        Session[] retval = new Session[found.size()];
        int i = 0;
        for (Session s : found) {
            retval[i++] = s;
        }
        return retval;
    }

    @Override
    public Session getAnyActiveSessionForUser(int userId, int contextId) {
        Session[] userSessions = getUserSessions(userId, contextId);
        if (userSessions.length > 0) {
            return userSessions[0];
        }
        return null;
    }

    @Override
    public Session findFirstSessionForUser(int userId, int contextId) {
        return getAnyActiveSessionForUser(userId, contextId);
    }

    @Override
    public synchronized List<Session> getSessions() {
        List<Session> retval = new ArrayList<Session>();
        for (String sessionId : sessions.keySet()) {
            retval.add(sessions.get(sessionId));
        }
        return retval;
    }

    @Override
    public int getNumberOfActiveSessions() {
        return sessions.size();
    }

    @Override
    public Session getSessionByRandomToken(String randomToken, String newIP) throws OXException {
        for (String sessionId : sessions.keySet()) {
            Session s = lookupSession(sessionId);
            if (s.getRandomToken().equals(randomToken)) {
                if (!s.getLocalIp().equals(newIP)) {
                    s.setLocalIp(newIP);
                }
                return s;
            }
        }
        throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND.create(randomToken);
    }

    @Override
    public Session getSessionByAlternativeId(String altId) throws OXException {
        for (String sessionId : sessions.keySet()) {
            HazelcastStoredSession s = sessions.get(sessionId);
            if (s.getParameter(Session.PARAM_ALTERNATIVE_ID).equals(altId)) {
                return s;
            }
        }
        throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND.create(altId);
    }

    @Override
    public Session getCachedSession(String sessionId) throws OXException {
        return lookupSession(sessionId);
    }

    @Override
    public void cleanUp() {
        sessions.clear();
    }

    @Override
    public void changePassword(String sessionId, String newPassword) throws OXException {
        Session s = lookupSession(sessionId);
        HazelcastStoredSession ss = new HazelcastStoredSession(s);
        ss.setPassword(newPassword);
        ss.setLastAccess(System.currentTimeMillis());
        sessions.replace(sessionId, new HazelcastStoredSession(s), ss);
    }

    @Override
    public void checkAuthId(String login, String authId) throws OXException {
        if (null != authId) {
            for (final Session session : getSessions()) {
                if (authId.equals(session.getAuthId())) {
                    throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_DUPLICATE_AUTHID.create(
                        session.getLogin(),
                        login);
                }
            }
        }
    }

    private String crypt(String password) throws OXException {
        return cryptoService.encrypt(password, encryptionKey);
    }

    private String decrypt(String encPassword) throws OXException {
        return cryptoService.decrypt(encPassword, encryptionKey);
    }

    public static HazelcastSessionStorageService getStorageService() {
        return instance;
    }

}
