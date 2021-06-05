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

package com.openexchange.gdpr.dataexport.impl.groupware;

import static com.openexchange.gdpr.dataexport.impl.storage.AbstractDataExportSql.isUseGlobalDb;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.impl.DataExportUtility;
import com.openexchange.gdpr.dataexport.impl.utils.FileStorageAndId;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.java.util.UUIDs;

/**
 * {@link DataExportDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportDeleteListener implements DeleteListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportDeleteListener.class);

    /**
     * Initializes a new {@link DataExportDeleteListener}.
     */
    public DataExportDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        int contextId = event.getContext().getContextId();

        if (DeleteEvent.TYPE_USER == event.getType()) {
            int userId = event.getId();
            LOG.debug("Going to drop possibly existing data export task and its remnants due to deletion of user {} in context {}", I(userId), I(contextId));
            if (isUseGlobalDb()) {
                deleteTaskOnUserDeletion(userId, contextId, event.getGlobalDbConnection());
            } else {
                deleteTaskOnUserDeletion(userId, contextId, writeCon);
            }
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            LOG.debug("Going to drop possibly existing data export tasks and its remnants due to deletion of context {}", I(contextId));
            Set<FileStorageAndId> fileStorages;
            if (isUseGlobalDb()) {
                fileStorages = deleteTasksOnContextDeletion(contextId, event.getGlobalDbConnection());
            } else {
                fileStorages = deleteTasksOnContextDeletion(contextId, writeCon);
            }
            deleteFileStorages(fileStorages);
        }
    }

    private void deleteFileStorages(Set<FileStorageAndId> fileStorages) {
        for (FileStorageAndId fileStorageAndId : fileStorages) {
            FileStorage fileStorage = fileStorageAndId.fileStorage;
            if ("file".equals(fileStorage.getUri().getScheme())) {
                // Don't delete since a single static prefix is used
            } else {
                try {
                    fileStorage.remove();
                } catch (Exception e) {
                    LOG.warn("Failed to delete the filestore {}", fileStorage.getUri(), e);
                }
            }
        }
    }

    private boolean deleteTaskOnUserDeletion(int userId, int contextId, Connection con) throws OXException {
        if (con == null) {
            LOG.debug("Cannot drop possibly existing data export task and its remnants for user {} in context {}. No such connection available.", I(userId), I(contextId));
            return false;
        }

        try {
            if (false == Databases.tableExists(con, "dataExportTask")) {
                LOG.debug("Cannot drop possibly existing data export task and its remnants for user {} in context {}. No such table existent.", I(userId), I(contextId));
                return false;
            }

            UUID taskId;
            FileStorageAndId fileStorage;

            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT uuid, filestore FROM dataExportTask WHERE cid = ? AND user = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return false;
                }

                taskId = UUIDs.toUUID(rs.getBytes(1));
                int fileStorageId = rs.getInt(2);
                fileStorage = new FileStorageAndId(fileStorageId, DataExportUtility.getFileStorageFor(fileStorageId, contextId));
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            LOG.debug("Found data export task {} using file storage {} ({}) for user {} in context {}.", UUIDs.getUnformattedString(taskId), I(fileStorage.fileStorageId), fileStorage.fileStorage.getUri(), I(userId), I(contextId));
            byte[] taskIdBytes = UUIDs.toByteArray(taskId);

            List<String> fileStorageLocations = null;
            try {
                stmt = con.prepareStatement("SELECT filestoreLocation FROM dataExportTaskWorklist WHERE taskId = ? AND filestoreLocation IS NOT NULL");
                stmt.setBytes(1, taskIdBytes);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    fileStorageLocations = new ArrayList<>();
                    do {
                        fileStorageLocations.add(rs.getString(1));
                    } while (rs.next());
                }
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }


            try {
                stmt = con.prepareStatement("SELECT filestoreLocation FROM dataExportFilestoreLocation WHERE taskId = ?");
                stmt.setBytes(1, taskIdBytes);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    if (fileStorageLocations == null) {
                        fileStorageLocations = new ArrayList<>();
                    }
                    do {
                        fileStorageLocations.add(rs.getString(1));
                    } while (rs.next());
                }
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            if (fileStorageLocations == null) {
                LOG.debug("Found no file storage locations associated with data export task {} using file storage {} ({}) for user {} in context {}.", UUIDs.getUnformattedString(taskId), I(fileStorage.fileStorageId), fileStorage.fileStorage.getUri(), I(userId), I(contextId));
            } else {
                LOG.debug("Collected all file storage locations associated with data export task {} using file storage {} ({}) for user {} in context {}: {}", UUIDs.getUnformattedString(taskId), I(fileStorage.fileStorageId), fileStorage.fileStorage.getUri(), I(userId), I(contextId), fileStorageLocations);
            }

            try {
                stmt = con.prepareStatement("DELETE FROM dataExportTask WHERE uuid = ?");
                stmt.setBytes(1, taskIdBytes);
                int count = stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;

                if (count > 0) {
                    stmt = con.prepareStatement("DELETE FROM dataExportTaskWorklist WHERE taskId = ?");
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;

                    stmt = con.prepareStatement("DELETE FROM dataExportFilestoreLocation WHERE taskId = ?");
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;

                    stmt = con.prepareStatement("DELETE FROM dataExportReport WHERE taskId = ?");
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;

                    LOG.debug("Deleted data export task {} for user {} in context {}.", UUIDs.getUnformattedString(taskId), I(userId), I(contextId));

                    if (fileStorageLocations != null ) {
                        for (String fileStorageLocation : fileStorageLocations) {
                            try {
                                fileStorage.fileStorage.deleteFile(fileStorageLocation);
                                LOG.debug("Deleted file storage location {} of data export task {} for user {} in context {}.", fileStorageLocation, UUIDs.getUnformattedString(taskId), I(userId), I(contextId));
                            } catch (Exception e) {
                                LOG.warn("Failed to delete file storage location {} from file storage {} ({})", fileStorageLocation, I(fileStorage.fileStorageId), fileStorage.fileStorage.getUri(), e);
                            }
                        }
                    }
                    return true;
                }
            } finally {
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
        } catch (SQLException e) {
            throw DataExportExceptionCode.SQL_ERROR.create(e);
        }
        return false;
    }

    /**
     * Deletes all tasks (and Work Items) for the given context.
     * Transaction handling must be done by the caller.
     *
     * @param contextId The context identifier
     * @param con A connection to be used
     * @return A List of UUIDs of all deleted tasks
     * @throws OXException
     */
    private Set<FileStorageAndId> deleteTasksOnContextDeletion(int contextId, Connection con) throws OXException {
        if (con == null) {
            LOG.debug("Cannot drop possibly existing data export tasks and its remnants for context {}. No such connection available.", I(contextId));
            return Collections.emptySet();
        }

        try {
            if (false == Databases.tableExists(con, "dataExportTask")) {
                LOG.debug("Cannot drop possibly existing data export tasks and its remnants for  context {}. No such table existent.", I(contextId));
                return Collections.emptySet();
            }

            List<UUID> taskIds = new ArrayList<>();
            Map<UUID, FileStorageAndId> fileStorages = new LinkedHashMap<>();

            PreparedStatement stmt = null;
            ResultSet rs = null;
            {
                Map<Integer, FileStorageAndId> visistedFileStorages = new HashMap<>(2);
                try {
                    stmt = con.prepareStatement("SELECT uuid, filestore FROM dataExportTask WHERE cid = ?");
                    stmt.setInt(1, contextId);
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        UUID taskId = UUIDs.toUUID(rs.getBytes(1));
                        taskIds.add(taskId);
                        int fileStorageId = rs.getInt(2);
                        FileStorageAndId fileStorageAndId = visistedFileStorages.get(I(fileStorageId));
                        if (fileStorageAndId == null) {
                            fileStorageAndId = new FileStorageAndId(fileStorageId, DataExportUtility.getFileStorageFor(fileStorageId, contextId));
                            visistedFileStorages.put(I(fileStorageId), fileStorageAndId);
                        }
                        fileStorages.put(taskId, fileStorageAndId);
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                }
                visistedFileStorages = null; // Help GC
            }

            if (taskIds.isEmpty()) {
                LOG.debug("Found no data export tasks for context {}.", I(contextId));
            }

            LOG.debug("Found {} data export task(s) for context {}.", I(taskIds.size()), I(contextId));

            Map<UUID, List<String>> fileStorageLocations = new LinkedHashMap<>();
            for (UUID taskId : taskIds) {
                byte[] taskIdBytes = UUIDs.toByteArray(taskId);
                List<String> locations = null;
                try {
                    stmt = con.prepareStatement("SELECT filestoreLocation FROM dataExportTaskWorklist WHERE taskId = ? AND filestoreLocation IS NOT NULL");
                    stmt.setBytes(1, taskIdBytes);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        locations = new ArrayList<>();
                        do {
                            locations.add(rs.getString(1));
                        } while (rs.next());
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                }
                try {
                    stmt = con.prepareStatement("SELECT filestoreLocation FROM dataExportFilestoreLocation WHERE taskId = ?");
                    stmt.setBytes(1, taskIdBytes);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        if (locations == null) {
                            locations = new ArrayList<>();
                        }
                        do {
                            locations.add(rs.getString(1));
                        } while (rs.next());
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                }
                fileStorageLocations.put(taskId, locations == null ? Collections.emptyList() : locations);
            }

            LOG.debug("Found {} file storage location(s) for context {}.", I(fileStorageLocations.size()), I(contextId));

            for (UUID taskId : taskIds) {
                byte[] taskIdBytes = UUIDs.toByteArray(taskId);
                try {
                    stmt = con.prepareStatement("DELETE FROM dataExportTask WHERE uuid = ?");
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                } finally {
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                try {
                    stmt = con.prepareStatement("DELETE FROM dataExportTaskWorklist WHERE taskId = ?");
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                } finally {
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                try {
                    stmt = con.prepareStatement("DELETE FROM dataExportFilestoreLocation WHERE taskId = ?");
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                } finally {
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                try {
                    stmt = con.prepareStatement("DELETE FROM dataExportReport WHERE taskId = ?");
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                } finally {
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                LOG.debug("Deleted data export task {} for context {}.", UUIDs.getUnformattedString(taskId), I(contextId));
            }

            for (Map.Entry<UUID, List<String>> fileStorageLocation : fileStorageLocations.entrySet()) {
                UUID taskId = fileStorageLocation.getKey();
                FileStorageAndId fileStorage = fileStorages.get(taskId);
                for (String location : fileStorageLocation.getValue()) {
                    try {
                        fileStorage.fileStorage.deleteFile(location);
                        LOG.debug("Deleted file storage location {} of data export task {} for context {}.", fileStorageLocation, UUIDs.getUnformattedString(taskId), I(contextId));
                    } catch (Exception e) {
                        LOG.warn("Failed to delete file storage location {} from file storage {} ({})", fileStorageLocation, I(fileStorage.fileStorageId), fileStorage.fileStorage.getUri(), e);
                    }
                }
            }

            return new LinkedHashSet<>(fileStorages.values());
        } catch (SQLException e) {
            throw DataExportExceptionCode.SQL_ERROR.create(e);
        }
    }

}
