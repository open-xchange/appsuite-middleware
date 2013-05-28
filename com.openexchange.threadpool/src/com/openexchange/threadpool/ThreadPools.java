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

package com.openexchange.threadpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.threadpool.internal.CustomThreadFactory;
import com.openexchange.threadpool.osgi.ThreadPoolActivator;
import com.openexchange.timer.TimerService;

/**
 * {@link ThreadPools} - Utility methods for {@link ThreadPoolService} and {@link Task}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadPools {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ThreadPools.class));

    /**
     * Initializes a new {@link ThreadPools}.
     */
    private ThreadPools() {
        super();
    }

    /**
     * Gets registered thread pool.
     *
     * @return The thread pool or <code>null</code>
     */
    public static ThreadPoolService getThreadPool() {
        return ThreadPoolActivator.REF_THREAD_POOL.get();
    }

    /**
     * Gets registered timer service.
     *
     * @return The timer service or <code>null</code>
     */
    public static TimerService getTimerService() {
        return ThreadPoolActivator.REF_TIMER.get();
    }

    /**
     * Handles specified unexpectedly interrupted thread.
     *
     * @param t The unexpectedly interrupted thread
     */
    public static void unexpectedlyInterrupted(final Thread t) {
        // Keep interrupted flag
        t.interrupt();
        if (t instanceof InterruptorAware) {
            final StackTraceElement[] interruptorStack = ((InterruptorAware) t).getInterruptorStack();
            if (null != interruptorStack) {
                final StringBuilder sb = new StringBuilder(256).append("Thread interrupted unexpectedly at:");
                if (Log.appendTraceToMessage()) {
                    final String lineSeparator = System.getProperty("line.separator");
                    sb.append(lineSeparator);
                    appendStackTrace(interruptorStack, sb, lineSeparator);
                    LOG.error(sb.toString());
                } else {
                    final Throwable th = new Throwable();
                    th.setStackTrace(interruptorStack);
                    LOG.error(sb.toString(), th);
                }
            }
        }
    }

    /**
     * Appends specified stack trace to given {@link StringBuilder} instance.
     *
     * @param trace The stack trace
     * @param sb The string builder to write to
     */
    public static void appendStackTrace(final StackTraceElement[] trace, final StringBuilder sb, final String lineSeparator) {
        if (null == trace) {
            sb.append("<missing stack trace>\n");
            return;
        }
        for (final StackTraceElement ste : trace) {
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

    /**
     * Appends current thread's stack trace to given {@link StringBuilder} instance.
     *
     * @param sb The string builder to write to
     */
    public static void appendCurrentStackTrace(final StringBuilder sb, final String lineSeparator) {
        appendStackTrace(new Throwable().getStackTrace(), sb, lineSeparator);
    }

    public interface ExpectedExceptionFactory<E extends Exception> {

        /**
         * Gets the exception's type.
         *
         * @return The exception's type
         */
        Class<E> getType();

        /**
         * Creates a new exception from given unexpected (checked) exception.
         * <p>
         * Passed {@link Throwable} instance is either a {@link IllegalStateException} wrapping a {@link Throwable} or a
         * {@link InterruptedException}
         *
         * @param t The unexpected (checked) exception
         * @return A new exception
         */
        E newUnexpectedError(Throwable t);

    }

    /**
     * Polls given completion service and returns its results as a list.
     *
     * @param <R> The result type
     * @param <E> The exception type
     * @param completionService The completion service to poll
     * @param size The number of tasks performed by completion service
     * @param timeoutMillis The time-out in milliseconds
     * @param factory The exception factory to launder a possible {@link ExecutionException} or {@link InterruptedException}
     * @return A list of results polled from completion service
     * @throws E If polling completion service fails
     * @throws IllegalStateException If cause is neither a {@link RuntimeException} nor an {@link Error} but a checked exception
     * @throws RuntimeException If cause is an unchecked {@link RuntimeException}
     * @throws Error If cause is an unchecked {@link Error}
     * @see #launderThrowable(ExecutionException, Class)
     */
    public static <R, E extends Exception> java.util.List<R> pollCompletionService(final CompletionService<R> completionService, final int size, final long timeoutMillis, final ExpectedExceptionFactory<E> factory) throws E {
        /*
         * Wait for completion
         */
        try {
            final java.util.List<R> ret = new ArrayList<R>(size);
            for (int i = 0; i < size; i++) {
                final Future<R> f = completionService.poll(timeoutMillis, TimeUnit.MILLISECONDS);
                if (null != f) {
                    ret.add(f.get());
                } else if (LOG.isWarnEnabled()) {
                    LOG.warn(new StringBuilder(32).append("Completion service's task elapsed time-out of ").append(timeoutMillis).append(
                        "msec").toString());
                }
            }
            return ret;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw factory.newUnexpectedError(e);
        } catch (final ExecutionException e) {
            throw launderThrowable(e, factory.getType());
        }
    }

    /**
     * The default exception factory for {@link OXException} class.
     */
    public static final ExpectedExceptionFactory<OXException> DEFAULT_EXCEPTION_FACTORY = new ExpectedExceptionFactory<OXException>() {

        @Override
        public Class<OXException> getType() {
            return OXException.class;
        }

        @Override
        public OXException newUnexpectedError(final Throwable t) {
            return new OXException(t);
        }

    };

    /**
     * Takes from given completion service and returns its results as a list.
     *
     * @param <R> The result type
     * @param <E> The exception type
     * @param completionService The completion service to take from
     * @param size The number of tasks performed by completion service
     * @param factory The exception factory to launder a possible {@link ExecutionException} or {@link InterruptedException}
     * @return A list of results taken from completion service
     * @throws E If taking from completion service fails
     * @throws IllegalStateException If cause is neither a {@link RuntimeException} nor an {@link Error} but a checked exception
     * @throws RuntimeException If cause is an unchecked {@link RuntimeException}
     * @throws Error If cause is an unchecked {@link Error}
     * @see #launderThrowable(ExecutionException, Class)
     * @see #DEFAULT_EXCEPTION_FACTORY
     */
    public static <R, E extends Exception> java.util.List<R> takeCompletionService(final CompletionService<R> completionService, final int size, final ExpectedExceptionFactory<E> factory) throws E {
        /*
         * Wait for completion
         */
        try {
            final java.util.List<R> ret = new ArrayList<R>(size);
            for (int i = 0; i < size; i++) {
                ret.add(completionService.take().get());
            }
            return ret;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw factory.newUnexpectedError(e);
        } catch (final ExecutionException e) {
            throw launderThrowable(e, factory.getType());
        }
    }

    /**
     * Handles given {@link Throwable} in a safe way.
     * <p>
     * This method is helpful when dealing with {@link ExecutionException}:
     *
     * <pre>
     * public void myMethod throws MyException {
     *  ...
     *  final Future&lt;MyResult&gt; future = threadPoolService.submit(task);
     *  try {
     *      return future.get();
     *  } catch (final ExecutionException e) {
     *      throw launderThrowable(e.getCause(), MyException.class);
     *  }
     *  ...
     * }
     * </pre>
     *
     * @param e The execution exception thrown by an asynchronous computation
     * @param expectedExceptionType The expected exception type or <code>null</code> if nothing is expected
     * @return The laundered exception
     * @throws IllegalStateException If cause is neither a {@link RuntimeException} nor an {@link Error} but a checked exception
     * @throws RuntimeException If cause is an unchecked {@link RuntimeException}
     * @throws Error If cause is an unchecked {@link Error}
     */
    public static <E extends Exception> E launderThrowable(final ExecutionException e, final Class<E> expectedExceptionType) {
        final Throwable t = e.getCause();
        if (null != expectedExceptionType && expectedExceptionType.isInstance(t)) {
            return expectedExceptionType.cast(t);
        }
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new IllegalStateException("Not unchecked", t);
        }
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
     * Returns a {@link Task} object that, when called, runs the given task and returns the given result. This can be useful when applying
     * methods requiring a <tt>Task</tt> to an otherwise resultless action.
     *
     * @param task The task to run
     * @param result The result to return
     * @param trackable Whether the task is trackable
     * @throws NullPointerException If task is <code>null</code>
     * @return A {@link Task} object
     */
    public static <T> Task<T> task(final Runnable task, final T result, final boolean trackable) {
        if (task == null) {
            throw new NullPointerException();
        }
        final RunnableAdapter<T> callable = new RunnableAdapter<T>(task, result);
        return trackable ? new TrackableTaskAdapter<T>(callable) : new TaskAdapter<T>(callable);
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
     * Returns a trackable {@link Task} object that, when called, runs the given task and returns <tt>null</tt>.
     *
     * @param task The task to run
     * @return A {@link Task} object
     * @throws NullPointerException If task is <code>null</code>
     */
    public static Task<Object> trackableTask(final Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        return new TrackableTaskAdapter<Object>(new RunnableAdapter<Object>(task, null));
    }

    /**
     * @param trackable Whether the task is trackable
     * @return A {@link Task} object
     * @throws NullPointerException If task is <code>null</code>
     */
    public static Task<Object> task(final Runnable task, final boolean trackable) {
        if (task == null) {
            throw new NullPointerException();
        }
        final RunnableAdapter<Object> callable = new RunnableAdapter<Object>(task, null);
        return trackable ? new TrackableTaskAdapter<Object>(callable) : new TaskAdapter<Object>(callable);
    }

    /**
     * Returns a {@link Task} object that, when called, runs the given task, renames thread's prefix and returns <tt>null</tt>.
     *
     * @param task The task to run
     * @param prefix The thread's prefix
     * @return A {@link Task} object
     * @throws NullPointerException If task is <code>null</code>
     */
    public static Task<Object> task(final Runnable task, final String prefix) {
        if (task == null || prefix == null) {
            throw new NullPointerException();
        }
        return new RenamingTaskAdapter<Object>(new RunnableAdapter<Object>(task, null), prefix);
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
     * Returns a {@link Task} object that, when called, returns the given task's result.
     *
     * @param task The task to run
     * @param trackable Whether the task is trackable
     * @return A {@link Task} object
     * @throws NullPointerException If task is <code>null</code>
     */
    public static <T> Task<T> task(final Callable<T> task, final boolean trackable) {
        if (task == null) {
            throw new NullPointerException();
        }
        return trackable ? new TrackableTaskAdapter<T>(task) : new TaskAdapter<T>(task);
    }

    /**
     * Returns a {@link Task} object that, when called, returns the given task's result.
     *
     * @param task The task to run
     * @param prefix The thread's prefix
     * @return A {@link Task} object
     * @throws NullPointerException If task is <code>null</code>
     */
    public static <T> Task<T> task(final Callable<T> task, final String prefix) {
        if (task == null) {
            throw new NullPointerException();
        }
        return new RenamingTaskAdapter<T>(task, prefix);
    }

    /**
     * Initializes a new {@link ThreadFactory}.
     *
     * @param namePrefix The name prefix for created threads; e.g. "MyWorker-"
     */
    public static ThreadFactory newThreadFactory(final String namePrefix) {
        return new CustomThreadFactory(namePrefix);
    }

    /**
     * Creates a new <code>CancelableCompletionService</code> from passed thread pool.
     *
     * @param threadPool The thread pool
     * @return A newly created <code>CancelableCompletionService</code>
     */
    public static <V> CancelableCompletionService<V> newCompletionService(final ThreadPoolService threadPool) {
        return new ThreadPoolCompletionService<V>(threadPool);
    }

    /**
     * The dummy {@link ExecutorService} using current thread.
     */
    public static final ExecutorService CURRENT_THREAD_EXECUTOR_SERVICE = new CurrentThreadExecutorService();

    /**
     * A {@link Callable} that runs given task and returns given result
     */
    private static class RunnableAdapter<T> implements Callable<T> {

        private final Runnable task;

        private final T result;

        RunnableAdapter(final Runnable task, final T result) {
            this.task = task;
            this.result = result;
        }

        @Override
        public T call() {
            task.run();
            return result;
        }

    }

    private static class TaskAdapter<V> implements Task<V> {

        private final Callable<V> callable;

        /**
         * Initializes a new {@link TaskAdapter}.
         */
        TaskAdapter(final Callable<V> callable) {
            super();
            this.callable = callable;
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

        @Override
        public V call() throws Exception {
            return callable.call();
        }

    }

    private static class TrackableTaskAdapter<V> extends TaskAdapter<V> implements Trackable {
        private final Props props;
        TrackableTaskAdapter(final Callable<V> callable) {
            super(callable);
            this.props = LogProperties.optLogProperties(Thread.currentThread());
        }

        @Override
        public Props optLogProperties() {
            return props;
        }
    }

    private static class RenamingTaskAdapter<V> implements Task<V> {

        private final Callable<V> callable;

        private final String prefix;

        /**
         * Initializes a new {@link TaskAdapter}.
         */
        RenamingTaskAdapter(final Callable<V> callable, final String prefix) {
            super();
            this.callable = callable;
            this.prefix = prefix;
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
            threadRenamer.renamePrefix(prefix);
        }

        @Override
        public V call() throws Exception {
            return callable.call();
        }

    }

    private static class TrackableRenamingTaskAdapter<V> extends RenamingTaskAdapter<V> implements Trackable {

        private final Props props;
        TrackableRenamingTaskAdapter(final Callable<V> callable, final String prefix) {
            super(callable, prefix);
            this.props = LogProperties.optLogProperties(Thread.currentThread());
        }

        @Override
        public Props optLogProperties() {
            return props;
        }
    }

    private static class CurrentThreadExecutorService extends java.util.concurrent.AbstractExecutorService {

        public CurrentThreadExecutorService() {
            super();
        }

        @Override
        public void shutdown() {
            // Nothing to do
        }

        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void execute(final Runnable command) {
            command.run();
        }
    }

    /**
     * Combines Callable and Trackable.
     */
    public static abstract class TrackableCallable<V> implements Callable<V>, Trackable {

        /** The properties */
        protected final Props props;

        /**
         * Initializes a new {@link TrackableCallable}.
         */
        protected TrackableCallable() {
            this(LogProperties.optLogProperties(Thread.currentThread()));
        }

        /**
         * Initializes a new {@link TrackableCallable}.
         *
         * @param props The properties
         */
        protected TrackableCallable(final Props props) {
            super();
            this.props = props;
        }

        @Override
        public Props optLogProperties() {
            return props;
        }
    }

}
