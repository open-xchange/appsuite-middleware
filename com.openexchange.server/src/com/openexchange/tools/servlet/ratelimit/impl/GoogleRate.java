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
