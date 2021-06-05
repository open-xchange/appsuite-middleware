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

package com.openexchange.tools.servlet.ratelimit;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link RateLimitedException} - Thrown if associated request is rate limited.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RateLimitedException extends RuntimeException {

    private static final long serialVersionUID = 5342199025241682441L;
    private static final int SC_TOO_MANY_REQUESTS = 429;

    private final int retryAfterSeconds;

    /**
     * Initializes a new {@link RateLimitedException}.
     *
     * @param message The message
     * @param retryAfterSeconds The time in seconds to wait before retrying
     */
    public RateLimitedException(final String message, final int retryAfterSeconds) {
        this(message, retryAfterSeconds, null);
    }

    /**
     * Initializes a new {@link RateLimitedException}.
     *
     * @param message The message
     * @param retryAfterSeconds The time in seconds to wait before retrying
     * @param cause The cause
     */
    public RateLimitedException(final String message, final int retryAfterSeconds, Throwable cause) {
        super(message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * Gets the time in seconds to wait before retrying
     *
     * @return The time in seconds to wait before retrying
     */
    public int getRetryAfter() {
        return retryAfterSeconds;
    }

    /**
     * Sends an <code>HTTP 429</code> error response to the client based on this rate limited exception. The advised retry interval is
     * put into an appropriate <code>Retry-After</code> header.
     *
     * @param response The response to use for sending the error
     */
    public void send(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        if (0 < retryAfterSeconds) {
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        }
        response.sendError(SC_TOO_MANY_REQUESTS, "Too Many Requests - Your request is being rate limited.");
    }

}
