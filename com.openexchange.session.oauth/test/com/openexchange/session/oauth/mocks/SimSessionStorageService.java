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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.session.SessionAttributes;
import com.openexchange.sessionstorage.SessionStorageExceptionCodes;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.StoredSession;


/**
 * {@link SimSessionStorageService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class SimSessionStorageService implements SessionStorageService {

    private final ConcurrentMap<String, StoredSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public Session lookupSession(String sessionId) throws OXException {
        return sessionMap.get(sessionId);
    }

    @Override
    public Session lookupSession(String sessionId, long timeoutMillis) throws OXException {
        return sessionMap.get(sessionId);
    }

    @Override
    public void addSession(Session session) throws OXException {
        sessionMap.put(session.getSessionID(), new StoredSession(session));
    }

    @Override
    public void addSessionsIfAbsent(Collection<Session> sessions) throws OXException {
        for (Session session : sessions) {
            sessionMap.putIfAbsent(session.getSessionID(), new StoredSession(session));
        }
    }

    @Override
    public boolean addSessionIfAbsent(Session session) throws OXException {
        Session existing = sessionMap.putIfAbsent(session.getSessionID(), new StoredSession(session));
        return existing == null;
    }

    @Override
    public void removeSession(String sessionId) throws OXException {
        sessionMap.remove(sessionId);
    }

    @Override
    public List<Session> removeSessions(List<String> sessionIds) throws OXException {
        List<Session> sessions = new LinkedList<Session>();
        for (String sessionId : sessionIds) {
            Session existing = sessionMap.remove(sessionId);
            if (existing != null) {
                sessions.add(existing);
            }
        }

        return sessions;
    }

    @Override
    public Session[] removeLocalUserSessions(int userId, int contextId) throws OXException {
        List<Session> sessions = removeByPredicate(s -> s.getUserId() == userId && s.getContextId() == contextId);
        return sessions.toArray(new Session[sessions.size()]);
    }

    @Override
    public Session[] removeUserSessions(int userId, int contextId) throws OXException {
        return removeLocalUserSessions(userId, contextId);
    }

    @Override
    public void removeLocalContextSessions(int contextId) throws OXException {
       removeByPredicate(s -> s.getContextId() == contextId);
    }

    @Override
    public void removeContextSessions(int contextId) throws OXException {
        removeLocalContextSessions(contextId);
    }

    @Override
    public boolean hasForContext(int contextId) throws OXException {
        return findByPredicate(s -> s.getContextId() == contextId).size() > 0;
    }

    @Override
    public Session[] getUserSessions(int userId, int contextId) throws OXException {
        List<Session> sessions = findByPredicate(s -> s.getUserId() == userId && s.getContextId() == contextId);
        return sessions.toArray(new Session[sessions.size()]);
    }

    @Override
    public Session getAnyActiveSessionForUser(int userId, int contextId) throws OXException {
        return findFirstSessionForUser(userId, contextId);
    }

    @Override
    public Session findFirstSessionForUser(int userId, int contextId) throws OXException {
        return findFirstByPredicate(s -> s.getUserId() == userId && s.getContextId() == contextId);
    }

    @Override
    public List<Session> getSessions() {
        return new ArrayList<>(sessionMap.values());
    }

    @Override
    public int getNumberOfActiveSessions() {
        return sessionMap.size();
    }

    @Override
    public Session getSessionByRandomToken(String randomToken, String newIP) throws OXException {
        return findFirstByPredicate(s -> randomToken.equals(s.getRandomToken()));
    }

    @Override
    public Session getSessionByAlternativeId(String altId) throws OXException {
        return findFirstByPredicate(s -> altId.equals(s.getSecret()));
    }

    @Override
    public Session getCachedSession(String sessionId) throws OXException {
        return sessionMap.get(sessionId);
    }

    @Override
    public void changePassword(String sessionId, String newPassword) throws OXException {
        alterIfPresent(sessionId, s -> s.setPassword(newPassword));
    }

    @Override
    public void checkAuthId(String login, String authId) throws OXException {
        Session session = findFirstByPredicate(s -> authId.equals(s.getAuthId()));
        if (session != null) {
            throw SessionStorageExceptionCodes.DUPLICATE_AUTHID.create(session.getLogin(), login);
        }
    }

    @Override
    public void cleanUp() throws OXException {
        sessionMap.clear();
    }

    @Override
    public int getUserSessionCount(int userId, int contextId) throws OXException {
        return findByPredicate(s -> s.getUserId() == userId && s.getContextId() == contextId).size();
    }

    private void alterIfPresent(String sessionId, Consumer<StoredSession> consumer) {
        Optional.ofNullable(sessionMap.get(sessionId)).ifPresent(consumer);
    }

    private List<Session> removeByPredicate(Predicate<Session> predicate) {
        List<Session> sessions = findByPredicate(predicate);
        for (Session session : sessions) {
            sessionMap.remove(session.getSessionID());
        }

        return sessions;
    }

    private List<Session> findByPredicate(Predicate<Session> predicate) {
        List<Session> sessions = sessionMap.values().stream()
            .filter(predicate)
            .collect(Collectors.toList());

        return sessions;
    }

    private Session findFirstByPredicate(Predicate<Session> predicate) {
        List<Session> sessions = findByPredicate(predicate);
        if (sessions.isEmpty()) {
            return null;
        }
        return sessions.get(0);
    }

    @Override
    public void setSessionAttributes(String sessionId, SessionAttributes attrs) throws OXException {
        alterIfPresent(sessionId, session -> {
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
        });
    }

}
