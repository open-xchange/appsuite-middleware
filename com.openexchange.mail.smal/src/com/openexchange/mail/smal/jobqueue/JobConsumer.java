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
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(JobConsumer.class));

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

    private static final int NUM_OF_MAX_CONCURRENT_WORKERS = Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<Job> queue;

    private final ConcurrentMap<String, Job> identifiers;

    private final AtomicBoolean keepgoing;

    protected final Semaphore semaphore;

    protected final Queue<Job> currentJobs;

    /**
     * Initializes a new {@link JobConsumer}.
     */
    protected JobConsumer(final BlockingQueue<Job> queue, final ConcurrentMap<String, Job> identifiers) {
        super();
        keepgoing = new AtomicBoolean(true);
        this.queue = queue;
        this.identifiers = identifiers;
        currentJobs = new ConcurrentLinkedQueue<Job>();
        semaphore = new Semaphore(NUM_OF_MAX_CONCURRENT_WORKERS);
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
     * Gets the jobs currently being executed.
     *
     * @return The jobs currently being executed or an empty list if none is executed at the moment
     */
    protected List<Job> currentJobs() {
        // TODO: Useful? return new ArrayList<Job>(currentJobs);
        return Collections.emptyList();
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

    @Override
    public Object call() throws Exception {
        try {
            final List<Job> jobs = new ArrayList<Job>(16);
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
                            performJob(job, threadPool);
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

    protected void performJob(final Job job, final ThreadPoolService threadPool) {
        /*
         * Check if canceled in the meantime
         */
        if (job.isCanceled()) {
            identifiers.remove(job.getIdentifier());
            return;
        }
        if (job.isPaused()) {
            /*
             * Unset "paused" flag & re-enqueue
             */
            job.proceed();
            queue.offer(job);
            return;
        }
        try {
            if (semaphore.tryAcquire()) {
                // Further concurrent worker allowed
                final Future<Object> future = threadPool.submit(wrapperFor(job, true), CallerRunsBehavior.getInstance());
                job.future = future;
            } else {
                // Execute with "Job-Consumer" thread
                final JobWrapper jobWrapper = wrapperFor(job, false);
                boolean ran = false;
                jobWrapper.beforeExecute(Thread.currentThread());
                try {
                    jobWrapper.call();
                    ran = true;
                    jobWrapper.afterExecute(null);
                } catch (final Throwable t) {
                    if (!ran) {
                        afterExecute(t);
                    }
                    // Else the exception occurred within afterExecute itself in which case we don't want to call it again.
                    LOG.warn("Exception occurred within afterExecute().", t);
                } finally {
                    Thread.interrupted();
                }
            }
        } finally {
            /*
             * Last, but not least, remove from known identifiers if done
             */
            identifiers.remove(job.getIdentifier());
        }
    }

    private JobWrapper wrapperFor(final Job job, final boolean releasePermit) {
        return new JobWrapper(job, releasePermit);
    }

    private final class JobWrapper implements Task<Object> {

        private final Job job;

        private final boolean releasePermit;

        protected JobWrapper(final Job job, final boolean releasePermit) {
            super();
            this.releasePermit = releasePermit;
            this.job = job;
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            job.setThreadName(threadRenamer);
        }

        @Override
        public void beforeExecute(final Thread t) {
            // TODO: Useful? currentJobs.offer(job);
            job.beforeExecute(t);
        }

        @Override
        public void afterExecute(final Throwable t) {
            
            System.out.println(job.getIdentifier() + " done.");
            
            if (releasePermit) {
                semaphore.release();
            }
            job.done = true;
            job.executionFailure = t;
            // TODO: Useful? currentJobs.remove(job);
            job.afterExecute(t);
        }

        @Override
        public Object call() throws Exception {
            return job.call();
        }

    }

}
