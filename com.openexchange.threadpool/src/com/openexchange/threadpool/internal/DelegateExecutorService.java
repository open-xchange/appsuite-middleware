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

package com.openexchange.threadpool.internal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * {@link DelegateExecutorService} - TODO Short description of this class' purpose.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DelegateExecutorService implements ExecutorService {

    private final ExecutorService executorService;

    /**
     * Initializes a new {@link DelegateExecutorService}.
     * 
     * @param executorService The executor service
     */
    public DelegateExecutorService(final ExecutorService executorService) {
        super();
        this.executorService = executorService;
    }

    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    public void execute(final Runnable command) {
        executorService.execute(command);
    }

    public <T> List<Future<T>> invokeAll(final Collection<Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(tasks, timeout, unit);
    }

    public <T> List<Future<T>> invokeAll(final Collection<Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks);
    }

    public <T> T invokeAny(final Collection<Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks, timeout, unit);
    }

    public <T> T invokeAny(final Collection<Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks);
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    public void shutdown() {
        throw new UnsupportedOperationException("Shutdown not allowed.");
    }

    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Shutdown not allowed.");
    }

    public <T> Future<T> submit(final Callable<T> task) {
        return executorService.submit(task);
    }

    public <T> Future<T> submit(final Runnable task, final T result) {
        return executorService.submit(task, result);
    }

    public Future<?> submit(final Runnable task) {
        return executorService.submit(task);
    }

    

}
