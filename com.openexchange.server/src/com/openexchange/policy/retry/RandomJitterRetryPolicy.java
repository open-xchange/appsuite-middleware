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

package com.openexchange.policy.retry;

/**
 * {@link RandomJitterRetryPolicy}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.12.0
 */
public class RandomJitterRetryPolicy extends AbstractRetryPolicy {

    private final float jitter;

    /**
     * Initialises a new {@link RandomJitterRetryPolicy}.
     * The {@link #maxTries} is initialised with
     * {@link Integer#MAX_VALUE}, {@link #sleepTime}
     * with 2500 milliseconds and the random {@link #jitter} with 20%.
     */
    public RandomJitterRetryPolicy() {
        this(Integer.MAX_VALUE, 2500, 0.2f);
    }

    /**
     * Initialises a new {@link RandomJitterRetryPolicy}.
     * 
     * @param maxTries The amount of maximum tries
     * @param sleepTime The delay between each try in milliseconds
     * @param jitter The random jitter percentage to add to the sleep time to avoid retry storms
     */
    public RandomJitterRetryPolicy(int maxTries, long sleepTime, float jitter) {
        super(maxTries, sleepTime);
        if (jitter < 0 || jitter > 1) {
            throw new IllegalArgumentException("The jitter should be less than or equal to 1.0f and greater than or equal to 0");
        }
        this.jitter = jitter;
    }

    @Override
    protected long getSleepTime() {
        return super.getSleepTime() + Math.round(Math.random() * jitter * 1000);
    }
}
