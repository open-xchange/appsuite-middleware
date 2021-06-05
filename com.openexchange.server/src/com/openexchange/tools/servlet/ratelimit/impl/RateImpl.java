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
import com.openexchange.tools.servlet.ratelimit.Rate;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.procedure.TLongProcedure;

/**
 * {@link RateImpl} - Implements time stamp based rate limit.
 * <p>
 * This class is derived from <a href="http://www.cordinc.com/blog/2010/04/java-multichannel-asynchronous.html">this</a> post.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RateImpl implements Rate {

    private static class MyProc implements TLongProcedure {
        final long lastStart;
        long firstPeriodCall;
        int count;

        MyProc(long lastStart) {
            super();
            this.lastStart = lastStart;
            firstPeriodCall = lastStart;
            count = 0;
        }

        @Override
        public boolean execute(long call) {
            if (call < lastStart) {
                return false;
            }
            count++;
            firstPeriodCall = call;
            return true;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private final AtomicLong lastLogStamp;
    private final int permits;
    private long timeInMillis;
    private final TLongList callHistory;
    private boolean deprecated;

    /**
     * Initializes a new {@link RateImpl}.
     *
     * @param permits The number of allowed calls
     * @param timeLength The time length
     * @param timeUnit The time unit
     */
    public RateImpl(int permits, int timeLength, TimeUnit timeUnit) {
        super();
        lastLogStamp = new AtomicLong(0L);
        deprecated = false;
        callHistory = new TLongArrayList();
        this.permits = permits;
        this.timeInMillis = timeUnit.toMillis(timeLength);
    }

    private void cleanOld(long now) {
        long threshold = now - timeInMillis;
        while (!callHistory.isEmpty() && callHistory.get(0) <= threshold) {
            callHistory.removeAt(0);
        }
    }

    private long callTime(long now) {
        cleanOld(now);
        final int size = callHistory.size();
        if (size < permits) {
            return now;
        }
        long lastStart = callHistory.get(size - 1) - timeInMillis;
        MyProc procedure = new MyProc(lastStart);
        callHistory.forEachDescending(procedure);

        long retval = procedure.count < permits ? (procedure.firstPeriodCall + 1) : (procedure.firstPeriodCall + timeInMillis + 1);
        return retval > procedure.firstPeriodCall ? retval : procedure.firstPeriodCall;
    }

    @Override
    public AtomicLong getLastLogStamp() {
        return lastLogStamp;
    }

    @Override
    public long lastAccessTime() {
        synchronized (callHistory) {
            return callHistory.isEmpty() ? Long.MIN_VALUE : callHistory.get(callHistory.size() - 1);
        }
    }

    @Override
    public boolean isDeprecated() {
        synchronized (callHistory) {
            return deprecated;
        }
    }

    @Override
    public boolean markDeprecatedIfElapsed(final long threshold) {
        synchronized (callHistory) {
            if (!callHistory.isEmpty() && callHistory.get(callHistory.size() - 1) > threshold) {
                return false;
            }
            deprecated = true;
            return true;
        }
    }

    @Override
    public Result consume(final long now) {
        synchronized (callHistory) {
            if (deprecated) {
                return Result.DEPRECATED;
            }
            long callTime = callTime(now);
            callHistory.add(callTime);
            return ((callTime - now) <= 0) ? Result.SUCCESS : Result.FAILED;
        }
    }

    @Override
    public int getPermits() {
        return permits;
    }

    @Override
    public long getTimeInMillis() {
        synchronized (callHistory) {
            return timeInMillis;
        }
    }

    @Override
    public void setTimeInMillis(long timeInMillis) {
        synchronized (callHistory) {
            this.timeInMillis = timeInMillis;
        }
    }

    // ----------------------------------------------------------------------------- //

    public static void main(String[] args) {
        Rate rate = new RateImpl(5, 5, TimeUnit.SECONDS);

        long callTime = System.currentTimeMillis();

        for (int i = 0; i < 6; i++) {
            System.out.println(rate.consume(callTime));
            callTime += 100;
        }
        System.out.println("-------------------------");

        callTime += 6000;

        for (int i = 0; i < 6; i++) {
            System.out.println(rate.consume(callTime));
            callTime += 100;
        }
        System.out.println("-------------------------");
    }

}
