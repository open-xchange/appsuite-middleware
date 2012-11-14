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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13.watcher;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.ajp13.monitoring.AJPv13TaskMonitorMBean;

/**
 * {@link AJPv13TaskMonitor} - The task monitor MBean implementation monitoring AJP tasks processed by global thread pool.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13TaskMonitor extends StandardMBean implements AJPv13TaskMonitorMBean {

    private final AtomicInteger numWaiting = new AtomicInteger();

    private final AtomicInteger numProcessing = new AtomicInteger();

    private final AtomicInteger numActive = new AtomicInteger();

    private final AtomicLong numRequests = new AtomicLong();

    private final long[] avgUseTimeArr;

    private int avgUseTimePointer;

    private long maxUseTime;

    private long minUseTime = Long.MAX_VALUE;

    private final long[] avgProcessingTimeArr;

    private int avgProcessingTimePointer;

    private long maxProcessingTime;

    private long minProcessingTime = Long.MAX_VALUE;

    private final Lock useTimeLock = new ReentrantLock();

    private final Lock processingTimeLock = new ReentrantLock();

    /**
     * Initializes a new {@link AJPv13TaskMonitor}.
     *
     * @throws NotCompliantMBeanException If MBean is not compliant
     */
    public AJPv13TaskMonitor() throws NotCompliantMBeanException {
        super(AJPv13TaskMonitorMBean.class);
        avgUseTimeArr = new long[1000];
        avgProcessingTimeArr = new long[1000];
    }

    @Override
    public int getPoolSize() {
        // There is no more a thread pool for AJP tasks
        return 0;
    }

    @Override
    public int getNumActive() {
        return numActive.get();
    }

    /**
     * Atomically increments the number of active AJP tasks.
     */
    public void incrementNumActive() {
        numActive.incrementAndGet();
    }

    /**
     * Atomically decrements the number of active AJP tasks.
     */
    public void decrementNumActive() {
        numActive.decrementAndGet();
    }

    @Override
    public int getNumIdle() {
        // There is no more a thread pool for AJP tasks
        return 0;
    }

    @Override
    public int getNumWaiting() {
        return numWaiting.get();
    }

    /**
     * Atomically increments the number of AJP tasks currently waiting on an incoming AJP package.
     */
    public void incrementNumWaiting() {
        numWaiting.incrementAndGet();
    }

    /**
     * Atomically decrements the number of AJP tasks currently waiting on an incoming AJP package.
     */
    public void decrementNumWaiting() {
        numWaiting.decrementAndGet();
    }

    @Override
    public int getNumProcessing() {
        return numProcessing.get();
    }

    /**
     * Atomically increments the number of AJP tasks currently processing a received AJP package.
     */
    public void incrementNumProcessing() {
        numProcessing.incrementAndGet();
    }

    /**
     * Atomically decrements the number of AJP tasks currently processing a received AJP package.
     */
    public void decrementNumProcessing() {
        numProcessing.decrementAndGet();
    }

    @Override
    public long getNumRequests() {
        return numRequests.get();
    }

    /**
     * Atomically increments the number of received AJP requests.
     */
    public void incrementNumRequests() {
        numRequests.incrementAndGet();
    }

    @Override
    public double getAvgUseTime() {
        long duration = 0;
        for (final long element : avgUseTimeArr) {
            duration += element;
        }
        return (((double) duration) / (avgUseTimeArr.length));
    }

    /**
     * Adds the total time in milliseconds an AJP thread processed a client socket until socket closure.
     *
     * @param time The total time in milliseconds
     */
    public void addUseTime(final long time) {
        if (useTimeLock.tryLock()) {
            /*
             * Add use time only when lock could be acquired
             */
            try {
                avgUseTimeArr[avgUseTimePointer++] = time;
                avgUseTimePointer = avgUseTimePointer % avgUseTimeArr.length;
                setMaxUseTime(time);
                setMinUseTime(time);
            } finally {
                useTimeLock.unlock();
            }
        }
    }

    @Override
    public long getMaxUseTime() {
        return maxUseTime;
    }

    /**
     * Sets the max use time to the maximum of given <code>maxUseTime</code> and existing value
     */
    private void setMaxUseTime(final long maxUseTime) {
        this.maxUseTime = Math.max(maxUseTime, this.maxUseTime);
    }

    @Override
    public void resetMaxUseTime() {
        useTimeLock.lock();
        try {
            maxUseTime = 0;
        } finally {
            useTimeLock.unlock();
        }
    }

    @Override
    public long getMinUseTime() {
        return minUseTime;
    }

    private void setMinUseTime(final long minUseTime) {
        this.minUseTime = Math.min(minUseTime, this.minUseTime);
    }

    @Override
    public void resetMinUseTime() {
        useTimeLock.lock();
        try {
            minUseTime = Long.MAX_VALUE;
        } finally {
            useTimeLock.unlock();
        }
    }

    @Override
    public int getNumBrokenConnections() {
        return 0;
    }

    /**
     * Adds the time in milliseconds an AJP thread processed an AJP cycle; meaning from initial FORWARD-REQUEST until terminating
     * END-RESPONSE.
     *
     * @param time The time in milliseconds
     */
    public void addProcessingTime(final long time) {
        if (processingTimeLock.tryLock()) {
            /*
             * Add processing time only when lock could be acquired
             */
            try {
                avgProcessingTimeArr[avgProcessingTimePointer++] = time;
                avgProcessingTimePointer = avgProcessingTimePointer % avgProcessingTimeArr.length;
                setMaxProcessingTime(time);
                setMinProcessingTime(time);
            } finally {
                processingTimeLock.unlock();
            }
        }
    }

    private void setMaxProcessingTime(final long maxProcessingTime) {
        this.maxProcessingTime = Math.max(this.maxProcessingTime, maxProcessingTime);
    }

    private void setMinProcessingTime(final long minProcessingTime) {
        this.minProcessingTime = Math.min(this.minProcessingTime, minProcessingTime);
    }

    @Override
    public long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    @Override
    public long getMinProcessingTime() {
        return minProcessingTime;
    }

    @Override
    public double getAvgProcessingTime() {
        long duration = 0;
        for (final long element : avgProcessingTimeArr) {
            duration += element;
        }
        return (((double) duration) / avgProcessingTimeArr.length);
    }

    @Override
    public void resetMaxProcessingTime() {
        maxProcessingTime = 0;
    }

    @Override
    public void resetMinProcessingTime() {
        minProcessingTime = Long.MAX_VALUE;
    }

}
