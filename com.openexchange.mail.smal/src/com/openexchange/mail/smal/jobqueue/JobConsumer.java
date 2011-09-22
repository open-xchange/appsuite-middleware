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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link JobConsumer} - The job consumer which takes jobs from passed blocking queue.
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

        @Override
        public String getIdentifier() {
            return "poison";
        }
    };

    private final BlockingQueue<Job> queue;

    private final ConcurrentMap<String, Object> identifiers;

    private final AtomicBoolean keepgoing;

    protected final AtomicReference<Job> currentJob;

    /**
     * Initializes a new {@link JobConsumer}.
     */
    protected JobConsumer(final BlockingQueue<Job> queue, final ConcurrentMap<String, Object> identifiers) {
        super();
        keepgoing = new AtomicBoolean(true);
        this.queue = queue;
        this.identifiers = identifiers;
        currentJob = new AtomicReference<Job>();
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

    /**
     * Gets the identifier of the job currently being executed.
     *
     * @return The current job's identifier or <code>null</code> if none is executed at the moment
     */
    protected Job currentJob() {
        return currentJob.get();
    }

    /**
     * Checks if there is a job in queue with a higher ranking than specified ranking.
     *
     * @param ranking The ranking to check against
     * @return <code>true</code> if there is a higher-ranked job; otherwise <code>false</code>
     */
    protected boolean hasHigherRankedJobInQueue(final int ranking) {
        final Job peekedJob = queue.peek();
        return null != peekedJob && peekedJob.getRanking() > ranking;
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
                        final ThreadPoolService threadPool =
                            DELEGATE ? SMALServiceLookup.getInstance().getService(ThreadPoolService.class) : null;
                        for (final Job job : jobs) {
                            /*
                             * Check if canceled in the meantime
                             */
                            if (job.isCanceled()) {
                                identifiers.remove(job.getIdentifier());
                            } else {
                                if (job.isPaused()) {
                                    /*
                                     * Unset "paused" flag & re-enqueue
                                     */
                                    job.proceed();
                                    queue.offer(job);
                                } else {
                                    identifiers.remove(job.getIdentifier());
                                    final JobWrapper jobWrapper = wrapperFor(job);
                                    if (DELEGATE) {
                                        final Future<Object> future = threadPool.submit(jobWrapper, CallerRunsBehavior.getInstance());
                                        job.future = future;
                                    } else {
                                        jobWrapper.beforeExecute(consumerThread);
                                        try {
                                            jobWrapper.call();
                                            jobWrapper.afterExecute(null);
                                        } catch (final Throwable t) {
                                            jobWrapper.afterExecute(t);
                                        } finally {
                                            Thread.interrupted();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    jobs.clear();
                    identifiers.clear();
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

    private JobWrapper wrapperFor(final Job job) {
        return new JobWrapper(job);
    }

    private final class JobWrapper implements Task<Object> {

        private final Job job;

        protected JobWrapper(final Job job) {
            super();
            this.job = job;
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            job.setThreadName(threadRenamer);
        }

        @Override
        public void beforeExecute(final Thread t) {
            currentJob.set(job);
            job.beforeExecute(t);
        }

        @Override
        public void afterExecute(final Throwable t) {
            job.done = true;
            job.executionFailure = t;
            currentJob.set(null);
            job.afterExecute(t);
        }

        @Override
        public Object call() throws Exception {
            return job.call();
        }

    }

}
