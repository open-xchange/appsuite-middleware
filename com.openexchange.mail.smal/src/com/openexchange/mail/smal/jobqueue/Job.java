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
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.LogFactory;
import com.openexchange.log.Log;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.mail.smal.adapter.IndexService;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link Job} - A job that is placed into {@link JobQueue}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class Job implements Task<Object>, Comparable<Job> {

    private static final AtomicReference<BlockingQueue<Job>> QUEUE_REF = new AtomicReference<BlockingQueue<Job>>();

    /**
     * Sets the blocking queue.
     */
    static final void set(final BlockingQueue<Job> newValue) {
        QUEUE_REF.set(newValue);
    }

    /**
     * The canceled flag.
     */
    protected volatile boolean canceled;

    /**
     * The paused flag.
     */
    protected volatile boolean paused;

    /**
     * Initializes a new {@link Job}.
     */
    protected Job() {
        super();
    }

    /**
     * Gets this job's ranking.
     * 
     * @return The ranking
     */
    public abstract int getRanking();

    /**
     * Gets an identifier for this job (used to look-up/filter identical jobs).
     * 
     * @return The identifier.
     */
    public abstract String getIdentifier();

    /**
     * Performs this job.
     */
    public abstract void perform();

    /**
     * Gets the working queue.
     * 
     * @return The working queue or <code>null</code>
     */
    public final BlockingQueue<Job> getQueue() {
        return QUEUE_REF.get();
    }

    /**
     * Gets the index adapter.
     * 
     * @return The index adapter
     */
    public IndexAdapter getAdapter() {
        return SMALServiceLookup.getInstance().getService(IndexService.class).getAdapter();
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        // Nothing to do
    }

    @Override
    public void beforeExecute(final Thread t) {
        // Nothing to do
    }

    @Override
    public void afterExecute(final Throwable t) {
        // Nothing to do
    }

    @Override
    public final int compareTo(final Job other) {
        final int thisVal = this.getRanking();
        final int anotherVal = other.getRanking();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    @Override
    public final Object call() throws Exception {
        try {
            perform();
        } catch (final Exception e) {
            Log.valueOf(LogFactory.getLog(Job.class)).error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Sets the canceled flag.
     * <p>
     * If set the job is not performed if not already done.
     */
    public void cancel() {
        canceled = true;
    }

    /**
     * Checks if the canceled flag is set.
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
}
