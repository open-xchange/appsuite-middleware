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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.AbortBehavior;

/**
 * {@link JobQueue} - The job queue.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JobQueue {

    private static final int CAPACITY = 1048576;

    private static volatile JobQueue instance;

    /**
     * Gets the {@link JobQueue} instance
     *
     * @return The {@link JobQueue} instance
     */
    public static JobQueue getInstance() {
        JobQueue tmp = instance;
        if (null == tmp) {
            synchronized (JobQueue.class) {
                tmp = instance;
                if (null == tmp) {
                    instance = tmp = new JobQueue(SmalServiceLookup.getThreadPool());
                }
            }
        }
        return tmp;
    }

    /**
     * Drops the {@link JobQueue} instance.
     */
    public static void dropInstance() {
        final JobQueue tmp = instance;
        if (null != tmp) {
            tmp.stop();
            instance = null;
        }
    }

    // private static final Object PRESENT = new Object();

    private final BlockingQueue<Job> queue;

    private final AtomicInteger jobCounter;
    
    private final ConcurrentMap<String, Job> identifiers;

    private final Future<Object> consumerFuture;

    private final JobConsumer consumer;

    /**
     * Initializes a new {@link JobQueue}.
     */
    private JobQueue(final ThreadPoolService threadPool) {
        super();
        queue = new PriorityBlockingQueue<Job>(CAPACITY);
        jobCounter = new AtomicInteger();
        identifiers = new ConcurrentHashMap<String, Job>(CAPACITY);
        consumer = new JobConsumer(queue, identifiers, false, jobCounter);
        consumerFuture = threadPool.submit(consumer, AbortBehavior.getInstance());
        final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(JobQueue.class));
        logger.info("\n\tSMAL JobQueue orderly started.");
    }

    /**
     * Stops the job queue orderly.
     */
    public void stop() {
        consumer.stop();
        try {
            consumerFuture.get(1, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            // Wait time elapsed; enforce cancelation
            consumerFuture.cancel(true);
        } catch (final InterruptedException e) {
            /*
             * Cannot occur, but keep interrupted state
             */
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            // What?!
            final Throwable t = e.getCause();
            final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(JobQueue.class));
            log.error(t.getMessage(), t);
        }
    }

    /**
     * Adds specified job to this service's job queue.
     *
     * @param job The job to add
     * @return <code>true</code> if job could be added; otherwise <code>false</code>
     */
    public boolean addJob(final Job job) {
        if (null == job || !checkCount()) {
            return false;
        }
        if (job.forcedRun()) {
            return queue.offer(job);
        }
        // Check for possible duplicates already located in queue
        final String identifier = job.getIdentifier();
        final Job prev = identifiers.putIfAbsent(identifier, job);
        if (null == prev) {
            /*
             * Not yet contained in queue
             */
            if (!queue.offer(job)) {
                identifiers.remove(identifier);
                decrementJobCount();
                return false;
            }
            return true;
        }
        /*
         * Either replace or deny
         */
        decrementJobCount();
        if (prev.getRanking() < job.getRanking()) {
            /*
             * Replace
             */
            prev.replaceWith(job);
            return true;
        }
        return false;
    }

    private boolean checkCount() {
        final int max = CAPACITY;
        int cur;
        do {
            cur = jobCounter.get();
            if (cur >= max) {
                return false;
            }
        } while (!jobCounter.compareAndSet(cur, cur + 1));
        return true;
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

    /**
     * Gets the jobs currently being executed.
     *
     * @return The jobs currently being executed or an empty list if none is executed at the moment
     */
    public List<Job> currentJobs() {
        return consumer.currentJobs();
    }

    /**
     * Checks if there is a job in queue with a higher ranking than specified ranking.
     *
     * @param ranking The ranking to check against
     * @return <code>true</code> if there is a higher-ranked job; otherwise <code>false</code>
     */
    public boolean hasHigherRankedJobInQueue(final int ranking) {
        return consumer.hasHigherRankedJobInQueue(ranking);
    }

}
