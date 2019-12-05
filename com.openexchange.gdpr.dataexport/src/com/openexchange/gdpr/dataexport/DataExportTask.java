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

package com.openexchange.gdpr.dataexport;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * {@link DataExportTask} - Represents a data export task for a certain user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportTask {

    private UUID uuid;
    private int userId;
    private int contextId;
    private DataExportStatus status;
    private List<DataExportWorkItem> workItems;
    private int fileStorageId;
    private Date creationTime;
    private Date startTime;
    private long duration;
    private DataExportArguments arguments;
    private List<DataExportResultFile> resultFiles;

    /**
     * Initializes a new {@link DataExportTask}.
     */
    public DataExportTask() {
        super();
        status = DataExportStatus.PENDING;
        duration = -1;
    }

    /**
     * Gets the result files.
     *
     * @return The result files or <code>null</code> (if task is not yet done)
     */
    public List<DataExportResultFile> getResultFiles() {
        return resultFiles;
    }

    /**
     * Sets the result files
     *
     * @param resultFiles The result files to set
     */
    public void setResultFiles(List<DataExportResultFile> resultFiles) {
        this.resultFiles = resultFiles;
    }

    /**
     * Gets the arguments
     *
     * @return The arguments
     */
    public DataExportArguments getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments
     *
     * @param arguments The arguments to set
     */
    public void setArguments(DataExportArguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Gets the task identifier.
     *
     * @return The task identifier
     */
    public UUID getId() {
        return uuid;
    }

    /**
     * Sets the task identifier.
     *
     * @param uuid The task identifier
     */
    public void setId(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the time when this task's processing has been started represented as number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return The start time or <code>null</code> if not yet started
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the time when this task's processing has been started represented as number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @param startTime The start time or <code>null</code> if not yet started
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime < 0 ? null : new Date(startTime);
    }

    /**
     * Gets the duration of this task's processing.
     *
     * @return The duration or <code>-1</code> if not yet started
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration of this task's processing.
     *
     * @param duration The duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration < 0 ? -1L : duration;
    }

    /**
     * Gets the time when this task has been created represented as number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return The creation time
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the time when this task has been created represented as number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @param creationTime The creation time
     */
    public void setCreationTime(long creationTime) {
        this.creationTime = new Date(creationTime);
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user identifier.
     *
     * @param userId The user identifier
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Sets the context identifier.
     *
     * @param contextId The context identifier
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    /**
     * Gets this task's status.
     *
     * @return The status
     */
    public DataExportStatus getStatus() {
        return status;
    }

    /**
     * Sets the tasks's status.
     *
     * @param status The task's status
     */
    public void setStatus(DataExportStatus status) {
        this.status = status;
    }

    /**
     * Gets the work items associated with this task.
     *
     * @return The work items
     */
    public List<DataExportWorkItem> getWorkItems() {
        return workItems;
    }

    /**
     * Sets the work items associated with this task.
     *
     * @param workItems The work items
     */
    public void setWorkItems(List<DataExportWorkItem> workItems) {
        this.workItems = workItems;
    }

    /**
     * Gets the identifier of the file storage, which is used by this task.
     *
     * @return The file storage identifier
     */
    public int getFileStorageId() {
        return fileStorageId;
    }

    /**
     * Sets the identifier of the file storage, which is used by this task.
     *
     * @param fileStorageId The file storage identifier
     */
    public void setFileStorageId(int fileStorageId) {
        this.fileStorageId = fileStorageId;
    }

}
