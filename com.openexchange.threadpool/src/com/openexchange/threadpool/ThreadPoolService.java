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

package com.openexchange.threadpool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ThreadPoolService} - The thread pool service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface ThreadPoolService {

    /**
     * Returns <tt>true</tt> if this thread pool has been shut down.
     *
     * @return <tt>true</tt> if this thread pool has been shut down
     */
    boolean isShutdown();

    /**
     * Returns <tt>true</tt> if all tasks have completed following a shut-down.
     *
     * @return <tt>true</tt> if all tasks have completed following a shut-down
     */
    boolean isTerminated();

    /**
     * Submits a value-returning task for execution and returns a Future representing the pending results of the task.
     * <p>
     * If you would like to immediately block waiting for a task, you can use constructions of the form
     * <tt>result = pool.submit(aCallable).get();</tt>
     *
     * @param task The task to submit
     * @return A PendingCompletion representing pending completion of the task
     * @throws RejectedExecutionException If task cannot be scheduled for execution and <tt>"abort"</tt> is the configured default behavior
     * @throws NullPointerException If task is <code>null</code>
     */
    <T> Future<T> submit(Task<T> task);

    /**
     * Submits a value-returning task for execution and returns a Future representing the pending results of the task.
     * <p>
     * The caller may pass his own refused execution behavior for given task.
     * <p>
     * If you would like to immediately block waiting for a task, you can use constructions of the form
     * <tt>result = pool.submit(aCallable).get();</tt>
     *
     * @param task The task to submit
     * @param refusedExecutionBehavior The behavior to obey when execution is rejected or <code>null</code> to use pool's configured default
     *            behavior
     * @return A PendingCompletion representing pending completion of the task
     * @throws RejectedExecutionException If task cannot be scheduled for execution
     * @throws NullPointerException If task is <code>null</code>
     */
    <T> Future<T> submit(Task<T> task, RefusedExecutionBehavior<T> refusedExecutionBehavior);

    /**
     * Executes the given tasks, returning a list of Futures holding their status and results when all complete. {@link Future#isDone} is
     * <tt>true</tt> for each element of the returned list. Note that a <em>completed</em> task could have terminated either normally or by
     * throwing an exception. The results of this method are undefined if the given collection is modified while this operation is in
     * progress.
     *
     * @param tasks The collection of tasks
     * @return A list of Futures representing the tasks, in the same sequential order as produced by the iterator for the given task list,
     *         each of which has completed.
     * @throws InterruptedException If interrupted while waiting, in which case unfinished tasks are canceled.
     * @throws NullPointerException If tasks or any of its elements are <tt>null</tt>
     * @throws RejectedExecutionException If any task cannot be scheduled for execution
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Task<T>> tasks) throws InterruptedException;

    /**
     * Executes the given tasks, returning a list of Futures holding their status and results when all complete or the timeout expires,
     * whichever happens first. {@link Future#isDone} is <tt>true</tt> for each element of the returned list. Upon return, tasks that have
     * not completed are canceled. Note that a <em>completed</em> task could have terminated either normally or by throwing an exception.
     * The results of this method are undefined if the given collection is modified while this operation is in progress.
     *
     * @param tasks The collection of tasks
     * @param timeout The maximum time in milliseconds to wait
     * @return A list of Futures representing the tasks, in the same sequential order as produced by the iterator for the given task list.
     *         If the operation did not time out, each task will have completed. If it did time out, some of these tasks will not have
     *         completed.
     * @throws InterruptedException If interrupted while waiting, in which case unfinished tasks are canceled.
     * @throws NullPointerException If tasks, or any of its elements are <tt>null</tt>
     * @throws RejectedExecutionException If any task cannot be scheduled for execution
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Task<T>> tasks, long timeout) throws InterruptedException;

    /**
     * Executes the given task using this thread pool. Returned {@link CompletionFuture} can then be used to await completion of given
     * tasks. Unlike <tt>invokeAll()</tt> tasks' execution is completely decoupled from the consumption of the tasks through returned
     * {@link CompletionFuture}.
     * <p>
     * When awaiting completion of given tasks, programmer should obey the following pattern:
     *
     * <pre>
     * try {
     *     for (int i = tasks.size(); i &gt; 0; i--) {
     *         // Waits until next task has completed; otherwise poll() method needs to be used to define a timeout
     *         final Future&lt;V&gt; f = completionFuture.take();
     *         /* Do something &#42;/
     *     }
     * } catch (InterruptedException e) {
     *     // Keep interrupted status
     *     Thread.currentThread().interrupt();
     *     throw new OXException(OXException.Code.INTERRUPT_ERROR, e);
     * } catch (CancellationException e) {
     *     // Can only occur if task was canceled
     *     /* Do something &#42;/
     * } catch (ExecutionException e) {
     *     throw ThreadPools.launderThrowable(e, ExpectedException.class);
     * }
     * </pre>
     *
     * @param tasks The collection of tasks
     * @return A {@link CompletionFuture} instance to await completion of given tasks
     * @throws RejectedExecutionException If task cannot be scheduled for execution
     * @throws NullPointerException If tasks or any of its elements are <tt>null</tt>
     */
    <T> CompletionFuture<T> invoke(Collection<? extends Task<T>> tasks);

    /**
     * Executes the given taskd using this thread pool. Returned {@link CompletionFuture} can then be used to await completion of given
     * tasks. Unlike <tt>invokeAll()</tt> tasks' execution is completely decoupled from the consumption of the tasks through returned
     * {@link CompletionFuture}.
     * <p>
     * When awaiting completion of given tasks, programmer should obey the following pattern:
     *
     * <pre>
     * try {
     *     for (int i = tasks.size(); i &gt; 0; i--) {
     *         // Waits until next task has completed; otherwise poll() method needs to be used to define a timeout
     *         final Future&lt;V&gt; f = completionFuture.take();
     *         /* Do something &#42;/
     *     }
     * } catch (InterruptedException e) {
     *     // Keep interrupted status
     *     Thread.currentThread().interrupt();
     *     throw new OXException(OXException.Code.INTERRUPT_ERROR, e);
     * } catch (CancellationException e) {
     *     // Can only occur if task was canceled
     *     /* Do something &#42;/
     * } catch (ExecutionException e) {
     *     throw ThreadPools.launderThrowable(e, ExpectedException.class);
     * }
     * </pre>
     *
     * @param tasks The collection of tasks
     * @return A {@link CompletionFuture} instance to await completion of given tasks
     * @throws RejectedExecutionException If task cannot be scheduled for execution
     * @throws NullPointerException If tasks or any of its elements are <tt>null</tt>
     */
    <T> CompletionFuture<T> invoke(Task<T>[] tasks);

    /**
     * Executes the given task using this thread pool. Given refused execution behavior is triggered for each task that cannot be executed.
     * Returned {@link CompletionFuture} can then be used to await completion of given tasks. Unlike <tt>invokeAll()</tt> tasks' execution
     * is completely decoupled from the consumption of the tasks through returned {@link CompletionFuture}.
     * <p>
     * When awaiting completion of given tasks, programmer should obey the following pattern:
     *
     * <pre>
     * try {
     *     for (int i = tasks.size(); i &gt; 0; i--) {
     *         // Awaits until next task has completed; otherwise poll() method needs to be used to define a timeout
     *         final Future&lt;V&gt; f = completionFuture.take();
     *         /* Do something &#42;/
     *     }
     * } catch (InterruptedException e) {
     *     // Keep interrupted status
     *     Thread.currentThread().interrupt();
     *     throw new OXException(OXException.Code.INTERRUPT_ERROR, e);
     * } catch (CancellationException e) {
     *     // Can only occur if task was canceled
     *     /* Do something &#42;/
     * } catch (ExecutionException e) {
     *     final Throwable t = e.getCause();
     *     if (t instanceof ExpectedException) {
     *         // An expected exception type
     *         throw (ExpectedException) t;
     *     } else if (t instanceof RuntimeException) {
     *         throw (RuntimeException) t;
     *     } else if (t instanceof Error) {
     *         throw (Error) t;
     *     } else {
     *         throw new IllegalStateException(&quot;Not unchecked&quot;, t);
     *     }
     * }
     * </pre>
     *
     * @param tasks The collection of tasks
     * @param refusedExecutionBehavior The behavior to obey when execution is rejected or <code>null</code> to use pool's configured default
     *            behavior
     * @return A {@link CompletionFuture} instance to await completion of given tasks
     * @throws RejectedExecutionException If task cannot be scheduled for execution
     * @throws NullPointerException If tasks or any of its elements are <tt>null</tt>
     */
    <T> CompletionFuture<T> invoke(Collection<? extends Task<T>> tasks, RefusedExecutionBehavior<T> refusedExecutionBehavior);

    /**
     * Gets the {@link ExecutorService} view on this thread pool.
     * <p>
     * <b>Note</b>: Shut-down operations are not permitted and will throw an {@link UnsupportedOperationException}.
     *
     * @return The {@link ExecutorService} view on this thread pool
     */
    ExecutorService getExecutor();

    /**
     * Spawns a new {@link ExecutorService} from this thread pool which only uses thread pool's core size as number of concurrent active tasks.
     * <p>
     * <b>Note</b>: Shut-down operations are not permitted and will throw an {@link UnsupportedOperationException}.
     *
     * @return The fixed-size {@link ExecutorService} backed by this thread pool
     */
    ExecutorService getFixedExecutor();

    /**
     * Spawns a new {@link ExecutorService} from this thread pool which only uses specified number of concurrent active tasks.
     * <p>
     * <b>Note</b>: Shut-down operations are not permitted and will throw an {@link UnsupportedOperationException}.
     *
     * @param size The number of concurrent active tasks.
     * @return The fixed-size {@link ExecutorService} backed by this thread pool
     */
    ExecutorService getFixedExecutor(int size);

    /* Statistics */

    /**
     * Returns the current number of threads in the pool.
     *
     * @return The number of threads
     */
    int getPoolSize();

    /**
     * Returns the approximate number of threads that are actively executing tasks.
     *
     * @return The number of threads
     */
    int getActiveCount();

    /**
     * Returns the largest number of threads that have ever simultaneously been in the pool.
     *
     * @return The number of threads
     */
    int getLargestPoolSize();

    /**
     * Returns the approximate total number of tasks that have been scheduled for execution. Because the states of tasks and threads may
     * change dynamically during computation, the returned value is only an approximation, but one that does not ever decrease across
     * successive calls.
     *
     * @return The number of tasks
     */
    long getTaskCount();

    /**
     * Returns the approximate total number of tasks that have completed execution. Because the states of tasks and threads may change
     * dynamically during computation, the returned value is only an approximation, but one that does not ever decrease across successive
     * calls.
     *
     * @return The number of tasks
     */
    long getCompletedTaskCount();
}
