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


/**
 * {@link RateLimiter} provides methods to do rate limiting. <code>RateLimiter</code> instances are created via a {@link RateLimiterFactory}.
 * <p>
 * Every <code>RateLimiter</code> provides a given number of permits for a specific user and context in a given time-frame.
 * By calling {@link #acquire()} or {@link #acquire(long)} it is tried to reduce this number of permits by one or by the given number.
 * If it is successful the amount is reduced and the method returns <code>true</code>. If the operation would reduce the amount of permits
 * to a value equal to or smaller than <code>0</code> (zero) then the amount is not reduced and <code>false</code> is returned instead.
 * <p>
 * The caller is then responsible to handle it appropriately. E.g. by throwing an error or by waiting a specified amount of time and try
 * again.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public interface RateLimiter {

    /**
     * Tries to acquire a single permit. E.g. for sending a single message via a transport service like SMS.
     *
     * @return <code>false</code> in case the maximum amount of permits is reached; otherwise <code>true</code>
     */
    boolean acquire();

    /**
     * Tries to acquire given number of permits. E.g. the number of bytes for a download limit.
     *
     * @param permits The number of permits to acquire
     * @return <code>false</code> in case the maximum amount of permits is reached; otherwise <code>true</code>
     */
    boolean acquire(long permits);

    /**
     * Returns true if already at limit
     * 
     * @return true if it is exceeded, false otherwise
     */
    boolean exceeded();

    /**
     * Removes all previous permits for the user and id associated with this rateLimiter
     */
    void reset();

}
