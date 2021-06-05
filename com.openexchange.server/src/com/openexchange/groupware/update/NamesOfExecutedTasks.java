/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.update;

import java.util.HashSet;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link NamesOfExecutedTasks} - A collection of executed update tasks consisting of successfully executed and failed ones.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class NamesOfExecutedTasks {

    /**
     * Creates a new builder for an instance of <code>NamesOfExecutedTasks</code>.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>NamesOfExecutedTasks</code> */
    public static class Builder {

        private Set<String> successfullyExecutedTasks;
        private Set<String> failedExecutedTasks;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Adds the name of a successfully executed update task to this builder.
         *
         * @param taskName The task name
         * @return This builder
         */
        public Builder addSuccessfullyExecutedTask(String taskName) {
            if (taskName != null) {
                if (successfullyExecutedTasks == null) {
                    successfullyExecutedTasks = new HashSet<>();
                }
                successfullyExecutedTasks.add(taskName);
            }
            return this;
        }

        /**
         * Adds the name of a failed update task to this builder.
         *
         * @param taskName The task name
         * @return This builder
         */
        public Builder addFailedTask(String taskName) {
            if (taskName != null) {
                if (failedExecutedTasks == null) {
                    failedExecutedTasks = new HashSet<>();
                }
                failedExecutedTasks.add(taskName);
            }
            return this;
        }

        /**
         * Sets the successfully executed update tasks
         *
         * @param successfullyExecutedTasks The successfully executed update tasks
         * @return This builder
         */
        public Builder withSuccessfullyExecutedTasks(Set<String> successfullyExecutedTasks) {
            this.successfullyExecutedTasks = successfullyExecutedTasks == null ? null : new HashSet<>(successfullyExecutedTasks);
            return this;
        }

        /**
         * Sets the successfully failed tasks
         *
         * @param successfullyExecutedTasks The failed update tasks
         * @return This builder
         */
        public Builder withFailedTasks(Set<String> failedExecutedTasks) {
            this.failedExecutedTasks = failedExecutedTasks == null ? null : new HashSet<>(failedExecutedTasks);
            return this;
        }

        /**
         * Builds the resulting instance of <code>NamesOfExecutedTasks</code> from this builder's arguments.
         *
         * @return The resulting instance of <code>NamesOfExecutedTasks</code>
         */
        public NamesOfExecutedTasks build() {
            return new NamesOfExecutedTasks(successfullyExecutedTasks, failedExecutedTasks);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Set<String> successfullyExecutedTasks;
    private final Set<String> failedExecutedTasks;

    /**
     * Initializes a new {@link NamesOfExecutedTasks}.
     *
     * @param successfullyExecutedTasks The names of successfully executed update tasks
     * @param failedExecutedTasks The names of failed update tasks
     */
    NamesOfExecutedTasks(Set<String> successfullyExecutedTasks, Set<String> failedExecutedTasks) {
        super();
        this.successfullyExecutedTasks = successfullyExecutedTasks == null || successfullyExecutedTasks.isEmpty() ? ImmutableSet.of() : ImmutableSet.copyOf(successfullyExecutedTasks);
        this.failedExecutedTasks = failedExecutedTasks == null || failedExecutedTasks.isEmpty() ? ImmutableSet.of() : ImmutableSet.copyOf(failedExecutedTasks);
    }

    /**
     * Gets the names of successfully executed update tasks.
     *
     * @return The names of successfully executed update tasks
     */
    public Set<String> getSuccessfullyExecutedTasks() {
        return successfullyExecutedTasks;
    }

    /**
     * Gets the names of failed update tasks.
     *
     * @return The names of failed update tasks
     */
    public Set<String> getFailedExecutedTasks() {
        return failedExecutedTasks;
    }

}
