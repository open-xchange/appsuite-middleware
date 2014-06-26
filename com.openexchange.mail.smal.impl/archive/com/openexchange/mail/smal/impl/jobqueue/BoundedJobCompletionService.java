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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link BoundedJobCompletionService} - A bounded completion service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoundedJobCompletionService extends AbstractJobCompletionService {

    private final class BoundedQueueingJob extends QueueingJob {

        private static final long serialVersionUID = 5952429835529112702L;

        protected BoundedQueueingJob(final Job job) {
            super(job);
        }

        @Override
        public void afterExecute(final Throwable t) {
            super.afterExecute(t);
            runningCount.decrementAndGet();
        }

    }

    protected final AtomicInteger runningCount;

    protected final int capacity;

    /**
     * Initializes a new {@link BoundedJobCompletionService}.
     *
     * @param capacity The capacity
     * @throws IllegalArgumentException If <code>capacity</code> is not a positive integer
     */
    public BoundedJobCompletionService(final int capacity) {
        super();
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive integer.");
        }
        runningCount = new AtomicInteger();
        this.capacity = capacity;
    }

    @Override
    public boolean addJob(final Job job) throws InterruptedException {
        final BoundedQueueingJob queueingJob = new BoundedQueueingJob(job);
        final Thread thread = Thread.currentThread();
        int count;
        do {
            if (thread.isInterrupted()) {
                Thread.interrupted();
                throw new InterruptedException("Thread interrupted while awaiting a completed job.");
            }
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

    @Override
    public boolean tryAddJob(final Job job) {
        final BoundedQueueingJob queueingJob = new BoundedQueueingJob(job);
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

}
