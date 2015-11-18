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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.tools.images.scheduler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.images.osgi.Services;
import com.openexchange.tools.images.scheduler.Scheduler.Selector;

/**
 * {@link SchedulerThreadPoolExecutor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class SchedulerThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulerThreadPoolExecutor.class);

    private final Scheduler scheduler;

    /**
     * Initializes a new {@link SchedulerThreadPoolExecutor}.
     *
     * @param nThreads The number of threads in the pool
     * @param scheduler The parental {@link Scheduler} instance owning this thread pool
     */
    public SchedulerThreadPoolExecutor(int nThreads, Scheduler scheduler) {
        // See java.util.concurrent.Executors.newFixedThreadPool(int, ThreadFactory)
        super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new SchedulerThreadFactory());
        this.scheduler = scheduler;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (t != null) {
            // The RuntimeException or Error that caused execution to terminate abruptly.
            ExceptionUtils.handleThrowable(t);

            if (r instanceof Selector) {
                LOG.info("Image transformation selector thread '{}' terminated abruptly.", Thread.currentThread().getName(), t);

                TimerService optService = Services.optService(TimerService.class);
                if (null != optService) {
                    optService.schedule(new SelectorAdder(this, scheduler.newSelector()), 1, TimeUnit.SECONDS);
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private static final class SelectorAdder implements Runnable {

        private final SchedulerThreadPoolExecutor threadPool;
        private final Selector selector;

        SelectorAdder(SchedulerThreadPoolExecutor threadPool, Selector selector) {
            this.threadPool = threadPool;
            this.selector = selector;
        }

        @Override
        public void run() {
            threadPool.execute(selector);
        }
    }

}
