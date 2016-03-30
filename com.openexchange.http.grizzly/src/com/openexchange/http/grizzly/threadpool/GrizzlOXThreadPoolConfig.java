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

package com.openexchange.http.grizzly.threadpool;

import java.util.Queue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.threadpool.AbstractThreadPool;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;

/**
 * {@link GrizzlOXThreadPoolConfig} Configuration for the GrizzlOXThreadPool.
 *
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class GrizzlOXThreadPoolConfig extends ThreadPoolConfig {

    private static final ThreadPoolConfig DEFAULT = new GrizzlOXThreadPoolConfig(
        "GrizzlOX",
        AbstractThreadPool.DEFAULT_MIN_THREAD_COUNT,
        AbstractThreadPool.DEFAULT_MAX_THREAD_COUNT,
        null,
        AbstractThreadPool.DEFAULT_MAX_TASKS_QUEUED,
        AbstractThreadPool.DEFAULT_IDLE_THREAD_KEEPALIVE_TIMEOUT,
        TimeUnit.MILLISECONDS,
        null,
        Thread.NORM_PRIORITY,
        null,
        null,
        -1);

    /**
     * Initializes a new {@link GrizzlOXThreadPoolConfig}.
     *
     * @param poolName The name of pool configured via this ThreadPoolConfing
     * @param corePoolSize The minimum pool size
     * @param maxPoolSize The maximum pool size
     * @param queue The queue to use for the Runnables, may be null
     * @param queueLimit How many Runnables are allowed to be queued, -1 for unlimmited
     * @param keepAliveTime KeepAlive timeout for idle threads
     * @param timeUnit Unit for the keepAliveTime paramter
     * @param threadFactory Which ThreadFactory to use for the Creation of WorkerThreads, may be null
     * @param priority Priority
     * @param mm mm, may be null
     * @param transactionMonitor tm, may be null
     * @param transactionTimeoutMillis transaction timout ms, -1 to
     */
    public GrizzlOXThreadPoolConfig(String poolName, int corePoolSize, int maxPoolSize, Queue<Runnable> queue, int queueLimit, long keepAliveTime, TimeUnit timeUnit, ThreadFactory threadFactory, int priority, MemoryManager mm, DelayedExecutor transactionMonitor, long transactionTimeoutMillis) {
        super(
            poolName,
            corePoolSize,
            maxPoolSize,
            queue,
            queueLimit,
            keepAliveTime,
            timeUnit,
            threadFactory,
            priority,
            mm,
            transactionMonitor,
            transactionTimeoutMillis);
    }

    /**
     * Gets the default
     *
     * @return The default
     */
    public static ThreadPoolConfig getDefault() {
        return DEFAULT;
    }

}
