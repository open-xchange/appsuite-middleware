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

package com.openexchange.ajp13.najp.threadpool;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.watcher.AJPv13TaskWatcher;
import com.openexchange.monitoring.MonitoringInfo;

/**
 * {@link AJPv13ThreadPoolExecutor} - Custom {@link ThreadPoolExecutor} for AJP module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class AJPv13ThreadPoolExecutor extends ThreadPoolExecutor {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13ThreadPoolExecutor.class));

    private final AtomicInteger numRunning;

    /**
     * Initializes a new {@link AJPv13ThreadPoolExecutor}.
     *
     * @param keepAliveTime When the number of threads is greater than the core, this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit The time unit for the <code>keepAliveTime</code> argument.
     * @param watcher The AJP watcher instance
     */
    public AJPv13ThreadPoolExecutor(final long keepAliveTime, final TimeUnit unit, final AJPv13TaskWatcher watcher) {
        super(
            getCorePoolSize(AJPv13Config.getAJPListenerPoolSize()),
            Integer.MAX_VALUE,
            keepAliveTime,
            unit,
            AJPv13SynchronousQueueProvider.getInstance().newSynchronousQueue(Runnable.class),
            new AJPv13ThreadFactory("AJPListener-")
            /*,new AJPv13RefusedExecutionBehavior(watcher)*/);
        numRunning = new AtomicInteger();
    }

    private static int getCorePoolSize(final int desiredCorePoolSize) {
        final int minCorePoolSize = Runtime.getRuntime().availableProcessors() + 1;
        if (desiredCorePoolSize < minCorePoolSize) {
            LOG.warn(new StringBuilder(128).append("\n\n\tConfigured pool size of ").append(desiredCorePoolSize).append(
                " through property \"AJP_LISTENER_POOL_SIZE\" does not obey the rule\n\tfor minimum core pool size: ").append(
                Runtime.getRuntime().availableProcessors()).append(" (number of CPUs) + 1 = ").append(minCorePoolSize).append(". Using ").append(
                minCorePoolSize).append(" as core pool size.\n"));
            return minCorePoolSize;
        }
        return desiredCorePoolSize;
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        super.beforeExecute(t, r);
        changeNumberOfRunningAJPTasks(true);
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        changeNumberOfRunningAJPTasks(false);
        super.afterExecute(r, t);
    }

    /**
     * Increments/decrements the number of running AJP tasks.
     *
     * @param increment whether to increment or to decrement
     */
    private void changeNumberOfRunningAJPTasks(final boolean increment) {
        MonitoringInfo.setNumberOfRunningAJPListeners(increment ? numRunning.incrementAndGet() : numRunning.decrementAndGet());
    }

}
