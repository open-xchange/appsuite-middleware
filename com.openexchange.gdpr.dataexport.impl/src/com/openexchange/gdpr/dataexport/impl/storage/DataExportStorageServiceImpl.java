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

package com.openexchange.gdpr.dataexport.impl.storage;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.json.JSONObject;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DataExportStatus;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportTaskInfo;
import com.openexchange.gdpr.dataexport.DataExportJob;
import com.openexchange.gdpr.dataexport.DataExportSavepoint;
import com.openexchange.gdpr.dataexport.FileLocation;
import com.openexchange.gdpr.dataexport.FileLocations;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DataExportStorageServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class DataExportStorageServiceImpl implements DataExportStorageService {

    private final AbstractDataExportSql<?> sql;

    /**
     * Initializes a new {@link DataExportStorageServiceImpl}.
     *
     * @param useGlobalDb Whether to use Global DB
     * @param config The configuration
     * @param dbProvider The database provider
     * @throws OXException
     */
    public DataExportStorageServiceImpl(boolean useGlobalDb, DataExportConfig config, ServiceLookup services) throws OXException {
        super();
        if (useGlobalDb) {
            this.sql = new GlobalDbDataExportSql(services.getServiceSafe(DatabaseService.class), services.getServiceSafe(ConfigViewFactory.class), config);
        } else {
            this.sql = new UserDbDataExportSql(services.getServiceSafe(DatabaseService.class), services.getServiceSafe(ContextService.class), config);
        }
    }

    @Override
    public boolean createIfAbsent(DataExportTask task, int userId, int contextId) throws OXException {
        return sql.createIfAbsent(task, userId, contextId);
    }

    @Override
    public List<DataExportTask> getDataExportTasks() throws OXException {
        return sql.selectDataExportTasks();
    }

    @Override
    public List<DataExportTaskInfo> deleteCompletedOrAbortedTasksAndGetTasksWithPendingNotification() throws OXException {
        return sql.deleteCompletedOrAbortedTasksAndGetTasksWithPendingNotification();
    }

    @Override
    public List<DataExportTaskInfo> getDataExportTasksWithPendingNotification() throws OXException {
        return sql.getDataExportTasksWithPendingNotification();
    }

    @Override
    public Optional<DataExportJob> getNextDataExportJob() throws OXException {
        return Optional.ofNullable(sql.selectNextJob());
    }

    @Override
    public void touch(int userId, int contextId) throws OXException {
        sql.touchTask(userId, contextId);
    }

    @Override
    public boolean markPaused(UUID taskId, int userId, int contextId) throws OXException {
        return sql.updateTaskStatus(DataExportStatus.PAUSED, taskId, contextId) != null;
    }

    @Override
    public Optional<DataExportTask> getDataExportTask(UUID taskId) throws OXException {
        return taskId == null ? Optional.empty() : Optional.ofNullable(sql.selectDataExportTask(taskId));
    }

    @Override
    public Optional<DataExportTask> getDataExportTask(int userId, int contextId) throws OXException {
        return Optional.ofNullable(sql.selectTask(contextId, userId));
    }

    @Override
    public List<DataExportTask> getDataExportTasks(int contextId) throws OXException {
        return sql.selectTasks(contextId);
    }

    @Override
    public Optional<Date> getLastAccessedTimeStamp(int userId, int contextId) throws OXException {
        return Optional.ofNullable(sql.selectLastAccessedTimeStamp(contextId, userId));
    }

    @Override
    public Optional<FileLocations> getDataExportResultFiles(int userId, int contextId) throws OXException {
        return Optional.ofNullable(sql.selectResultFiles(contextId, userId));
    }

    @Override
    public Optional<FileLocation> getDataExportResultFile(int number, int userId, int contextId) throws OXException {
        return Optional.ofNullable(sql.selectResultFile(number, contextId, userId));
    }

    @Override
    public Optional<DataExportStatus> getDataExportStatus(UUID taskId) throws OXException {
        return taskId == null ? Optional.empty() : Optional.ofNullable(sql.selectTaskStatus(taskId));
    }

    @Override
    public Optional<DataExportStatus> getDataExportStatus(int userId, int contextId) throws OXException {
        return Optional.ofNullable(sql.selectTaskStatus(contextId, userId));
    }

    @Override
    public void setSavePoint(UUID taskId, String moduleId, DataExportSavepoint savePoint, int userId, int contextId) throws OXException {
        sql.updateSavePoint(taskId, moduleId, savePoint, contextId);
    }

    @Override
    public boolean markDone(UUID taskId, int userId, int contextId) throws OXException {
        return sql.updateTaskStatus(DataExportStatus.DONE, taskId, contextId, DataExportStatus.FAILED) != null;
    }

    @Override
    public boolean markAborted(UUID taskId, int userId, int contextId) throws OXException {
        DataExportStatus prev = sql.updateTaskStatus(DataExportStatus.ABORTED, taskId, contextId, DataExportStatus.DONE, DataExportStatus.FAILED);
        if (prev == DataExportStatus.PENDING) {
            deleteDataExportTask(userId, contextId);
        }
        return prev != null;
    }

    @Override
    public boolean markFailed(UUID taskId, int userId, int contextId) throws OXException {
        return sql.updateTaskStatus(DataExportStatus.FAILED, taskId, contextId, DataExportStatus.DONE) != null;
    }

    @Override
    public boolean markPending(UUID taskId, int userId, int contextId) throws OXException {
        return sql.updateTaskStatus(DataExportStatus.PENDING, taskId, contextId) != null;
    }

    @Override
    public boolean setNotificationSent(UUID taskId, int userId, int contextId) throws OXException {
        return sql.setNotificationSent(taskId, contextId);
    }

    @Override
    public boolean unsetNotificationSent(UUID taskId, int userId, int contextId) throws OXException {
        return sql.unsetNotificationSent(taskId, contextId);
    }

    @Override
    public void markWorkItemDone(String fileStorageLocation, UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        sql.updateWorkItemStatus(DataExportStatus.DONE, fileStorageLocation, null, taskId, moduleId, contextId);
    }

    @Override
    public void markWorkItemPaused(String fileStorageLocation, UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        sql.updateWorkItemStatus(DataExportStatus.PAUSED, fileStorageLocation, null, taskId, moduleId, contextId);
    }

    @Override
    public void markWorkItemFailed(Optional<JSONObject> jFailureInfo, UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        sql.updateWorkItemStatus(DataExportStatus.DONE, null, jFailureInfo.orElse(null), taskId, moduleId, contextId);
    }

    @Override
    public DataExportSavepoint getSavePoint(UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        return sql.selectSavePoint(taskId, moduleId, contextId);
    }

    @Override
    public void markWorkItemPending(UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        sql.updateWorkItemStatus(DataExportStatus.PENDING, null, null, taskId, moduleId, contextId);
    }

    @Override
    public boolean deleteDataExportTask(UUID taskId) throws OXException {
        return sql.deleteTask(taskId);
    }

    @Override
    public boolean deleteDataExportTask(int userId, int contextId) throws OXException {
        return sql.deleteTask(userId, contextId);
    }

    @Override
    public void addResultFile(String fileStorageLocation, int number, long size, UUID taskId, int contextId) throws OXException {
        sql.addResultFile(fileStorageLocation, number, size, taskId, contextId);
    }

    @Override
    public void deleteResultFiles(UUID taskId, int contextId) throws OXException {
        sql.deleteResultFiles(taskId, contextId);
    }

    @Override
    public void dropIntermediateFiles(UUID taskId, int contextId) throws OXException {
        sql.dropIntermediateFiles(taskId, contextId);
    }

    @Override
    public boolean incrementFailCount(UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        return sql.incrementFailCount(taskId, moduleId, contextId);
    }

}
