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

package com.openexchange.gdpr.dataexport;

import java.util.UUID;
import com.openexchange.java.util.UUIDs;

/**
 * {@link DataExportTaskInfo} - Basic information about a data export task.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportTaskInfo {

    private final UUID taskId;
    private final int userId;
    private final int contextId;
    private final DataExportStatus status;

    /**
     * Initializes a new {@link DataExportTaskInfo}.
     *
     * @param taskId The task identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param status The task's status
     * @throws IllegalArgumentException If task identifier or status is <code>null</code>
     */
    public DataExportTaskInfo(UUID taskId, int userId, int contextId, DataExportStatus status) {
        super();
        this.status = status;
        if (taskId == null) {
            throw new IllegalArgumentException("Task identifier must not be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        this.taskId = taskId;
        this.userId = userId;
        this.contextId = contextId;
    }

    /**
     * Gets the status
     *
     * @return The status
     */
    public DataExportStatus getStatus() {
        return status;
    }

    /**
     * Gets the task identifier
     *
     * @return The task identifier
     */
    public UUID getTaskId() {
        return taskId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataExportTaskInfo [");
        if (taskId != null) {
            builder.append("taskId=").append(UUIDs.getUnformattedString(taskId)).append(", ");
        }
        builder.append("userId=").append(userId).append(", contextId=").append(contextId).append("]");
        return builder.toString();
    }

}
