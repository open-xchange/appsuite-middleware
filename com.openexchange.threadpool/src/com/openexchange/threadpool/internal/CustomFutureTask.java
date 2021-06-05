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

package com.openexchange.threadpool.internal;

import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.threadpool.MdcProvider;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.Trackable;

/**
 * {@link CustomFutureTask} - A custom {@link FutureTask}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CustomFutureTask<V> extends FutureTask<V> implements MdcProvider {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final Task<V> task;
    private final RefusedExecutionBehavior<V> refusedExecutionBehavior;
    private final long number;
    private final boolean trackable;
    private final Map<String, String> mdcMap;

    /**
     * Initializes a new {@link CustomFutureTask}.
     *
     * @param task The task
     * @param mdcMap The MDC map
     */
    public CustomFutureTask(final Task<V> task, final Map<String, String> mdcMap) {
        this(task, null, mdcMap);
    }

    /**
     * Initializes a new {@link CustomFutureTask}.
     *
     * @param task The task
     * @param refusedExecutionBehavior The refused execution behavior
     * @param mdcMap The MDC map
     */
    public CustomFutureTask(final Task<V> task, final RefusedExecutionBehavior<V> refusedExecutionBehavior, final Map<String, String> mdcMap) {
        super(task);
        this.task = task;
        this.mdcMap = mdcMap;
        this.refusedExecutionBehavior = refusedExecutionBehavior;
        // Assign number
        number = COUNTER.incrementAndGet();
        trackable = (task instanceof Trackable);
    }

    @Override
    public Map<String, String> getMdc() {
        return mdcMap;
    }

    /**
     * Checks if trackable.
     *
     * @return <code>true</code> if trackable; otherwise <code>false</code>
     */
    public boolean isTrackable() {
        return trackable;
    }

    /**
     * Gets the number
     *
     * @return The number
     */
    public long getNumber() {
        return number;
    }

    /**
     * Gets the task performed by this future task.
     *
     * @return The task
     */
    public Task<V> getTask() {
        return task;
    }

    /**
     * Gets the refused execution behavior.
     *
     * @return The refused execution behavior or <code>null</code> if task has no individual behavior
     */
    public RefusedExecutionBehavior<V> getRefusedExecutionBehavior() {
        return refusedExecutionBehavior;
    }

    /**
     * Sets the result of this future to the given value unless this future has already been set or has been canceled.
     *
     * @param v The value
     */
    @Override
    public void set(final V v) {
        super.set(v);
    }

    /**
     * Causes this future to report an <tt>ExecutionException</tt> with the given throwable as its cause, unless this Future has already
     * been set or has been canceled.
     *
     * @param t The cause of failure.
     */
    @Override
    public void setException(final Throwable t) {
        super.setException(t);
    }

}
