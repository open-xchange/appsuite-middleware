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


/**
 * {@link NoSuchSessionHandshakeException} - The special error during Web Socket handshake phase signaling that a certain session could not be found.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class NoSuchSessionHandshakeException extends SessionValidationHandshakeException {

    private static final long serialVersionUID = -3886129451110147910L;

    private final String sessionId;

    /**
     * Initializes a new {@link NoSuchSessionHandshakeException} carrying a <code>400</code> (Bad Request) error code.
     *
     * @param sessionId The identifier of the session that could not be found
     */
    public NoSuchSessionHandshakeException(String sessionId) {
        super("No such session: " + sessionId);
        this.sessionId = sessionId;
    }

    /**
     * Initializes a new {@link NoSuchSessionHandshakeException}.
     *
     * @param code The HTTP error code associated with this exception
     * @param sessionId The identifier of the session that could not be found
     */
    public NoSuchSessionHandshakeException(int code, String sessionId) {
        super(code, "No such session: " + sessionId);
        this.sessionId = sessionId;
    }

    /**
     * Gets the identifier of the session that could not be found
     *
     * @return The identifier of the session that could not be found
     */
    public String getSessionId() {
        return sessionId;
    }

}
