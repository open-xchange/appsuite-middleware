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
 *    trademarks of the OX Software GmbH. group of companies.
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
     * @param builder the builder
     */
    @SuppressWarnings("synthetic-access")
    private TaskFailure(Builder builder) {
        taskName = builder.taskName;
        className = builder.className;
        schemaName = builder.schemaName;

        final int prime = 31;
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
     * Gets the name of the schema on which the update task failed
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

    //////////////////////////////////// BUILDER ////////////////////////////////////

    /**
     * Creates builder to build {@link TaskFailure}.
     *
     * @return created builder
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

        public Builder() {
            super();
        }

        public Builder withTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder withClassName(String className) {
            this.className = className;
            return this;
        }

        public Builder withSchemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        @SuppressWarnings("synthetic-access")
        public TaskFailure build() {
            return new TaskFailure(this);
        }
    }
}
