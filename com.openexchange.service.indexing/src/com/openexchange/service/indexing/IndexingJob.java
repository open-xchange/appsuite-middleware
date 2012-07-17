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

package com.openexchange.service.indexing;

import java.io.Serializable;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link IndexingJob} - Represents an arbitrary, {@link java.io.Serializable serializable} job described only using POJO (plain old Java
 * objects) for the sake of reliability and consistency throughout clustered nodes.
 * <p>
 * Specify how a job is supposed to be performed by {@link #getBehavior()} method.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IndexingJob extends Serializable {

    /**
     * The job's behavior: Either {@link #CONSUMER_RUNS consumer-runs} (low-cost) or {@link #DELEGATE delegate} (high-cost) job.
     */
    public static enum Behavior implements Serializable {
        /**
         * Consumer runs associated job (default). Appropriate for small jobs which are performed in a timely manner.
         */
        CONSUMER_RUNS,
        /**
         * Consumer delegates job's execution to another thread. Appropriate for high-cost jobs.
         */
        DELEGATE, ;
    }

    /**
     * A job's origin.
     */
    public static enum Origin implements Serializable {
        /**
         * Active user interaction
         */
        ACTIVE,
        /**
         * Initiated by passive operation.
         */
        PASSIVE
    }

    /**
     * The default priority is <code>4</code>.
     * 
     * @see javax.jms.Message#DEFAULT_PRIORITY
     */
    public static final int DEFAULT_PRIORITY = 4;

    /**
     * The default behavior is {@link Behavior#CONSUMER_RUNS consumer-runs}.
     */
    public static final Behavior DEFAULT_BEHAVIOR = Behavior.CONSUMER_RUNS;

    /**
     * The default origin is {@link Origin#ACTIVE active}.
     */
    public static final Origin DEFAULT_ORIGIN = Origin.ACTIVE;

    /**
     * The empty class array.
     */
    public static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

    /**
     * Gets the classes of the services which need to be available to start this activator.
     * 
     * @return The array of {@link Class} instances of needed services
     */
    Class<?>[] getNeededServices();

    /**
     * Performs this job's task.
     * 
     * @throws OXException If performing job fails for any reason
     * @throws InterruptedException If job has been interrupted
     */
    void performJob() throws OXException, InterruptedException;

    /**
     * Indicates whether this job is durable.
     * <p>
     * Durable jobs will be persisted in permanent storage and will survive server failure or restart. Non durable jobs will not survive
     * server failure or restart.
     * 
     * @return <code>true</code> if durable; otherwise <code>false</code>
     */
    boolean isDurable();

    /**
     * Gets the priority, ranges from <code>0</code> (lowest) to <code>9</code> (highest).
     * <p>
     * Default is <code>4</code>.
     * 
     * @return This job's priority
     */
    int getPriority();

    /**
     * Sets the priority, ranges from <code>0</code> (lowest) to <code>9</code> (highest).
     * <p>
     * Default is <code>4</code>.
     * 
     * @param priority This job's priority
     */
    void setPriority(int priority);

    /**
     * Gets this job's time stamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT).
     * 
     * @return The time stamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT)
     */
    long getTimeStamp();

    /**
     * Gets this job's origin
     * 
     * @return The origin
     */
    Origin getOrigin();

    /**
     * Gets this job's {@link Behavior behavior}.
     * 
     * @return The behavior determining how to execute this job
     */
    Behavior getBehavior();

    /**
     * Invoked prior to executing this task in the given thread. This method is invoked by pooled thread <tt>t</tt> that will execute this
     * task, and may be used to re-initialize {@link ThreadLocal}s, or to perform logging.
     * <p>
     * Implementations may leave this method empty if nothing should be performed.
     */
    void beforeExecute();

    /**
     * Invoked upon completion of execution of this task. This method is invoked by the thread that executed the task. If non-null, the
     * {@link Throwable} is the uncaught exception that caused execution to terminate abruptly.
     * <p>
     * Implementations may leave this method empty if nothing should be performed.
     * 
     * @param t The exception that caused termination, or <code>null</code> if execution completed normally
     */
    void afterExecute(Throwable t);

    /**
     * Gets the modifiable properties associated with this job.
     * 
     * @return The properties
     */
    Map<String, ?> getProperties();

}
