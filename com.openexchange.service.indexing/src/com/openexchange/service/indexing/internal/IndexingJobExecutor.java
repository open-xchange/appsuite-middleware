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

package com.openexchange.service.indexing.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.IndexingJob.Behavior;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.AbortBehavior;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link IndexingJobExecutor} - Executes incoming {@link IndexingJob jobs}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexingJobExecutor implements Callable<Void> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexingJobExecutor.class));

    /**
     * The max. queue capacity.
     */
    private static final int CAPACITY = 1048576;

    /**
     * The special poison element to stop executing jobs.
     */
    private static final IndexingJob POISON = new IndexingJob() {

        private static final long serialVersionUID = -1L;

        @Override
        public void performJob() throws OXException {
            // Nothing to do
        }

        @Override
        public Behavior getBehavior() {
            return Behavior.CONSUMER_RUNS;
        }

        @Override
        public boolean isDurable() {
            return false;
        }

        @Override
        public int getPriority() {
            return 9;
        }

        @Override
        public void setPriority(final int priority) {
            // Nothing to do
        }

        @Override
        public void beforeExecute() {
            // Nothing to do
        }

        @Override
        public void afterExecute(final Throwable t) {
            // Nothing to do
        }
    };

    private final ThreadPoolService threadPool;

    private final BlockingQueue<IndexingJob> queue;

    private final int maxConcurrentJobs;

    private volatile Future<Void> future;

    /**
     * Initializes a new {@link IndexingJobExecutor}.
     */
    public IndexingJobExecutor(final int maxConcurrentJobs, final ThreadPoolService threadPool) {
        super();
        this.maxConcurrentJobs = maxConcurrentJobs;
        this.threadPool = threadPool;
        this.queue = new BoundedPriorityBlockingQueue<IndexingJob>(CAPACITY);
    }

    @Override
    public Void call() throws Exception {
        final RefusedExecutionBehavior<Void> callerRunsBehavior = CallerRunsBehavior.<Void> getInstance();
        final List<IndexingJob> jobs = new ArrayList<IndexingJob>(maxConcurrentJobs);
        while (true) {
            try {
                if (queue.isEmpty()) {
                    /*
                     * Blocking wait for at least one job to arrive.
                     */
                    final IndexingJob job = queue.take();
                    if (POISON == job) {
                        return null;
                    }
                    jobs.add(job);
                }
                queue.drainTo(jobs, maxConcurrentJobs);
                final boolean quit = jobs.remove(POISON);
                for (final IndexingJob indexingJob : jobs) {
                    if (Behavior.DELEGATE.equals(indexingJob.getBehavior())) {
                        threadPool.submit(new IndexingJobTask(indexingJob), callerRunsBehavior);
                    } else {
                        performJob(indexingJob);
                    }
                }
                if (quit) {
                    return null;
                }
                jobs.clear();
            } catch (final RuntimeException e) {
                // Consumer run failed...
                LOG.info("Job consumer run terminated with unchecked error.", e);
            }
        }
    }

    /**
     * Starts this executor orderly.
     */
    public IndexingJobExecutor start() {
        future = threadPool.submit(ThreadPools.task(this), AbortBehavior.<Void> getInstance());
        return this;
    }

    /**
     * Stops this executor orderly.
     */
    public void stop() {
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
        /*
         * Cancel future
         */
        final Future<Void> future = this.future;
        if (null != future) {
            future.cancel(false);
            this.future = null;
        }
    }

    /**
     * Adds specified job to this service's job queue.
     * 
     * @param job The job to add
     * @return <code>true</code> if job could be added; otherwise <code>false</code>
     */
    public boolean addJob(final IndexingJob job) {
        if (null == job) {
            return false;
        }
        return queue.offer(new IndexingJobWrapper(job));
    }

    /**
     * Performs given job with respect to beforeExecute() and afterExecute() call-backs.
     * 
     * @param job The job to perform
     * @throws OXException If job execution fails orderly
     * @throws RuntimeException If job execution fails unexpectedly
     */
    protected static void performJob(final IndexingJob job) throws OXException {
        boolean ran = false;
        job.beforeExecute();
        try {
            job.performJob();
            ran = true;
            job.afterExecute(null);
        } catch (final RuntimeException e) {
            if (!ran) {
                job.afterExecute(e);
            }
            LOG.warn("Indexing job failed with unchecked error.", e);
            throw e;
        }
    }

    /*-
     * -----------------------------------------------------------------------
     * --------------------------- Helper classes ----------------------------
     * -----------------------------------------------------------------------
     */

    private static final class IndexingJobTask extends AbstractTask<Void> {

        private final IndexingJob job;

        public IndexingJobTask(final IndexingJob job) {
            super();
            this.job = job;
        }

        @Override
        public Void call() throws Exception {
            performJob(job);
            return null;
        }

    }

    private static final class IndexingJobWrapper implements IndexingJob, Comparable<IndexingJob> {

        private static final long serialVersionUID = -3358800982328898854L;

        private final IndexingJob job;

        public IndexingJobWrapper(final IndexingJob job) {
            super();
            this.job = job;
        }

        @Override
        public int getPriority() {
            return job.getPriority();
        }

        @Override
        public void setPriority(final int priority) {
            job.setPriority(priority);
        }

        @Override
        public void beforeExecute() {
            job.beforeExecute();
        }

        @Override
        public void afterExecute(final Throwable t) {
            job.afterExecute(t);
        }

        @Override
        public void performJob() throws OXException {
            job.performJob();
        }

        @Override
        public boolean isDurable() {
            return job.isDurable();
        }

        @Override
        public Behavior getBehavior() {
            return job.getBehavior();
        }

        @Override
        public int hashCode() {
            return job.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return job.equals(obj);
        }

        @Override
        public String toString() {
            return job.toString();
        }

        @Override
        public int compareTo(final IndexingJob o) {
            final int thisVal = job.getPriority();
            final int anotherVal = o.getPriority();
            return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
        }

    }

    private static final class BoundedPriorityBlockingQueue<E> extends PriorityBlockingQueue<E> {

        private static final long serialVersionUID = 1L;

        private final int maxSize;

        public BoundedPriorityBlockingQueue(final int maxSize) {
            super(maxSize + 1);
            this.maxSize = maxSize;
        }

        @Override
        public boolean offer(final E e) {
            if (!super.offer(e)) {
                return false;
            }
            if (size() > maxSize) {
                poll();
            }
            return true;
        }

    } // End of BoundedPriorityBlockingQueue class

}
