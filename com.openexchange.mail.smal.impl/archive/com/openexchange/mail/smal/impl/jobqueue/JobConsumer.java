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

package com.openexchange.mail.smal.impl.jobqueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link JobConsumer} - The job consumer which takes jobs from passed blocking queue.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class JobConsumer extends AbstractTask<Object> {

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(JobConsumer.class));

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

        @Override
        public boolean forcedRun() {
            return false;
        }
    };

    private static final int NUM_OF_MAX_CONCURRENT_WORKERS = Runtime.getRuntime().availableProcessors() << 1;

    private final BlockingQueue<Job> queue;

    protected final ConcurrentMap<String, Job> identifiers;

    private final AtomicBoolean keepgoing;

    private final boolean consumerMayPerformTasks;

    protected final Semaphore semaphore;

    private final AtomicInteger jobCounter;

    /**
     * Initializes a new {@link JobConsumer}.
     */
    protected JobConsumer(final BlockingQueue<Job> queue, final ConcurrentMap<String, Job> identifiers, final boolean consumerMayPerformTasks, final AtomicInteger jobCounter) {
        super();
        keepgoing = new AtomicBoolean(true);
        this.queue = queue;
        this.jobCounter = jobCounter;
        this.identifiers = identifiers;
        semaphore = new Semaphore(NUM_OF_MAX_CONCURRENT_WORKERS);
        this.consumerMayPerformTasks = consumerMayPerformTasks;
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
        /*
         * Method's intention is to pause a long-running to allow further consuming intermediate tasks from queue. This is not
         * appropriate if main thread does not perform tasks. Meaning consuming from queue is possible unless further permits are
         * available from semaphore instance.
         */
        if (!consumerMayPerformTasks && semaphore.availablePermits() > 0) {
            return false;
        }
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
            final class JobPerformerTask implements Runnable {
                
                private volatile List<Job> jobs;

                protected JobPerformerTask() {
                    super();
                }

                protected void setJobs2Perform(final List<Job> jobs) {
                    this.jobs = new ArrayList<Job>(jobs);
                }
                
                @Override
                public void run() {
                    try {
                        final boolean debug = LOG.isDebugEnabled();
                        final ThreadPoolService threadPool = SmalServiceLookup.getThreadPool();
                        final List<Job> tmp = jobs;
                        for (final Job job : tmp) {
                            performJob(job, threadPool, debug);
                        }
                        if (debug) {
                            LOG.debug("Performed " + tmp.size() + " jobs");
                        }
                    } catch (final InterruptedException e) {
                        // Consumer interrupted... Keep interrupted flag
                        Thread.currentThread().interrupt();
                        LOG.info("Job performer task interrupted.", e);
                    } catch (final RuntimeException e) {
                        // Consumer run failed...
                        LOG.info("Job performer run terminated with unchecked error.", e);
                    }
                }
            }
            final JobPerformerTask jobPerformerTask = new JobPerformerTask();
            final Task<Object> task = ThreadPools.task(jobPerformerTask);
            final List<Job> jobs = new ArrayList<Job>(16);
            while (keepgoing.get()) {
                try {
                    if (queue.isEmpty()) {
                        /*
                         * Blocking wait for at least one job to arrive.
                         */
                        final Job job = queue.take();
                        if (POISON == job) {
                            return null;
                        }
                        jobs.add(job);
                    }
                    queue.drainTo(jobs);
                    final boolean quit = jobs.remove(POISON);
                    jobPerformerTask.setJobs2Perform(jobs);
                    SmalServiceLookup.getThreadPool().submit(task, CallerRunsBehavior.getInstance());
                    if (quit) {
                        return null;
                    }
                    jobs.clear();
                    // TODO: identifiers.clear();
                } catch (final RuntimeException e) {
                    // Consumer run failed...
                    LOG.info("Job consumer run terminated with unchecked error.", e);
                }
            }
            LOG.info("Job consumer terminated.");
        } catch (final InterruptedException e) {
            // Consumer interrupted... Keep interrupted flag
            Thread.currentThread().interrupt();
            LOG.info("Job consumer interrupted.", e);
        } catch (final Exception e) {
            // Consumer failed...
            LOG.info("Job consumer terminated with error.", e);
        }
        return null;
    }

    /**
     * Performs specified job.
     * 
     * @param job The job to perform
     * @param threadPool The thread pool to delegate execution to
     * @param debug Whether debug logging is enabled
     * @throws InterruptedException If job execution is interrupted
     */
    protected void performJob(final Job job, final ThreadPoolService threadPool, final boolean debug) throws InterruptedException {
        /*
         * Check if canceled in the meantime
         */
        if (job.isCanceled()) {
            job.afterExecute(null);
            jobCleanUp(job);
            if (debug) {
                LOG.debug("Aborted execution of canceled job: " + job.getIdentifier());
            }
            return;
        }
        if (job.isPaused()) {
            /*
             * Unset "paused" flag & re-enqueue
             */
            job.proceed();
            queue.offer(job);
            if (debug) {
                LOG.debug("Re-enqueued temporarily paused job: " + job.getIdentifier());
            }
            return;
        }
        /*
         * Perform that job
         */
        if (consumerMayPerformTasks) {
            if (semaphore.tryAcquire()) {
                // Further concurrent worker allowed
                final Future<Void> future = threadPool.submit(wrapperFor(job, true, debug), CallerRunsBehavior.<Void> getInstance());
                job.future = future;
            } else {
                // Execute with "Job-Consumer" thread
                final JobWrapper jobWrapper = wrapperFor(job, false, debug);
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
        } else {
            if (debug) {
                LOG.debug("Awaiting free worker thread to execute job: " + job.getIdentifier());
            }
            semaphore.acquire();
            // Free worker
            final Future<Void> future = threadPool.submit(wrapperFor(job, true, debug), CallerRunsBehavior.<Void> getInstance());
            job.future = future;
        }
    }

    protected void jobCleanUp(final Job job) {
        identifiers.remove(job.getIdentifier());
        decrementJobCount();
    }

    private void decrementJobCount() {
        int cur;
        do {
            cur = jobCounter.get();
            if (cur <= 0) {
                return;
            }
        } while (!jobCounter.compareAndSet(cur, cur - 1));
    }

    private JobWrapper wrapperFor(final Job job, final boolean releasePermit, final boolean debug) {
        return new JobWrapper(job, releasePermit, debug);
    }

    private final class JobWrapper implements Task<Void> {

        private final Job job;

        private final boolean releasePermit;

        private final boolean debug;

        protected JobWrapper(final Job job, final boolean releasePermit, final boolean debug) {
            super();
            this.releasePermit = releasePermit;
            this.job = job;
            this.debug = debug;
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
            jobCleanUp(job);
            if (releasePermit) {
                semaphore.release();
            }
            // TODO: Useful? currentJobs.remove(job);
            job.afterExecute(t);
        }

        @Override
        public Void call() throws Exception {
            if (!debug) {
                return job.call();
            }
            job.call();
            LOG.debug("Job successfully performed: " + job.getIdentifier());
            return null; // Job always returns null
        }

    }

}
