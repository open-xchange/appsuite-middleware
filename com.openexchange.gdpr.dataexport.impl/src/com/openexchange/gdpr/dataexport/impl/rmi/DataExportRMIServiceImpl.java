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

package com.openexchange.gdpr.dataexport.impl.rmi;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.ImmutableMap;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportResultFile;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportWorkItem;
import com.openexchange.gdpr.dataexport.impl.DataExportUtility;
import com.openexchange.gdpr.dataexport.impl.osgi.Services;
import com.openexchange.gdpr.dataexport.rmi.DataExportRMIService;
import com.openexchange.java.ISO8601Utils;
import com.openexchange.java.util.UUIDs;

/**
 * {@link DataExportRMIServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportRMIServiceImpl implements DataExportRMIService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportRMIServiceImpl.class);

    private final DataExportService service;
    private final ContextService contexts;

    /**
     * Initializes a new {@link DataExportRMIServiceImpl}.
     *
     * @param service The data export service
     * @throws OXException
     */
    public DataExportRMIServiceImpl(DataExportService service) throws OXException {
        super();
        this.service = service;
        contexts = Services.requireService(ContextService.class);
    }

    @Override
    public boolean cancelDataExportTask(int userId, int contextId) throws RemoteException {
        try {
            boolean canceled = service.cancelDataExportTask(userId, contextId);
            if (!canceled) {
                throw DataExportExceptionCode.CANCEL_TASK_FAILED.create(I(userId), I(contextId));
            }
            return true;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<String> cancelDataExportTasks(int contextId) throws RemoteException {
        try {
            List<UUID> taskIds = service.cancelDataExportTasks(contextId);
            if (taskIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> retval = new ArrayList<>(taskIds.size());
            for (UUID taskId : taskIds) {
                retval.add(UUIDs.getUnformattedString(taskId));
            }
            return retval;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getDataExportTask(int userId, int contextId) throws RemoteException {
        try {
            Optional<DataExportTask> optionalTask = service.getDataExportTask(userId, contextId);
            if (!optionalTask.isPresent()) {
                throw DataExportExceptionCode.NO_SUCH_TASK.create(I(userId), I(contextId));
            }

            Map<String, Object> retval = generateTaskMetadata(optionalTask.get());
            return retval;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getDataExportTasks(int contextId) throws RemoteException {
        try {
            List<DataExportTask> tasks = service.getDataExportTasks(contextId);

            List<Map<String, Object>> retval = new ArrayList<>(tasks.size());
            for (DataExportTask task : tasks) {
                retval.add(generateTaskMetadata(task));
            }
            return retval;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getDataExportTasks() throws RemoteException {
        try {
            List<DataExportTask> tasks = service.getDataExportTasks();

            List<Map<String, Object>> retval = new ArrayList<>(tasks.size());
            for (DataExportTask task : tasks) {
                retval.add(generateTaskMetadata(task));
            }
            return retval;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getOrphanedWorkItems() throws RemoteException {
        List<Map<String, Object>> retval = new ArrayList<>();

        for (Pair<DataExportTask, DataExportWorkItem> pair : getOrphanedWorkItems_()) {
            retval.add(generateWorkItem(pair.getLeft(), pair.getRight()));
        }

        return retval;
    }

    @Override
    public List<Map<String, Object>> getOrphanedResultFiles() throws RemoteException {
        List<Map<String, Object>> retval = new ArrayList<>();

        for (Pair<DataExportTask, DataExportResultFile> pair : getOrphanedResultFiles_()) {
            retval.add(generateResultFile(pair.getLeft(), pair.getRight()));
        }

        return retval;
    }

    @Override
    public List<Map<String, Object>> getOrphanedFileStoreLocations(List<Integer> filestoreIds) throws RemoteException {
        List<Map<String, Object>> retval = new ArrayList<>();

        for (Map.Entry<String, FileStorageAndId> entry : getOrphanedFileStoreLocations_(filestoreIds).entrySet()) {
            retval.add(generateFileStoreLocation(entry.getValue().filestoreId, entry.getValue().fileStorage.getUri(), entry.getKey()));
        }

        return retval;
    }

    @Override
    public int fixOrphanedEntries(List<Integer> filestoreIds) throws RemoteException {
        int fixed = 0;
        try {
            for (Map.Entry<String, FileStorageAndId> entry : getOrphanedFileStoreLocations_(filestoreIds).entrySet()) {
                entry.getValue().fileStorage.deleteFile(entry.getKey());
                fixed++;
            }

            DataExportStorageService storageService = Services.requireService(DataExportStorageService.class);
            for (Pair<DataExportTask, DataExportWorkItem> pair : getOrphanedWorkItems_()) {
                DataExportTask task = pair.getLeft();
                DataExportWorkItem workItem = pair.getRight();
                storageService.markWorkItemPending(task.getId(), workItem.getModuleId(), task.getUserId(), task.getContextId());
                fixed++;
            }

            for (Pair<DataExportTask, DataExportResultFile> pair : getOrphanedResultFiles_()) {
                DataExportTask task = pair.getLeft();
                storageService.markPending(task.getId(), task.getUserId(), task.getContextId());
                for (DataExportWorkItem item : task.getWorkItems()) {
                    storageService.markWorkItemPending(task.getId(), item.getModuleId(), task.getUserId(), task.getContextId());
                }
                storageService.deleteResultFiles(task.getId(), task.getContextId());
                fixed++;
            }
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        }
        return fixed;
    }

    private Map<String, FileStorageAndId> getOrphanedFileStoreLocations_(List<Integer> filestoreIds) throws RemoteException {
        try {
            List<Integer> allContextIds = contexts.getAllContextIds();
            Map<String, FileStorageAndId> allFiles = new HashMap<>();
            Set<URI> alreadyVisited = new HashSet<>(allContextIds.size() >> 2);
            for (Integer filestoreId : filestoreIds) {
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
            }

            for (DataExportTask task : service.getDataExportTasks()) {
                if (task.getResultFiles() != null) {
                    for (DataExportResultFile resultFile : task.getResultFiles()) {
                        if (resultFile.getFileStorageLocation() != null && allFiles.containsKey(resultFile.getFileStorageLocation())) {
                            allFiles.remove(resultFile.getFileStorageLocation());
                        }
                    }
                }
                for (DataExportWorkItem item : task.getWorkItems()) {
                    if (item.getFileStorageLocation() != null && allFiles.containsKey(item.getFileStorageLocation())) {
                        allFiles.remove(item.getFileStorageLocation());
                    }
                }
            }
            return allFiles;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        }
    }

    private List<Pair<DataExportTask, DataExportWorkItem>> getOrphanedWorkItems_() throws RemoteException {
        List<Pair<DataExportTask, DataExportWorkItem>> retval = new ArrayList<>();
        try {
            List<DataExportTask> tasks = service.getDataExportTasks();
            for (DataExportTask task : tasks) {
                SortedSet<String> fileList = DataExportUtility.getFileStorageFor(task).getFileList();
                for (DataExportWorkItem item : task.getWorkItems()) {
                    if (item.getFileStorageLocation() != null && !fileList.contains(item.getFileStorageLocation())) {
                        retval.add(Pair.of(task, item));
                    }
                }
            }
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        }
        return retval;
    }

    private List<Pair<DataExportTask, DataExportResultFile>> getOrphanedResultFiles_() throws RemoteException {
        try {
            List<DataExportTask> tasks = service.getDataExportTasks();
            List<Pair<DataExportTask, DataExportResultFile>> retval = new ArrayList<>(tasks.size());
            for (DataExportTask task : tasks) {
                if (task.getResultFiles() != null && !task.getResultFiles().isEmpty()) {
                    SortedSet<String> fileList = DataExportUtility.getFileStorageFor(task).getFileList();
                    for (DataExportResultFile resultFile : task.getResultFiles()) {
                        if (resultFile.getFileStorageLocation() != null && !fileList.contains(resultFile.getFileStorageLocation())) {
                            retval.add(Pair.of(task, resultFile));
                        }
                    }
                }
            }
            return retval;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        }
    }

    private Map<String, Object> generateFileStoreLocation(int filestoreId, URI fileStorageUri, String file) {
        ImmutableMap.Builder<String, Object> workItemMap = ImmutableMap.builderWithExpectedSize(3);
        workItemMap.put(DataExportRMIService.COLUMN_FILESTORE, I(filestoreId));
        workItemMap.put(DataExportRMIService.COLUMN_FILESTORE_URI, fileStorageUri.toString());
        workItemMap.put(DataExportRMIService.COLUMN_LOCATION, file);
        ImmutableMap<String, Object> build = workItemMap.build();
        return build;
    }

    private Map<String, Object> generateWorkItem(DataExportTask task, DataExportWorkItem item) {
        ImmutableMap.Builder<String, Object> workItemMap = ImmutableMap.builderWithExpectedSize(6);
        workItemMap.put(DataExportRMIService.COLUMN_TASK, UUIDs.getUnformattedString(task.getId()));
        workItemMap.put(DataExportRMIService.COLUMN_MODULE, item.getModuleId());
        workItemMap.put(DataExportRMIService.COLUMN_USER, I(task.getUserId()));
        workItemMap.put(DataExportRMIService.COLUMN_CONTEXT, I(task.getContextId()));
        workItemMap.put(DataExportRMIService.COLUMN_FILESTORE, I(task.getFileStorageId()));
        workItemMap.put(DataExportRMIService.COLUMN_LOCATION, Optional.ofNullable(item.getFileStorageLocation()).orElse(""));
        ImmutableMap<String, Object> build = workItemMap.build();
        return build;
    }

    private Map<String, Object> generateResultFile(DataExportTask task, DataExportResultFile resultFile) {
        ImmutableMap.Builder<String, Object> workItemMap = ImmutableMap.builderWithExpectedSize(6);
        workItemMap.put(DataExportRMIService.COLUMN_TASK, UUIDs.getUnformattedString(task.getId()));
        workItemMap.put(DataExportRMIService.COLUMN_PACKAGE, I(resultFile.getNumber()));
        workItemMap.put(DataExportRMIService.COLUMN_USER, I(task.getUserId()));
        workItemMap.put(DataExportRMIService.COLUMN_CONTEXT, I(task.getContextId()));
        workItemMap.put(DataExportRMIService.COLUMN_FILESTORE, I(task.getFileStorageId()));
        workItemMap.put(DataExportRMIService.COLUMN_LOCATION, resultFile.getFileStorageLocation());
        ImmutableMap<String, Object> build = workItemMap.build();
        return build;
    }

    private Map<String, Object> generateTaskMetadata(DataExportTask task) {
        ImmutableMap.Builder<String, Object> taskMap = ImmutableMap.builderWithExpectedSize(7);
        taskMap.put(DataExportRMIService.COLUMN_ID, UUIDs.getUnformattedString(task.getId()));
        taskMap.put(DataExportRMIService.COLUMN_USER, I(task.getUserId()));
        taskMap.put(DataExportRMIService.COLUMN_CONTEXT, I(task.getContextId()));
        taskMap.put(DataExportRMIService.COLUMN_CREATION_TIME, ISO8601Utils.format(task.getCreationTime()));
        taskMap.put(DataExportRMIService.COLUMN_START_TIME, task.getStartTime() == null ? "" : ISO8601Utils.format(task.getStartTime()));
        taskMap.put(DataExportRMIService.COLUMN_STATUS, task.getStatus().toString());
        {
            List<DataExportWorkItem> workItems = task.getWorkItems();
            List<Map<String, Object>> lWorkItems = new ArrayList<>(workItems.size());
            for (DataExportWorkItem workItem : workItems) {
                lWorkItems.add(generateWorkItemMetadata(workItem));
            }
            taskMap.put(DataExportRMIService.COLUMN_WORK_ITEMS, lWorkItems);
        }
        {
            List<DataExportResultFile> resultFiles = task.getResultFiles();
            List<Map<String, Object>> lResultFiles = new ArrayList<>(resultFiles.size());
            for (DataExportResultFile resultFile : resultFiles) {
                lResultFiles.add(generateResultFileMetadata(resultFile));
            }
            taskMap.put(DataExportRMIService.COLUMN_RESULT_FILES, lResultFiles);
        }
        return taskMap.build();
    }

    private Map<String, Object> generateResultFileMetadata(DataExportResultFile resultFile) {
        ImmutableMap.Builder<String, Object> resultFileMap = ImmutableMap.builderWithExpectedSize(5);
        resultFileMap.put(DataExportRMIService.COLUMN_NUMBER, I(resultFile.getNumber()));
        resultFileMap.put(DataExportRMIService.COLUMN_FILE_NAME, resultFile.getFileName() == null ? "" : resultFile.getFileName());
        resultFileMap.put(DataExportRMIService.COLUMN_CONTENT_TYPE, resultFile.getContentType() == null ? "" : resultFile.getContentType());
        resultFileMap.put(DataExportRMIService.COLUMN_SIZE, L(resultFile.getSize()));
        resultFileMap.put(DataExportRMIService.COLUMN_LOCATION, resultFile.getFileStorageLocation() == null ? "" : resultFile.getFileStorageLocation());
        return resultFileMap.build();
    }

    private Map<String, Object> generateWorkItemMetadata(DataExportWorkItem workItem) {
        ImmutableMap.Builder<String, Object> workItemMap = ImmutableMap.builderWithExpectedSize(5);
        workItemMap.put(DataExportRMIService.COLUMN_ID, UUIDs.getUnformattedString(workItem.getId()));
        workItemMap.put(DataExportRMIService.COLUMN_MODULE, workItem.getModuleId());
        workItemMap.put(DataExportRMIService.COLUMN_STATUS, workItem.getStatus().toString());
        workItemMap.put(DataExportRMIService.COLUMN_INFO, workItem.getInfo() == null ? "" : workItem.getInfo().toString());
        workItemMap.put(DataExportRMIService.COLUMN_LOCATION, workItem.getFileStorageLocation() == null ? "" : workItem.getFileStorageLocation());
        return workItemMap.build();
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
