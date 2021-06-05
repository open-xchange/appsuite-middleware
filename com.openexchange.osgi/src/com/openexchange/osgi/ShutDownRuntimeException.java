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

package com.openexchange.osgi;

/**
 * {@link ShutDownRuntimeException} - Thrown if a shut-down has been initiated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ShutDownRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -8818954753984880228L;

    /**
     * Throws a <code>ShutDownRuntimeException</code> if specified service reference is <code>null</code>
     *
     * @param service The service to check
     */
    public static <S> void throwShutDownRuntimeExceptionIfNull(S service) {
        if (null == service) {
            throw new ShutDownRuntimeException();
        }
    }

    /**
     * Initializes a new {@link ShutDownRuntimeException} with default message <code>"The server is about to shut-down"</code>.
     */
    public ShutDownRuntimeException() {
        super("The server is about to shut-down");
    }

    /**
     * Initializes a new {@link ShutDownRuntimeException}.
     *
     * @param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public ShutDownRuntimeException(String message) {
        super(message);
    }

    /**
     * Initializes a new {@link ShutDownRuntimeException}.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ShutDownRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link ShutDownRuntimeException}.
     *
     * @param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ShutDownRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
