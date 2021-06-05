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

package com.openexchange.threadpool;

/**
 * {@link AbstractTask} - An abstract {@link Task} which leaves {@link #afterExecute(Throwable)}, {@link #beforeExecute(Thread)}, and
 * {@link #setThreadName(ThreadRenamer)} empty.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractTask<V> implements Task<V> {

    /**
     * Initializes a new {@link AbstractTask}.
     */
    protected AbstractTask() {
        super();
    }

    /**
     * Executes this task with current thread.
     *
     * @return The task's return value or <code>null</code> if an {@code Exception} occurred (in that {@link #afterExecute(Throwable)} is
     *         invoked with a non-<code>null</code> {@code Throwable} reference)
     */
    public V execute() {
        final Thread currentThread = Thread.currentThread();
        if (!(currentThread instanceof ThreadRenamer)) {
            return innerExecute(currentThread);
        }
        // Current thread supports ThreadRenamer
        final String name = currentThread.getName();
        setThreadName((ThreadRenamer) currentThread);
        try {
            return innerExecute(currentThread);
        } finally {
            currentThread.setName(name);
        }
    }

    /**
     * Execute with respect to <code>beforeExecute()</code> and <code>afterExecute()</code> methods
     *
     * @param currentThread The current thread
     * @return The return value or <code>null</code>
     */
    protected V innerExecute(final Thread currentThread) {
        V retval = null;
        boolean ran = false;
        beforeExecute(currentThread);
        try {
            retval = call();
            ran = true;
            afterExecute(null);
        } catch (Exception ex) {
            if (!ran) {
                afterExecute(ex);
            }
            // Else the exception occurred within
            // afterExecute itself in which case we don't
            // want to call it again.
        }
        return retval;
    }

    @Override
    public void afterExecute(final Throwable throwable) {
        // NOP
    }

    @Override
    public void beforeExecute(final Thread thread) {
        // NOP
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        // NOP
    }

}
