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

    /**
     * Checks whether the specified session is from a guest user
     *
     * @param session The session to check
     * @return <code>true</code> if it's a guest user; <code>false</code> otherwise
     */
    public static boolean isGuest(Session session) {
        return null != session && Boolean.TRUE.equals(session.getParameter(Session.PARAM_GUEST));
    }
}
