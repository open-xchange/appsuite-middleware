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

package com.openexchange.java;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * {@link RunOrGetFutureTask} - Extends {@link FutureTask} by "either run or get" semantics when invoking one of {@link #get()} or {@link #get(long, TimeUnit)} methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class RunOrGetFutureTask<V> extends FutureTask<V> {

    /**
     * Creates a {@code RunOrGetFutureTask} that will execute the given {@code Callable} either by explicit call to <code>run</code> or
     * by one of its <code>get</code> methods.
     *
     * @param callable The callable task
     * @throws NullPointerException If the callable is <code>null</code>
     */
    public static <V> RunOrGetFutureTask<V> newInstance(Callable<V> callable) {
        return new RunOrGetFutureTask<V>(callable);
    }

    /**
     * Creates a {@code RunOrGetFutureTask} that will execute the given {@code Runnable} either by explicit call to <code>run</code> or
     * by one of its <code>get</code> methods. {@code get} will return the given result on successful completion.
     *
     * @param runnable The runnable task
     * @param result The result to return on successful completion or <code>null</code>
     * @throws NullPointerException If the runnable is <code>null</code>
     */
    public static <V> RunOrGetFutureTask<V> newInstance(Runnable runnable, V result) {
        return new RunOrGetFutureTask<V>(Executors.callable(runnable, result));
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link RunOrGetFutureTask}.
     *
     * @param callable The callable task
     */
    public RunOrGetFutureTask(Callable<V> callable) {
        super(callable);
    }

    /**
     * Initializes a new {@link RunOrGetFutureTask}.
     *
     * @param runnable The runnable task
     * @param result The result on successful completion
     */
    public RunOrGetFutureTask(Runnable runnable, V result) {
        super(runnable, result);
    }

    /**
     * <ul>
     * <li>Either atomically executes this task's callable (if not yet done) and returns its result
     * <li>Or waits if necessary for the computation to complete, and then retrieves its result.
     * </ul>
     *
     * @return The computed result
     * @throws CancellationException If the computation was cancelled
     * @throws ExecutionException If the computation threw an exception
     * @throws InterruptedException If the current thread was interrupted while waiting
     */
    @Override
    public V get() throws InterruptedException, ExecutionException {
        run();
        return super.get();
    }

    /**
     * <ul>
     * <li>Either atomically executes this task's callable (if not yet done) and returns its result
     * <li>Or waits if necessary for at most the given time for the computation to complete, and then retrieves its result, if available.
     * </ul>
     *
     * @param timeout The maximum time to wait
     * @param unit The time unit of the timeout argument
     * @return The computed result
     * @throws CancellationException If the computation was cancelled
     * @throws ExecutionException If the computation threw an exception
     * @throws InterruptedException If the current thread was interrupted while waiting
     * @throws TimeoutException If the wait timed out
     */
    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        run();
        return super.get(timeout, unit);
    }

}
