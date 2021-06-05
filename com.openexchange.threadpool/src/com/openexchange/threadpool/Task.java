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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * {@link Task} - A task being submitted to thread pool.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Task<V> extends Callable<V> {

    /**
     * Sets an appropriate name for executing thread using given {@link ThreadRenamer} instance.
     * <p>
     * Implementations may leave this method empty if given thread needs not to be renamed.
     *
     * @param threadRenamer The thread renamer
     */
    void setThreadName(ThreadRenamer threadRenamer);

    /**
     * Invoked prior to executing this task in the given thread. This method is invoked by pooled thread <tt>t</tt> that will execute this
     * task, and may be used to re-initialize {@link ThreadLocal}s, or to perform logging.
     * <p>
     * Implementations may leave this method empty if nothing should be performed.
     *
     * @param t The thread that will run this task
     */
    void beforeExecute(Thread t);

    /**
     * Invoked upon completion of execution of this task. This method is invoked by the thread that executed the task. If non-null, the
     * {@link Throwable} is the uncaught exception that caused execution to terminate abruptly.
     * <p>
     * Implementations may leave this method empty if nothing should be performed.
     *
     * @param t The exception that caused termination, or <code>null</code> if execution completed normally
     */
    void afterExecute(Throwable t);

    /**
     * Computes a result, or throws an exception if unable to do so.
     * <p>
     * Use one of the <tt>com.openexchange.threadpool.ThreadPools.task()</tt> methods to create an appropriate {@link Task} from a
     * {@link Runnable} or {@link Callable} instance.
     * <p>
     * <code>&nbsp;&nbsp;&nbsp;&nbsp;final Task&lt;MyResult&gt; task = ThreadPools.task(myRunnable, myResult);</code>
     * <p>
     * Some useful memory rules:
     * <ul>
     * <li>Never maintain a huge state in {@link Task}. The state variables will not be explicitly <tt>GC</tt>'ed as long as a reference to
     * the returned {@link Future} is held.</li>
     * <li>If you need to have a lot of state in {@link Task}, ensure that you clean them up at the end of the call method by setting them
     * to <code>null</code></li>
     * <li>Never hang on to the returned {@link Future} indiscriminately. This will prevent the {@link Task} and its return value from being
     * <tt>GC</tt>'ed.</li>
     * </ul>
     *
     * @return The computed result or <code>null</code>
     * @throws Exception If unable to compute a result
     */
    @Override
    V call() throws Exception;

}
