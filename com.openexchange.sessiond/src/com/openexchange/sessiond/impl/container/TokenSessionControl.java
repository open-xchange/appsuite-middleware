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

package com.openexchange.sessiond.impl.container;

import com.openexchange.sessiond.impl.SessionImpl;

/**
 * Stores the additional values necessary for a session created using the token login mechanism.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TokenSessionControl {

    private final SessionImpl session;
    private final String clientToken;
    private final String serverToken;
    private final long creationStamp;

    /**
     * Initializes a new {@link TokenSessionControl}.
     *
     * @param session The associated session
     * @param clientToken The client token
     * @param serverToken The server token
     */
    public TokenSessionControl(SessionImpl session, String clientToken, String serverToken, long creationStamp) {
        super();
        this.session = session;
        this.clientToken = clientToken;
        this.serverToken = serverToken;
        this.creationStamp = creationStamp;
    }

    /**
     * Gets the creation time in milliseconds
     *
     * @return The creation time in milliseconds
     */
    public long getCreationStamp() {
        return creationStamp;
    }

    /**
     * Gets the associated session
     *
     * @return The session
     */
    public SessionImpl getSession() {
        return session;
    }

    /**
     * Gets the client token
     *
     * @return The client token
     */
    public String getClientToken() {
        return clientToken;
    }

    /**
     * Gets the server token
     *
     * @return The server token
     */
    public String getServerToken() {
        return serverToken;
    }
}
