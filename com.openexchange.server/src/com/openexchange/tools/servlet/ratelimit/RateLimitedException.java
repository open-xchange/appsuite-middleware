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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
