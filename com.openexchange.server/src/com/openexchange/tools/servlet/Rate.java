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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.tools.servlet;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * {@link Rate} - Implements time stamp based rate limit.
 * <p>
 * This class is derived from <a href="http://www.cordinc.com/blog/2010/04/java-multichannel-asynchronous.html">this</a> post.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Rate {

    private final int numberCalls;
    private final long timeInMillis;
    private final Deque<Long> callHistory;
    private boolean deprecated;

    /**
     * Initializes a new {@link Rate}.
     *
     * @param numberCalls The number of allowed calls
     * @param timeLength The time length
     * @param timeUnit The time unit
     */
    public Rate(final int numberCalls, final int timeLength, final TimeUnit timeUnit) {
        super();
        deprecated = false;
        callHistory = new LinkedList<Long>();
        this.numberCalls = numberCalls;
        this.timeInMillis = timeUnit.toMillis(timeLength);
    }

    private void cleanOld(final long now) {
        final long threshold = now - timeInMillis;
        Long first;
        while ((first = callHistory.peekFirst()) != null && first.longValue() <= threshold) {
            callHistory.pollFirst();
        }
    }

    private long callTime(final long now) {
        cleanOld(now);
        final int size = callHistory.size();
        if (size < numberCalls) {
            return now;
        }
        final long lastStart = callHistory.peekLast().longValue() - timeInMillis;
        long firstPeriodCall = lastStart, call;
        int count = 0;
        final Iterator<Long> i = callHistory.descendingIterator();
        while (i.hasNext()) {
            call = i.next().longValue();
            if (call < lastStart) {
                break;
            }
            count++;
            firstPeriodCall = call;
        }
        return count < numberCalls ? (firstPeriodCall + 1) : (firstPeriodCall + timeInMillis + 1);
    }

    /**
     * Gets this rate's last-accessed time stamp.
     *
     * @return The last-accessed time stamp
     */
    public long lastAccessTime() {
        synchronized (callHistory) {
            final Long last = callHistory.peekLast();
            return null == last ? Long.MIN_VALUE : last.longValue();
        }
    }

    /**
     * Checks if this rate is deprecated
     *
     * @return <code>true</code> if deprecated; otherwise <code>false</code>
     * @see #markDeprecatedIfElapsed(long)
     */
    public boolean isDeprecated() {
        synchronized (callHistory) {
            return deprecated;
        }
    }

    /**
     * Marks this rate as deprecated if elapsed in comparison to given threshold.
     *
     * @param threshold The threshold
     * @return <code>true</code> if elapsed (and marked as deprecated); otherwise <code>false</code>
     */
    public boolean markDeprecatedIfElapsed(final long threshold) {
        synchronized (callHistory) {
            final Long last = callHistory.peekLast();
            if (null != last && last.longValue() > threshold) {
                return false;
            }
            deprecated = true;
            return true;
        }
    }

    /**
     * Checks if this rate is empty; meaning there is no slot occupied anymore because time is elapsed.
     *
     * @param now The current time stamp
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty(final long now) {
        synchronized (callHistory) {
            cleanOld(now);
            return callHistory.isEmpty();
        }
    }

    /**
     * Consumes one slot from this rate.
     *
     * @param now The current time stamp
     * @return <code>1</code> if successfully consumed, <code>0</code> if all available slots are occupied, or <code>-1</code> if marked as
     *         deprecated
     */
    public int consume(final long now) {
        synchronized (callHistory) {
            if (deprecated) {
                return -1;
            }
            final long callTime = callTime(now);
            callHistory.offerLast(Long.valueOf(callTime));
            return ((callTime - now) <= 0) ? 1 : 0;
        }
    }

}
