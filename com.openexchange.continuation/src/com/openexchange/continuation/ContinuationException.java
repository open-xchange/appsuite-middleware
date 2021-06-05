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

package com.openexchange.continuation;

import java.util.UUID;
import com.openexchange.exception.OXException;

/**
 * {@link ContinuationException} - Signals an error for Continuation module.
 * <p>
 * Extends {@link OXException} by {@link #getUuid()} method.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class ContinuationException extends OXException {

    private static final long serialVersionUID = -454163929764879301L;

    private final UUID uuid;

    /**
     * Initializes a new {@link ContinuationException}.
     *
     * @param uuid The UUID of associated continuation or <code>null</code>
     */
    public ContinuationException(final UUID uuid) {
        super();
        this.uuid = uuid;
    }

    /**
     * Initializes a new {@link ContinuationException}.
     *
     * @param uuid The UUID of associated continuation or <code>null</code>
     * @param cause The optional cause
     */
    public ContinuationException(final UUID uuid, Throwable cause) {
        super(cause);
        this.uuid = uuid;
    }

    /**
     * Initializes a new {@link ContinuationException}.
     *
     * @param uuid The UUID of associated continuation or <code>null</code>
     * @param code The error code number
     * @param displayMessage The display message
     * @param displayArgs The display message arguments
     */
    public ContinuationException(final UUID uuid, int code, String displayMessage, Object... displayArgs) {
        super(code, displayMessage, displayArgs);
        this.uuid = uuid;
    }

    /**
     * Initializes a new {@link ContinuationException}.
     *
     * @param uuid The UUID of associated continuation or <code>null</code>
     * @param code The error code number
     * @param displayMessage The display message
     * @param cause The optional cause
     * @param displayArgs The display message arguments
     */
    public ContinuationException(final UUID uuid, int code, String displayMessage, Throwable cause, Object... displayArgs) {
        super(code, displayMessage, cause, displayArgs);
        this.uuid = uuid;
    }

    /**
     * Gets the UUID
     *
     * @return The UUID or <code>null</code>
     */
    public UUID getUuid() {
        return uuid;
    }

}
