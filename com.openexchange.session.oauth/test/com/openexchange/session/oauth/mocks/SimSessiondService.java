/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.session.oauth.mocks;

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
import com.openexchange.session.SessionAttributes;
import com.openexchange.session.SimSession;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;

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

        List<SessionEnhancement> enhancements = param.getEnhancements();
        if (null != enhancements) {
            for (SessionEnhancement enhancement: enhancements) {
                enhancement.enhanceSession(session);
            }
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
            removeContextSessions(contextId.intValue());
        }
    }

    @Override
    public void removeUserSessionsGlobally(int userId, int contextId) throws OXException {
        removeUserSessions(userId, contextId);
    }

    @Override
    public Collection<String> removeSessions(SessionFilter filter) throws OXException {
        return filterSessionIds(filter, true);
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
    public Collection<Session> getSessions(int userId, int contextId, boolean considerSessionStorage) {
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
    public Session getSession(String sessionId, boolean considerSessionStorage) {
        return sessionsById.get(sessionId);
    }

    @Override
    public Session peekSession(String sessionId) {
        return sessionsById.get(sessionId);
    }

    @Override
    public Session peekSession(String sessionId, boolean considerSessionStorage) {
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
    public boolean storeSession(String sessionId) throws OXException {
        return true;
    }

    @Override
    public boolean storeSession(String sessionId, boolean addIfAbsent) throws OXException {
        return true;
    }

    @Override
    public Collection<String> findSessions(SessionFilter filter) {
        return filterSessionIds(filter, false);
    }

    @Override
    public void setSessionAttributes(String sessionId, SessionAttributes attrs) throws OXException {
        SimSession session = sessionsById.get(sessionId);
        if (session != null) {
            if (attrs.getLocalIp().isSet()) {
                session.setLocalIp(attrs.getLocalIp().get());
            }
            if (attrs.getClient().isSet()) {
                session.setClient(attrs.getClient().get());
            }
            if (attrs.getHash().isSet()) {
                session.setHash(attrs.getHash().get());
            }
            if (attrs.getUserAgent().isSet()) {
                session.setParameter(Session.PARAM_USER_AGENT, attrs.getUserAgent().get());
            }
        }
    }

}
