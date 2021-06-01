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

package com.openexchange.gdpr.dataexport.impl.cleanup;

import static com.eaio.util.text.HumanTime.exactly;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.tuple.Pair;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.CleanUpExecution;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageInfoService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportResultFile;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportWorkItem;
import com.openexchange.gdpr.dataexport.impl.DataExportLock;
import com.openexchange.gdpr.dataexport.impl.DataExportUtility;
import com.openexchange.java.ConvertUtils;
import com.openexchange.reseller.ResellerService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DataExportCleanUpTask} - Clean-up task for orphaned data export files.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class DataExportCleanUpTask implements CleanUpExecution {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportCleanUpTask.class);

    private static final String PROP_CLEANUP_ENABLED = "com.openexchange.gdpr.dataexport.cleanup.enabled";
    private static final String PROP_DATABASE_SERVICE = "com.openexchange.gdpr.dataexport.cleanup.databaseService";
    private static final String PROP_LOCK_ACQUISITION = "com.openexchange.gdpr.dataexport.cleanup.lockAcquisition";
    private static final String PROP_CLEANUP_PROCESSED_FILESTORE_IDS = "com.openexchange.gdpr.dataexport.cleanup.processedFilestoreIds";
    private static final String PROP_CLEANUP_GENERIC_FILESTORE_IDS = "com.openexchange.gdpr.dataexport.cleanup.genericFilestoreIds";

    /** The decimal format to use when printing milliseconds */
    private static final NumberFormat MILLIS_FORMAT = newNumberFormat();

    /** The accompanying lock for shared decimal format */
    private static final Lock MILLIS_FORMAT_LOCK = new ReentrantLock();

    /**
     * Creates a new {@code DecimalFormat} instance.
     *
     * @return The format instance
     */
    private static NumberFormat newNumberFormat() {
        NumberFormat f = NumberFormat.getInstance(Locale.US);
        if (f instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) f;
            df.applyPattern("#,##0");
        }
        return f;
    }

    private static String formatDuration(long duration) {
        if (MILLIS_FORMAT_LOCK.tryLock()) {
            try {
                return MILLIS_FORMAT.format(duration);
            } finally {
                MILLIS_FORMAT_LOCK.unlock();
            }
        }

        // Use thread-specific DecimalFormat instance
        NumberFormat format = newNumberFormat();
        return format.format(duration);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final DataExportService dataExportService;
    private final DataExportStorageService storageService;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link DataExportCleanUpTask}.
     *
     * @param dataExportService The data export service
     * @param storageService The storage service
     * @param services The service look-up
     */
    public DataExportCleanUpTask(DataExportService dataExportService, DataExportStorageService storageService, ServiceLookup services) {
        super();
        this.dataExportService = dataExportService;
        this.storageService = storageService;
        this.services = services;
    }

    @Override
    public boolean prepareCleanUp(Map<String, Object> state) throws OXException {
        {
            boolean defaultValue = true;
            ConfigurationService configService = services.getOptionalService(ConfigurationService.class);
            boolean enabled = configService == null ? defaultValue : configService.getBoolProperty(PROP_CLEANUP_ENABLED, defaultValue);
            if (enabled == false) {
                LOG.warn("Clean-up of data export files disabled per configuration. Aborting clean-up run");
                return false;
            }
        }

        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (databaseService == null) {
            // Missing database service
            LOG.warn("Failed to acquire database service, which is needed to clean-up data export files. Aborting clean-up run");
            return false;
        }

        // Check for possibly currently running data export tasks
        Boolean hasRunningTasks = checkForRunningDataExportTasks();
        if (hasRunningTasks == null) {
            // Check failed and has already been logged
            return false;
        } else if (hasRunningTasks.booleanValue()) {
            LOG.info("Detected currently running data export tasks. Aborting clean-up run");
            return false;
        }

        // Try to acquire lock
        boolean result = false;
        DataExportLock lock = DataExportLock.getInstance();
        DataExportLock.LockAcquisition acquisition = null;
        try {
            acquisition = lock.acquireCleanUpTaskLock(true, databaseService);
            if (acquisition.isNotAcquired()) {
                // Failed to acquire clean-up lock
                LOG.info("Failed to acquire clean-up lock for data export files since another process is currently running. Aborting clean-up run");
                return false;
            }

            // Lock acquired
            state.put(PROP_DATABASE_SERVICE, databaseService);
            state.put(PROP_LOCK_ACQUISITION, acquisition);
            result = true;
            return result;
        } finally {
            if (result == false) {
                releaseCleanUpTaskLockSafe(databaseService, acquisition);
            }
        }
    }

    @Override
    public void finishCleanUp(Map<String, Object> state) throws OXException {
        DatabaseService databaseService = (DatabaseService) state.get(PROP_DATABASE_SERVICE);
        DataExportLock.LockAcquisition acquisition = (DataExportLock.LockAcquisition) state.get(PROP_LOCK_ACQUISITION);
        releaseCleanUpTaskLockSafe(databaseService, acquisition);
    }

    private static void releaseCleanUpTaskLockSafe(DatabaseService databaseService, DataExportLock.LockAcquisition acquisition) {
        try {
            DataExportLock.getInstance().releaseCleanUpTaskLock(acquisition, databaseService);
        } catch (Exception e) {
            LOG.warn("Failed to release clean-up lock for data export files", e);
        }
    }

    @Override
    public boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) {
        // Determine the file storage identifiers to process for this execution's invocation
        DatabaseService databaseService = (DatabaseService) state.get(PROP_DATABASE_SERVICE);
        Set<Integer> fileStoreIdsToProcess;
        try {
            Set<Integer> fileStoreIdsWoSchema = (Set<Integer>) state.get(PROP_CLEANUP_GENERIC_FILESTORE_IDS);
            if (fileStoreIdsWoSchema == null) {
                fileStoreIdsWoSchema = determineFileStorageIdsFromAllScopesButSchema(databaseService);
                state.put(PROP_CLEANUP_GENERIC_FILESTORE_IDS, fileStoreIdsWoSchema);
            }
            fileStoreIdsToProcess = new HashSet<>(fileStoreIdsWoSchema);
            fileStoreIdsToProcess.addAll(determineFileStorageIdsFromSchema(databaseService, schema, representativeContextId));
            Set<Integer> processedFileStoreIds = (Set<Integer>) state.get(PROP_CLEANUP_PROCESSED_FILESTORE_IDS);
            if (processedFileStoreIds != null) {
                fileStoreIdsToProcess.removeAll(processedFileStoreIds);
            }
            if (fileStoreIdsToProcess.isEmpty()) {
                LOG.info("Found no file storage(s) to process for data export files. Aborting clean-up run for schema {}", schema);
                return;
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.warn("Unable to determine the file storage identifiers to process. Aborting clean-up run for schema {}", schema, t);
            return;
        }

        // Process determined file storage identifiers
        try {
            LOG.info("Going to check {} file storage(s) for orphaned data export files", I(fileStoreIdsToProcess.size()));
            long start = System.currentTimeMillis();
            fixOrphanedEntries(fileStoreIdsToProcess);
            long duration = System.currentTimeMillis() - start;
            LOG.info("Data export clean-up task took {}ms ({}) for {} file storage(s)", formatDuration(duration), exactly(duration, true), I(fileStoreIdsToProcess.size()));
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.warn("Failed clean-up run for schema {}", schema, t);
            return;
        }

        // Remember already processed file storage identifiers
        state.put(PROP_CLEANUP_PROCESSED_FILESTORE_IDS, fileStoreIdsToProcess);
    }

    private Boolean checkForRunningDataExportTasks() {
        try {
            return Boolean.valueOf(dataExportService.hasRunningDataExportTasks());
        } catch (Exception e) {
            LOG.warn("Failed to check for running data export tasks. Assuming there are running tasks for safety's sake and therefore aborting clean-up run...", e);
            return null;
        }
    }

    private void fixOrphanedEntries(Set<Integer> filestoreIds) throws OXException {
        for (Map.Entry<String, FileStorage> entry : getOrphanedFileStoreLocations(filestoreIds).entrySet()) {
            String orphanedFile = entry.getKey();
            FileStorage fileStorage = entry.getValue();
            fileStorage.deleteFile(orphanedFile);
            LOG.debug("Deleted orphaned data export file {} from file storage {}", orphanedFile, fileStorage.getUri());
        }

        for (Pair<DataExportTask, DataExportWorkItem> pair : getOrphanedWorkItems()) {
            DataExportTask task = pair.getLeft();
            DataExportWorkItem workItem = pair.getRight();
            storageService.markWorkItemPending(task.getId(), workItem.getModuleId(), task.getUserId(), task.getContextId());
            LOG.debug("Marked orphaned work item {} of data export task from user {} in context {} as pending (missing its artifact in file storage) to enforce re-execution", workItem.getModuleId(), I(task.getUserId()), I(task.getContextId()));
        }

        for (Pair<DataExportTask, DataExportResultFile> pair : getOrphanedResultFiles()) {
            DataExportTask task = pair.getLeft();
            storageService.markPending(task.getId(), task.getUserId(), task.getContextId());
            for (DataExportWorkItem item : task.getWorkItems()) {
                storageService.markWorkItemPending(task.getId(), item.getModuleId(), task.getUserId(), task.getContextId());
                LOG.debug("Marked orphaned work item {} of data export task from user {} in context {} as pending (missing task result files file storage) to enforce re-execution", item.getModuleId(), I(task.getUserId()), I(task.getContextId()));
            }
            storageService.deleteResultFiles(task.getId(), task.getUserId(), task.getContextId());
        }
    }

    private Map<String, FileStorage> getOrphanedFileStoreLocations(Set<Integer> filestoreIds) throws OXException {
        // Fetch required service
        FileStorageInfoService infoService = FileStorages.getFileStorageInfoService();
        if (null == infoService) {
            throw ServiceExceptionCode.absentService(FileStorageInfoService.class);
        }

        // Collect available files from given file storages
        Map<String, FileStorage> allFiles = new HashMap<>();
        List<Integer> allContextIds = null;
        Set<URI> alreadyVisited = new HashSet<>(filestoreIds.size());
        for (Integer filestoreId : filestoreIds) {
            // Determine base URI and scheme
            URI baseUri = infoService.getFileStorageInfo(i(filestoreId)).getUri();
            String scheme = baseUri.getScheme();
            if (scheme == null) {
                scheme = "file";
            }

            // Static prefix in case of "file"-schemed file storage
            if ("file".equals(scheme)) {
                FileStorage fileStorage = DataExportUtility.getFileStorageFor(baseUri, scheme, 0 /*don't care*/);
                if (alreadyVisited.add(fileStorage.getUri())) {
                    addFilesFrom(fileStorage, allFiles);
                }
            } else {
                if (allContextIds == null) {
                    allContextIds = services.getServiceSafe(ContextService.class).getAllContextIds();
                }
                for (Integer contextId : allContextIds) {
                    FileStorage fileStorage = DataExportUtility.getFileStorageFor(baseUri, scheme, i(contextId));
                    if (alreadyVisited.add(fileStorage.getUri())) {
                        addFilesFrom(fileStorage, allFiles);
                    }
                }
            }
        }

        for (DataExportTask task : dataExportService.getDataExportTasks(true)) {
            if (task.getResultFiles() != null) {
                for (DataExportResultFile resultFile : task.getResultFiles()) {
                    if (resultFile.getFileStorageLocation() != null) {
                        allFiles.remove(resultFile.getFileStorageLocation());
                    }
                }
            }
            for (DataExportWorkItem item : task.getWorkItems()) {
                if (item.getFileStorageLocation() != null) {
                    allFiles.remove(item.getFileStorageLocation());
                }
            }
        }

        LOG.info("Detected {} orphaned data export file(s)", I(allFiles.size()));
        return allFiles;
    }

    private void addFilesFrom(FileStorage fileStorage, Map<String, FileStorage> allFiles) throws OXException {
        SortedSet<String> fileList = fileStorage.getFileList();
        if (fileList.isEmpty()) {
            LOG.debug("No files available in file storage {}", fileStorage.getUri());
        } else {
            LOG.debug("Found {} file(s) in file storage {}", I(fileList.size()), fileStorage.getUri());
            for (String file : fileList) {
                allFiles.put(file, fileStorage);
                LOG.debug("Considering file {} from file storage {}", file, fileStorage.getUri());
            }
        }
    }

    private List<Pair<DataExportTask, DataExportWorkItem>> getOrphanedWorkItems() throws OXException {
        List<DataExportTask> tasks = dataExportService.getDataExportTasks(false);
        List<Pair<DataExportTask, DataExportWorkItem>> retval = null;
        for (DataExportTask task : tasks) {
            SortedSet<String> fileList = DataExportUtility.getFileStorageFor(task).getFileList();
            for (DataExportWorkItem item : task.getWorkItems()) {
                if (item.getFileStorageLocation() != null && !fileList.contains(item.getFileStorageLocation())) {
                    if (retval == null) {
                        retval = new ArrayList<>();
                    }
                    retval.add(Pair.of(task, item));
                }
            }
        }
        return retval == null ? Collections.emptyList() : retval;
    }

    private List<Pair<DataExportTask, DataExportResultFile>> getOrphanedResultFiles() throws OXException {
        List<DataExportTask> tasks = dataExportService.getDataExportTasks(false);
        List<Pair<DataExportTask, DataExportResultFile>> retval = null;
        for (DataExportTask task : tasks) {
            if (task.getResultFiles() != null && !task.getResultFiles().isEmpty()) {
                SortedSet<String> fileList = DataExportUtility.getFileStorageFor(task).getFileList();
                for (DataExportResultFile resultFile : task.getResultFiles()) {
                    if (resultFile.getFileStorageLocation() != null && !fileList.contains(resultFile.getFileStorageLocation())) {
                        if (retval == null) {
                            retval = new ArrayList<>();
                        }
                        retval.add(Pair.of(task, resultFile));
                    }
                }
            }
        }
        return retval == null ? Collections.emptyList() : retval;
    }

    // ----------------------------------- Retrieval of file storage identifiers -----------------------------------------------------------

    private Set<Integer> determineFileStorageIdsFromAllScopesButSchema(DatabaseService databaseService) {
        Set<Integer> fileStoreIds = null;

        // Check for possible configured file storage identifiers for context-sets scope
        try {
            ConfigurationService configurationService = services.getOptionalService(ConfigurationService.class);
            if (configurationService != null) {
                Set<Integer> contextSetsFileStoreIds = determineContextSetsFileStoreIds(configurationService);
                if (contextSetsFileStoreIds.isEmpty() == false) {
                    fileStoreIds = new HashSet<>();
                    fileStoreIds.addAll(contextSetsFileStoreIds);
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to retrieve possible identifiers of data export file storage from context-sets scope", e);
        }

        // Check for possible configured file storage identifiers for reseller scope
        try {
            ResellerService resellerService = services.getOptionalService(ResellerService.class);
            if (resellerService != null && resellerService.isEnabled()) {
                Set<Integer> resellerFileStoreIds = determineResellerFileStoreIds(databaseService);
                if (resellerFileStoreIds.isEmpty() == false) {
                    if (fileStoreIds == null) {
                        fileStoreIds = new HashSet<>();
                    }
                    fileStoreIds.addAll(resellerFileStoreIds);
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to retrieve possible identifiers of data export file storage from reseller scope", e);
        }

        // Check for possible configured file storage identifiers for server scope
        try {
            ConfigurationService configurationService = services.getOptionalService(ConfigurationService.class);
            int fileStoreId = configurationService == null ? -1 : configurationService.getIntProperty("com.openexchange.gdpr.dataexport.fileStorageId", -1);
            if (fileStoreId > 0) {
                if (fileStoreIds == null) {
                    fileStoreIds = new HashSet<>();
                }
                fileStoreIds.add(I(fileStoreId));
            }
        } catch (Exception e) {
            LOG.warn("Failed to retrieve possible identifiers of data export file storage from server scope", e);
        }
        return fileStoreIds == null ? Collections.emptySet() : fileStoreIds;
    }

    private Set<Integer> determineFileStorageIdsFromSchema(DatabaseService databaseService, String schema, int representativeContextId) {
        // Check for possible configured file storage identifiers for schema
        Set<Integer> fileStoreIds = null;
        try {
            Set<Integer> fileStoreIdsForSchema = determineFileStoreIdsFor(representativeContextId, databaseService);
            if (fileStoreIdsForSchema.isEmpty() == false) {
                fileStoreIds = fileStoreIdsForSchema;
            }
        } catch (Exception e) {
            LOG.warn("Failed to retrieve possible identifiers of data export file storage for schema {}", schema, e);
        }
        return fileStoreIds == null ? Collections.emptySet() : fileStoreIds;
    }

    private Set<Integer> determineContextSetsFileStoreIds(ConfigurationService configurationService) {
        Set<Integer> fileStoreIds = null;
        Map<String, Object> yamlFiles = configurationService.getYamlInFolder("contextSets");
        if (yamlFiles != null) {
            for (Map.Entry<String, Object> file : yamlFiles.entrySet()) {
                Map<Object, Map<String, Object>> content = (Map<Object, Map<String, Object>>) file.getValue();
                if (content != null) {
                    for (Map.Entry<Object, Map<String, Object>> configData : content.entrySet()) {
                        Map<String, Object> configuration = configData.getValue();
                        Object oFileStorageId = configuration.get("com.openexchange.gdpr.dataexport.fileStorageId");
                        if (oFileStorageId != null) {
                            if (fileStoreIds == null) {
                                fileStoreIds = new HashSet<>();
                            }
                            try {
                                fileStoreIds.add(Integer.valueOf(oFileStorageId.toString()));
                            } catch (NumberFormatException e) {
                                // Ignore
                            }
                        }
                    }
                }
            }
        }
        return fileStoreIds == null ? Collections.emptySet() : fileStoreIds;
    }

    private Set<Integer> determineResellerFileStoreIds(DatabaseService databaseService) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = databaseService.getReadOnly();
        try {
            if (Databases.tableExists(con, "subadmin_config_properties") == false) {
                return Collections.emptySet();
            }

            stmt = con.prepareStatement("SELECT propertyValue FROM subadmin_config_properties WHERE propertyKey = ?");
            stmt.setString(1, "com.openexchange.gdpr.dataexport.fileStorageId");
            rs = stmt.executeQuery();
            if (rs.next() == false) {
                return Collections.emptySet();
            }

            Set<Integer> fileStoreIds = new HashSet<>();
            do {
                try {
                    fileStoreIds.add(Integer.valueOf(rs.getString(1)));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            } while (rs.next());
            return fileStoreIds;
        } catch (SQLException e) {
            throw DataExportExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(con);
        }
    }

    private Set<Integer> determineFileStoreIdsFor(int representativeContextId, DatabaseService databaseService) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = databaseService.getReadOnly(representativeContextId);
        try {
            stmt = con.prepareStatement("SELECT value FROM user_attribute WHERE name=?");
            stmt.setString(1, "config/com.openexchange.gdpr.dataexport.fileStorageId");
            rs = stmt.executeQuery();

            Set<Integer> fileStoreIds = null;
            if (rs.next()) {
                fileStoreIds = new HashSet<>();
                do {
                    try {
                        String value = rs.getString(1);
                        value = ConvertUtils.loadConvert(value);
                        fileStoreIds.add(Integer.valueOf(value));
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                } while (rs.next());
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            stmt = con.prepareStatement("SELECT value FROM contextAttribute WHERE name=?");
            stmt.setString(1, "config/com.openexchange.gdpr.dataexport.fileStorageId");
            rs = stmt.executeQuery();

            if (rs.next()) {
                if (fileStoreIds == null) {
                    fileStoreIds = new HashSet<>();
                }
                do {
                    try {
                        String value = rs.getString(1);
                        int pos = value.indexOf(';');
                        if (pos >= 0) {
                            value = value.substring(0, pos).trim();
                        }
                        value = value.replace("%3B", ";").replace("%25", "%");
                        value = ConvertUtils.loadConvert(value);
                        fileStoreIds.add(Integer.valueOf(value));
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                } while (rs.next());
            }

            return fileStoreIds == null ? Collections.emptySet() : fileStoreIds;
        } catch (SQLException e) {
            throw DataExportExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(representativeContextId, con);
        }
    }

}
