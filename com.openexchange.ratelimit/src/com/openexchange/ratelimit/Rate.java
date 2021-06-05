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
 * {@link Rate} defines a rate limit which contains the amount of permits and the time frame in milliseconds.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class Rate {

    /**
     * Creates a new rate for given max. amount of permits in specified time frame.
     *
     * @param amount The max. number of permits
     * @param timeframe The time frame in milliseconds, in which the defined amount of permits are available.
     * @return The new rate
     */
    public static Rate create(long amount, long timeframe) {
        return new Rate(amount, timeframe);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final long amount;
    private final long timeframe;

    /**
     * Initializes a new {@link Rate}.
     */
    private Rate(long amount, long timeframe) {
        this.amount = amount;
        this.timeframe = timeframe;
    }

    /**
     * Gets the amount
     *
     * @return The amount
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Gets the time frame in milliseconds
     *
     * @return The time frame
     */
    public long getTimeframe() {
        return timeframe;
    }

    /**
     * Checks is this rate is effectively enabled; that is specified max. number of permits is greater than 0 (zero).
     *
     * @return <code>true</code> if enabled; other wise <code>false</code>
     */
    public boolean isEnabled() {
        return amount >= 0;
    }
}
