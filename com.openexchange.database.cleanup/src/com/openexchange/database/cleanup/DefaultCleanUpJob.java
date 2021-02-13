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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.database.cleanup;

import java.time.Duration;

/**
 * {@link DefaultCleanUpJob} - The default clean-up job implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class DefaultCleanUpJob implements CleanUpJob {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for an instance of <code>DefaultCleanUpJob</code>.
     */
    public static class Builder {

        private CleanUpJobId id;
        private CleanUpExecution execution;
        private Duration initialDelay;
        private Duration delay;
        private boolean runsExclusive;
        private boolean preferNoConnectionTimeout;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the identifier to given class' name.
         *
         * @param jobClass The class of the clean-up job execution
         * @return This builder
         * @throws IllegalArgumentException If name of given class is not suitable as job identifier
         * @see CleanUpJobId#newInstanceFor(String)
         */
        public Builder withId(Class<? extends CleanUpExecution> jobClass) {
            if (jobClass == null) {
                throw new IllegalArgumentException("Class must not be null");
            }
            return withId(CleanUpJobId.newInstanceFor(jobClass.getName()));
        }

        /**
         * Sets the identifier.
         *
         * @param jobId The identifier as string
         * @return This builder
         * @throws IllegalArgumentException If given job identifier is illegal
         * @see CleanUpJobId#newInstanceFor(String)
         */
        public Builder withId(String jobId) {
            return withId(CleanUpJobId.newInstanceFor(jobId));
        }

        /**
         * Sets the identifier.
         *
         * @param id The identifier to set
         * @return This builder
         */
        public Builder withId(CleanUpJobId id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the execution.
         *
         * @param execution The execution to set
         * @return This builder
         */
        public Builder withExecution(CleanUpExecution execution) {
            this.execution = execution;
            return this;
        }

        /**
         * Sets the initial delay.
         *
         * @param initialDelay The initial delay to set
         * @return This builder
         */
        public Builder withInitialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        /**
         * Sets the delay.
         *
         * @param delay The delay to set
         * @return This builder
         */
        public Builder withDelay(Duration delay) {
            this.delay = delay;
            return this;
        }

        /**
         * Sets whether the job is supposed to run exclusively.
         *
         * @param runsExclusive The flag to set
         * @return This builder
         */
        public Builder withRunsExclusive(boolean runsExclusive) {
            this.runsExclusive = runsExclusive;
            return this;
        }

        /**
         * Sets whether the job prefers a database connection w/o timeout.
         *
         * @param preferNoConnectionTimeout <code>true</code> if the job prefers a database connection w/o timeout, otherwise <code>false</code>
         * @return This builder
         */
        public Builder withPreferNoConnectionTimeout(boolean preferNoConnectionTimeout) {
            this.preferNoConnectionTimeout = preferNoConnectionTimeout;
            return this;
        }

        /**
         * Builds the instance of <code>DefaultCleanUpJob</code> from this builder's arguments.
         *
         * @return The instance of <code>DefaultCleanUpJob</code>
         * @throws IllegalStateException If this builder's arguments are not suitable to build an instance of <code>DefaultCleanUpJob</code>
         */
        public DefaultCleanUpJob build() {
            if (id == null) {
                throw new IllegalStateException("Identifier must not be null");
            }
            if (execution == null) {
                throw new IllegalStateException("Execution must not be null");
            }
            if (delay == null) {
                throw new IllegalStateException("Delay must not be null");
            }
            if (delay.isNegative() || delay.isZero()) {
                throw new IllegalStateException("Delay must not be negative or 0 (zero)");
            }
            return new DefaultCleanUpJob(id, execution, initialDelay == null ? Duration.ZERO : initialDelay, delay, runsExclusive, preferNoConnectionTimeout);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final CleanUpJobId id;
    private final CleanUpExecution execution;
    private final Duration initialDelay;
    private final Duration delay;
    private final boolean runsExclusive;
    private final boolean preferNoConnectionTimeout;

    /**
     * Initializes a new {@link DefaultCleanUpJob}.
     *
     * @param id The job identifier
     * @param execution The actual execution
     * @param initialDelay The initial delay
     * @param delay The delay
     * @param runsExclusive Whether the execution is supposed to run exclusive
     * @param preferNoConnectionTimeout <code>true</code> if the job prefers a database connection w/o timeout, <code>false</code>, otherwise
     */
    DefaultCleanUpJob(CleanUpJobId id, CleanUpExecution execution, Duration initialDelay, Duration delay, boolean runsExclusive, boolean preferNoConnectionTimeout) {
        super();
        this.id = id;
        this.execution = execution;
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.runsExclusive = runsExclusive;
        this.preferNoConnectionTimeout = preferNoConnectionTimeout;
    }

    @Override
    public CleanUpJobId getId() {
        return id;
    }

    @Override
    public CleanUpExecution getExecution() {
        return execution;
    }

    @Override
    public Duration getInitialDelay() {
        return initialDelay;
    }

    @Override
    public Duration getDelay() {
        return delay;
    }

    @Override
    public boolean isRunsExclusive() {
        return runsExclusive;
    }

    @Override
    public boolean isPreferNoConnectionTimeout() {
        return preferNoConnectionTimeout;
    }

}
