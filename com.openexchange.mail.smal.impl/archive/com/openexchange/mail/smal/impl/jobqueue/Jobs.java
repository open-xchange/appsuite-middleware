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

import java.util.UUID;
import java.util.concurrent.Callable;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Jobs} - Utility class for {@link Job}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Jobs {

    /**
     * Initializes a new {@link Jobs}.
     */
    private Jobs() {
        super();
    }

    /**
     * Generates a new {@link Job job} for specified task with a unique identifier and {@link Job#DEFAULT_RANKING default ranking}.
     *
     * @param task The task to perform as a job
     * @return A new {@link Job job} for specified task
     */
    public static Job jobFor(final Runnable task) {
        return jobFor(task, new StringBuilder(48).append("RunnableJob@").append(UUIDs.getUnformattedString(UUID.randomUUID())).toString());
    }

    /**
     * Generates a new {@link Job job} for specified task with given identifier and {@link Job#DEFAULT_RANKING default ranking}.
     *
     * @param task The task to perform as a job
     * @param identifier The identifier
     * @return A new {@link Job job} for specified task
     */
    public static Job jobFor(final Runnable task, final String identifier) {
        return jobFor(task, identifier, Job.DEFAULT_RANKING);
    }

    /**
     * Generates a new {@link Job job} for specified task with a unique identifier and given ranking.
     *
     * @param task The task to perform as a job
     * @param ranking The ranking
     * @return A new {@link Job job} for specified task
     */
    public static Job jobFor(final Runnable task, final int ranking) {
        final StringBuilder tmp = new StringBuilder(48).append("RunnableJob@").append(UUIDs.getUnformattedString(UUID.randomUUID()));
        return jobFor(task, tmp.toString(), ranking);
    }

    /**
     * Generates a new {@link Job job} for specified task with given identifier and ranking.
     *
     * @param task The task to perform as a job
     * @param identifier The identifier
     * @param ranking The ranking
     * @return A new {@link Job job} for specified task
     */
    public static Job jobFor(final Runnable task, final String identifier, final int ranking) {
        return new RunnableJob(task, identifier, ranking);
    }

    /**
     * Generates a new {@link Job job} for specified task with a unique identifier and {@link Job#DEFAULT_RANKING default ranking}.
     *
     * @param task The task to perform as a job
     * @return A new {@link Job job} for specified task
     */
    public static <V> Job jobFor(final Callable<V> task) {
        return jobFor(task, new StringBuilder(48).append("CallableJob@").append(UUIDs.getUnformattedString(UUID.randomUUID())).toString());
    }

    /**
     * Generates a new {@link Job job} for specified task with given identifier and {@link Job#DEFAULT_RANKING default ranking}.
     *
     * @param task The task to perform as a job
     * @param identifier The identifier
     * @return A new {@link Job job} for specified task
     */
    public static <V> Job jobFor(final Callable<V> task, final String identifier) {
        return jobFor(task, identifier, Job.DEFAULT_RANKING);
    }

    /**
     * Generates a new {@link Job job} for specified task with a unique identifier and given ranking.
     *
     * @param task The task to perform as a job
     * @param ranking The ranking
     * @return A new {@link Job job} for specified task
     */
    public static <V> Job jobFor(final Callable<V> task, final int ranking) {
        final StringBuilder tmp = new StringBuilder(48).append("CallableJob@").append(UUIDs.getUnformattedString(UUID.randomUUID()));
        return jobFor(task, tmp.toString(), ranking);
    }

    /**
     * Generates a new {@link Job job} for specified task with given identifier and ranking.
     *
     * @param task The task to perform as a job
     * @param identifier The identifier
     * @param ranking The ranking
     * @return A new {@link Job job} for specified task
     */
    public static <V> Job jobFor(final Callable<V> task, final String identifier, final int ranking) {
        return new CallableJob<V>(task, identifier, ranking);
    }

    private static final class CallableJob<V> extends Job {

        private static final long serialVersionUID = -1177119754852426366L;

        private final Callable<V> task;

        private final String identifier;

        private final int ranking;

        protected CallableJob(final Callable<V> task, final String identifier, final int ranking) {
            super();
            this.task = task;
            this.identifier = identifier;
            this.ranking = ranking;
        }

        @Override
        public int getRanking() {
            return ranking;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public void perform() {
            try {
                task.call();
            } catch (final Exception e) {
                executionFailure = e;
            }
        }

        @Override
        public boolean forcedRun() {
            return true;
        }
    }

    private static final class RunnableJob extends Job {

        private static final long serialVersionUID = -3062273727289929417L;

        private final Runnable task;

        private final String identifier;

        private final int ranking;

        protected RunnableJob(final Runnable task, final String identifier, final int ranking) {
            super();
            this.task = task;
            this.identifier = identifier;
            this.ranking = ranking;
        }

        @Override
        public int getRanking() {
            return ranking;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public void perform() {
            try {
                task.run();
            } catch (final RuntimeException e) {
                final Throwable cause = e.getCause();
                executionFailure = null == cause ? e : cause;
            }
        }

        @Override
        public boolean forcedRun() {
            return true;
        }

    }

}
