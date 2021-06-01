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

/**
 * {@link TaskFailure}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class TaskFailure implements Comparable<TaskFailure> {

    private final int hashCode;

    private final String taskName;
    private final String className;
    private final String schemaName;

    /**
     * Initializes a new {@link TaskFailure}.
     *
     * @param taskName The name of the failed update task
     * @param className The name of the class the update task belongs to
     * @param schemaName The name of the schema on which the update task failed
     */
    TaskFailure(String taskName, String className, String schemaName) {
        super();
        this.taskName = taskName;
        this.className = className;
        this.schemaName = schemaName;

        int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
        result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
        hashCode = result;
    }

    /**
     * Gets the name of the failed update task.
     *
     * @return The task name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Gets the name of the class the update task belongs to.
     *
     * @return The class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the name of the schema on which the update task failed.
     *
     * @return The schema name
     */
    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public int compareTo(TaskFailure o) {
        int res = schemaName.compareTo(o.schemaName);
        return 0 == res ? taskName.compareTo(o.taskName) : res;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TaskFailure other = (TaskFailure) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (schemaName == null) {
            if (other.schemaName != null) {
                return false;
            }
        } else if (!schemaName.equals(other.schemaName)) {
            return false;
        }
        if (taskName == null) {
            if (other.taskName != null) {
                return false;
            }
        } else if (!taskName.equals(other.taskName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TaskFailure [");
        if (taskName != null) {
            sb.append("taskName=").append(taskName).append(", ");
        }
        if (className != null) {
            sb.append("className=").append(className).append(", ");
        }
        if (schemaName != null) {
            sb.append("schemaName=").append(schemaName);
        }
        sb.append("]");
        return sb.toString();
    }

    //////////////////////////////////// BUILDER ////////////////////////////////////

    /**
     * Creates builder to build {@link TaskFailure}.
     *
     * @return The newly created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link TaskFailure}.
     */
    public static final class Builder {

        private String taskName;
        private String className;
        private String schemaName;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the name of the update task that failed.
         *
         * @param taskName The task name
         * @return This instance
         */
        public Builder withTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        /**
         * Sets the name of the class the failed update task belongs to.
         *
         * @param className The class name
         * @return This instance
         */
        public Builder withClassName(String className) {
            this.className = className;
            return this;
        }

        /**
         * Sets the name of the schema, on which the update task failed.
         *
         * @param schemaName The schema name
         * @return This instance
         */
        public Builder withSchemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        /**
         * Builds the resulting instance of {@link TaskFailure} from this builder's arguments.
         *
         * @return The resulting instance of {@link TaskFailure}
         */
        public TaskFailure build() {
            return new TaskFailure(taskName, className, schemaName);
        }
    }
}
