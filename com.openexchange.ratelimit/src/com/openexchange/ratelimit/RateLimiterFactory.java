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

package com.openexchange.ratelimit;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link RateLimiterFactory} is a factory for {@link RateLimiter}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
@SingletonService
public interface RateLimiterFactory {

    /**
     * Creates a {@link RateLimiter} with the specified {@link Rate} for the given user and context.
     *
     * @param id The identifier of the {@link RateLimiter}
     * @param rate The rate of the {@link RateLimiter}
     * @param userId The user identifier
     * @param ctxId The context identifier
     * @return The {@link RateLimiter} instance
     * @throws OXException If such a rate limiter cannot be returned
     */
    RateLimiter createLimiter(String id, Rate rate, int userId, int ctxId) throws OXException;

}
