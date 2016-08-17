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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;


/**
 * {@link SimSessiondService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SimSessiondService implements SessiondService {

    private final ConcurrentMap<String, SimSession> sessionsById = new ConcurrentHashMap<String, SimSession>();

    @Override
    public Session addSession(AddSessionParameter param) throws OXException {
        SimSession session = new SimSession(param.getUserId(), param.getContext().getContextId());
        session.setClient(param.getClient());
        session.setHash(param.getHash());
        session.setLoginName(param.getFullLogin());
        session.setPassword(param.getPassword());
        session.setLocalIp(param.getClientIP());
        session.setSecret(UUIDs.getUnformattedString(UUID.randomUUID()));
        Session existing = null;
        do {
            session.setSessionID(UUIDs.getUnformattedString(UUID.randomUUID()));
            existing = sessionsById.putIfAbsent(session.getSessionID(), session);
        } while (existing != null);

        SessionEnhancement enhancement = param.getEnhancement();
        if (null != enhancement) {
            enhancement.enhanceSession(session);
        }
        return session;
    }

    @Override
    public void changeSessionPassword(String sessionId, String newPassword) throws OXException {
        SimSession session = sessionsById.get(sessionId);
        if (session == null) {
            throw SessionExceptionCodes.PASSWORD_UPDATE_FAILED.create();
        }

        session.setPassword(newPassword);
    }

    @Override
    public boolean removeSession(String sessionId) {
        return sessionsById.remove(sessionId) != null;
    }

    @Override
    public int removeUserSessions(int userId, Context ctx) {
        return removeUserSessions(userId, ctx.getContextId());
    }

    @Override
    public void removeContextSessions(int contextId) {
        SessionFilter filter = SessionFilter.create("(" + SessionFilter.CONTEXT_ID + "=" + contextId + ")");
        filterSessionIds(filter, true);
    }

    @Override
    public void removeContextSessionsGlobal(Set<Integer> contextIds) throws OXException {
        for (Integer contextId : contextIds) {
            removeContextSessions(contextId);
        }
    }

    @Override
    public void removeUserSessionsGlobally(int userId, int contextId) throws OXException {
        removeUserSessions(userId, contextId);
    }

    @Override
    public Collection<String> removeSessionsGlobally(SessionFilter filter) throws OXException {
        return filterSessionIds(filter, true);
    }

    private int removeUserSessions(int userId, int contextId) {
        SessionFilter filter = SessionFilter.create("(&(" + SessionFilter.USER_ID + "=" + userId + ")(" + SessionFilter.CONTEXT_ID + "=" + contextId + "))");
        List<String> removed = filterSessionIds(filter, true);
        return removed.size();
    }

    private List<String> filterSessionIds(SessionFilter filter, boolean remove) {
        List<String> sessions = new LinkedList<String>();
        Iterator<SimSession> iterator = sessionsById.values().iterator();
        while (iterator.hasNext()) {
            SimSession session = iterator.next();
            if (filter.apply(session)) {
                if (remove) {
                    iterator.remove();
                }
                sessions.add(session.getSessionID());
            }
        }

        return sessions;
    }

    private List<Session> filterSessions(SessionFilter filter, boolean remove) {
        List<Session> sessions = new LinkedList<Session>();
        Iterator<SimSession> iterator = sessionsById.values().iterator();
        while (iterator.hasNext()) {
            SimSession session = iterator.next();
            if (filter.apply(session)) {
                if (remove) {
                    iterator.remove();
                }
                sessions.add(session);
            }
        }

        return sessions;
    }

    @Override
    public int getUserSessions(int userId, int contextId) {
        SessionFilter filter = SessionFilter.create("(&(" + SessionFilter.USER_ID + "=" + userId + ")(" + SessionFilter.CONTEXT_ID + "=" + contextId + "))");
        List<String> sessionIds = filterSessionIds(filter, false);
        return sessionIds.size();
    }

    @Override
    public Collection<Session> getSessions(int userId, int contextId) {
        SessionFilter filter = SessionFilter.create("(&(" + SessionFilter.USER_ID + "=" + userId + ")(" + SessionFilter.CONTEXT_ID + "=" + contextId + "))");
        return filterSessions(filter, false);
    }

    @Override
    public Session findFirstMatchingSessionForUser(int userId, int contextId, SessionMatcher matcher) {
        SessionFilter filter = SessionFilter.create("(&(" + SessionFilter.USER_ID + "=" + userId + ")(" + SessionFilter.CONTEXT_ID + "=" + contextId + "))");
        List<Session> userSessions = filterSessions(filter, false);
        for (Session session : userSessions) {
            if (matcher.accepts(session)) {
                return session;
            }
        }

        return null;
    }

    @Override
    public Session getSession(String sessionId) {
        return sessionsById.get(sessionId);
    }

    @Override
    public Session getSessionByAlternativeId(String altId) {
        SessionFilter filter = SessionFilter.create("(" + Session.PARAM_ALTERNATIVE_ID + "=" + altId + ")");
        List<Session> sessions = filterSessions(filter, false);
        if (sessions.isEmpty()) {
            return null;
        }

        return sessions.get(0);
    }

    @Override
    public Session getSessionByAlternativeId(String altId, boolean lookupSessionStorage) {
        return getSessionByAlternativeId(altId);
    }

    @Override
    public Session getSessionByRandomToken(String randomToken, String localIp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Session getSessionByRandomToken(String randomToken) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Session getSessionWithTokens(String clientToken, String serverToken) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> findSessionsGlobally(SessionFilter filter) throws OXException {
        return filterSessionIds(filter, false);
    }

    @Override
    public int getNumberOfActiveSessions() {
        return sessionsById.size();
    }

    @Override
    public Session getAnyActiveSessionForUser(int userId, int contextId) {
        SessionFilter filter = SessionFilter.create("(&(" + SessionFilter.USER_ID + "=" + userId + ")(" + SessionFilter.CONTEXT_ID + "=" + contextId + "))");
        List<Session> sessions = filterSessions(filter, false);
        if (sessions.isEmpty()) {
            return null;
        }

        return sessions.get(0);
    }

    @Override
    public void setLocalIp(String sessionId, String localIp) throws OXException {
        SimSession session = sessionsById.get(sessionId);
        if (session != null) {
            session.setLocalIp(localIp);
        }
    }

    @Override
    public void setClient(String sessionId, String client) throws OXException {
        SimSession session = sessionsById.get(sessionId);
        if (session != null) {
            session.setClient(client);
        }
    }

    @Override
    public void setHash(String sessionId, String hash) throws OXException {
        SimSession session = sessionsById.get(sessionId);
        if (session != null) {
            session.setHash(hash);
        }
    }

    @Override
    public boolean storeSession(String sessionId) throws OXException {
        // Nothing to do.
        return true;
    }

    @Override
    public Collection<String> findSessions(SessionFilter filter) {
        return filterSessionIds(filter, false);
    }
}
