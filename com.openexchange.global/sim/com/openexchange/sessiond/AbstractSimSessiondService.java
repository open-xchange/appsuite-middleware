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
import java.util.Collections;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

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
    public void changeSessionPassword(final String sessionId, final String newPassword) {
        // Nothing to do
    }

    @Override
    public void setClient(String sessionId, String client) throws OXException {
        // Nothing to do
    }

    @Override
    public void setHash(String sessionId, String hash) throws OXException {
        // Nothing to do
    }

    @Override
    public void setLocalIp(String sessionId, String localIp) throws OXException {
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
    public Session getAnyActiveSessionForUser(final int userId, final int contextId) {
        return null;
    }

    @Override
    public Session findFirstMatchingSessionForUser(final int userId, final int contextId, final SessionMatcher matcher) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.sessiond.SessiondService#removeContextSessions(int)
     */
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
    public Collection<String> removeSessionsGlobally(SessionFilter filter) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> findSessions(SessionFilter filter) throws OXException {
        return Collections.emptyList();
    }

}
