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

package com.openexchange.unifiedinbox.utility;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * {@link UnifiedINBOXExecutors} - Factory and utility methods for {@link Executor} and {@link ExecutorService}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXExecutors {

    /**
     * Initializes a new {@link UnifiedINBOXExecutors}.
     */
    private UnifiedINBOXExecutors() {
        super();
    }

    /**
     * Creates a thread pool that reuses a fixed set of threads operating off a shared unbounded queue.
     * 
     * @param numberOfThreads The number of threads in the pool
     * @return The newly created thread pool
     */
    public static ExecutorService newFixedThreadPool(final int numberOfThreads) {
        return new ThreadPoolExecutor(
            numberOfThreads,
            numberOfThreads,
            0L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new UnifiedINBOXThreadFactory());
    }

    /**
     * Creates a thread pool that creates new threads as needed, but will reuse previously constructed threads when they are available.
     * 
     * @param numberOfTasks The number of tasks that shall be executed by returned thread pool
     * @return The newly created thread pool
     */
    public static ExecutorService newCachedThreadPool(final int numberOfTasks) {
        return new ThreadPoolExecutor(
            divide(numberOfTasks),
            numberOfTasks,
            1L,
            TimeUnit.SECONDS,
            new Java6SynchronousQueue<Runnable>(),
            new UnifiedINBOXThreadFactory());
    }

    /**
     * Creates a thread pool that creates new threads as needed, but will reuse previously constructed threads when they are available.
     * 
     * @param numberOfTasks The number of tasks that shall be executed by returned thread pool
     * @param namePrefix The name prefix applied to newly created threads by returned thread pool
     * @return The newly created thread pool
     */
    public static ExecutorService newCachedThreadPool(final int numberOfTasks, final String namePrefix) {
        return new ThreadPoolExecutor(
            divide(numberOfTasks),
            numberOfTasks,
            1L,
            TimeUnit.SECONDS,
            new Java6SynchronousQueue<Runnable>(),
            new UnifiedINBOXThreadFactory(namePrefix));
    }

    private static int divide(final int number) {
        return (number / 2);
    }
}
