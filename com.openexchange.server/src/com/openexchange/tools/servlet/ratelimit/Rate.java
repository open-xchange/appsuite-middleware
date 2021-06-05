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

import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link Rate} - Represents a rate information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Rate {

    /**
     * The rate result.
     */
    public static enum Result {
        SUCCESS, FAILED, DEPRECATED;
    }

    /**
     * Gets the lastLogStamp
     *
     * @return The lastLogStamp
     */
    AtomicLong getLastLogStamp();

    /**
     * Gets this rate's last-accessed time stamp.
     *
     * @return The last-accessed time stamp
     */
    long lastAccessTime();

    /**
     * Checks if this rate is deprecated
     *
     * @return <code>true</code> if deprecated; otherwise <code>false</code>
     * @see #markDeprecatedIfElapsed(long)
     */
    boolean isDeprecated();

    /**
     * Marks this rate as deprecated if elapsed in comparison to given threshold.
     *
     * @param threshold The threshold
     * @return <code>true</code> if elapsed (and marked as deprecated); otherwise <code>false</code>
     */
    boolean markDeprecatedIfElapsed(long threshold);

    /**
     * Consumes one slot from this rate.
     *
     * @param now The current time stamp
     * @return The rate result
     */
    Result consume(long now);

    /**
     * Gets the number of permits.
     *
     * @return The permits
     */
    int getPermits();

    /**
     * Gets the time window in milliseconds
     *
     * @return The time window
     */
    long getTimeInMillis();

    /**
     * Sets the time window in milliseconds
     *
     * @param timeInMillis The time window
     */
    void setTimeInMillis(long timeInMillis);

}
