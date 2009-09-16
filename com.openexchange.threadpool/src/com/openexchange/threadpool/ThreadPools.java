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

package com.openexchange.threadpool;

import java.util.concurrent.Callable;

/**
 * {@link ThreadPools} - Utility methods for {@link ThreadPoolService} and {@link Task}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadPools {

    /**
     * Initializes a new {@link ThreadPools}.
     */
    private ThreadPools() {
        super();
    }

    /**
     * Returns a {@link Task} object that, when called, runs the given task and returns the given result. This can be useful when applying
     * methods requiring a <tt>Task</tt> to an otherwise resultless action.
     * 
     * @param task The task to run
     * @param result The result to return
     * @throws NullPointerException If task is <code>null</code>
     * @return A {@link Task} object
     */
    public static <T> Task<T> task(final Runnable task, final T result) {
        if (task == null) {
            throw new NullPointerException();
        }
        return new TaskAdapter<T>(new RunnableAdapter<T>(task, result));
    }

    /**
     * Returns a {@link Task} object that, when called, runs the given task and returns <tt>null</tt>.
     * 
     * @param task The task to run
     * @return A {@link Task} object
     * @throws NullPointerException If task is <code>null</code>
     */
    public static Task<Object> task(final Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        return new TaskAdapter<Object>(new RunnableAdapter<Object>(task, null));
    }

    /**
     * Returns a {@link Task} object that, when called, returns the given task's result.
     * 
     * @param task The task to run
     * @return A {@link Task} object
     * @throws NullPointerException If task is <code>null</code>
     */
    public static <T> Task<T> task(final Callable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        return new TaskAdapter<T>(task);
    }

    /**
     * A {@link Callable} that runs given task and returns given result
     */
    private static final class RunnableAdapter<T> implements Callable<T> {

        private final Runnable task;

        private final T result;

        RunnableAdapter(final Runnable task, final T result) {
            this.task = task;
            this.result = result;
        }

        public T call() {
            task.run();
            return result;
        }

    }

    private static final class TaskAdapter<V> implements Task<V> {

        private final Callable<V> callable;

        /**
         * Initializes a new {@link TaskAdapter}.
         */
        TaskAdapter(final Callable<V> callable) {
            super();
            this.callable = callable;
        }

        public void afterExecute(final Throwable throwable) {
            // NOP
        }

        public void beforeExecute(final Thread thread) {
            // NOP
        }

        public void setThreadName(final ThreadRenamer threadRenamer) {
            // NOP
        }

        public V call() throws Exception {
            return callable.call();
        }

    }

}
