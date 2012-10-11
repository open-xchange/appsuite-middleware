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

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import com.openexchange.log.LogFactory;
import com.openexchange.log.Log;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.adapter.IndexAdapter;
import com.openexchange.mail.smal.impl.adapter.IndexService;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link Job} - A job that is placed into {@link JobQueue}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class Job implements Task<Void>, Comparable<Job>, Serializable {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Job.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final long serialVersionUID = 5618306933455163193L;

    /**
     * The default ranking for a job.
     */
    public static final int DEFAULT_RANKING = 0;

    /**
     * The associated future object.
     */
    protected volatile Future<Void> future;

    /**
     * The execution failure (<code>null</code> for no error during execution).
     */
    protected volatile Throwable executionFailure;

    /**
     * The done flag.
     */
    protected volatile boolean done;

    /**
     * The canceled flag.
     */
    protected volatile boolean canceled;

    /**
     * The paused flag.
     */
    protected volatile boolean paused;

    /**
     * The volatile queue reference.
     */
    private volatile BlockingQueue<Job> queue;

    /**
     * Initializes a new {@link Job}.
     */
    protected Job() {
        super();
    }

    /**
     * Gets this job's ranking.
     * <p>
     * The job ranking is used by the {@link JobQueue} to determine the <i>natural order</i> of jobs, see
     * {@link Comparable#compareTo(Object)}.
     * <p>
     * The default ranking is zero (0). A job with a ranking of {@code Integer.MAX_VALUE} is very likely to be immediately executed, whereas
     * a job with a ranking of {@code Integer.MIN_VALUE} is very unlikely to be executed.
     *
     * @return The ranking
     */
    public abstract int getRanking();

    /**
     * Gets an identifier for this job.
     * <p>
     * The returned identifier is used to look-up/filter identical jobs through {@link #equals(Object)}
     *
     * @return The identifier.
     */
    public abstract String getIdentifier();

    /**
     * Performs this job.
     */
    public abstract void perform();

    /**
     * Checks whether this job enters a forced run.
     * 
     * @return <code>true</code> for forced run; otherwise <code>false</code>
     */
    public abstract boolean forcedRun();

    /**
     * Replaces this job with values/operations from passed job.
     *
     * @param anotherJob The other job to copy from
     */
    public void replaceWith(final Job anotherJob) {
        // Default do nothing
    }

    /**
     * Gets the associated future.
     *
     * @return The associated future or <code>null</code> if not in progress
     */
    public final Future<Void> getAssociatedFuture() {
        return future;
    }

    /**
     * Gets the index adapter.
     *
     * @return The index adapter
     */
    public IndexAdapter getAdapter() {
        return SmalServiceLookup.getInstance().getService(IndexService.class).getAdapter();
    }

    /**
     * Resets this job.
     */
    public void reset() {
        done = false;
        canceled = false;
        paused = false;
        executionFailure = null;
        future = null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final String id = getIdentifier();
        result = prime * result + (null == id ? 0 : id.hashCode());
        result = prime * result + getRanking();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Job)) {
            return false;
        }
        final Job other = (Job) obj;
        if (getRanking() != other.getRanking()) {
            return false;
        }
        final String thisIdentifier = getIdentifier();
        final String otherIdentifier = other.getIdentifier();
        if (null == thisIdentifier) {
            if (null != otherIdentifier) {
                return false;
            }
        } else if (!thisIdentifier.equalsIgnoreCase(otherIdentifier)) {
            return false;
        }
        return true;
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        // Nothing to do; override if needed
    }

    @Override
    public void beforeExecute(final Thread t) {
        // Nothing to do; override if needed
    }

    @Override
    public void afterExecute(final Throwable t) {
        if (null != t) {
            executionFailure = t;
        }
        done = true;
        if (DEBUG) {
            final StringBuilder sb = new StringBuilder(32);
            sb.append("Job \"").append(getIdentifier()).append("\" terminated");
            if (null != t) {
                sb.append(" with error: ").append(t.getMessage());
            }
            sb.append('.');
            LOG.debug(sb.toString());
        }
    }

    @Override
    public final int compareTo(final Job other) {
        final int thisVal = this.getRanking();
        final int anotherVal = other.getRanking();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    @Override
    public final Void call() throws Exception {
        // final List<Job> currentJobs = JobQueue.getInstance().currentJobs();
        // for (final Job currentJob : currentJobs) {
        // if (this != currentJob && equals(currentJob)) {
        // /*
        // * Same or similar-purpose job in progress
        // */
        // return null;
        // }
        // }
        performJob();
        return null;
    }

    private void performJob() {
        try {
            perform();
        } catch (final Exception e) {
            Log.valueOf(LogFactory.getLog(Job.class)).error(e.getMessage(), e);
        }
    }

    /**
     * Sets the canceled flag.
     * <p>
     * If flag is set and job has not been performed, yet, the job is discarded.
     */
    public void cancel() {
        canceled = true;
    }

    /**
     * Checks if the canceled flag is set.
     * <p>
     * If flag is set and job has not been performed, yet, the job is discarded.
     *
     * @return Whether canceled or not
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Gets the paused flag
     *
     * @return The paused flag
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets the paused flag
     */
    public void pause() {
        this.paused = true;
    }

    /**
     * Un-Sets the paused flag
     */
    public void proceed() {
        this.paused = false;
    }

    /**
     * Gets the done flag
     *
     * @return The done flag
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Gets the execution failure
     *
     * @return The execution failure
     */
    public Throwable getExecutionFailure() {
        return executionFailure;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

}
