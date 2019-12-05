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
