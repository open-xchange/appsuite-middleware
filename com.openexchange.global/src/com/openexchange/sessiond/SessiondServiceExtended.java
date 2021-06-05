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
import java.util.List;
import com.openexchange.session.Session;

/**
 * {@link SessiondServiceExtended} - The extended {@link SessiondService SessionD service}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessiondServiceExtended extends SessiondService {

    /**
     * Checks for any active session for specified context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if at least one active session is found; otherwise <code>false</code>
     */
    boolean hasForContext(final int contextId);

    /**
     * Checks if denoted session is <code>locally</code> available and located in short-term container.
     *
     * @param sessionId The session identifier
     * @return <code>true</code> if <code>locally</code> active; otherwise <code>false</code>
     */
    boolean isActive(String sessionId);

    /**
     * Gets a list of <i>active</i> sessions, i.e. those sessions that are <code>locally</code> available and located in one of the
     * short-term containers.
     *
     * @return The identifiers of all active sessions in a list
     */
    List<String> getActiveSessionIDs();

    /**
     * Checks if specified session is applicable for session storage.
     *
     * @param session The session to check
     * @return <code>true</code> if applicable for session storage; otherwise <code>false</code>
     */
    boolean isApplicableForSessionStorage(Session session);

    /**
     * Gets the <b>local-only</b> active (short-term-only) sessions associated with specified user in given context.
     * <p>
     * <b>Note</b>: Remote sessions are not considered by this method.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The <b>local-only</b> active sessions associated with specified user in given context
     */
    Collection<Session> getActiveSessions(int userId, int contextId);

}
