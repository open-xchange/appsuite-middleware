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

package com.openexchange.api2;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.mail.api.MailAccess;

/**
 * {@link MailInterfaceMonitor} - The mail module's MBean.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailInterfaceMonitor implements MailInterfaceMonitorMBean {

    private static final int USE_TIME_COUNT = 1000;

    private final long[] avgUseTimeArr;

    private int avgUseTimePointer;

    private long maxUseTime;

    private long minUseTime = Long.MAX_VALUE;

    private final AtomicInteger numBrokenConnections = new AtomicInteger();

    private final AtomicInteger numTimeoutConnections = new AtomicInteger();

    private final AtomicInteger numSuccessfulLogins = new AtomicInteger();

    private final AtomicInteger numFailedLogins = new AtomicInteger();

    private final Lock useTimeLock = new ReentrantLock();

    private final Map<String, Integer> unsupportedEnc;

    /**
     * Initializes a new {@link MailInterfaceMonitor}.
     */
    public MailInterfaceMonitor() {
        super();
        avgUseTimeArr = new long[USE_TIME_COUNT];
        unsupportedEnc = new ConcurrentHashMap<String, Integer>();
    }

    @Override
    public int getNumActive() {
        return MailAccess.getCounter();
    }

    /**
     * Omit call to this method. It does nothing at all...
     */
    public void changeNumActive(final boolean increment) {
        // Delete this method
    }

    @Override
    public double getAvgUseTime() {
        long duration = 0;
        for (int i = avgUseTimeArr.length; i-- > 0;) {
            duration += avgUseTimeArr[i];
        }
        return (duration / (double) avgUseTimeArr.length);
    }

    /**
     * Adds given use time to average use time array and invokes the setMaxUseTime() and setMinUseTime() methods.
     */
    public void addUseTime(final long time) {
        if (useTimeLock.tryLock()) {
            /*
             * Add use time only when lock could be acquired
             */
            try {
                avgUseTimeArr[avgUseTimePointer++] = time;
                if (avgUseTimePointer >= avgUseTimeArr.length) {
                    avgUseTimePointer = 0;
                }
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
    private final void setMaxUseTime(final long maxUseTime) {
        if (maxUseTime > this.maxUseTime) {
            this.maxUseTime = maxUseTime;
        }
    }

    @Override
    public void resetMaxUseTime() {
        maxUseTime = 0;
    }

    @Override
    public long getMinUseTime() {
        return minUseTime;
    }

    private final void setMinUseTime(final long minUseTime) {
        if (minUseTime < this.minUseTime) {
            this.minUseTime = minUseTime;
        }
    }

    @Override
    public void resetMinUseTime() {
        minUseTime = Long.MAX_VALUE;
    }

    @Override
    public int getNumBrokenConnections() {
        return numBrokenConnections.get();
    }

    /**
     * Changes number of broken connections.
     */
    public void changeNumBrokenConnections(final boolean increment) {
        if (increment) {
            numBrokenConnections.incrementAndGet();
        } else {
            numBrokenConnections.decrementAndGet();
        }
    }

    @Override
    public int getNumTimeoutConnections() {
        return numTimeoutConnections.get();
    }

    /**
     * Changes number of timed-out connections.
     */
    public void changeNumTimeoutConnections(final boolean increment) {
        if (increment) {
            numTimeoutConnections.incrementAndGet();
        } else {
            numTimeoutConnections.decrementAndGet();
        }
    }

    @Override
    public int getNumSuccessfulLogins() {
        return numSuccessfulLogins.get();
    }

    /**
     * Changes number of successful logins.
     */
    public void changeNumSuccessfulLogins(final boolean increment) {
        if (increment) {
            numSuccessfulLogins.incrementAndGet();
        } else {
            numSuccessfulLogins.decrementAndGet();
        }
    }

    @Override
    public int getNumFailedLogins() {
        return numFailedLogins.get();
    }

    /**
     * Changes number of failed logins.
     */
    public void changeNumFailedLogins(final boolean increment) {
        if (increment) {
            numFailedLogins.incrementAndGet();
        } else {
            numFailedLogins.decrementAndGet();
        }
    }

    @Override
    public void resetNumBrokenConnections() {
        numBrokenConnections.set(0);
    }

    @Override
    public void resetNumTimeoutConnections() {
        numTimeoutConnections.set(0);
    }

    @Override
    public void resetNumSuccessfulLogins() {
        numSuccessfulLogins.set(0);
    }

    @Override
    public void resetNumFailedLogins() {
        numFailedLogins.set(0);
    }

    @Override
    public String getUnsupportedEncodingExceptions() {
        final int size = unsupportedEnc.size();
        if (size == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(100);
        final Iterator<Entry<String, Integer>> iter = unsupportedEnc.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            final Entry<String, Integer> entry = iter.next();
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" times");
        }
        return sb.toString();
    }

    /**
     * Adds an occurrence of an unsupported encoding.
     *
     * @param encoding - the unsupported encoding
     */
    public void addUnsupportedEncodingExceptions(final String encoding) {
        final String key = encoding.toLowerCase(Locale.ENGLISH);
        final Integer num = unsupportedEnc.get(key);
        if (null == num) {
            unsupportedEnc.put(key, Integer.valueOf(1));
        } else {
            unsupportedEnc.put(key, Integer.valueOf(num.intValue() + 1));
        }
    }

}
