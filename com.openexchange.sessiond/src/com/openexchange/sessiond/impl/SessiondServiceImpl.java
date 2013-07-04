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

package com.openexchange.sessiond.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.log.LogFactory;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.session.Session;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;

/**
 * {@link SessiondServiceImpl} - Implementation of {@link SessiondService}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessiondServiceImpl implements SessiondServiceExtended {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessiondServiceImpl.class));

    private final Lock migrateLock;

    /**
     * Initializes a new {@link SessiondServiceImpl}.
     */
    public SessiondServiceImpl() {
        super();
        migrateLock = new ReentrantLock();
    }

    @Override
    public boolean hasForContext(final int contextId) {
        return SessionHandler.hasForContext(contextId, false);
    }

    @Override
    public Session addSession(final AddSessionParameter param) throws OXException {
        return SessionHandler.addSession(
            param.getUserId(),
            param.getUserLoginInfo(),
            param.getPassword(),
            param.getContext().getContextId(),
            param.getClientIP(),
            param.getFullLogin(),
            param.getAuthId(),
            param.getHash(),
            param.getClient(),
            param.getClientToken(),
            param.isTransient());
    }

    @Override
    public void changeSessionPassword(final String sessionId, final String newPassword) throws OXException {
        SessionHandler.changeSessionPassword(sessionId, newPassword);
    }

    @Override
    public void setClient(final String sessionId, final String client) throws OXException {
        SessionHandler.setClient(getSession(sessionId), client);
    }

    @Override
    public void setHash(final String sessionId, final String hash) throws OXException {
        SessionHandler.setHash(getSession(sessionId), hash);
    }

    @Override
    public void setLocalIp(final String sessionId, final String localIp) throws OXException {
        SessionHandler.setLocalIp(getSession(sessionId), localIp);
    }

    @Override
    public boolean removeSession(final String sessionId) {
        return SessionHandler.clearSession(sessionId);
    }

    @Override
    public int removeUserSessions(final int userId, final Context ctx) {
        return SessionHandler.removeUserSessions(userId, ctx.getContextId()).length;
    }

    @Override
    public void removeContextSessions(final int contextId) {
        SessionHandler.removeContextSessions(contextId);
    }

    @Override
    public int getUserSessions(final int userId, final int contextId) {
        return SessionHandler.SESSION_COUNTER.getNumberOfSessions(userId, contextId);
    }

    @Override
    public Collection<Session> getSessions(final int userId, final int contextId) {
        final SessionControl[] sessionControls = SessionHandler.getUserSessions(userId, contextId);
        if (null == sessionControls || 0 == sessionControls.length) {
            return Collections.emptyList();
        }
        final int length = sessionControls.length;
        final List<Session> list = new ArrayList<Session>(length);
        for (int i = 0; i < length; i++) {
            list.add(sessionControls[i].getSession());
        }
        return list;
    }

    @Override
    public SessionImpl getSession(final String sessionId) {
        SessionControl sessionControl = SessionHandler.getSession(sessionId);
        if (null == sessionControl) {
            // No local session found. Maybe it should be migrated.
            // Look for a cached session must be serialized. Multiple threads can reach simultaneously this code part. The first one
            // migrates the session and the second one will not find a session in the cache.
            migrateLock.lock();
            try {
                // First look again locally. Maybe another thread already migrated the session while this one waits on the lock.
                sessionControl = SessionHandler.getSession(sessionId);
                if (null == sessionControl) {
                    // Migrate session.
                    sessionControl = SessionHandler.getCachedSession(sessionId);
                }
            } finally {
                migrateLock.unlock();
            }
        }
        if (null == sessionControl) {
            LOG.info("Session not found. ID: " + sessionId);
            return null;
        }
        return sessionControl.touch().getSession();
    }

    @Override
    public Session getSessionByAlternativeId(final String altId) {
        if (null == altId) {
            return null;
        }
        final SessionControl sessionControl = SessionHandler.getSessionByAlternativeId(altId);
        if (null == sessionControl) {
            LOG.info("Session not found by alternative identifier. Alternative ID: " + altId);
            return null;
        }
        return sessionControl.touch().getSession();
    }

    @Override
    public Session getSessionByRandomToken(final String randomToken, final String localIp) {
        return SessionHandler.getSessionByRandomToken(randomToken, localIp);
    }

    @Override
    public Session getSessionByRandomToken(final String randomToken) {
        return SessionHandler.getSessionByRandomToken(randomToken, null);
    }

    @Override
    public Session getSessionWithTokens(final String clientToken, final String serverToken) throws OXException {
        return SessionHandler.getSessionWithTokens(clientToken, serverToken);
    }

    @Override
    public int getNumberOfActiveSessions() {
        return SessionHandler.getNumberOfActiveSessions();
    }

    @Override
    public Session getAnyActiveSessionForUser(final int userId, final int contextId) {
        final Props logProperties = LogProperties.optLogProperties();
        if (null != logProperties) {
            final Session session = logProperties.get(LogProperties.Name.SESSION_SESSION);
            if (null != session && userId == session.getUserId() && contextId == session.getContextId()) {
                return session;
            }
        }
        final SessionControl sessionControl = SessionHandler.getAnyActiveSessionForUser(userId, contextId, false, false);
        return null == sessionControl ? null: sessionControl.getSession();
    }

    @Override
    public Session findFirstMatchingSessionForUser(final int userId, final int contextId, final SessionMatcher matcher) {
        if (null == matcher) {
            return null;
        }
        final Set<SessionMatcher.Flag> flags = matcher.flags();
        return SessionHandler.findFirstSessionForUser(userId, contextId, matcher, flags.contains(SessionMatcher.Flag.IGNORE_LONG_TERM), flags.contains(SessionMatcher.Flag.IGNORE_SESSION_STORAGE));
    }

}
