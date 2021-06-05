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

package com.openexchange.gdpr.dataexport.impl.storage;

import java.util.Arrays;
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
import com.openexchange.user.UserService;

/**
 * {@link DataExportStorageServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class DataExportStorageServiceImpl implements DataExportStorageService {

    private final AbstractDataExportSql<?> sql;
    private final DataExportConfig config;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link DataExportStorageServiceImpl}.
     *
     * @param useGlobalDb Whether to use Global DB
     * @param config The configuration
     * @param services The service look-up
     * @throws OXException
     */
    public DataExportStorageServiceImpl(boolean useGlobalDb, DataExportConfig config, ServiceLookup services) throws OXException {
        super();
        this.config = config;
        this.services = services;
        if (useGlobalDb) {
            this.sql = new GlobalDbDataExportSql(services.getServiceSafe(DatabaseService.class), services.getServiceSafe(ConfigViewFactory.class), config, services);
        } else {
            this.sql = new UserDbDataExportSql(services.getServiceSafe(DatabaseService.class), services.getServiceSafe(ContextService.class), config, services);
        }
    }

    @Override
    public DataExportConfig getConfig() {
        return config;
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
    public List<DataExportTask> getRunningDataExportTasks() throws OXException {
        return sql.selectRunningDataExportTasks();
    }

    @Override
    public boolean hasRunningDataExportTasks() throws OXException {
        return sql.hasRunningDataExportTasks();
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
        return sql.updateTaskStatus(DataExportStatus.PAUSED, taskId, userId, contextId) != null;
    }

    @Override
    public Optional<DataExportTask> getDataExportTask(UUID taskId) throws OXException {
        return taskId == null ? Optional.empty() : Optional.ofNullable(sql.selectDataExportTask(taskId));
    }

    @Override
    public Optional<DataExportTask> getDataExportTask(int userId, int contextId) throws OXException {
        return Optional.ofNullable(sql.selectTasks(contextId, new int[] { userId }, false)[0]);
    }

    @Override
    public List<DataExportTask> getDataExportTasks(int contextId) throws OXException {
        int[] userIds = services.getServiceSafe(UserService.class).listAllUser(contextId, true, false);
        DataExportTask[] dataExportTasks = sql.selectTasks(contextId, userIds, true);
        return Arrays.asList(dataExportTasks);
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
        sql.updateSavePoint(taskId, moduleId, savePoint, userId, contextId);
    }

    @Override
    public boolean markDone(UUID taskId, int userId, int contextId) throws OXException {
        return sql.updateTaskStatus(DataExportStatus.DONE, taskId, userId, contextId, DataExportStatus.FAILED) != null;
    }

    @Override
    public boolean markAborted(UUID taskId, int userId, int contextId) throws OXException {
        DataExportStatus prev = sql.updateTaskStatus(DataExportStatus.ABORTED, taskId, userId, contextId, DataExportStatus.DONE, DataExportStatus.FAILED);
        if (prev == DataExportStatus.PENDING) {
            deleteDataExportTask(userId, contextId);
        }
        return prev != null;
    }

    @Override
    public boolean markFailed(UUID taskId, int userId, int contextId) throws OXException {
        return sql.updateTaskStatus(DataExportStatus.FAILED, taskId, userId, contextId, DataExportStatus.DONE) != null;
    }

    @Override
    public boolean markPending(UUID taskId, int userId, int contextId) throws OXException {
        return sql.updateTaskStatus(DataExportStatus.PENDING, taskId, userId, contextId) != null;
    }

    @Override
    public boolean setNotificationSent(UUID taskId, int userId, int contextId) throws OXException {
        return sql.setNotificationSent(taskId, userId, contextId);
    }

    @Override
    public boolean unsetNotificationSent(UUID taskId, int userId, int contextId) throws OXException {
        return sql.unsetNotificationSent(taskId, userId, contextId);
    }

    @Override
    public void markWorkItemDone(String fileStorageLocation, UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        sql.updateWorkItemStatus(DataExportStatus.DONE, fileStorageLocation, null, taskId, moduleId, userId, contextId);
    }

    @Override
    public void markWorkItemPaused(String fileStorageLocation, UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        sql.updateWorkItemStatus(DataExportStatus.PAUSED, fileStorageLocation, null, taskId, moduleId, userId, contextId);
    }

    @Override
    public void markWorkItemFailed(Optional<JSONObject> jFailureInfo, UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        sql.updateWorkItemStatus(DataExportStatus.DONE, null, jFailureInfo.orElse(null), taskId, moduleId, userId, contextId);
    }

    @Override
    public DataExportSavepoint getSavePoint(UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        return sql.selectSavePoint(taskId, moduleId, userId, contextId);
    }

    @Override
    public void markWorkItemPending(UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        sql.updateWorkItemStatus(DataExportStatus.PENDING, null, null, taskId, moduleId, userId, contextId);
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
    public void addResultFile(String fileStorageLocation, int number, long size, UUID taskId, int userId, int contextId) throws OXException {
        sql.addResultFile(fileStorageLocation, number, size, taskId, userId, contextId);
    }

    @Override
    public void deleteResultFiles(UUID taskId, int userId, int contextId) throws OXException {
        sql.deleteResultFiles(taskId, userId, contextId);
    }

    @Override
    public void dropIntermediateFiles(UUID taskId, int userId, int contextId) throws OXException {
        sql.dropIntermediateFiles(taskId, userId, contextId);
    }

    @Override
    public boolean incrementFailCount(UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        return sql.incrementFailCount(taskId, moduleId, userId, contextId);
    }

}
