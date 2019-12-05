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

package com.openexchange.session;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import com.openexchange.framework.request.RequestContext;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.log.LogProperties;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;


/**
 * {@link Sessions} - Utility class for session handling.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public final class Sessions {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Sessions.class);
    }

    /**
     * Initializes a new {@link Sessions}.
     */
    private Sessions() {
        super();
    }

    /**
     * Gets the optional sessions associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional sessions; <code>Optional</code> provides a value if and only if wrapped <code>Collection</code> is <b>not</b> empty
     */
    public static Optional<Collection<String>> getSessionsOfUser(int userId, int contextId) {
        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        return getSessionsOfUser(userId, contextId, sessiondService);
    }

    /**
     * Gets the optional sessions associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param sessiondService The SessionD service reference to use
     * @return The optional sessions; <code>Optional</code> provides a value if and only if wrapped <code>Collection</code> is <b>not</b> empty
     */
    public static Optional<Collection<String>> getSessionsOfUser(int userId, int contextId, SessiondService sessiondService) {
        if (sessiondService == null) {
            return Optional.empty();
        }

        SessionFilter sessionFilter;
        {
            StringBuilder sb = new StringBuilder(32);
            sb.append("(&");
            sb.append('(').append(SessionFilter.CONTEXT_ID).append('=').append(contextId).append(')');
            sb.append('(').append(SessionFilter.USER_ID).append('=').append(userId).append(')');
            sb.append(')');
            sessionFilter = SessionFilter.create(sb.toString());
            sb = null;
        }

        Collection<String> foundSessions;
        try {
            foundSessions = sessiondService.findSessions(sessionFilter);
        } catch (Exception e) {
            LoggerHolder.LOG.debug("Failed to find sessions for user {} in context {}", I(userId), I(contextId), e);
            foundSessions = java.util.Collections.emptyList();
        }
        return foundSessions.isEmpty() ? Optional.empty() : Optional.of(foundSessions);
    }

    /**
     * Gets the optional session for current thread and validates (if available) that it is associated with given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional validated session
     */
    public static Optional<Session> getValidatedSessionForCurrentThread(int userId, int contextId) {
        Optional<Session> optionalSession = getSessionForCurrentThread();
        if (!optionalSession.isPresent()) {
            return optionalSession;
        }

        Session session = optionalSession.get();
        return userId == session.getUserId() && contextId == session.getContextId() ? optionalSession : Optional.empty();
    }

    /**
     * Gets the optional session for current thread.
     *
     * @return The optional session
     */
    public static Optional<Session> getSessionForCurrentThread() {
        Session session = ThreadLocalSessionHolder.getInstance().getSessionObject();
        if (null != session) {
            return Optional.of(session);
        }

        RequestContext requestContext = RequestContextHolder.get();
        if (requestContext != null) {
            session = requestContext.getSession();
            if (null != session) {
                return Optional.of(session);
            }
        }

        String sessionId = LogProperties.getLogProperty(LogProperties.Name.SESSION_SESSION_ID);
        if (sessionId == null) {
            return Optional.empty();
        }

        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (sessiondService == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(sessiondService.getSession(sessionId));
    }

    /**
     * Gets the optional lock for specified session.
     *
     * @param session The session
     * @return The lock; either session's lock or a dummy lock instance
     */
    public static Lock optLock(Session session) {
        if (null == session) {
            return null;
        }
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        return null == lock ? Session.EMPTY_LOCK : lock;
    }

    /**
     * Tests if specified session is annotated to be an OAuth session; means it is marked as being created through
     * <a href="https://documentation.open-xchange.com/7.8.4/middleware/components/oauth_provider/developer_guide.html">Open-Xchange Middleware OAuth authentication flow</a>.
     *
     * @param session The session to check
     * @return <code>true</code> if session was initialized through OAuth provider; otherwise <code>false</code>
     */
    public static boolean isOAuthSession(Session session) {
        Object obj = session.getParameter(Session.PARAM_IS_OAUTH);
        if (null == obj) {
            return false;
        }

        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }

        return "true".equalsIgnoreCase(obj.toString().trim());
    }

}
