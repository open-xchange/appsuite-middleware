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
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcasts;
import com.hazelcast.core.IMap;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageExceptionCodes;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.hazelcast.exceptions.OXHazelcastSessionStorageExceptionCodes;

/**
 * {@link HazelcastSessionStorageService} - The {@link SessionStorageService} backed by {@link HazelcastInstance}.
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastSessionStorageService implements SessionStorageService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastSessionStorageService.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private final String mapName;
    private final String encryptionKey;
    private final CryptoService cryptoService;

    /**
     * Initializes a new {@link HazelcastSessionStorageService}.
     */
    public HazelcastSessionStorageService(final HazelcastSessionStorageConfiguration config, final HazelcastInstance hazelcast) {
        super();
        encryptionKey = config.getEncryptionKey();
        cryptoService = config.getCryptoService();
        final MapConfig mapConfig = config.getMapConfig();
        final String name = mapConfig.getName();
        mapName = name;
        final Config hzConfig = hazelcast.getConfig();
        if (null == hzConfig.getMapConfig(name)) {
            hzConfig.addMapConfig(mapConfig);
        }
    }

    /**
     * Gets the map
     * 
     * @return The map
     * @throws OXException If service is unavailable or currently paused
     */
    private IMap<String, HazelcastStoredSession> sessions() throws OXException {
        final HazelcastInstance hazelcastInstance = Services.optService(HazelcastInstance.class);
        if (null == hazelcastInstance) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        if (Hazelcasts.isPaused()) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        return hazelcastInstance.getMap(mapName);
    }

    /**
     * Gets the map
     * 
     * @return The map
     * @throws HazelcastException If service is unavailable or currently paused
     */
    private IMap<String, HazelcastStoredSession> sessionsUnchecked() {
        try {
            return sessions();
        } catch (final OXException e) {
            throw new HazelcastException(e.getMessage(), e);
        }
    }

    @Override
    public Session lookupSession(String sessionId) throws OXException {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessions();
            if (null != sessionId && sessions.containsKey(sessionId)) {
                HazelcastStoredSession s = sessions.get(sessionId);
                s.setLastAccess(System.currentTimeMillis());
                s.setPassword(decrypt(s.getPassword()));
                sessions.replace(sessionId, s);
                return s;
            }
            throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(sessionId);
        } catch (final HazelcastException e) {
            throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(e, sessionId);
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                throw SessionStorageExceptionCodes.NO_SESSION_FOUND.create(e, sessionId);
            }
            throw e;
        }
    }

    @Override
    public void addSession(Session session) throws OXException {
        if (null != session) {
            try {
                HazelcastStoredSession ss = new HazelcastStoredSession(session);
                ss.setPassword(crypt(ss.getPassword()));
                sessions().put(session.getSessionID(), ss);
            } catch (HazelcastException e) {
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_SAVE_FAILED.create(e, session.getSessionID());
            } catch (final OXException e) {
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_SAVE_FAILED.create(e, session.getSessionID());
                }
                throw e;
            }
        }
    }

    @Override
    public void removeSession(String sessionId) throws OXException {
        if (null != sessionId) {
            try {
                sessions().remove(sessionId);
            } catch (HazelcastException e) {
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_REMOVE_FAILED.create(e, sessionId);
            } catch (final OXException e) {
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_REMOVE_FAILED.create(e, sessionId);
                }
                throw e;
            }
        }
    }

    @Override
    public Session[] removeUserSessions(int userId, int contextId) throws OXException {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessions();
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
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
                return new Session[0];
            }
            throw e;
        }
    }

    @Override
    public void removeContextSessions(int contextId) throws OXException {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessions();
            for (String sessionId : sessions.keySet()) {
                Session s = sessions.get(sessionId);
                if (s.getContextId() == contextId) {
                    removeSession(sessionId);
                }
            }
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
            }
            throw e;
        }
    }

    @Override
    public boolean hasForContext(int contextId) {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked();
            for (String sessionId : sessions.keySet()) {
                Session s = sessions.get(sessionId);
                if (s.getContextId() == contextId) {
                    return true;
                }
            }
            return false;
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return false;
        }
    }

    @Override
    public Session[] getUserSessions(int userId, int contextId) {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked();
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
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return new Session[0];
        }
    }

    @Override
    public Session getAnyActiveSessionForUser(int userId, int contextId) {
        try {
            Session[] userSessions = getUserSessions(userId, contextId);
            if (userSessions.length > 0) {
                return userSessions[0];
            }
            return null;
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public Session findFirstSessionForUser(int userId, int contextId) {
        return getAnyActiveSessionForUser(userId, contextId);
    }

    @Override
    public synchronized List<Session> getSessions() {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessionsUnchecked();
            List<Session> retval = new ArrayList<Session>();
            for (String sessionId : sessions.keySet()) {
                retval.add(sessions.get(sessionId));
            }
            return retval;
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public int getNumberOfActiveSessions() {
        return sessionsUnchecked().size();
    }

    @Override
    public Session getSessionByRandomToken(String randomToken, String newIP) throws OXException {
        try {
            for (String sessionId : sessions().keySet()) {
                Session s = lookupSession(sessionId);
                if (s.getRandomToken().equals(randomToken)) {
                    if (!s.getLocalIp().equals(newIP)) {
                        s.setLocalIp(newIP);
                    }
                    return s;
                }
            }
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND.create(randomToken);
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND.create(e, randomToken);
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND.create(e, randomToken);
            }
            throw e;
        }
    }

    @Override
    public Session getSessionByAlternativeId(String altId) throws OXException {
        try {
            final IMap<String, HazelcastStoredSession> sessions = sessions();
            for (String sessionId : sessions.keySet()) {
                HazelcastStoredSession s = sessions.get(sessionId);
                if (s.getParameter(Session.PARAM_ALTERNATIVE_ID).equals(altId)) {
                    return s;
                }
            }
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND.create(altId);
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND.create(e, altId);
        } catch (final OXException e) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                if (DEBUG) {
                    LOG.debug(e.getMessage(), e);
                }
                throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND.create(e, altId);
            }
            throw e;
        }
    }

    @Override
    public Session getCachedSession(String sessionId) throws OXException {
        return lookupSession(sessionId);
    }

    @Override
    public void cleanUp() {
        sessionsUnchecked().clear();
    }

    @Override
    public void changePassword(String sessionId, String newPassword) throws OXException {
        try {
            Session s = lookupSession(sessionId);
            HazelcastStoredSession ss = new HazelcastStoredSession(s);
            ss.setPassword(newPassword);
            ss.setLastAccess(System.currentTimeMillis());
            sessions().replace(sessionId, new HazelcastStoredSession(s), ss);
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void checkAuthId(String login, String authId) throws OXException {
        try {
            if (null != authId) {
                for (final Session session : getSessions()) {
                    if (authId.equals(session.getAuthId())) {
                        throw OXHazelcastSessionStorageExceptionCodes.HAZELCAST_SESSIONSTORAGE_DUPLICATE_AUTHID.create(
                            session.getLogin(),
                            login);
                    }
                }
            }
        } catch (HazelcastException e) {
            if (DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw SessionStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private String crypt(String password) throws OXException {
        return cryptoService.encrypt(password, encryptionKey);
    }

    private String decrypt(String encPassword) throws OXException {
        return cryptoService.decrypt(encPassword, encryptionKey);
    }

}
