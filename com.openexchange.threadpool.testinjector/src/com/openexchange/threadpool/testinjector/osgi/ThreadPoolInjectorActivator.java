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

package com.openexchange.threadpool.testinjector.osgi;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;


/**
 * {@link ThreadPoolInjectorActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadPoolInjectorActivator extends HousekeepingActivator {

    private volatile ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link ThreadPoolInjectorActivator}.
     */
    public ThreadPoolInjectorActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class, TimerService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Log log = com.openexchange.log.Log.loggerFor(ThreadPoolInjectorActivator.class);
        log.info("Starting bundle: com.openexchange.threadpool.testinjector");

        final ConfigurationService configurationService = getService(ConfigurationService.class);
        if (!configurationService.getBoolProperty("com.openexchange.threadpool.testinjector.enabled", false)) {
            log.info("Bundle \"com.openexchange.threadpool.testinjector\" disabled.");
            return;
        }

        final int rampUp = configurationService.getIntProperty("com.openexchange.threadpool.testinjector.rampup", 1000);
        final int factor = configurationService.getIntProperty("com.openexchange.threadpool.testinjector.factor", 100);
        final int frequencyMillis = configurationService.getIntProperty("com.openexchange.threadpool.testinjector.freqmillis", 10000);
        final int sleepMillis = configurationService.getIntProperty("com.openexchange.threadpool.testinjector.sleepmillis", 2500);

        final TimerService timerService = getService(TimerService.class);
        final AtomicInteger count = new AtomicInteger();

        final Runnable task = new Runnable() {

            @Override
            public void run() {
                final ThreadPoolService threadPool = getService(ThreadPoolService.class);

                final Task<Object> ttask = ThreadPools.task(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(sleepMillis);
                        } catch (final InterruptedException e) {
                            // Interrupted
                            Thread.currentThread().interrupt();
                        }
                    }
                });

                final Collection<Task<Object>> tasks = new LinkedList<Task<Object>>();
                final int num = rampUp + (factor * count.getAndIncrement());
                for (int i = 0; i < num; i++) {
                    tasks.add(ttask);
                }

                log.info("\tSubmitting "+num+" tasks into thread pool.");
                threadPool.invoke(tasks);
                log.info("\tAfter submit of "+num+" tasks, the current thread pool size: pool-size=" + threadPool.getPoolSize() + ", active-count=" + threadPool.getActiveCount());
            }
        };

        timerTask = timerService.scheduleAtFixedRate(task, 1000, frequencyMillis);
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log log = com.openexchange.log.Log.loggerFor(ThreadPoolInjectorActivator.class);
        log.info("Stopping bundle: com.openexchange.threadpool.testinjector");
        final ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            timerTask.cancel();
            this.timerTask = null;
        }
        super.stopBundle();
    }

}
