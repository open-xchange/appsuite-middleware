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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;

/**
 * {@link Jobs} - Utility class for {@link IndexingJob}s.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Jobs {

    private static final int DEFAULT_PRIORITY = IndexingJob.DEFAULT_PRIORITY;

    /**
     * Initializes a new {@link Jobs}.
     */
    private Jobs() {
        super();
    }

    /**
     * Generates a new {@link IndexingJob job} for specified task.
     * 
     * @param task The task to perform as a job
     * @return A new {@link IndexingJob job} for specified task
     */
    public static IndexingJob jobFor(final Runnable task) {
        return jobFor(task, DEFAULT_PRIORITY);
    }

    /**
     * Generates a new {@link IndexingJob job} for specified task with given priority.
     * 
     * @param task The task to perform as a job
     * @param priority The priority
     * @return A new {@link Job job} for specified task
     */
    public static IndexingJob jobFor(final Runnable task, final int priority) {
        return new RunnableJob(task, priority);
    }

    private static final class RunnableJob implements IndexingJob {

        private static final long serialVersionUID = -3089273727289929417L;

        private final Runnable task;

        private final long timeStamp;

        private int priority;

        private final Map<String, ?> props;

        protected RunnableJob(final Runnable task, final int priority) {
            super();
            props = new ConcurrentHashMap<String, Object>();
            this.task = task;
            this.priority = priority;
            timeStamp = System.currentTimeMillis();
        }

        @Override
        public Map<String, ?> getProperties() {
            return props;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public Class<?>[] getNeededServices() {
            return EMPTY_CLASSES;
        }

        @Override
        public void performJob() throws OXException, InterruptedException {
            task.run();
        }

        @Override
        public boolean isDurable() {
            return false;
        }

        @Override
        public void setPriority(final int priority) {
            this.priority = priority;
        }

        @Override
        public Behavior getBehavior() {
            return Behavior.CONSUMER_RUNS;
        }

        @Override
        public void beforeExecute() {
            // Nope
        }

        @Override
        public void afterExecute(final Throwable t) {
            // Nope
        }

        @Override
        public long getTimeStamp() {
            return timeStamp;
        }

        @Override
        public Origin getOrigin() {
            return Origin.ACTIVE;
        }

    }

}
