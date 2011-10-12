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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link JobCompletionService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JobCompletionService {

    private final class QueueingJob extends Job {

        private final Job job;

        private final CountDownLatch latch;

        protected QueueingJob(final Job job) {
            super();
            this.job = job;
            latch = new CountDownLatch(1);
        }

        protected void start() {
            latch.countDown();
        }

        @Override
        public int getRanking() {
            return job.getRanking();
        }

        @Override
        public String getIdentifier() {
            return job.getIdentifier();
        }

        @Override
        public void perform() {
            try {
                latch.await();
                job.perform();
            } catch (final InterruptedException e) {
                // Keep interrupted state
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void afterExecute(final Throwable t) {
            super.afterExecute(t);
            if (capacity > 0) {
                runningCount.decrementAndGet();
            }
            queue.add(job);
        }

    }

    protected final BlockingQueue<Job> queue;

    protected final AtomicInteger runningCount;

    protected final int capacity;

    /**
     * Initializes a new {@link JobCompletionService}.
     */
    public JobCompletionService(final int capacity) {
        super();
        queue = new LinkedBlockingQueue<Job>();
        runningCount = new AtomicInteger();
        this.capacity = capacity;
    }

    /**
     * Adds specified job to this completion service
     * 
     * @param job The job to add
     * @return <code>true</code> if job could be added; otherwise <code>false</code>
     */
    public boolean addJob(final Job job) {
        final QueueingJob queueingJob = new QueueingJob(job);
        if (capacity <= 0) {
            return JobQueue.getInstance().addJob(queueingJob);
        }
        int count;
        do {
            count = runningCount.get();
        } while (count >= capacity || !runningCount.compareAndSet(count, count + 1));
        /*
         * Add to job queue
         */
        if (JobQueue.getInstance().addJob(queueingJob)) {
            queueingJob.start();
            return true;
        }
        runningCount.decrementAndGet();
        return false;
    }

    /**
     * Adds specified job to this completion service
     * 
     * @param job The job to add
     * @return <code>true</code> if job could be added; otherwise <code>false</code>
     */
    public boolean tryAddJob(final Job job) {
        final QueueingJob queueingJob = new QueueingJob(job);
        if (capacity <= 0) {
            return JobQueue.getInstance().addJob(queueingJob);
        }
        int count;
        do {
            count = runningCount.get();
            if (count >= capacity) {
                return false;
            }
        } while (!runningCount.compareAndSet(count, count + 1));
        /*
         * Add to job queue
         */
        if (JobQueue.getInstance().addJob(queueingJob)) {
            queueingJob.start();
            return true;
        }
        runningCount.decrementAndGet();
        return false;
    }

    /**
     * Retrieves and removes the next completed job, waiting if none are yet present.
     * 
     * @return The next completed job
     * @throws InterruptedException If interrupted while waiting
     */
    public Job take() throws InterruptedException {
        return queue.take();
    }

    /**
     * Retrieves and removes the next completed job or <tt>null</tt> if none are present.
     * 
     * @return The the next completed job, or <tt>null</tt> if none are present
     */
    public Job poll() {
        return queue.poll();
    }

    /**
     * Retrieves and removes the next completed job, waiting if necessary up to the specified wait time if none are yet present.
     * 
     * @param timeout How long to wait before giving up, in units of <tt>unit</tt>
     * @param unit A <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
     * @return The the next completed job or <tt>null</tt> if the specified waiting time elapses before one is present
     * @throws InterruptedException If interrupted while waiting
     */
    public Job poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

}
