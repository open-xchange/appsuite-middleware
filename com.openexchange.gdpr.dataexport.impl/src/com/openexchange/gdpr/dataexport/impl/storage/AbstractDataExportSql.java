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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportArguments;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportJob;
import com.openexchange.gdpr.dataexport.DataExportDiagnosticsReport;
import com.openexchange.gdpr.dataexport.DataExportResultFile;
import com.openexchange.gdpr.dataexport.DataExportSavepoint;
import com.openexchange.gdpr.dataexport.DataExportStatus;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportTaskInfo;
import com.openexchange.gdpr.dataexport.DataExportWorkItem;
import com.openexchange.gdpr.dataexport.DefaultDataExportResultFile;
import com.openexchange.gdpr.dataexport.DefaultDataExportResultFile.Builder;
import com.openexchange.gdpr.dataexport.DefaultFileLocation;
import com.openexchange.gdpr.dataexport.FileLocation;
import com.openexchange.gdpr.dataexport.FileLocations;
import com.openexchange.gdpr.dataexport.HostInfo;
import com.openexchange.gdpr.dataexport.Message;
import com.openexchange.gdpr.dataexport.Module;
import com.openexchange.gdpr.dataexport.impl.DataExportUtility;
import com.openexchange.gdpr.dataexport.impl.osgi.Services;
import com.openexchange.gdpr.dataexport.impl.utils.FileStorageAndId;
import com.openexchange.java.util.UUIDs;

/**
 * {@link AbstractDataExportSql} - The abstract SQL access to data export storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 * @param <R> The schema reference type
 */
public abstract class AbstractDataExportSql<R> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractDataExportSql.class);

    /**
     * Checks whether Global DB is supposed to be used or not.
     *
     * @return <code>true</code> to use GLobal DB; otherwise <code>false</code>
     * @throws OXException If option cannot be checked
     */
    public static boolean isUseGlobalDb() throws OXException {
        ConfigurationService configService = Services.requireService(ConfigurationService.class);
        return configService.getBoolProperty("com.openexchange.gdpr.dataexport.useGlobalDb", true);
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    /** The database service */
    protected final DatabaseService databaseService;

    private final DataExportConfig config;
    private final ConcurrentMap<String, Boolean> tableExistsCache;

    /**
     * Initializes a new {@link AbstractDataExportSql}.
     *
     * @param databaseService The database service
     * @param config The configuration
     */
    protected AbstractDataExportSql(DatabaseService databaseService, DataExportConfig config) {
        super();
        this.databaseService = databaseService;
        this.config = config;
        tableExistsCache = new ConcurrentHashMap<String, Boolean>();
    }

    /**
     * Checks if table 'dataExportTask' does exist.
     *
     * @param connection The connection to use
     * @param schema The name of the database schema
     * @return <code>true</code> if table exists; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    private boolean tableExists(Connection connection, String schema) throws OXException {
        Boolean bExists = tableExistsCache.get(schema);
        if (bExists == null) {
            try {
                boolean exists = Databases.tableExists(connection, "dataExportTask");
                if (!exists) {
                    return false;
                }

                // Only cache positive results
                bExists = Boolean.TRUE;
                tableExistsCache.put(schema, bExists);
            } catch (SQLException e) {
                throw handleException(e);
            }
        }
        return bExists.booleanValue();
    }

    /**
     * Puts given read-only connection back into pool.
     *
     * @param schemaReference The instance referencing database schema
     * @param con The read-only connection
     */
    protected abstract void backReadOnly(R schemaReference, Connection con);

    /**
     * Puts given read-write connection back into pool.
     *
     * @param modified Whether connection has actually been used for modifying data
     * @param schemaReference The instance referencing database schema
     * @param con The read-write connection
     */
    protected abstract void backWritable(boolean modified, R schemaReference, Connection con);

    /**
     * Acquires a read-only connection for given schema reference.
     *
     * @param schemaReference The schema reference
     * @return The read-only connection
     * @throws OXException if connection cannot be returned
     */
    protected abstract Connection getReadOnly(R schemaReference) throws OXException;

    /**
     * Acquires a read-write connection for given schema reference.
     *
     * @param schemaReference The schema reference
     * @return The read-write connection
     * @throws OXException if connection cannot be returned
     */
    protected abstract Connection getWritable(R schemaReference) throws OXException;

    /**
     * Gets the references for existing schemas.
     *
     * @return The schema references
     * @throws OXException If schema references cannot be returned
     */
    protected abstract Collection<R> getSchemaReferences() throws OXException;

    /**
     * Gets the schema references for given arguments.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The schema reference
     * @throws OXException If schema reference cannot be returned
     */
    protected abstract R getSchemaReference(int userId, int contextId) throws OXException;

    /**
     * Gets a certain Export Task.
     *
     * @param taskId The task id
     * @return The Task
     * @throws OXException
     */
    DataExportTask selectDataExportTask(UUID taskId) throws OXException {
        byte[] taskIdBytes = UUIDs.toByteArray(taskId);
        for (R schemaReference : getSchemaReferences()) {
            Connection connection = null;
            try {
                connection = getReadOnly(schemaReference);
                if (tableExists(connection, connection.getCatalog())) {
                    try (PreparedStatement stmt = connection.prepareStatement("SELECT cid, uuid, user, status, filestore, creationTime, startTime, duration, arguments FROM dataExportTask WHERE uuid = ?")) {
                        stmt.setBytes(1, taskIdBytes);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                DataExportTask retval = parsetTask(rs);
                                retval.setWorkItems(selectWorkItems(taskId, connection));
                                List<DataExportResultFile> resultFiles = selectResultFiles(taskId, connection);
                                if (resultFiles != null && !resultFiles.isEmpty()) {
                                    retval.setResultFiles(resultFiles);
                                }
                                return retval;
                            }
                        }
                    }
                }
            } catch (OXException e) {
                handleOXException(e);
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backReadOnly(schemaReference, connection);
            }
        }
        return null;
    }

    /**
     * Gets all Tasks of all contexts
     *
     * @return A List of Tasks
     * @throws OXException
     */
    List<DataExportTask> selectDataExportTasks() throws OXException {
        List<DataExportTask> tasks = null;
        for (R schemaReference : getSchemaReferences()) {
            Connection connection = null;
            try {
                connection = getReadOnly(schemaReference);
                if (tableExists(connection, connection.getCatalog())) {
                    try (PreparedStatement stmt = connection.prepareStatement("SELECT cid, uuid, user, status, filestore, creationTime, startTime, duration, arguments FROM dataExportTask")) {
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                if (tasks == null) {
                                    tasks = new ArrayList<>();
                                }
                                do {
                                    DataExportTask task = parsetTask(rs);
                                    task.setWorkItems(selectWorkItems(task.getId(), connection));
                                    List<DataExportResultFile> resultFiles = selectResultFiles(task.getId(), connection);
                                    if (resultFiles != null && !resultFiles.isEmpty()) {
                                        task.setResultFiles(resultFiles);
                                    }
                                    tasks.add(task);
                                } while (rs.next());
                            }
                        }
                    }
                }
            } catch (OXException e) {
                handleOXException(e);
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backReadOnly(schemaReference, connection);
            }
        }
        return tasks == null ? Collections.emptyList() : tasks;
    }

    /**
     * Deletes all completed or aborted tasks of all contexts and checks for tasks with pending notification.
     *
     * @return A List of Tasks
     * @throws OXException
     */
    List<DataExportTaskInfo> deleteCompletedOrAbortedTasksAndGetTasksWithPendingNotification() throws OXException {
        long now = System.currentTimeMillis();
        long expirationThreshold = now - config.getExpirationTimeMillis();
        long maxTimeToLiveThreshold = now - config.getMaxTimeToLiveMillis();

        List<TaskInfo> tasksToDelete = null;
        List<DataExportTaskInfo> taskInfos = null;
        for (R schemaReference : getSchemaReferences()) {
            Connection connection = null;
            try {
                connection = getReadOnly(schemaReference);
                if (tableExists(connection, connection.getCatalog())) {
                    try (PreparedStatement stmt = connection.prepareStatement("SELECT cid, uuid, timestamp, user, status FROM dataExportTask WHERE (status=? AND timestamp<?) OR (status IN (?, ?) AND (timestamp<? OR notificationSent = 0))")) {
                        stmt.setString(1, DataExportStatus.ABORTED.toString());
                        stmt.setLong(2, expirationThreshold);
                        stmt.setString(3, DataExportStatus.DONE.toString());
                        stmt.setString(4, DataExportStatus.FAILED.toString());
                        stmt.setLong(5, maxTimeToLiveThreshold);
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                DataExportStatus status = DataExportStatus.statusFor(rs.getString(5)/* status */);
                                if (status.isAborted()) {
                                    if (tasksToDelete == null) {
                                        tasksToDelete = new ArrayList<>();
                                    }
                                    tasksToDelete.add(new TaskInfo(UUIDs.toUUID(rs.getBytes(2)/* uuid */), 0, rs.getInt(4)/* user */, rs.getInt(1)/* cid */));
                                } else if (status.isDone() || status.isFailed()) {
                                    UUID taskId = UUIDs.toUUID(rs.getBytes(2)/* uuid */);
                                    // Check if part of result set because expired or has pending notification
                                    if (rs.getLong(3) < maxTimeToLiveThreshold) {
                                        if (tasksToDelete == null) {
                                            tasksToDelete = new ArrayList<>();
                                        }
                                        tasksToDelete.add(new TaskInfo(taskId, 0, rs.getInt(4)/* user */, rs.getInt(1)/* cid */));
                                    } else {
                                        if (taskInfos == null) {
                                            taskInfos = new ArrayList<>();
                                        }
                                        taskInfos.add(new DataExportTaskInfo(taskId, rs.getInt(4)/* user */, rs.getInt(1)/* cid */, status));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (OXException e) {
                handleOXException(e);
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backReadOnly(schemaReference, connection);
            }
        }

        if (tasksToDelete != null) {
            for (TaskInfo taskInfo : tasksToDelete) {
                deleteTask(taskInfo.taskId, taskInfo.userId,  taskInfo.contextId);
            }
        }

        return taskInfos == null ? Collections.emptyList() : taskInfos;
    }

    /**
     * Gets all data export tasks with pending notifications.
     *
     * @return The data export tasks with pending notifications
     * @throws OXException If data export tasks cannot be returned
     */
    List<DataExportTaskInfo> getDataExportTasksWithPendingNotification() throws OXException {
        List<DataExportTaskInfo> tasks = null;
        for (R schemaReference : getSchemaReferences()) {
            Connection connection = null;
            try {
                connection = getReadOnly(schemaReference);
                if (tableExists(connection, connection.getCatalog())) {
                    try (PreparedStatement stmt = connection.prepareStatement("SELECT cid, uuid, user, status FROM dataExportTask WHERE ((status IN (?, ?) AND notificationSent = 0)")) {
                        stmt.setString(1, DataExportStatus.DONE.toString());
                        stmt.setString(2, DataExportStatus.FAILED.toString());
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                if (tasks == null) {
                                    tasks = new ArrayList<>();
                                }
                                do {
                                    tasks.add(new DataExportTaskInfo(UUIDs.toUUID(rs.getBytes(2)), rs.getInt(3), rs.getInt(1), DataExportStatus.statusFor(rs.getString(4))));
                                } while (rs.next());
                            }
                        }
                    }
                }
            } catch (OXException e) {
                handleOXException(e);
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backReadOnly(schemaReference, connection);
            }
        }
        return tasks == null ? Collections.emptyList() : tasks;
    }

    boolean createIfAbsent(DataExportTask task, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean created = false;
        int rollback = 0;
        Connection con = getWritable(schemaReference);
        try {
            // Start transaction
            con.setAutoCommit(false);
            rollback = 1;

            // Perform INSERT
            created = createIfAbsent(task, userId, contextId, con);

            // Commit
            con.commit();
            rollback = 2;

            // Return result
            return created;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            backWritable(created, schemaReference, con);
        }
    }

    private boolean createIfAbsent(DataExportTask task, int userId, int contextId, Connection con) throws OXException {
        byte[] taskIdBytes = UUIDs.toByteArray(task.getId());
        try (PreparedStatement stmt = con.prepareStatement("INSERT INTO dataExportTask (uuid, cid, user, creationTime, status, filestore, arguments) VALUES (?,?,?,?,?,?,?)")) {
            stmt.setBytes(1, taskIdBytes);
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setString(5, DataExportStatus.PENDING.toString());
            stmt.setInt(6, task.getFileStorageId());
            stmt.setString(7, convertArguments(task.getArguments()).toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (Databases.isDuplicateKeyConflictInMySQL(e)) {
                return false;
            }
            throw handleException(e);
        }

        List<DataExportWorkItem> workItems = task.getWorkItems();
        for (DataExportWorkItem workItem : workItems) {
            try (PreparedStatement stmt = con.prepareStatement("INSERT INTO dataExportTaskWorklist (uuid, cid, taskId, id, status) VALUES (?,?,?,?,?)")) {
                stmt.setBytes(1, UUIDs.toByteArray(workItem.getId()));
                stmt.setInt(2, contextId);
                stmt.setBytes(3, taskIdBytes);
                stmt.setString(4, workItem.getModuleId());
                stmt.setString(5, DataExportStatus.PENDING.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw handleException(e);
            }
        }

        return true;
    }

    /**
     * Sets a save-point for a certain Work Item of a Task
     *
     * @param taskId The task identifier
     * @param module The module identifier
     * @param savePoint The save-point to be set
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException
     */
    void updateSavePoint(UUID taskId, String module, DataExportSavepoint savePoint, int userId, int contextId) throws OXException {
        Optional<JSONObject> jSavePoint = savePoint.getSavepoint();
        Optional<DataExportDiagnosticsReport> report = savePoint.getReport();
        Optional<String> fileStorageLocation = savePoint.getFileStorageLocation();

        R schemaReference = getSchemaReference(userId, contextId);
        int rollback = 0;
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try {
            Databases.startTransaction(con);
            rollback = 1;

            try (PreparedStatement stmt = con.prepareStatement(jSavePoint.isPresent() && fileStorageLocation.isPresent() ? "UPDATE dataExportTaskWorklist SET savepoint = ?, filestoreLocation = ? WHERE taskId = ? AND id = ?" : "UPDATE dataExportTaskWorklist SET savepoint = ? WHERE taskId = ? AND id = ?")) {
                int pos = 1;
                if (jSavePoint.isPresent()) {
                    JSONObject jsp = jSavePoint.get();
                    if (jsp.isEmpty()) {
                        stmt.setNull(pos++, Types.VARCHAR);
                    } else {
                        stmt.setString(pos++, jsp.toString());
                    }
                    if (fileStorageLocation.isPresent()) {
                        stmt.setString(pos++, fileStorageLocation.get());
                    }
                } else {
                    stmt.setNull(pos++, Types.VARCHAR);
                }
                stmt.setBytes(pos++, UUIDs.toByteArray(taskId));
                stmt.setString(pos++, module);
                modified = stmt.executeUpdate() > 0;
            }

            try (PreparedStatement stmt = con.prepareStatement("DELETE FROM dataExportReport WHERE taskId = ?")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                modified |= stmt.executeUpdate() > 0;
            }
            if (report.isPresent() && !report.get().isEmpty()) {
                for (Message message : report.get()) {
                    try (PreparedStatement stmt = con.prepareStatement("INSERT INTO dataExportReport (cid, messageId, taskId, message, timeStamp, moduleId) VALUES (?, ?, ?, ?,?, ?)")) {
                        stmt.setInt(1, contextId);
                        stmt.setBytes(2, UUIDs.toByteArray(message.getId()));
                        stmt.setBytes(3, UUIDs.toByteArray(taskId));
                        stmt.setString(4, message.getMessage());
                        stmt.setLong(5, message.getTimeStamp().getTime());
                        stmt.setString(6, message.getModuleId());
                        modified |= stmt.executeUpdate() > 0;
                    }
                }
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            backWritable(modified, schemaReference, con);
        }
    }

    /**
     * Updates a Work Item Status. Optionally sets a file storage location if the status is set to {@link DataExportStatus.DONE}
     * or a failure information if the status is set to {@link DataExportStatus.FAILED}.
     *
     * @param status The {@link DataExportStatus}
     * @param fileStorageLocation The optional file storage location if the work item is done
     * @param jFailureInfo Optional failure information if the work item failed
     * @param taskId The task identifier
     * @param moduleId The module identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException
     */
    void updateWorkItemStatus(DataExportStatus status, String fileStorageLocation, JSONObject jFailureInfo, UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try {
            String sql;
            {
                StringBuilder sqlBuilder = new StringBuilder("UPDATE dataExportTaskWorklist SET status = ?");
                if (fileStorageLocation != null) {
                    sqlBuilder.append(", filestoreLocation = ?");
                }
                if (jFailureInfo != null) {
                    sqlBuilder.append(", info = ?");
                }
                if (DataExportStatus.DONE == status) {
                    sqlBuilder.append(", savepoint = ?");
                }
                sqlBuilder.append(" WHERE taskId = ? AND id = ?");
                sql = sqlBuilder.toString();
                sqlBuilder = null;
            }

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                int pos = 1;
                stmt.setString(pos++, status.toString());
                if (fileStorageLocation != null) {
                    stmt.setString(pos++, fileStorageLocation);
                }
                if (jFailureInfo != null) {
                    stmt.setString(pos++, jFailureInfo.toString());
                }
                if (DataExportStatus.DONE == status) {
                    stmt.setNull(pos++, Types.VARCHAR);
                }
                stmt.setBytes(pos++, UUIDs.toByteArray(taskId));
                stmt.setString(pos, moduleId);
                modified = stmt.executeUpdate() > 0;
            }
            if (status == DataExportStatus.PENDING) {
                try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTaskWorklist SET filestoreLocation = ? WHERE taskId = ? AND id = ?")) {
                    stmt.setNull(1, Types.VARCHAR);
                    stmt.setBytes(2, UUIDs.toByteArray(taskId));
                    stmt.setString(3, moduleId);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, con);
        }
    }

    /**
     * Touches a task through updating last-accessed time stamp and incrementing duration.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If operation fails
     */
    void touchTask(int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean success = false;
        boolean readOnly = true;
        Connection con = null;
        try {
            do {
                // Acquire read-only connection
                con = getReadOnly(schemaReference);
                readOnly = true;

                UUID taskId;
                long duration;
                long timestamp;
                try (PreparedStatement stmt = con.prepareStatement("SELECT uuid, timestamp, duration, status FROM dataExportTask WHERE user = ? AND cid = ?")) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, contextId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            // No such task
                            throw DataExportExceptionCode.NO_SUCH_TASK.create(I(userId), I(contextId));
                        }
                        DataExportStatus status = DataExportStatus.statusFor(rs.getString(4));
                        if (!status.isRunning()) {
                            // Not running
                            return;
                        }
                        taskId = UUIDs.toUUID(rs.getBytes(1));
                        timestamp = rs.getLong(2);
                        if (rs.wasNull()) {
                            // Time stamp not yet set. Hence, not in progress
                            return;
                        }
                        duration = rs.getLong(3); // falls-back to 0 (zero) if value is SQL NULL
                    }
                } catch (SQLException e) {
                    throw handleException(e);
                }

                // Give up read-only and acquire read-write connection
                backReadOnly(schemaReference, con);
                con = null;
                con = getWritable(schemaReference);
                readOnly = false;

                // Try to update time stamp
                long now = System.currentTimeMillis();
                duration = duration + (now - timestamp);
                try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTask SET timestamp = ?, duration = ? WHERE uuid = ? AND timestamp = ?")) {
                    stmt.setLong(1, now);
                    stmt.setLong(2, duration);
                    stmt.setBytes(3, UUIDs.toByteArray(taskId));
                    stmt.setLong(4, timestamp);
                    success = stmt.executeUpdate() > 0;
                } catch (SQLException e) {
                    throw handleException(e);
                }

                // Give up read-write connection
                backWritable(success, schemaReference, con);
                con = null;
            } while (!success);
        } finally {
            if (con != null) {
                if (readOnly) {
                    backReadOnly(schemaReference, con);
                } else {
                    backWritable(success, schemaReference, con);
                }
            }
        }
    }

    /**
     * Atomically sets the status of specified data export task to given updated value if the current status equals the expected value.
     *
     * @param update The new status to set
     * @param expect The expected status
     * @param taskId The identifier for the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if status could be successfully set; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean compareAndSetTaskStatus(DataExportStatus update, DataExportStatus expect, UUID taskId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTask SET status = ?, timestamp = ? WHERE uuid = ? AND status = ?")) {
            stmt.setString(1, update.toString());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setBytes(3, UUIDs.toByteArray(taskId));
            stmt.setString(4, expect.toString());
            modified = stmt.executeUpdate() > 0;
            return modified;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, con);
        }
    }

    /**
     * Sets a Task status.
     *
     * @param status The status
     * @param taskId The task identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param abortIfAnyOf An optional listing of statuses that are not allowed being changed through this invocation
     * @return The previous status if this invocation successfully set given status; otherwise <code>null</code>
     * @throws OXException
     */
    DataExportStatus updateTaskStatus(DataExportStatus status, UUID taskId, int userId, int contextId, DataExportStatus... abortIfAnyOf) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try {
            DataExportStatus expected;
            do {
                // Select previous status
                try (PreparedStatement stmt = con.prepareStatement("SELECT status FROM dataExportTask WHERE uuid = ?")) {
                    stmt.setBytes(1, UUIDs.toByteArray(taskId));
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            // No such task
                            return null;
                        }
                        expected = DataExportStatus.statusFor(rs.getString(1));
                    }
                }

                if (expected == status) {
                    // Status already set
                    return null;
                }
                if (abortIfAnyOf != null) {
                    for (DataExportStatus abortIfMatches : abortIfAnyOf) {
                        if (abortIfMatches == expected) {
                            // Status must not be changed
                            return null;
                        }
                    }
                }

                // Only set to new status if previous status is still the expected one
                try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTask SET status = ?, timestamp = ? WHERE uuid = ? AND status = ?")) {
                    stmt.setString(1, status.toString());
                    stmt.setLong(2, System.currentTimeMillis());
                    stmt.setBytes(3, UUIDs.toByteArray(taskId));
                    stmt.setString(4, expected.toString());
                    boolean updated = stmt.executeUpdate() > 0;
                    modified = updated;
                    if (updated) {
                        return expected;
                    }
                }
            } while (true);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, con);
        }
    }

    /**
     * Attempts to increment the fail count for given provider.
     *
     * @param taskId The identifier of the data export task
     * @param moduleId The identifier of the provider
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if successfully incremented; otherwise <code>false</code> if max. fail count already reached
     * @throws OXException If operation fails
     */
    boolean incrementFailCount(UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try {
            while (!modified) {
                int currentFailCount;
                try (PreparedStatement stmt = con.prepareStatement("SELECT failCount FROM dataExportTaskWorklist WHERE taskId = ? AND id = ?")) {
                    stmt.setBytes(1, UUIDs.toByteArray(taskId));
                    stmt.setString(2, moduleId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw DataExportExceptionCode.NO_SUCH_TASK_FOR_ID.create(UUIDs.getUnformattedString(taskId));
                        }
                        currentFailCount = rs.getInt(1);
                    }
                }

                if (currentFailCount >= config.getMaxFailCountForWorkItem()) {
                    return false;
                }

                try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTaskWorklist SET failCount = ? WHERE taskId = ? AND id = ? AND failCount = ?")) {
                    stmt.setInt(1, currentFailCount + 1);
                    stmt.setBytes(2, UUIDs.toByteArray(taskId));
                    stmt.setString(3, moduleId);
                    stmt.setInt(4, currentFailCount);
                    modified = stmt.executeUpdate() > 0;
                }
            }
            return true;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, con);
        }
    }

    /**
     * Sets the marker that notification has been sent out for specified task.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException
     */
    boolean setNotificationSent(UUID taskId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTask SET notificationSent = 1 WHERE uuid = ? AND notificationSent = 0")) {
            stmt.setBytes(1, UUIDs.toByteArray(taskId));
            modified = stmt.executeUpdate() > 0;
            return modified;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, con);
        }
    }

    /**
     * Un-sets the marker that notification has been sent out for specified task.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException
     */
    boolean unsetNotificationSent(UUID taskId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTask SET notificationSent = 0 WHERE uuid = ? AND notificationSent = 1")) {
            stmt.setBytes(1, UUIDs.toByteArray(taskId));
            modified = stmt.executeUpdate() > 0;
            return modified;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, con);
        }
    }

    /**
     * Gets the save-point of a certain work item.
     *
     * @param taskId The task identifier
     * @param moduleId The module identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The save-point
     * @throws OXException
     */
    DataExportSavepoint selectSavePoint(UUID taskId, String moduleId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        Connection con = getReadOnly(schemaReference);
        try {
            DataExportSavepoint.Builder savepoint = DataExportSavepoint.builder();

            JSONObject jSavePoint = null;
            String fileStorageLocation = null;
            try (PreparedStatement stmt = con.prepareStatement("SELECT savepoint, filestoreLocation FROM dataExportTaskWorklist WHERE taskId = ? AND id = ? AND savepoint IS NOT NULL")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                stmt.setString(2, moduleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String s = rs.getString(1);
                        if (!rs.wasNull()) {
                            try {
                                jSavePoint = new JSONObject(s);
                            } catch (JSONException e) {
                                // Unable to parse to JSON
                                LOGGER.warn("Unable to parse save-pont to JSON. Assuming null instead.", e);
                            }
                            s = rs.getString(2);
                            if (!rs.wasNull()) {
                                fileStorageLocation = s;
                            }
                        }
                    }
                }
            }
            savepoint.withSavepoint(jSavePoint);
            savepoint.withFileStorageLocation(fileStorageLocation);

            try (PreparedStatement stmt = con.prepareStatement("SELECT messageId, message, timeStamp, moduleId FROM dataExportReport WHERE taskId = ?")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Message.Builder message = Message.builder();
                        message.withId(UUIDs.toUUID(rs.getBytes(1)));
                        message.withMessage(rs.getString(2));
                        message.withModuleId(rs.getString(4));
                        message.withTimeStamp(new Date(rs.getLong(3)));
                        savepoint.addMessage(message.build());
                    }
                }
            }

            return savepoint.build();
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backReadOnly(schemaReference, con);
        }
    }

    /**
     * deletes a certain task and all of its work items.
     *
     * @param taskId The task Identifier
     * @return <code>true</code> if the task was deleted, <code>false</code> otherwise.
     * @throws OXException
     */
    boolean deleteTask(UUID taskId) throws OXException {
        for (R schemaReference : getSchemaReferences()) {
            boolean deleted = deleteTask(taskId, schemaReference);
            if (deleted) {
                return true;
            }
        }
        return false;
    }

    /**
     * deletes a certain task and all of its work items.
     *
     * @param taskId The task Identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if the task was deleted, <code>false</code> otherwise.
     * @throws OXException
     */
    boolean deleteTask(UUID taskId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        return deleteTask(taskId, schemaReference);
    }

    private boolean deleteTask(UUID taskId, R schemaReference) throws OXException {
        byte[] taskIdBytes = UUIDs.toByteArray(taskId);

        boolean modified = false;
        int rollback = 0;
        Connection connection = null;
        try {
            connection = getWritable(schemaReference);
            if (!tableExists(connection, connection.getCatalog())) {
                // No such task
                return false;
            }

            FileStorageAndId fileStorage;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT cid, filestore FROM dataExportTask WHERE uuid = ?")) {
                stmt.setBytes(1, taskIdBytes);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        // No such task
                        return false;
                    }

                    int fileStorageId = rs.getInt(2);
                    fileStorage = new FileStorageAndId(fileStorageId, DataExportUtility.getFileStorageFor(fileStorageId, rs.getInt(1)));
                }
            }

            List<String> fileStorageLocations = null;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT filestoreLocation FROM dataExportTaskWorklist WHERE taskId = ? AND filestoreLocation IS NOT NULL")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        fileStorageLocations = new ArrayList<>();
                        do {
                            fileStorageLocations.add(rs.getString(1));
                        } while (rs.next());
                    }
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement("SELECT filestoreLocation FROM dataExportFilestoreLocation WHERE taskId = ? AND filestoreLocation IS NOT NULL")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        if (fileStorageLocations == null) {
                            fileStorageLocations = new ArrayList<>();
                        }
                        do {
                            fileStorageLocations.add(rs.getString(1));
                        } while (rs.next());
                    }
                }
            }

            connection.setAutoCommit(false);
            rollback = 1;

            boolean taskDeleted = false;
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportTask WHERE uuid = ?")) {
                stmt.setBytes(1, taskIdBytes);
                modified = stmt.executeUpdate() > 0;
                taskDeleted = modified;
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportTaskWorklist WHERE taskId = ?")) {
                stmt.setBytes(1, taskIdBytes);
                modified |= stmt.executeUpdate() > 0;
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportFilestoreLocation WHERE taskId = ?")) {
                stmt.setBytes(1, taskIdBytes);
                modified |= stmt.executeUpdate() > 0;
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportReport WHERE taskId = ?")) {
                stmt.setBytes(1, taskIdBytes);
                modified |= stmt.executeUpdate() > 0;
            }

            if (fileStorageLocations != null) {
                for (String fileStorageLocation : fileStorageLocations) {
                    try {
                        fileStorage.fileStorage.deleteFile(fileStorageLocation);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to delete file storage item {} from file storage {}", fileStorageLocation, I(fileStorage.fileStorageId), e);
                    }
                }
            }

            connection.commit();
            rollback = 2;

            if (taskDeleted) {
                return true;
            }
        } catch (OXException e) {
            handleOXException(e);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            backWritable(modified, schemaReference, connection);
        }

        // No such task
        return false;
    }

    /**
     * Deletes a Task and its Work Items for a specific user.
     * Either a connection or a context identifier must be present.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return
     * @throws OXException
     */
    boolean deleteTask(int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        Connection connection = getWritable(schemaReference);
        int rollback = 0;
        boolean modified = false;
        try {
            UUID taskId = null;
            FileStorageAndId fileStorage = null;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT uuid, filestore FROM dataExportTask WHERE cid = ? AND user = ?")) {
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        taskId = UUIDs.toUUID(rs.getBytes(1));
                        int fileStorageId = rs.getInt(2);
                        fileStorage = new FileStorageAndId(fileStorageId, DataExportUtility.getFileStorageFor(fileStorageId, contextId));
                    } else {
                        return false;
                    }
                }
            }

            byte[] taskIdBytes = UUIDs.toByteArray(taskId);

            List<String> fileStorageLocations = null;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT filestoreLocation FROM dataExportTaskWorklist WHERE taskId = ? AND filestoreLocation IS NOT NULL")) {
                stmt.setBytes(1, taskIdBytes);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        fileStorageLocations = new ArrayList<>();
                        do {
                            fileStorageLocations.add(rs.getString(1));
                        } while (rs.next());
                    }
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement("SELECT filestoreLocation FROM dataExportFilestoreLocation WHERE taskId = ? AND filestoreLocation IS NOT NULL")) {
                stmt.setBytes(1, taskIdBytes);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        if (fileStorageLocations == null) {
                            fileStorageLocations = new ArrayList<>();
                        }
                        do {
                            fileStorageLocations.add(rs.getString(1));
                        } while (rs.next());
                    }
                }
            }

            connection.setAutoCommit(false);
            rollback = 1;

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportTask WHERE uuid = ?")) {
                stmt.setBytes(1, taskIdBytes);
                modified = stmt.executeUpdate() > 0;
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportTaskWorklist WHERE taskId = ?")) {
                stmt.setBytes(1, taskIdBytes);
                modified |= stmt.executeUpdate() > 0;
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportFilestoreLocation WHERE taskId = ?")) {
                stmt.setBytes(1, taskIdBytes);
                modified |= stmt.executeUpdate() > 0;
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportReport WHERE taskId = ?")) {
                stmt.setBytes(1, taskIdBytes);
                modified |= stmt.executeUpdate() > 0;
            }

            if (fileStorageLocations != null) {
                for (String fileStorageLocation : fileStorageLocations) {
                    try {
                        fileStorage.fileStorage.deleteFile(fileStorageLocation);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to delete file storage item {} from file storage {}", fileStorageLocation, I(fileStorage.fileStorageId), e);
                    }
                }
            }

            connection.commit();
            rollback = 2;
            return true;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            backWritable(modified, schemaReference, connection);
        }
    }

    private static final Comparator<TaskInfo> TASK_INFO_COMPARATOR = new Comparator<TaskInfo>() {

        @Override
        public int compare(TaskInfo o1, TaskInfo o2) {
            return (o1.timestamp < o2.timestamp) ? -1 : ((o1.timestamp == o2.timestamp) ? 0 : 1);
        }
    };

    /**
     * Finds the next appropriate job. Either a <code>PAUSED</code>, <code>PENDING</code> or <code>RUNNING</code> task which is too long in this state.
     * The <code>timestamp</code> is touched and the <code>status</code> is set to <code>RUNNING</code>.
     *
     * @return The next job or <code>null</code> if none could be found
     * @throws OXException
     */
    DataExportJob selectNextJob() throws OXException {
        List<TaskInfo> tasks = null;
        long now = System.currentTimeMillis();
        long expirationThreshold = now - config.getExpirationTimeMillis();

        // Iterate schemas
        Collection<R> schemaReferences = getSchemaReferences();
        for (R schemaReference : schemaReferences) {
            Connection con = null;
            try {
                con = getReadOnly(schemaReference);
                if (tableExists(con, con.getCatalog())) {
                    try (PreparedStatement stmt = con.prepareStatement("SELECT uuid, cid, user, timestamp FROM dataExportTask WHERE status IN (?, ?) OR (status = ? AND timestamp < ?) ORDER BY timestamp ASC LIMIT 1")) {
                        stmt.setString(1, DataExportStatus.PAUSED.toString());
                        stmt.setString(2, DataExportStatus.PENDING.toString());
                        stmt.setString(3, DataExportStatus.RUNNING.toString());
                        stmt.setLong(4, expirationThreshold);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                if (tasks == null) {
                                    tasks = new ArrayList<>();
                                }
                                do {
                                    tasks.add(new TaskInfo(UUIDs.toUUID(rs.getBytes(1)), rs.getLong(4), rs.getInt(3), rs.getInt(2)));
                                } while (rs.next());
                            }
                        }
                    }
                }
            } catch (OXException e) {
                handleOXException(e);
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backReadOnly(schemaReference, con);
            }
        }
        schemaReferences = null;

        // Check if any collected
        if (tasks == null) {
            return null;
        }

        tasks.sort(TASK_INFO_COMPARATOR);

        now = System.currentTimeMillis();
        for (TaskInfo task : tasks) {
            R schemaReference = getSchemaReference(task.userId, task.contextId);
            boolean modified = false;
            Connection con = getWritable(schemaReference);
            try {
                byte[] taskIdBytes = UUIDs.toByteArray(task.taskId);
                boolean hasOldTimestamp = task.timestamp > 0;
                try (PreparedStatement stmt = con.prepareStatement(hasOldTimestamp ? "UPDATE dataExportTask SET startTime = ?, timestamp = ?, status = ? WHERE uuid = ? AND timestamp = ?" : "UPDATE dataExportTask SET startTime = ?, timestamp = ?, status = ? WHERE uuid = ? AND timestamp IS NULL")) {
                    stmt.setLong(1, now);
                    stmt.setLong(2, now);
                    stmt.setString(3, DataExportStatus.RUNNING.toString());
                    stmt.setBytes(4, taskIdBytes);
                    if (hasOldTimestamp) {
                        stmt.setLong(5, task.timestamp);
                    }
                    modified = stmt.executeUpdate() > 0;
                    if (!modified) {
                        continue;
                    }
                }

                try (PreparedStatement stmt = con.prepareStatement("SELECT cid, uuid, user, status, filestore, creationTime, startTime, duration, arguments FROM dataExportTask WHERE uuid = ?")) {
                    stmt.setBytes(1, taskIdBytes);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            DataExportTask retval = parsetTask(rs);
                            retval.setWorkItems(selectWorkItems(task.taskId, con));
                            List<DataExportResultFile> resultFiles = selectResultFiles(task.taskId, con);
                            if (resultFiles != null && !resultFiles.isEmpty()) {
                                retval.setResultFiles(resultFiles);
                            }
                            return new DataExportJobImpl(retval, this);
                        }
                    }
                }
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backWritable(modified, schemaReference, con);
            }
        }

        return null;
    }

    /**
     * Returns the next work item for a specific task.
     *
     * @param taskId The task identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The next work item
     * @throws OXException
     */
    DataExportWorkItem getNextWorkItem(UUID taskId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try {
            List<DataExportWorkItem> items = selectWorkItems(taskId, con);

            // Prefer the ones already marked as running (which should be resumed)
            List<DataExportWorkItem> pausedItems = items.stream().filter(i -> i.getStatus().isRunning() || i.getStatus().isPaused()).collect(Collectors.toList());
            for (DataExportWorkItem pausedItem : pausedItems) {
                try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTaskWorklist SET status = ? WHERE cid = ? AND uuid = ? AND status = ?")) {
                    stmt.setString(1, DataExportStatus.RUNNING.toString());
                    stmt.setInt(2, contextId);
                    stmt.setBytes(3, UUIDs.toByteArray(pausedItem.getId()));
                    stmt.setString(4, pausedItem.getStatus().toString());
                    if (stmt.executeUpdate() == 1) {
                        modified = true;
                        return pausedItem;
                    }
                }
            }

            // Then check pending ones
            List<DataExportWorkItem> pendingItems = items.stream().filter(i -> i.getStatus().isPending()).collect(Collectors.toList());
            for (DataExportWorkItem pendingItem : pendingItems) {
                try (PreparedStatement stmt = con.prepareStatement("UPDATE dataExportTaskWorklist SET status = ? WHERE cid = ? AND uuid = ? AND status = ?")) {
                    stmt.setString(1, DataExportStatus.RUNNING.toString());
                    stmt.setInt(2, contextId);
                    stmt.setBytes(3, UUIDs.toByteArray(pendingItem.getId()));
                    stmt.setString(4, pendingItem.getStatus().toString());
                    if (stmt.executeUpdate() == 1) {
                        modified = true;
                        return pendingItem;
                    }
                }
            }
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, con);
        }

        return null;
    }

    void addResultFile(String fileStorageLocation, int number, long size, UUID taskId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection con = getWritable(schemaReference);
        try (PreparedStatement stmt = con.prepareStatement("INSERT INTO dataExportFilestoreLocation (cid, taskId, num, filestoreLocation, size) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, contextId);
            stmt.setBytes(2, UUIDs.toByteArray(taskId));
            stmt.setInt(3, number);
            stmt.setString(4, fileStorageLocation);
            stmt.setLong(5, size);
            modified = stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, con);
        }
    }

    void deleteResultFiles(UUID taskId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection connection = getWritable(schemaReference);
        try {
            FileStorageAndId fileStorage = null;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT filestore FROM dataExportTask WHERE uuid = ?")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }
                    int fileStorageId = rs.getInt(1);
                    fileStorage = new FileStorageAndId(fileStorageId, DataExportUtility.getFileStorageFor(fileStorageId, contextId));
                }
            } catch (SQLException e) {
                throw handleException(e);
            }

            List<String> fileStorageLocations;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT filestoreLocation FROM dataExportFilestoreLocation WHERE taskId = ? AND filestoreLocation IS NOT NULL")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }
                    fileStorageLocations = new ArrayList<>();
                    do {
                        fileStorageLocations.add(rs.getString(1));
                    } while (rs.next());
                }
            } catch (SQLException e) {
                throw handleException(e);
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportFilestoreLocation WHERE taskId = ?")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                modified = stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw handleException(e);
            }

            for (String fileStorageLocation : fileStorageLocations) {
                try {
                    fileStorage.fileStorage.deleteFile(fileStorageLocation);
                } catch (Exception e) {
                    LOGGER.warn("Failed to delete file storage item {} from file storage {}", fileStorageLocation, I(fileStorage.fileStorageId), e);
                }
            }
        } finally {
            backWritable(modified, schemaReference, connection);
        }
    }

    void dropIntermediateFiles(UUID taskId, int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        boolean modified = false;
        Connection connection = getWritable(schemaReference);
        try {
            FileStorageAndId fileStorage = null;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT filestore FROM dataExportTask WHERE uuid = ?")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }
                    int fileStorageId = rs.getInt(1);
                    fileStorage = new FileStorageAndId(fileStorageId, DataExportUtility.getFileStorageFor(fileStorageId, contextId));
                }
            }

            List<String> fileStorageLocations = null;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT filestoreLocation FROM dataExportTaskWorklist WHERE taskId = ? AND filestoreLocation IS NOT NULL")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        fileStorageLocations = new ArrayList<>();
                        do {
                            fileStorageLocations.add(rs.getString(1));
                        } while (rs.next());
                    }
                }
            }

            if (fileStorageLocations != null) {
                try (PreparedStatement stmt = connection.prepareStatement("UPDATE dataExportTaskWorklist SET filestoreLocation = NULL WHERE taskId = ?")) {
                    stmt.setBytes(1, UUIDs.toByteArray(taskId));
                    modified = stmt.executeUpdate() > 0;
                }
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dataExportReport WHERE taskId = ?")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                modified |= stmt.executeUpdate() > 0;
            }

            if (fileStorageLocations != null) {
                for (String fileStorageLocation : fileStorageLocations) {
                    try {
                        fileStorage.fileStorage.deleteFile(fileStorageLocation);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to delete file storage item {} from file storage {}", fileStorageLocation, I(fileStorage.fileStorageId), e);
                    }
                }
            }
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backWritable(modified, schemaReference, connection);
        }
    }

    /**
     * Returns the task status for a specific user.
     *
     * @param taskId The task identifier
     * @return The task status for the user or <code>null</code> if the user has currently no active task
     * @throws OXException
     */
    DataExportStatus selectTaskStatus(UUID taskId) throws OXException {
        byte[] taskIdBytes = UUIDs.toByteArray(taskId);
        for (R schemaReference : getSchemaReferences()) {
            Connection con = null;
            try {
                con = getReadOnly(schemaReference);
                if (tableExists(con, con.getCatalog())) {
                    try (PreparedStatement stmt = con.prepareStatement("SELECT status FROM dataExportTask WHERE uuid = ?")) {
                        stmt.setBytes(1, taskIdBytes);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                return DataExportStatus.statusFor(rs.getString(1));
                            }
                        }
                    }
                }
            } catch (OXException e) {
                handleOXException(e);
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backReadOnly(schemaReference, con);
            }
        }

        return null;
    }

    /**
     * Returns the task status for a specific user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The task status for the user or <code>null</code> if the user has currently no active task
     * @throws OXException
     */
    DataExportStatus selectTaskStatus(int contextId, int userId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        Connection con = getReadOnly(schemaReference);
        try (PreparedStatement stmt = con.prepareStatement("SELECT status FROM dataExportTask WHERE cid = ? AND user = ?")) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return DataExportStatus.statusFor(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backReadOnly(schemaReference, con);
        }

        return null;
    }

    Date selectLastAccessedTimeStamp(int contextId, int userId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        Connection con = getReadOnly(schemaReference);
        try (PreparedStatement stmt = con.prepareStatement("SELECT timestamp FROM dataExportTask WHERE cid = ? AND user = ?")) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Date(rs.getLong(1));
                }
            }
            return null;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backReadOnly(schemaReference, con);
        }
    }

    FileLocations selectResultFiles(int contextId, int userId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        Connection con = getReadOnly(schemaReference);
        try {
            UUID taskId = null;
            long timestamp = 0;
            try (PreparedStatement stmt = con.prepareStatement("SELECT uuid, timestamp FROM dataExportTask WHERE cid = ? AND user = ?")) {
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        taskId = UUIDs.toUUID(rs.getBytes(1));
                        timestamp = rs.getLong(2);
                    }
                }
            } catch (SQLException e) {
                throw handleException(e);
            }

            if (taskId == null) {
                return null;
            }

            try (PreparedStatement stmt = con.prepareStatement("SELECT filestoreLocation, num, size FROM dataExportFilestoreLocation WHERE taskId = ? ORDER BY num ASC")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    List<FileLocation> filestoreLocations = new ArrayList<>();
                    do {
                        DefaultFileLocation.Builder resultFile = DefaultFileLocation.builder().withFileStorageLocation(rs.getString(1)).withNumber(rs.getInt(2)).withSize(rs.getLong(3)).withTaskId(taskId);
                        filestoreLocations.add(resultFile.build());
                    } while (rs.next());
                    return new FileLocations(filestoreLocations, timestamp);
                }
            } catch (SQLException e) {
                throw handleException(e);
            }

        } finally {
            backReadOnly(schemaReference, con);
        }
    }

    FileLocation selectResultFile(int number, int contextId, int userId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        Connection con = getReadOnly(schemaReference);
        try {
            UUID taskId = null;
            try (PreparedStatement stmt = con.prepareStatement("SELECT uuid FROM dataExportTask WHERE cid = ? AND user = ?")) {
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        taskId = UUIDs.toUUID(rs.getBytes(1));
                    }
                }
            } catch (SQLException e) {
                throw handleException(e);
            }

            if (taskId == null) {
                return null;
            }

            try (PreparedStatement stmt = con.prepareStatement("SELECT filestoreLocation, num, size FROM dataExportFilestoreLocation WHERE taskId = ? AND num = ?")) {
                stmt.setBytes(1, UUIDs.toByteArray(taskId));
                stmt.setInt(2, number);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    return DefaultFileLocation.builder().withFileStorageLocation(rs.getString(1)).withNumber(rs.getInt(2)).withSize(rs.getLong(3)).withTaskId(taskId).build();
                }
            } catch (SQLException e) {
                throw handleException(e);
            }

        } finally {
            backReadOnly(schemaReference, con);
        }
    }

    /**
     * Returns the tasks for a specific context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The tasks for the context or an empty list
     * @throws OXException
     */
    List<DataExportTask> selectTasksForContext(int userId, int contextId) throws OXException {
        R schemaReference = getSchemaReference(userId, contextId);
        Connection con = getReadOnly(schemaReference);
        try (PreparedStatement stmt = con.prepareStatement("SELECT cid, uuid, user, status, filestore, creationTime, startTime, duration, arguments FROM dataExportTask WHERE cid = ?")) {
            stmt.setInt(1, contextId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Collections.emptyList();
                }

                List<DataExportTask> tasks = new ArrayList<>();
                do {
                    DataExportTask task = parsetTask(rs);
                    task.setWorkItems(selectWorkItems(task.getId(), con));
                    List<DataExportResultFile> resultFiles = selectResultFiles(task.getId(), con);
                    if (resultFiles != null && !resultFiles.isEmpty()) {
                        task.setResultFiles(resultFiles);
                    }
                    tasks.add(task);
                } while (rs.next());
                return tasks;
            }
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            backReadOnly(schemaReference, con);
        }
    }

    /**
     * Returns the task for a specific user.
     *
     * @param contextId The context identifier
     * @param userIds The user identifiers
     * @param dropNullElements Whether to drop <code>null</code> elements from returned array
     * @return The tasks for the users or <code>null</code> at array position if the user has currently no active task
     * @throws OXException
     */
    DataExportTask[] selectTasks(int contextId, int[] userIds, boolean dropNullElements) throws OXException {
        if (userIds == null) {
            return new DataExportTask[0];
        }

        int length = userIds.length;
        if (length <= 0) {
            return new DataExportTask[0];
        }

        if (length == 1) {
            int userId = userIds[0];
            R schemaReference = getSchemaReference(userId, contextId);
            Connection con = getReadOnly(schemaReference);
            try (PreparedStatement stmt = con.prepareStatement("SELECT cid, uuid, user, status, filestore, creationTime, startTime, duration, arguments FROM dataExportTask WHERE cid = ? AND user = ?")) {
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        DataExportTask retval = parsetTask(rs);
                        retval.setWorkItems(selectWorkItems(retval.getId(), con));
                        List<DataExportResultFile> resultFiles = selectResultFiles(retval.getId(), con);
                        if (resultFiles != null && !resultFiles.isEmpty()) {
                            retval.setResultFiles(resultFiles);
                        }
                        return new DataExportTask[] { retval };
                    }
                }
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backReadOnly(schemaReference, con);
            }

            return dropNullElements ? new DataExportTask[0] : new DataExportTask[1];
        }

        Map<R, List<Integer>> schemaReference2Users = new HashMap<>(length);
        for (int userId : userIds) {
            R schemaReference = getSchemaReference(userId, contextId);
            List<Integer> users = schemaReference2Users.get(schemaReference);
            if (users == null) {
                users = new ArrayList<>();
                schemaReference2Users.put(schemaReference, users);
            }
            users.add(I(userId));
        }

        Map<Integer, DataExportTask> loadedTasks = new HashMap<>(length);
        for (Map.Entry<R, List<Integer>> entry : schemaReference2Users.entrySet()) {
            R schemaReference = entry.getKey();
            List<Integer> users = entry.getValue();
            Connection con = getReadOnly(schemaReference);
            try (PreparedStatement stmt = con.prepareStatement(Databases.getIN("SELECT cid, uuid, user, status, filestore, creationTime, startTime, duration, arguments FROM dataExportTask WHERE cid = ? AND user IN (", users.size()))) {
                int pos = 1;
                stmt.setInt(pos++, contextId);
                for (Integer user : users) {
                    stmt.setInt(pos++, user.intValue());
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DataExportTask retval = parsetTask(rs);
                        retval.setWorkItems(selectWorkItems(retval.getId(), con));
                        List<DataExportResultFile> resultFiles = selectResultFiles(retval.getId(), con);
                        if (resultFiles != null && !resultFiles.isEmpty()) {
                            retval.setResultFiles(resultFiles);
                        }
                        loadedTasks.put(I(retval.getUserId()), retval);
                    }
                }
            } catch (SQLException e) {
                throw handleException(e);
            } finally {
                backReadOnly(schemaReference, con);
            }
        }

        DataExportTask[] retval;
        if (dropNullElements) {
            List<DataExportTask> nonNulls = new ArrayList<>(length);
            for (int i = length; i-- > 0;) {
                DataExportTask task = loadedTasks.get(I(userIds[i]));
                if (task != null) {
                    nonNulls.add(task);
                }
            }
            retval = nonNulls.toArray(new DataExportTask[nonNulls.size()]);
        } else {
            retval = new DataExportTask[length];
            for (int i = length; i-- > 0;) {
                retval[i] = loadedTasks.get(I(userIds[i]));
            }
        }
        return retval;
    }

    private static List<DataExportWorkItem> selectWorkItems(UUID taskUuid, Connection connection) throws SQLException, OXException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT uuid, id, savepoint, filestoreLocation, status, info FROM dataExportTaskWorklist WHERE taskId = ?")) {
            stmt.setBytes(1, UUIDs.toByteArray(taskUuid));
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Collections.emptyList();
                }

                List<DataExportWorkItem> retval = new ArrayList<>();
                do {
                    retval.add(parseWorkItem(rs));
                } while (rs.next());
                return retval;
            }
        }
    }

    private static List<DataExportResultFile> selectResultFiles(UUID taskUuid, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT cid, taskId, num, filestoreLocation, size FROM dataExportFilestoreLocation WHERE taskId = ?")) {
            stmt.setBytes(1, UUIDs.toByteArray(taskUuid));
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Collections.emptyList();
                }

                List<DataExportResultFile> retval = new ArrayList<>();
                do {
                    retval.add(parseResultFile(rs));
                } while (rs.next());
                return retval;
            }
        }
    }

    private static DataExportTask parsetTask(ResultSet rs) throws SQLException, OXException {
        DataExportTask retval = new DataExportTask();
        retval.setId(UUIDs.toUUID(rs.getBytes("uuid")));
        retval.setContextId(rs.getInt("cid"));
        retval.setUserId(rs.getInt("user"));
        retval.setFileStorageId(rs.getInt("filestore"));
        retval.setStatus(DataExportStatus.statusFor(rs.getString("status")));
        retval.setCreationTime(rs.getLong("creationTime"));
        {
            long startTime = rs.getLong("startTime");
            if (rs.wasNull()) {
                retval.setStartTime(-1L);
            } else {
                retval.setStartTime(startTime);
            }
        }
        {
            long duration = rs.getLong("duration");
            if (rs.wasNull()) {
                retval.setDuration(-1L);
            } else {
                retval.setDuration(duration);
            }
        }
        try {
            String args = rs.getString("arguments");
            if (!rs.wasNull()) {
                JSONObject jArgs = new JSONObject(args);
                retval.setArguments(parseArguments(jArgs));
            }
        } catch (JSONException e) {
            throw DataExportExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return retval;
    }

    private static DataExportWorkItem parseWorkItem(ResultSet rs) throws SQLException, OXException {
        DataExportWorkItem item = new DataExportWorkItem();
        item.setId(UUIDs.toUUID(rs.getBytes("uuid")));
        item.setFileStorageLocation(rs.getString("fileStoreLocation"));
        item.setModuleId(rs.getString("id"));
        item.setStatus(DataExportStatus.statusFor(rs.getString("status")));
        try {
            String sp = rs.getString("savepoint");
            item.setSavePoint(sp == null ? null : new JSONObject(sp));
            String info = rs.getString("info");
            item.setInfo(info == null ? null : new JSONObject(info));
        } catch (JSONException e) {
            throw DataExportExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return item;
    }

    private static DataExportResultFile parseResultFile(ResultSet rs) throws SQLException {
        Builder b = DefaultDataExportResultFile.builder();
        b.withFileStorageLocation(rs.getString("filestoreLocation"));
        b.withNumber(rs.getInt("num"));
        b.withSize(rs.getLong("size"));
        b.withTaskId(UUIDs.toUUID(rs.getBytes("taskId")));
        // TODO: b.withContentType(DataExportUtility.CONTENT_TYPE);
        // TODO: b.withFileName(DataExportUtility.generateFileNameFor("export", ".zip", '_', fileLocation.getNumber(), total, task.getCreationTime(), user));
        return b.build();
    }

    private static final String JSON_MAX_FILE_SIZE = "maxFileSize";
    private static final String JSON_HOST_INFO = "hostInfo";

    private static DataExportArguments parseArguments(JSONObject jArgs) {
        DataExportArguments arguments = new DataExportArguments();

        arguments.setMaxFileSize(jArgs.optLong(JSON_MAX_FILE_SIZE, -1));

        {
            JSONObject jHostInfo = jArgs.optJSONObject(JSON_HOST_INFO);
            if (jHostInfo != null) {
                arguments.setHostInfo(new HostInfo(jHostInfo.optString("host", null), jHostInfo.optBoolean("secure", false)));
            }
        }

        int length = jArgs.length();
        List<Module> modules = new ArrayList<>(length);
        for (String moduleId : jArgs.keySet()) {
            if (!JSON_MAX_FILE_SIZE.equals(moduleId) && !JSON_HOST_INFO.equals(moduleId)) {
                JSONObject jProperties = jArgs.optJSONObject(moduleId);
                modules.add(Module.valueOf(moduleId, jProperties == null ? null : jProperties.asMap()));
            }
        }
        arguments.setModules(modules);

        return arguments;
    }

    private static JSONObject convertArguments(DataExportArguments arguments) throws OXException {
        try {
            JSONObject jArgs = new JSONObject();

            jArgs.put(JSON_MAX_FILE_SIZE, arguments.getMaxFileSize() <= 0 ? -1 : arguments.getMaxFileSize());

            {
                HostInfo hostInfo = arguments.getHostInfo();
                if (hostInfo != null) {
                    jArgs.put(JSON_HOST_INFO, new JSONObject(2).putSafe("host", hostInfo.getHost()).putSafe("secure", B(hostInfo.isSecure())));
                }
            }

            for (Module module : arguments.getModules()) {
                Optional<Map<String, Object>> optionalProperties = module.getProperties();
                jArgs.put(module.getId(), optionalProperties.isPresent() ? new JSONObject(optionalProperties.get()) : JSONObject.NULL);
            }

            return jArgs;
        } catch (JSONException e) {
            throw DataExportExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static void handleOXException(OXException e) throws OXException {
        if (DBPoolingExceptionCodes.NO_CONNECTION.equals(e)) {
            // Database (currently) not reachable
            LOGGER.debug("No connection to database", e);
        } else if (DBPoolingExceptionCodes.SCHEMA_FAILED.equals(e)) {
            // Unknown database schema
            LOGGER.debug("Unknown database schema", e);
        } else {
            throw e;
        }
    }

    private static OXException handleException(SQLException e) {
        return DataExportExceptionCode.SQL_ERROR.create(e, e.getMessage());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class TaskInfo implements Comparable<TaskInfo> {

        final UUID taskId;
        final long timestamp;
        final int userId;
        final int contextId;

        TaskInfo(UUID taskId, long timestamp, int userId, int contextId) {
            super();
            this.taskId = taskId;
            this.timestamp = timestamp;
            this.userId = userId;
            this.contextId = contextId;
        }

        @Override
        public int compareTo(TaskInfo o) {
            long ostamp = o.timestamp;
            return (timestamp < ostamp) ? -1 : ((timestamp == ostamp) ? 0 : 1);
        }
    }

}
