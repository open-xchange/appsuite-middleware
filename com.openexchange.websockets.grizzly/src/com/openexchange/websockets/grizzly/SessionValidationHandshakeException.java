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

import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.websockets.HandshakeException;


/**
 * {@link SessionValidationHandshakeException} - The special error during Web Socket handshake phase signaling that session validation failed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SessionValidationHandshakeException extends HandshakeException {

    private static final long serialVersionUID = -3924716445851104179L;

    /**
     * Initializes a new {@link SessionValidationHandshakeException} carrying a <code>403</code> (Forbidden) error code.
     *
     * @param message The detail message describing why session validation failed
     */
    public SessionValidationHandshakeException(String message) {
        super(HttpStatus.FORBIDDEN_403.getStatusCode(), message);
    }

    /**
     * Initializes a new {@link SessionValidationHandshakeException}.
     *
     * @param code The HTTP error code associated with this session validation exception
     * @param message The detail message describing why session validation failed
     */
    public SessionValidationHandshakeException(int code, String message) {
        super(code, message);
    }

}
