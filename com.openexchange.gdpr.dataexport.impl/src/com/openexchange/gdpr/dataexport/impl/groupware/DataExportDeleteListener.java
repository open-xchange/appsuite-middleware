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

package com.openexchange.gdpr.dataexport.impl.groupware;

import static com.openexchange.gdpr.dataexport.impl.storage.AbstractDataExportSql.isUseGlobalDb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.impl.DataExportUtility;
import com.openexchange.gdpr.dataexport.impl.osgi.Services;
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

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportDeleteListener.class);
    }

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
            boolean useGlobalDb = isUseGlobalDb();

            if (useGlobalDb) {
                deleteTaskOnUserDeletion(event.getId(), contextId, event.getGlobalDbConnection());
            } else {
                deleteTaskOnUserDeletion(event.getId(), contextId, writeCon);
            }
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            ConfigurationService configService = Services.requireService(ConfigurationService.class);
            boolean useGlobalDb = configService.getBoolProperty("com.openexchange.gdpr.dataexport.useGlobalDb", true);

            Set<FileStorageAndId> fileStorages;
            if (useGlobalDb) {
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
                    LoggerHolder.LOG.warn("Failed to delete the filestore {}", fileStorage.getUri(), e);
                }
            }
        }
    }

    private boolean deleteTaskOnUserDeletion(int userId, int contextId, Connection con) throws OXException {
        if (con == null) {
            return false;
        }

        try {
            if (false == Databases.tableExists(con, "dataExportTask")) {
                return false;
            }

            UUID taskId = null;
            FileStorageAndId fileStorage = null;
            try (PreparedStatement stmt = con.prepareStatement("SELECT uuid, filestore FROM dataExportTask WHERE cid = ? AND user = ?")) {
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
            try (PreparedStatement stmt = con.prepareStatement("SELECT filestoreLocation FROM dataExportTaskWorklist WHERE taskId = ? AND filestoreLocation IS NOT NULL")) {
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
            try (PreparedStatement stmt = con.prepareStatement("SELECT filestoreLocation FROM dataExportFilestoreLocation WHERE taskId = ?")) {
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

            try (PreparedStatement stmt = con.prepareStatement("DELETE FROM dataExportTask WHERE uuid = ?")) {
                stmt.setBytes(1, taskIdBytes);
                int count = stmt.executeUpdate();
                if (count > 0) {
                    try (PreparedStatement stmt2 = con.prepareStatement("DELETE FROM dataExportTaskWorklist WHERE taskId = ?")) {
                        stmt2.setBytes(1, taskIdBytes);
                        stmt2.executeUpdate();
                    }
                    try (PreparedStatement stmt2 = con.prepareStatement("DELETE FROM dataExportFilestoreLocation WHERE taskId = ?")) {
                        stmt2.setBytes(1, taskIdBytes);
                        stmt2.executeUpdate();
                    }
                    if (fileStorageLocations != null ) {
                        for (String fileStorageLocation : fileStorageLocations) {
                            try {
                                fileStorage.fileStorage.deleteFile(fileStorageLocation);
                            } catch (Exception e) {
                                LoggerHolder.LOG.warn("Failed to delete file storage item {} from file storage {}", fileStorageLocation, fileStorage.fileStorage.getUri(), e);
                            }
                        }
                    }
                    return true;
                }
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
            return Collections.emptySet();
        }

        try {
            if (false == Databases.tableExists(con, "dataExportTask")) {
                return Collections.emptySet();
            }

            List<UUID> taskIds = new ArrayList<>();
            Map<UUID, FileStorageAndId> fileStorages = new LinkedHashMap<>();
            try (PreparedStatement stmt = con.prepareStatement("SELECT uuid, filestore FROM dataExportTask WHERE cid = ?")) {
                stmt.setInt(1, contextId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UUID taskId = UUIDs.toUUID(rs.getBytes(1));
                        taskIds.add(taskId);
                        int fileStorageId = rs.getInt(2);
                        fileStorages.put(taskId, new FileStorageAndId(fileStorageId, DataExportUtility.getFileStorageFor(fileStorageId, contextId)));
                    }
                }
            }

            Map<UUID, List<String>> fileStorageLocations = new LinkedHashMap<>();
            for (UUID taskId : taskIds) {
                byte[] taskIdBytes = UUIDs.toByteArray(taskId);
                List<String> locations = null;
                try (PreparedStatement stmt = con.prepareStatement("SELECT filestoreLocation FROM dataExportTaskWorklist WHERE taskId = ? AND filestoreLocation IS NOT NULL")) {
                    stmt.setBytes(1, taskIdBytes);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            locations = new ArrayList<>();
                            do {
                                locations.add(rs.getString(1));
                            } while (rs.next());
                        }
                    }
                }
                try (PreparedStatement stmt = con.prepareStatement("SELECT filestoreLocation FROM dataExportFilestoreLocation WHERE taskId = ?")) {
                    stmt.setBytes(1, taskIdBytes);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            if (locations == null) {
                                locations = new ArrayList<>();
                            }
                            do {
                                locations.add(rs.getString(1));
                            } while (rs.next());
                        }
                    }
                }
                fileStorageLocations.put(taskId, locations == null ? Collections.emptyList() : locations);
            }

            for (UUID taskId : taskIds) {
                byte[] taskIdBytes = UUIDs.toByteArray(taskId);
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM dataExportTask WHERE uuid = ?")) {
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM dataExportTaskWorklist WHERE taskId = ?")) {
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM dataExportFilestoreLocation WHERE taskId = ?")) {
                    stmt.setBytes(1, taskIdBytes);
                    stmt.executeUpdate();
                }
            }

            for (Map.Entry<UUID, List<String>> fileStorageLocation : fileStorageLocations.entrySet()) {
                UUID taskId = fileStorageLocation.getKey();
                FileStorageAndId fileStorage = fileStorages.get(taskId);
                for (String location : fileStorageLocation.getValue()) {
                    try {
                        fileStorage.fileStorage.deleteFile(location);
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to delete file storage item {} from file storage {}", fileStorageLocation, fileStorage.fileStorage.getUri(), e);
                    }
                }
            }

            return new LinkedHashSet<>(fileStorages.values());
        } catch (SQLException e) {
            throw DataExportExceptionCode.SQL_ERROR.create(e);
        }
    }

}
