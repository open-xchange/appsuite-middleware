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

package com.openexchange.pooling;

import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.database.internal.MysqlUtils;

/**
 * This class stores data about a pooled object.
 *
 * @param <T> type of object.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class PooledData<T> {

    /**
     * Counter for referencing uniquely all pooled objects.
     */
    private static final AtomicInteger counter = new AtomicInteger();

    /**
     * Unique identifier of the pooled object.
     */
    private final int identifier;

    /**
     * Time when this pooled object was created.
     */
    private final long createTime;

    /**
     * The pooled object.
     */
    private final T pooled;

    private volatile long timestamp;

    private volatile Thread thread;

    private volatile StackTraceElement[] trace;

    private volatile boolean deprecated = false;

    /**
     * Default constructor.
     *
     * @param pooled Pooled object.
     */
    PooledData(final T pooled) {
        super();
        this.createTime = System.currentTimeMillis();
        touch();
        this.identifier = counter.incrementAndGet();
        this.pooled = pooled;
    }

    void setThread(final Thread user) {
        this.thread = user;
    }

    final void touch() {
        timestamp = System.currentTimeMillis();
    }

    public T getPooled() {
        return pooled;
    }

    void setTrace(final StackTraceElement[] trace) {
        this.trace = trace;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PooledData<?>)) {
            return false;
        }
        return pooled.equals(((PooledData<?>) obj).pooled);
    }

    @Override
    public int hashCode() {
        return pooled.hashCode();
    }

    void resetTrace() {
        thread = null;
        trace = null;
    }

    /**
     * @return the time stamp when this pooled object is last used.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the number of milliseconds since this pooled object was last
     *         touched by the pool.
     */
    public long getTimeDiff() {
        return System.currentTimeMillis() - timestamp;
    }

    /**
     * Gets the "last packet either received or sent" diff from pooled object (if available); otherwise fall-back to {@link #getTimeDiff()}.
     *
     * @return The "last packet either received or sent" diff or {@link #getTimeDiff()}
     */
    public long getLastPacketDiffFallbackToTimeDiff() {
        Long lastPacketTime = MysqlUtils.getOLastPacketTime(pooled);
        if (null == lastPacketTime) {
            return getTimeDiff();
        }
        return System.currentTimeMillis() - lastPacketTime.longValue();
    }

    /**
     * @return the time this pooled object is living.
     */
    public long getLiveTime() {
        return System.currentTimeMillis() - createTime;
    }

    Thread getThread() {
        return thread;
    }

    public StackTraceElement[] getTrace() {
        return trace;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Marks this pooled object as deprecated
     */
    public void setDeprecated() {
        this.deprecated = true;
    }

    /**
     * Get a value indicating if the pooled object is deprecated. If so the object needs to be destroyed
     *
     * @return <code>true</code> if the pooled object is deprecated
     *         <code>false</code> otherwise
     *
     */
    public boolean isDeprecated() {
        return deprecated;
    }
}
