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

import java.util.concurrent.TimeUnit;

/**
 * {@link AbstractRetryPolicy}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.12.0
 */
class AbstractRetryPolicy implements RetryPolicy {

    private final int maxTries;
    private final long sleepTime;
    private int retryCount = 1;

    /**
     * Initialises a new {@link RandomJitterRetryPolicy}.
     * 
     * @param maxTries The amount of maximum tries
     * @param sleepTime The delay between each try in milliseconds
     */
    public AbstractRetryPolicy(int maxTries, long sleepTime) {
        this.maxTries = maxTries;
        this.sleepTime = sleepTime;
    }

    @Override
    public int getMaxTries() {
        return maxTries;
    }

    @Override
    public int retryCount() {
        return retryCount;
    }

    @Override
    public boolean isRetryAllowed() {
        if (++retryCount > maxTries) {
            return false;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(getSleepTime());
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the sleep time in milliseconds
     * 
     * @return the sleep time in milliseconds
     */
    protected long getSleepTime() {
        return sleepTime;
    }
}
