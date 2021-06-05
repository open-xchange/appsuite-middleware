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

package com.openexchange.websockets.grizzly;

import com.openexchange.session.Session;

/**
 * {@link SessionInfo} - Provides basic session information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SessionInfo {

    /**
     * Creates a new instance
     *
     * @param session The session to create for
     * @return The new instance
     */
    public static SessionInfo newInstance(Session session) {
        return new SessionInfo(session.getSessionID(), session.getUserId(), session.getContextId());
    }


    /**
     * Creates a new instance
     *
     * @param sessionId The session identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The new instance
     */
    public static SessionInfo newInstance(String sessionId, int userId, int contextId) {
        return new SessionInfo(sessionId, userId, contextId);
    }

    // ---------------------------------------------------------------------------------

    private final String sessionId;
    private final int userId;
    private final int contextId;

    /**
     * Initializes a new {@link SessionInfo}.
     */
    private SessionInfo(String sessionId, int userId, int contextId) {
        super();
        this.sessionId = sessionId;
        this.userId = userId;
        this.contextId = contextId;
    }

    /**
     * Gets the session identifier
     *
     * @return The session identifier
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("{");
        if (sessionId != null) {
            builder.append("sessionId=").append(sessionId).append(", ");
        }
        builder.append("userId=").append(userId).append(", contextId=").append(contextId).append("}");
        return builder.toString();
    }

}
