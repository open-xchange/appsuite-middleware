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

package com.openexchange.message.timeline.util;

import java.io.IOException;

/**
 * {@link LimitExceededIOException}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class LimitExceededIOException extends IOException {

    private static final long serialVersionUID = 4303119149782966974L;

    /**
     * Initializes a new {@link LimitExceededIOException}.
     */
    public LimitExceededIOException() {
        super();
    }

    /**
     * Initializes a new {@link LimitExceededIOException}.
     *
     * @param message The error message
     */
    public LimitExceededIOException(final String message) {
        super(message);
    }

    /**
     * Initializes a new {@link LimitExceededIOException}.
     *
     * @param cause The cause
     */
    public LimitExceededIOException(final Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link LimitExceededIOException}.
     *
     * @param message The error message
     * @param cause The cause
     */
    public LimitExceededIOException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
