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

package com.openexchange.sessionstorage.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.StoredSession;
import com.openexchange.sessionstorage.impl.exceptions.OXSessionStorageExceptionCodes;

/**
 * {@link SessionStorageServiceImpl}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SessionStorageServiceImpl implements SessionStorageService {
    
    private final Map<String, StoredSession> sessions;

    private static Log LOG = com.openexchange.log.Log.loggerFor(SessionStorageServiceImpl.class);

    /**
     * Initializes a new {@link SessionStorageServiceImpl}.
     */
    public SessionStorageServiceImpl() {
        super();
        sessions = new ConcurrentHashMap<String, StoredSession>();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.sessionstore.SessionStorageService#lookup(java.lang.String, java.lang.String)
     */
    @Override
    public Session lookupSession(String sessionId) throws OXException {
        StoredSession session = sessions.get(sessionId);
        if (session == null) {
            OXException e = OXSessionStorageExceptionCodes.SESSIONSTORAGE_SESSION_NOT_FOUND.create();
            LOG.error(e.getMessage(), e);
            throw e;
        }
        return session;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.sessionstore.SessionStorageService#save(java.lang.String, java.lang.String)
     */
    @Override
    public void addSession(Session session) throws OXException {
        StoredSession saved = sessions.put(session.getSessionID(), new StoredSession(session));
        if (saved != null) {
            sessions.put(saved.getSessionId(), saved);
            OXException e = OXSessionStorageExceptionCodes.SESSIONSTORAGE_SAVE_FAILED.create("Duplicate session");
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.sessionstore.SessionStorageService#remove(java.lang.String, java.lang.String)
     */
    @Override
    public void removeSession(String sessionId) throws OXException {
        StoredSession session = sessions.remove(sessionId);
        if (session == null) {
            OXException e = OXSessionStorageExceptionCodes.SESSIONSTORAGE_REMOVE_FAILED.create();
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Session[] removeUserSessions(int userId, int contextId) {
        List<Session> retval = new ArrayList<Session>();
        for (String sessionId : sessions.keySet()) {
            StoredSession session = sessions.get(sessionId);
            if (session.getUserId() == userId && session.getContextId() == contextId) {
                retval.add(sessions.remove(sessionId));
            }
        }
        return (Session[]) retval.toArray();
    }

    @Override
    public void removeContextSessions(int contextId) {
        for (String sessionId : sessions.keySet()) {
            StoredSession session = sessions.get(sessionId);
            if (session.getContextId() == contextId) {
                sessions.remove(sessionId);
            }
        }
    }

    @Override
    public boolean hasForContext(int contextId) {
        for (String sessionId : sessions.keySet()) {
            StoredSession session = sessions.get(sessionId);
            if (session.getContextId() == contextId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Session[] getUserSessions(int userId, int contextId) {
        List<Session> userSessions = new LinkedList<Session>();
        for (String sessionId : sessions.keySet()) {
            StoredSession session = sessions.get(sessionId);
            if (session.getUserId() == userId && session.getContextId() == contextId) {
                userSessions.add(session);
            }
        }
        return (Session[]) userSessions.toArray();
    }

    @Override
    public Session getAnyActiveSessionForUser(int userId, int contextId) throws OXException {
        for (String sessionId : sessions.keySet()) {
            StoredSession session = sessions.get(sessionId);
            if (session.getUserId() == userId && session.getContextId() == contextId) {
                //TODO: is active session?
                return session;
            }
        }
        OXException e = OXSessionStorageExceptionCodes.SESSIONSTORAGE_SESSION_NOT_FOUND.create();
        LOG.error(e.getMessage(), e);
        throw e;
    }

    @Override
    public Session findFirstSessionForUser(int userId, int contextId) throws OXException {
        for (String sessionId : sessions.keySet()) {
            StoredSession session = sessions.get(sessionId);
            if (session.getUserId() == userId && session.getContextId() == contextId) {
                return session;
            }
        }
        OXException e = OXSessionStorageExceptionCodes.SESSIONSTORAGE_SESSION_NOT_FOUND.create();
        LOG.error(e.getMessage(), e);
        throw e;
    }

    @Override
    public List<Session> getSessions() {
        List<Session> list = new LinkedList<Session>();
        for (String sessionId : sessions.keySet()) {
            StoredSession s = sessions.get(sessionId);
            list.add(s);
        }
        return list;
    }

    @Override
    public int getNumberOfActiveSessions()  {
        int active = 0;
        for (String sessionId : sessions.keySet()) {
            StoredSession session = sessions.get(sessionId);
            //if (session.isActive()) {
                active++;
            //}
        }
        return active;
    }

    @Override
    public Session getSessionByRandomToken(String randomToken, String newIP) throws OXException {
        for (String sessionId : sessions.keySet()) {
            StoredSession s = sessions.get(sessionId);
            if (s.getRandomToken().equals(randomToken)) {
                return s;
            }
        }
        OXException e = OXSessionStorageExceptionCodes.SESSIONSTORAGE_SESSION_NOT_FOUND.create();
        LOG.error(e.getMessage(), e);
        throw e;
    }

    @Override
    public Session getSessionByAlternativeId(String altId) throws OXException {
        for (String sessionId : sessions.keySet()) {
            StoredSession s = sessions.get(sessionId);
            if (s.containsParameter(Session.PARAM_ALTERNATIVE_ID) && s.getParameter(Session.PARAM_ALTERNATIVE_ID).equals(altId)) {
                return s;
            }
        }
        OXException e = OXSessionStorageExceptionCodes.SESSIONSTORAGE_SESSION_NOT_FOUND.create();
        LOG.error(e.getMessage(), e);
        throw e;
    }

    @Override
    public Session getCachedSession(String sessionId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cleanUp() throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void changePassword(String sessionId, String newPassword) throws OXException {
        StoredSession s = sessions.get(sessionId);
        if (s == null) {
            OXException e = OXSessionStorageExceptionCodes.SESSIONSTORAGE_SESSION_NOT_FOUND.create();
            LOG.error(e.getMessage(), e);
            throw e;
        }
        sessions.remove(sessionId);
        s.setPassword(newPassword);
        sessions.put(sessionId, s);
    }

    @Override
    public void checkAuthId(String login, String authId) throws OXException {
        // TODO Auto-generated method stub
        
    }

}
