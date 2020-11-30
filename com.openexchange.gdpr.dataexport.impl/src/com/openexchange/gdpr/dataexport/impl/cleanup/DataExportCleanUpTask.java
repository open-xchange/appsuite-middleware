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
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.tuple.Pair;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportResultFile;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportWorkItem;
import com.openexchange.gdpr.dataexport.impl.DataExportUtility;
import com.openexchange.java.ConvertUtils;
import com.openexchange.reseller.ResellerService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DataExportCleanUpTask} - Clean-up task for orphaned data export files.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class DataExportCleanUpTask implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportCleanUpTask.class);

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
    public void run() {
        Thread currentThread = Thread.currentThread();
        String prevName = currentThread.getName();
        currentThread.setName("DataExportCleanUpTask");
        try {
            long start = System.currentTimeMillis();

            Set<Integer> fileStoreIds = null;

            // Check for possible configured file storage identifiers once for context-sets, reseller and server scope
            try {
                ConfigurationService configurationService = services.getOptionalService(ConfigurationService.class);
                if (configurationService != null) {
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
                }
            } catch (Exception e) {
                LOG.warn("Failed to retrieve possible identifiers of data export file storage from context-sets scope", e);
            }

            try {
                ResellerService resellerService = services.getOptionalService(ResellerService.class);
                if (resellerService != null && resellerService.isEnabled()) {
                    Set<Integer> resellerFileStoreIds = determineResellerFileStoreIds();
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

            // Check for possible configured file storage identifiers once for context and user scope
            for (Integer representativeContextId : services.getServiceSafe(ContextService.class).getDistinctContextsPerSchema()) {
                fileStoreIds = addFileStoreIdsForSchema(representativeContextId.intValue(), fileStoreIds);
            }

            if (fileStoreIds != null) {
                LOG.info("Going to check {} file storage(s) for orphaned data export files", I(fileStoreIds.size()));
                fixOrphanedEntries(fileStoreIds);
            }

            long duration = System.currentTimeMillis() - start;
            LOG.info("Data export clean-up task took {}ms ({})", formatDuration(duration), exactly(duration, true));
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.warn("Failed to delete orphaned data export files", t);
        } finally {
            currentThread.setName(prevName);
        }
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

    private void fixOrphanedEntries(Set<Integer> filestoreIds) throws OXException {
        for (Map.Entry<String, FileStorageAndId> entry : getOrphanedFileStoreLocations(filestoreIds).entrySet()) {
            entry.getValue().fileStorage.deleteFile(entry.getKey());
        }

        for (Pair<DataExportTask, DataExportWorkItem> pair : getOrphanedWorkItems()) {
            DataExportTask task = pair.getLeft();
            DataExportWorkItem workItem = pair.getRight();
            storageService.markWorkItemPending(task.getId(), workItem.getModuleId(), task.getUserId(), task.getContextId());
        }

        for (Pair<DataExportTask, DataExportResultFile> pair : getOrphanedResultFiles()) {
            DataExportTask task = pair.getLeft();
            storageService.markPending(task.getId(), task.getUserId(), task.getContextId());
            for (DataExportWorkItem item : task.getWorkItems()) {
                storageService.markWorkItemPending(task.getId(), item.getModuleId(), task.getUserId(), task.getContextId());
            }
            storageService.deleteResultFiles(task.getId(), task.getUserId(), task.getContextId());
        }
    }

    private Map<String, FileStorageAndId> getOrphanedFileStoreLocations(Set<Integer> filestoreIds) throws OXException {
        List<Integer> allContextIds = services.getServiceSafe(ContextService.class).getAllContextIds();
        Map<String, FileStorageAndId> allFiles = new HashMap<>();
        Set<URI> alreadyVisited = new HashSet<>(allContextIds.size() >> 2);
        for (Integer filestoreId : filestoreIds) {
            try {
                for (Integer contextId : allContextIds) {
                    FileStorage fileStorage = null;
                    try {
                        fileStorage = DataExportUtility.getFileStorageFor(i(filestoreId), i(contextId));
                        if (fileStorage == null) {
                            continue;
                        }
                    } catch (OXException oe) {
                        continue; // ignore
                    }
                    if (alreadyVisited.add(fileStorage.getUri())) {
                        SortedSet<String> fileList = fileStorage.getFileList();
                        if (!fileList.isEmpty()) {
                            FileStorageAndId fsid = new FileStorageAndId(i(filestoreId), fileStorage);
                            for (String file : fileList) {
                                allFiles.put(file, fsid);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn("Failed to retrieve data export file(s) from file storage {}", filestoreId);
            }
        }

        for (DataExportTask task : dataExportService.getDataExportTasks()) {
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

        LOG.debug("Detected {} orphaned data export file(s)", I(allFiles.size()));

        return allFiles;
    }

    private List<Pair<DataExportTask, DataExportWorkItem>> getOrphanedWorkItems() throws OXException {
        List<DataExportTask> tasks = dataExportService.getDataExportTasks();
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
        List<DataExportTask> tasks = dataExportService.getDataExportTasks();
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

    private Set<Integer> addFileStoreIdsForSchema(int representativeContextId, Set<Integer> fileStoreIds) {
        Set<Integer> fids = null;

        try {
            Set<Integer> fileStoreIdsForSchema = determineFileStoreIdsFor(representativeContextId);
            if (fileStoreIdsForSchema.isEmpty() == false) {
                fids = fileStoreIds == null ? new HashSet<>() : fileStoreIds;
                fids.addAll(fileStoreIdsForSchema);
            }
        } catch (Exception e) {
            Optional<String> optSchema = getSchemaSafe(representativeContextId);
            if (optSchema.isPresent()) {
                LOG.warn("Failed to retrieve possible identifiers of data export file storage for schema {}", optSchema.get(), e);
            } else {
                LOG.warn("Failed to retrieve possible identifiers of data export file storage for schema association with context {}", I(representativeContextId), e);
            }
        }

        return fids;
    }

    private Set<Integer> determineResellerFileStoreIds() throws OXException {
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
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

    private Set<Integer> determineFileStoreIdsFor(int representativeContextId) throws OXException {
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
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

    private Optional<String> getSchemaSafe(int representativeContextId) {
        ContextService contextService = services.getOptionalService(ContextService.class);
        if (contextService == null) {
            return Optional.empty();
        }

        try {
            Map<PoolAndSchema, List<Integer>> associations = contextService.getSchemaAssociationsFor(Collections.singletonList(I(representativeContextId)));
            return Optional.of(associations.keySet().iterator().next().getSchema());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class FileStorageAndId {

        final FileStorage fileStorage;
        final int filestoreId;

        FileStorageAndId(int filestoreId, FileStorage fileStorage) {
            super();
            this.filestoreId = filestoreId;
            this.fileStorage = fileStorage;
        }
    }

}
