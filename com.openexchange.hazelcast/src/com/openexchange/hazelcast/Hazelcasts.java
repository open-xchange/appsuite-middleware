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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.hazelcast;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.OperationTimeoutException;
import com.hazelcast.nio.serialization.Portable;

/**
 * {@link Hazelcasts} - Utility methods for Hazelcast.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class Hazelcasts {

    /**
     * Initializes a new {@link Hazelcasts}.
     */
    private Hazelcasts() {
        super();
    }

    /**
     * Gets the remote members from specified Hazelcast instance.
     *
     * @param hazelcastInstance The Hazelcast instance for the cluster
     * @return The remote members
     */
    public static Set<Member> getRemoteMembers(HazelcastInstance hazelcastInstance) {
        if (null == hazelcastInstance) {
            return Collections.emptySet();
        }

        // Get cluster representation
        Cluster cluster = hazelcastInstance.getCluster();

        // Get local member
        Member localMember = cluster.getLocalMember();

        // Determine other cluster members
        Set<Member> otherMembers = new LinkedHashSet<Member>(cluster.getMembers());
        otherMembers.remove(localMember);
        return otherMembers;
    }

    /**
     * Executes specified ({@link Portable portable}) task by remote members using the <code>"default"</code> {@link IExecutorService executor} from specified Hazelcast instance.
     * <p>
     * Each member is probed three times in case an {@link OperationTimeoutException} occurs.
     *
     * @param task The ({@link Portable portable}) task to execute
     * @param remoteMembers The remote members by which the task is supposed to be executed
     * @param hazelcastInstance The Hazelcast instance to obtain the executor from
     * @return The results by members
     * @throws ExecutionException If execution fails on any remote member
     */
    public static <R> Map<Member, R> executeByMembers(Callable<R> task, Set<Member> remoteMembers, HazelcastInstance hazelcastInstance) throws ExecutionException {
        if (null == hazelcastInstance ) {
            return Collections.emptyMap();
        }

        return executeByMembers(task, remoteMembers, hazelcastInstance.getExecutorService("default"));
    }

    /**
     * Executes specified ({@link Portable portable}) task by remote members.
     * <p>
     * Each member is probed three times in case an {@link OperationTimeoutException} occurs.
     *
     * @param task The ({@link Portable portable}) task to execute
     * @param remoteMembers The remote members by which the task is supposed to be executed
     * @param executor The executor to use
     * @return The results by members
     * @throws ExecutionException If execution fails on any remote member
     */
    public static <R> Map<Member, R> executeByMembers(Callable<R> task, Set<Member> remoteMembers, IExecutorService executor) throws ExecutionException {
        if (null == task || null == executor || null == remoteMembers || remoteMembers.isEmpty() || !(task instanceof Portable)) {
            return Collections.emptyMap();
        }

        Map<Member, Future<R>> futureMap = executor.submitToMembers(task, remoteMembers);
        Map<Member, R> results = new LinkedHashMap<>(futureMap.size());
        for (Map.Entry<Member, Future<R>> entry : futureMap.entrySet()) {
            Future<R> future = entry.getValue();
            // Check Future's return value
            int retryCount = 3;
            while (retryCount-- > 0) {
                try {
                    R result = future.get();
                    retryCount = 0;
                    results.put(entry.getKey(), result);
                } catch (InterruptedException e) {
                    // Interrupted - Keep interrupted state
                    Thread.currentThread().interrupt();
                    retryCount = 0;
                } catch (CancellationException e) {
                    // Canceled
                    retryCount = 0;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();

                    // Check for Hazelcast timeout
                    if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                        throw e;
                    }

                    // Timeout while awaiting remote result
                    if (retryCount <= 0) {
                        // No further retry
                        cancelFutureSafe(future);
                    }
                }
            }
        }
        return results;
    }

    /** A filter for a result */
    public static interface Filter<R, F> {

        /**
         * Checks whether specified result is accepted by this filter
         *
         * @param result The result to check
         * @return The accepted result or <code>null</code>
         */
        F accept(R result);
    }

    /**
     * Executes specified ({@link Portable portable}) task by remote members and filters results.
     * <p>
     * Each member is probed three times in case an {@link OperationTimeoutException} occurs.
     *
     * @param task The ({@link Portable portable}) task to execute
     * @param remoteMembers The remote members by which the task is supposed to be executed
     * @param executor The executor to use
     * @param filter The filter which accepts or declines a result
     * @return The first<b>(!)</b> result accepted by specified filter or <code>null</code>
     * @throws ExecutionException If execution fails on any remote member
     */
    public static <R, F> F executeByMembersAndFilter(Callable<R> task, Set<Member> remoteMembers, IExecutorService executor, Filter<R, F> filter) throws ExecutionException {
        return executeByMembersAndFilter(task, remoteMembers, executor, filter, null);
    }

    /** The constant to signal no result was available from remote nodes */
    static final Object NO_RESULT = new Object();

    /**
     * Executes specified ({@link Portable portable}) task by remote members and filters results.
     * <p>
     * Each member is probed three times in case an {@link OperationTimeoutException} occurs.
     *
     * @param task The ({@link Portable portable}) task to execute
     * @param remoteMembers The remote members by which the task is supposed to be executed
     * @param executor The executor to use
     * @param filter The filter which accepts or declines a result
     * @param fetcher An optional executor to check remote results concurrently
     * @return The first<b>(!)</b> result accepted by specified filter or <code>null</code>
     * @throws ExecutionException If execution fails on any remote member
     */
    public static <R, F> F executeByMembersAndFilter(Callable<R> task, Set<Member> remoteMembers, IExecutorService executor, Filter<R, F> filter, ExecutorService fetcher) throws ExecutionException {
        if (null == task || null == executor || null == filter || null == remoteMembers || remoteMembers.isEmpty() || !(task instanceof Portable)) {
            return null;
        }

        Map<Member, Future<R>> futureMap = executor.submitToMembers(task, remoteMembers);
        int size = futureMap.size();
        if (null == fetcher || 1 == size) {
            for (Iterator<Map.Entry<Member, Future<R>>> it = futureMap.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Member, Future<R>> entry = it.next();
                Future<R> future = entry.getValue();
                // Check Future's return value
                int retryCount = 3;
                while (retryCount-- > 0) {
                    try {
                        R result = future.get();
                        retryCount = 0;
                        F accepted = filter.accept(result);
                        if (null != accepted) {
                            cancelRest(it);
                            return accepted;
                        }
                    } catch (InterruptedException e) {
                        // Interrupted - Keep interrupted state
                        Thread.currentThread().interrupt();
                        retryCount = 0;
                    } catch (CancellationException e) {
                        // Canceled
                        retryCount = 0;
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();

                        // Check for Hazelcast timeout
                        if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                            throw e;
                        }

                        // Timeout while awaiting remote result
                        if (retryCount <= 0) {
                            // No further retry
                            cancelFutureSafe(future);
                        }
                    }
                }
            }
            return null;
        }

        // Use ExecutorService to obtain results from submitted tasks to remote members
        BlockingReference<Object> resultReference = new BlockingReference<Object>();
        Map<Future<Void>, RemoteFetch<R, F>> submittedLookups = new LinkedHashMap<Future<Void>, Hazelcasts.RemoteFetch<R,F>>(size);
        AtomicInteger counter = new AtomicInteger(size);
        for (Future<R> future : futureMap.values()) {
            RemoteFetch<R, F> remoteFetch = new RemoteFetch<>(resultReference, future, filter, counter);
            Future<Void> remoteFetchFuture = fetcher.submit(remoteFetch);
            submittedLookups.put(remoteFetchFuture, remoteFetch);
        }

        // Await result
        Object result;
        try {
            result = resultReference.getNonNull();
        } catch (InterruptedException e) {
            // Interrupted - Keep interrupted state
            Thread.currentThread().interrupt();
            return null;
        }

        // Abort rest (if any)
        for (Map.Entry<Future<Void>, RemoteFetch<R, F>> submitted : submittedLookups.entrySet()) {
            submitted.getValue().abortRemoteFetch();
            submitted.getKey().cancel(true);
        }

        // Return result
        if (NO_RESULT == result) {
            // No result was available from remote nodes
            return null;
        }
        if (result instanceof Throwable) {
            Throwable cause = (Throwable) result;
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
        return (F) result;
    }

    static <R> void cancelFutureSafe(Future<R> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

    private static <V, R> void cancelRest(final Iterator<Map.Entry<Member, Future<R>>> it) {
        while (it.hasNext()) {
            Future<R> future = it.next().getValue();
            cancelFutureSafe(future);
        }
    }

    private static class RemoteFetch<R, F> implements Callable<Void> {

        private final BlockingReference<Object> resultReference;
        private final Future<R> toTakeFrom;
        private final Filter<R, F> filter;
        private final AtomicInteger counter;

        RemoteFetch(BlockingReference<Object> resultReference, Future<R> toTakeFrom, Filter<R, F> filter, AtomicInteger counter) {
            super();
            this.resultReference = resultReference;
            this.toTakeFrom = toTakeFrom;
            this.filter = filter;
            this.counter = counter;
        }

        @Override
        public Void call() throws Exception {
            try {
                int retryCount = 3;
                Thread currentThread = Thread.currentThread();
                while (retryCount-- > 0 && !currentThread.isInterrupted()) {
                    try {
                        R result = toTakeFrom.get();
                        retryCount = 0;
                        F accepted = filter.accept(result);
                        if (null != accepted) {
                            resultReference.setNonNull(accepted);
                        }
                    } catch (InterruptedException e) {
                        // Interrupted - Keep interrupted state
                        currentThread.interrupt();
                        retryCount = 0;
                    } catch (CancellationException e) {
                        // Canceled
                        retryCount = 0;
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();

                        // Check for Hazelcast timeout
                        if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                            resultReference.setNonNull(cause);
                            return null;
                        }

                        // Timeout while awaiting remote result
                        if (retryCount <= 0) {
                            // No further retry
                            cancelFutureSafe(toTakeFrom);
                        }
                    }
                }
                return null;
            } finally {
                int numOfRemainingTasks = counter.decrementAndGet();
                if (numOfRemainingTasks <= 0 && false == resultReference.isSetNonNull()) {
                    // All tasks are through, but nothing was set so far
                    resultReference.setNonNull(NO_RESULT);
                }
            }
        }

        public void abortRemoteFetch() {
            toTakeFrom.cancel(true);
        }
    }

    private static class BlockingReference<T> {

        private final ReentrantLock lock;
        private final Condition notEmpty;
        private T reference;

        /**
         * Initializes a new {@link Hazelcasts.BlockingReference}.
         */
        BlockingReference() {
            super();
            lock = new ReentrantLock();
            notEmpty = lock.newCondition();
        }

        T getNonNull() throws InterruptedException {
            ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                while (reference == null) {
                    notEmpty.await();
                }
                return reference;
            } finally {
                lock.unlock();
            }
        }

        void setNonNull(T t) {
            if (t == null) {
                throw new NullPointerException();
            }

            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                reference = t;
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        boolean isSetNonNull() {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return reference != null;
            } finally {
                lock.unlock();
            }
        }
    }

}
