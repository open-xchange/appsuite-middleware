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

import java.util.Collection;
import java.util.Collections;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.cache.SessionCache;

/**
 * {@link InvalidatedAwareSessiondService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InvalidatedAwareSessiondService implements SessiondService {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(InvalidatedAwareSessiondService.class));

    private final SessiondServiceImpl impl;

    /**
     * Initializes a new {@link InvalidatedAwareSessiondService}.
     */
    public InvalidatedAwareSessiondService(final SessiondServiceImpl impl) {
        super();
        this.impl = impl;
    }

    private boolean checkInvalidatedAndRemoveIfPresent(final int contextId) {
        try {
            if (SessionCache.getInstance().containsInvalidateMarker(contextId)) {
                // Drops sessions belonging to context
                SessionHandler.removeContextSessions(contextId, true);
                // Return null
                LOG.info("Context is marked as invalid: " + contextId + ". Removed all context-related sessions.");
                return true;
            }
            return false;
        } catch (final Exception e) {
            // Ignore
            return false;
        }
    }

    @Override
    public int hashCode() {
        return impl.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return impl.equals(obj);
    }

    @Override
    public String addSession(final AddSessionParameter param) throws OXException {
        checkInvalidatedAndRemoveIfPresent(param.getContext().getContextId());
        return impl.addSession(param);
    }

    @Override
    public void changeSessionPassword(final String sessionId, final String newPassword) throws OXException {
        impl.getSession(sessionId); // Invoked to implicitly check for invalidated-marker
        impl.changeSessionPassword(sessionId, newPassword);
    }

    @Override
    public boolean removeSession(final String sessionId) {
        impl.getSession(sessionId); // Invoked to implicitly check for invalidated-marker
        return impl.removeSession(sessionId);
    }

    @Override
    public int removeUserSessions(final int userId, final Context ctx) {
        if (checkInvalidatedAndRemoveIfPresent(ctx.getContextId())) {
            // Obviously no user-related session left
            return 0;
        }
        return impl.removeUserSessions(userId, ctx);
    }

    @Override
    public void removeContextSessions(final int contextId) {
        impl.removeContextSessions(contextId);
    }

    @Override
    public int getUserSessions(final int userId, final int contextId) {
        if (checkInvalidatedAndRemoveIfPresent(contextId)) {
            // Obviously no user-related session left
            return 0;
        }
        return impl.getUserSessions(userId, contextId);
    }

    @Override
    public Session getAnyActiveSessionForUser(final int userId, final int contextId) {
        if (checkInvalidatedAndRemoveIfPresent(contextId)) {
            // Obviously no user-related session left
            return null;
        }
        return impl.getAnyActiveSessionForUser(userId, contextId);
    }

    @Override
    public Session findFirstMatchingSessionForUser(final int userId, final int contextId, final SessionMatcher matcher) {
        if (checkInvalidatedAndRemoveIfPresent(contextId)) {
            // Obviously no session exists for that user anymore
            return null;
        }
        return impl.findFirstMatchingSessionForUser(userId, contextId, matcher);
    }

    @Override
    public Collection<Session> getSessions(final int userId, final int contextId) {
        if (checkInvalidatedAndRemoveIfPresent(contextId)) {
            // Obviously no session exists for that user anymore
            return Collections.emptyList();
        }
        return impl.getSessions(userId, contextId);
    }

    @Override
    public Session getSession(final String sessionId) {
        final Session session = impl.getSession(sessionId);
        if (null == session || checkInvalidatedAndRemoveIfPresent(session.getContextId())) {
            // Obviously no session exists anymore
            return null;
        }
        return session;
    }

    @Override
    public Session getSessionByAlternativeId(final String altId) {
        final Session session = impl.getSessionByAlternativeId(altId);
        if (null == session || checkInvalidatedAndRemoveIfPresent(session.getContextId())) {
            // Obviously no session exists anymore
            return null;
        }
        return session;
    }

    @Override
    public Session getSessionByRandomToken(final String randomToken, final String localIp) {
        final Session session = impl.getSessionByRandomToken(randomToken, localIp);
        if (null == session || checkInvalidatedAndRemoveIfPresent(session.getContextId())) {
            // Obviously no session exists anymore
            return null;
        }
        return session;
    }

    @Override
    public Session getSessionByRandomToken(final String randomToken) {
        final Session session = impl.getSessionByRandomToken(randomToken);
        if (null == session || checkInvalidatedAndRemoveIfPresent(session.getContextId())) {
            // Obviously no session exists anymore
            return null;
        }
        return session;
    }

    @Override
    public int getNumberOfActiveSessions() {
        // Not possible to check for invalidated-marker in this method
        return impl.getNumberOfActiveSessions();
    }

    @Override
    public String toString() {
        return InvalidatedAwareSessiondService.class.getName() + "," + impl.toString();
    }
}
