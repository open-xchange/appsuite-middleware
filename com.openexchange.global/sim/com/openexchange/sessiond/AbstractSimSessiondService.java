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

package com.openexchange.sessiond;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.session.SessionAttributes;

/**
 * {@link AbstractSimSessiondService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AbstractSimSessiondService implements SessiondService {

    @Override
    public Session addSession(final AddSessionParameter parameterObject) {
        return null;
    }

    @Override
    public boolean storeSession(String sessionId) throws OXException {
        return false;
    }

    @Override
    public boolean storeSession(String sessionId, boolean addIfAbsent) throws OXException {
        return false;
    }

    @Override
    public void changeSessionPassword(final String sessionId, final String newPassword) {
        // Nothing to do
    }

    @Override
    public void setSessionAttributes(String sessionId, SessionAttributes attrs) throws OXException {
        // Nothing to do
    }

    @Override
    public int getNumberOfActiveSessions() {
        return 0;
    }

    @Override
    public Session getSession(final String sessionId) {
        return null;
    }

    @Override
    public Session getSession(String sessionId, boolean considerSessionStorage) {
        return null;
    }

    @Override
    public Session peekSession(String sessionId) {
        return null;
    }

    @Override
    public Session peekSession(String sessionId, boolean considerSessionStorage) {
        return null;
    }

    @Override
    public Session getSessionByAlternativeId(final String altId) {
        return getSessionByAlternativeId(altId, false);
    }

    @Override
    public Session getSessionByAlternativeId(String altId, boolean lookupSessionStorage) {
        return null;
    }

    @Override
    public Session getSessionByRandomToken(final String randomToken, final String localIp) {
        return null;
    }

    @Override
    public Session getSessionByRandomToken(final String randomToken) {
        return null;
    }

    @Override
    public Session getSessionWithTokens(String clientToken, String serverToken) throws OXException {
        throw SessionExceptionCodes.NOT_IMPLEMENTED.create();
    }

    @Override
    public int getUserSessions(final int userId, final int contextId) {
        return 0;
    }

    @Override
    public boolean removeSession(final String sessionId) {
        return false;
    }

    @Override
    public int removeUserSessions(final int userId, final Context ctx) {
        return 0;
    }

    @Override
    public void removeUserSessionsGlobally(int userId, int contextId) throws OXException {
        // Nope
    }

    @Override
    public Collection<Session> getSessions(final int userId, final int contextId) {
        return null;
    }

    @Override
    public Collection<Session> getSessions(int userId, int contextId, boolean considerSessionStorage) {
        return null;
    }

    @Override
    public Session getAnyActiveSessionForUser(final int userId, final int contextId) {
        return null;
    }

    @Override
    public Session findFirstMatchingSessionForUser(final int userId, final int contextId, final SessionMatcher matcher) {
        return null;
    }

    @Override
    public void removeContextSessions(final int contextId) {
        // Nothing to do
    }

    @Override
    public void removeContextSessionsGlobal(Set<Integer> contextIds) {
        // Nothing to do
    }

    @Override
    public Collection<String> findSessionsGlobally(SessionFilter filter) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> removeSessions(SessionFilter filter) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> removeSessionsGlobally(SessionFilter filter) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> findSessions(SessionFilter filter) throws OXException {
        return Collections.emptyList();
    }

}
