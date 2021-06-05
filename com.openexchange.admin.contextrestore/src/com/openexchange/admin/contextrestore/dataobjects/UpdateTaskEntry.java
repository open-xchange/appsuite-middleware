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

package com.openexchange.admin.contextrestore.dataobjects;

/**
 * {@link UpdateTaskEntry} - Update task entry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskEntry {

    private int contextId;
    private String taskName;
    private boolean successful;
    private long lastModified;

    /**
     * Initializes a new {@link UpdateTaskEntry}.
     */
    public UpdateTaskEntry() {
        super();
    }

    /**
     * Initializes a new {@link UpdateTaskEntry}.
     */
    public UpdateTaskEntry(int contextId, String taskName, boolean successful, long lastModified) {
        super();
        this.contextId = contextId;
        this.lastModified = lastModified;
        this.successful = successful;
        this.taskName = taskName;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Sets the context identifier
     *
     * @param contextId The context identifier to set
     */
    public void setContextId(final int contextId) {
        this.contextId = contextId;
    }

    /**
     * Gets the task name
     *
     * @return The task name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Sets the task name
     *
     * @param taskName The task name to set
     */
    public void setTaskName(final String taskName) {
        this.taskName = taskName;
    }

    /**
     * Gets the successful flag
     *
     * @return The successful flag
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Sets the successful flag
     *
     * @param successful The successful flag to set
     */
    public void setSuccessful(final boolean successful) {
        this.successful = successful;
    }

    /**
     * Gets the last-modified time stamp.
     *
     * @return The last-modified time stamp
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last-modified time stamp
     *
     * @param lastModified The last-modified time stamp to set
     */
    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("{contextId=").append(contextId).append(", ");
        if (taskName != null) {
            builder.append("taskName=").append(taskName).append(", ");
        }
        builder.append("successful=").append(successful).append(", lastModified=").append(lastModified).append('}');
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
        result = prime * result + (successful ? 1231 : 1237);
        result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UpdateTaskEntry)) {
            return false;
        }
        final UpdateTaskEntry other = (UpdateTaskEntry) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (lastModified != other.lastModified) {
            return false;
        }
        if (successful != other.successful) {
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

}
