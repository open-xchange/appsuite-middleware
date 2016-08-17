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

        return procedure.count < permits ? (procedure.firstPeriodCall + 1) : (procedure.firstPeriodCall + timeInMillis + 1);
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
