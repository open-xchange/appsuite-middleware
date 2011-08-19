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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.jobqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link JobConsumer}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class JobConsumer extends AbstractTask<Object> {

    /**
     * The poison element.
     */
    private static final Job POISON = new Job() {

        @Override
        public void perform() {
            // Nope
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            // Nope
        }

        @Override
        public void beforeExecute(final Thread t) {
            // Nope
        }

        @Override
        public void afterExecute(final Throwable t) {
            // Nope
        }

        @Override
        public boolean isCanceled() {
            return true;
        }

        @Override
        public int getRanking() {
            return Integer.MAX_VALUE;
        }
    };

    private final BlockingQueue<Job> queue;

    private final AtomicBoolean keepgoing;

    /**
     * Initializes a new {@link JobConsumer}.
     */
    protected JobConsumer(final BlockingQueue<Job> queue) {
        super();
        keepgoing = new AtomicBoolean(true);
        this.queue = queue;
    }

    /**
     * Stops this consumer.
     */
    protected void stop() {
        keepgoing.set(false);
        /*
         * Feed poison element to enforce quit
         */
        try {
            queue.put(POISON);
        } catch (final InterruptedException e) {
            /*
             * Cannot occur, but keep interrupted state
             */
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        threadRenamer.rename("Job-Consumer");
    }

    private static final boolean DELEGATE = false;

    @Override
    public Object call() throws Exception {
        try {
            final List<Job> jobs = new ArrayList<Job>(16);
            final Thread consumerThread = Thread.currentThread();
            while (keepgoing.get()) {
                try {
                    if (queue.isEmpty()) {
                        /*
                         * Blocking wait for at least 1 job to arrive.
                         */
                        final Job job = queue.take();
                        if (POISON == job) {
                            return null;
                        }
                        jobs.add(job);
                    }
                    queue.drainTo(jobs);
                    final boolean quit = jobs.remove(POISON);
                    {
                        final ThreadPoolService threadPool = SMALServiceLookup.getInstance().getService(ThreadPoolService.class);
                        for (final Job job : jobs) {
                            /*
                             * Check if canceled in the meantime
                             */
                            if (!job.isCanceled()) {
                                if (job.isPaused()) {
                                    /*
                                     * Unset "pasued" flag & re-enqueue
                                     */
                                    job.proceed();
                                    queue.offer(job);
                                } else {
                                    if (DELEGATE) {
                                        threadPool.submit(job, CallerRunsBehavior.getInstance());
                                    } else {
                                        job.beforeExecute(consumerThread);
                                        job.call();
                                        job.afterExecute(null);
                                    }
                                }
                            }
                        }
                    }
                    jobs.clear();
                    if (quit) {
                        return null;
                    }
                } catch (final RuntimeException e) {
                    // Consumer run failed...
                }
            }
        } catch (final Exception e) {
            // Consumer failed...
        }
        return null;
    }

}
