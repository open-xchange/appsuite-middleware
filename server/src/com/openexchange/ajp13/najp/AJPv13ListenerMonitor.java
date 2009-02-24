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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajp13.najp;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.ajp13.monitoring.AJPv13ListenerMonitorMBean;
import com.openexchange.ajp13.najp.threadpool.AJPv13SocketHandler;

/**
 * {@link AJPv13ListenerMonitor} - The listener monitor MBean implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13ListenerMonitor extends StandardMBean implements AJPv13ListenerMonitorMBean {

    private final AtomicInteger numWaiting = new AtomicInteger();

    private final AtomicInteger numProcessing = new AtomicInteger();

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

    private final AJPv13SocketHandler ajpPool;

    /**
     * Initializes a new {@link AJPv13ListenerMonitor}.
     * 
     * @param ajpPool The active AJP pool.
     * @throws NotCompliantMBeanException If MBean is not compliant
     */
    public AJPv13ListenerMonitor(final AJPv13SocketHandler ajpPool) throws NotCompliantMBeanException {
        super(AJPv13ListenerMonitorMBean.class);
        avgUseTimeArr = new long[1000];
        avgProcessingTimeArr = new long[1000];
        this.ajpPool = ajpPool;
    }

    public int getPoolSize() {
        return ajpPool.getPoolSize();
    }

    public int getNumActive() {
        return ajpPool.getActiveCount();
    }

    public int getNumIdle() {
        return ajpPool.getPoolSize() - ajpPool.getActiveCount();
    }

    public int getNumWaiting() {
        return numWaiting.get();
    }

    /**
     * Atomically increments the number of AJP threads currently waiting on an incoming AJP package.
     */
    public void incrementNumWaiting() {
        numWaiting.incrementAndGet();
    }

    /**
     * Atomically decrements the number of AJP threads currently waiting on an incoming AJP package.
     */
    public void decrementNumWaiting() {
        numWaiting.decrementAndGet();
    }

    public int getNumProcessing() {
        return numProcessing.get();
    }

    /**
     * Atomically increments the number of AJP threads currently processing a received AJP package.
     */
    public void incrementNumProcessing() {
        numProcessing.incrementAndGet();
    }

    /**
     * Atomically decrements the number of AJP threads currently processing a received AJP package.
     */
    public void decrementNumProcessing() {
        numProcessing.decrementAndGet();
    }

    public long getNumRequests() {
        return numRequests.get();
    }

    /**
     * Atomically increments the number of received AJP requests.
     */
    public void incrementNumRequests() {
        numRequests.incrementAndGet();
    }

    public double getAvgUseTime() {
        long duration = 0;
        for (int i = 0; i < avgUseTimeArr.length; i++) {
            duration += avgUseTimeArr[i];
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

    public long getMaxUseTime() {
        return maxUseTime;
    }

    /**
     * Sets the max use time to the maximum of given <code>maxUseTime</code> and existing value
     */
    private void setMaxUseTime(final long maxUseTime) {
        this.maxUseTime = Math.max(maxUseTime, this.maxUseTime);
    }

    public void resetMaxUseTime() {
        useTimeLock.lock();
        try {
            maxUseTime = 0;
        } finally {
            useTimeLock.unlock();
        }
    }

    public long getMinUseTime() {
        return minUseTime;
    }

    private void setMinUseTime(final long minUseTime) {
        this.minUseTime = Math.min(minUseTime, this.minUseTime);
    }

    public void resetMinUseTime() {
        useTimeLock.lock();
        try {
            minUseTime = Long.MAX_VALUE;
        } finally {
            useTimeLock.unlock();
        }
    }

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

    public long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public long getMinProcessingTime() {
        return minProcessingTime;
    }

    public double getAvgProcessingTime() {
        long duration = 0;
        for (int i = 0; i < avgProcessingTimeArr.length; i++) {
            duration += avgProcessingTimeArr[i];
        }
        return (((double) duration) / avgProcessingTimeArr.length);
    }

    public void resetMaxProcessingTime() {
        maxProcessingTime = 0;
    }

    public void resetMinProcessingTime() {
        minProcessingTime = Long.MAX_VALUE;
    }

}
