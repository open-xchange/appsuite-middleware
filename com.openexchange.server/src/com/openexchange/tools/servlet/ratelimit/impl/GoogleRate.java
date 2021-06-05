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

package com.openexchange.tools.servlet.ratelimit.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.util.concurrent.RateLimiter;
import com.openexchange.tools.servlet.ratelimit.Rate;


/**
 * {@link GoogleRate}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GoogleRate implements Rate {

    private final AtomicLong lastLogStamp;
    private final AtomicLong lastAccessTime;
    private volatile RateLimiter googleRateLimiter;
    private volatile boolean deprecated;
    private final int permits;
    private volatile long millis;

    /**
     * Initializes a new {@link GoogleRate}.
     */
    public GoogleRate(int numberCalls, int timeLength, TimeUnit timeUnit) {
        super();
        permits = numberCalls;
        millis = TimeUnit.SECONDS.convert(timeLength, timeUnit);
        double rate = ((double) numberCalls) / ((double) millis);
        googleRateLimiter = RateLimiter.create(rate);
        lastLogStamp = new AtomicLong(0L);
        lastAccessTime = new AtomicLong(Long.MIN_VALUE);
        deprecated = false;
    }

    @Override
    public AtomicLong getLastLogStamp() {
        return lastLogStamp;
    }

    @Override
    public long lastAccessTime() {
        return lastAccessTime.get();
    }

    @Override
    public boolean isDeprecated() {
        return deprecated;
    }

    @Override
    public boolean markDeprecatedIfElapsed(long threshold) {
        synchronized (googleRateLimiter) {
            if (lastAccessTime.get() > threshold) {
                return false;
            }
            deprecated = true;
            return true;
        }
    }

    @Override
    public Result consume(long now) {
        synchronized (googleRateLimiter) {
            lastAccessTime.set(now);
            if (deprecated) {
                return Result.DEPRECATED;
            }
        }
        boolean permitted = googleRateLimiter.tryAcquire(1);
        return permitted ? Rate.Result.SUCCESS : Rate.Result.FAILED;
    }

    @Override
    public void setTimeInMillis(long timeInMillis) {
        millis = timeInMillis;
        double rate = ((double) permits) / ((double) timeInMillis);
        googleRateLimiter = RateLimiter.create(rate);
    }

    @Override
    public int getPermits() {
        return permits;
    }

    @Override
    public long getTimeInMillis() {
        return millis;
    }

}
