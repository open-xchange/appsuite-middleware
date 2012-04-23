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

package com.openexchange.timer.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link TimerImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TimerImpl implements TimerService {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.LogFactory.getLog(TimerImpl.class);

    private final AtomicBoolean started;

    private ScheduledThreadPoolExecutor executorService;

    /**
     * Initializes a new {@link TimerImpl}.
     */
    public TimerImpl() {
        super();
        started = new AtomicBoolean();
    }

    /**
     * Starts this timer.
     */
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        executorService = new ScheduledThreadPoolExecutor(
            (Runtime.getRuntime().availableProcessors() + 1),
            new TimerThreadFactory("Timer-"));
        executorService.prestartAllCoreThreads();
        /*
         * Add a task to frequently purge canceled tasks
         */
        final class PurgeRunnable implements Runnable {

            private final ScheduledThreadPoolExecutor exec;

            public PurgeRunnable(final ScheduledThreadPoolExecutor exec) {
                super();
                this.exec = exec;
            }

            @Override
            public void run() {
                exec.purge();
            }

        }
        executorService.scheduleWithFixedDelay(new PurgeRunnable(executorService), 30L, 30L, TimeUnit.SECONDS);
    }

    /**
     * Stops this timer.
     */
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            return;
        }
        executorService.shutdownNow();
        try {
            // TODO: Define a reasonable timeout
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            LOG.error(e.getMessage(), e);
        } finally {
            executorService = null;
        }
    }

    @Override
    public ScheduledTimerTask schedule(final Runnable command, final long delay, final TimeUnit unit) {
        return new WrappingScheduledTimerTask(executorService.schedule(command, delay, unit));
    }

    @Override
    public ScheduledTimerTask schedule(final Runnable command, final long delay) {
        return schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledTimerTask scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        return new WrappingScheduledTimerTask(executorService.scheduleAtFixedRate(command, initialDelay, period, unit));
    }

    @Override
    public ScheduledTimerTask scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period) {
        return scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledTimerTask scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        return new WrappingScheduledTimerTask(executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit));
    }

    @Override
    public ScheduledTimerTask scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay) {
        return scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void purge() {
        executorService.purge();
    }

    @Override
    public Executor getExecutor() {
        return executorService;
    }

}
