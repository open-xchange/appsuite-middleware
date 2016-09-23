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

package com.openexchange.drive.impl.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.checksum.ChecksumStore;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TempCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TempCleaner implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TempCleaner.class);
    private static final long MILLIS_PER_HOUR = 1000 * 60 * 60;
    private static final String PARAM_LAST_CLEANER_RUN = "com.openexchange.drive.lastCleanerRun";

    /**
     * Initializes and starts a new cleaner run in the background if needed.
     *
     * @param session The sync session
     */
    public static void cleanUpIfNeeded(SyncSession session) {
        Object parameter = session.getServerSession().getParameter(PARAM_LAST_CLEANER_RUN);
        if (null != parameter && Long.class.isInstance(parameter)) {
            long lastCleanerRun = ((Long)parameter).longValue();
            LOG.debug("Last cleaner run for session {} at: {}", session, DriveConstants.LOG_DATE_FORMAT.get().format(new Date(lastCleanerRun)));
            long interval = DriveConfig.getInstance().getCleanerInterval();
            if (MILLIS_PER_HOUR > interval) {
                LOG.warn("The configured interval of '{}' is smaller than the allowed minimum of one hour. Falling back to '1h' instead.", interval);
                interval = MILLIS_PER_HOUR;
            }
            if (System.currentTimeMillis() - lastCleanerRun < interval) {
                LOG.debug("Cleaner interval time of '{}' not yet exceeded, not starting new run for session {}", interval, session);
                return;
            }
        } else {
            LOG.debug("No previous cleaner run detected for session {}{}", session, '.');
        }
        try {
            String tempPath = session.getTemp().exists() ? session.getTemp().getPath(false) : null;
            final FileStorageFolder tempFolder = null != tempPath ? session.getStorage().optFolder(tempPath) : null;
            if (null == tempFolder) {
                LOG.debug("No '.drive' folder found, nothing to do.");
                return;
            }
            long maxAge = DriveConfig.getInstance().getCleanerMaxAge();
            if (MILLIS_PER_HOUR > maxAge) {
                LOG.warn("The configured maximum age of '{}' is smaller than the allowed minimum of one hour. Falling back to '1h' instead.", maxAge);
                maxAge = MILLIS_PER_HOUR;
            }
            long minimumTimestamp = System.currentTimeMillis() - maxAge;
            if (null != tempFolder.getCreationDate() &&  minimumTimestamp <= tempFolder.getCreationDate().getTime()) {
                LOG.debug("'.drive' was created within 'max age' interval, nothing to do.");
                return;
            }
            LOG.info("Starting cleaner run for session {}{}", session, '.');
            TempCleaner tempCleaner = new TempCleaner(
                session.getServerSession(), session.getChecksumStore(), tempFolder, minimumTimestamp);
            ThreadPoolService threadPoolService = DriveServiceLookup.getService(ThreadPoolService.class);
            if (null != threadPoolService) {
                try {
                    threadPoolService.getExecutor().submit(tempCleaner);
                } catch (RejectedExecutionException e) {
                    LOG.error("Unable to execute temp cleaner", e);
                    return;
                }
            } else {
                new Thread(tempCleaner).run();
            }
        } catch (OXException e) {
            LOG.error("Error starting temp cleaner", e);
        } finally {
            session.getServerSession().setParameter(PARAM_LAST_CLEANER_RUN, Long.valueOf(System.currentTimeMillis()));
        }
    }

    private static final List<Field> FILE_FIELDS = Arrays.asList(
        Field.LAST_MODIFIED, Field.CREATED, Field.ID, Field.FOLDER_ID, Field.SEQUENCE_NUMBER, Field.FILENAME);

    private final ServerSession session;
    private final ChecksumStore checksumStore;
    private final long minimumTimestamp;
    private final FileStorageFolder tempFolder;

    /**
     * Initializes a new {@link TempCleaner}.
     */
    public TempCleaner(ServerSession session, ChecksumStore checksumStore, FileStorageFolder tempFolder, long minimumTimestamp) {
        super();
        this.session = session;
        this.checksumStore = checksumStore;
        this.minimumTimestamp = minimumTimestamp;
        this.tempFolder = tempFolder;
    }

    @Override
    public void run() {
        try {
            if (null == tempFolder) {
                return; // nothing to do
            }
            boolean deleteAll = true;
            /*
             * check age of each contained file
             */
            IDBasedFileAccessFactory fileAccessFactory = DriveServiceLookup.getService(IDBasedFileAccessFactory.class, true);
            IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
            final List<File> filesToDelete = new ArrayList<File>();
            TimedResult<File> documents = fileAccess.getDocuments(tempFolder.getId(), FILE_FIELDS, null, SortDirection.DEFAULT);
            if (null == documents) {
                return;
            }
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = documents.results();
                while (searchIterator.hasNext()) {
                    File file = searchIterator.next();
                    if ((null == file.getLastModified() || minimumTimestamp > file.getLastModified().getTime()) &&
                        (null == file.getCreated() || minimumTimestamp > file.getCreated().getTime())) {
                        filesToDelete.add(file);
                    } else {
                        deleteAll = false;
                    }
                }
            } finally {
                if (null != searchIterator) {
                    searchIterator.close();
                }
            }
            /*
             * check age of each contained folder
             */
            IDBasedFolderAccessFactory folderAccessFactory = DriveServiceLookup.getService(IDBasedFolderAccessFactory.class, true);
            IDBasedFolderAccess folderAccess = folderAccessFactory.createAccess(session);
            final List<FileStorageFolder> foldersToDelete = new ArrayList<FileStorageFolder>();
            FileStorageFolder[] subfolders = folderAccess.getSubfolders(tempFolder.getId(), true);
            if (null != subfolders && 0 < subfolders.length) {
                for (FileStorageFolder subfolder : subfolders) {
                    if ((null == subfolder.getLastModifiedDate() || minimumTimestamp > subfolder.getLastModifiedDate().getTime()) &&
                        (null == subfolder.getCreationDate() || minimumTimestamp > subfolder.getCreationDate().getTime())) {
                        foldersToDelete.add(subfolder);
                    } else {
                        deleteAll = false;
                    }
                }
            }
            if (deleteAll) {
                LOG.debug("Detected all folders ({}) and files ({}) in temp folder being outdated, removing '.drive' folder completely.", foldersToDelete.size(), filesToDelete.size());
                List<FolderID> folderIDs = new ArrayList<FolderID>(1 + foldersToDelete.size());
                folderIDs.add(new FolderID(tempFolder.getId()));
                for (FileStorageFolder folder : foldersToDelete) {
                    folderIDs.add(new FolderID(folder.getId()));
                }
                folderAccess.deleteFolder(tempFolder.getId(), true);
                checksumStore.removeFileChecksumsInFolders(folderIDs);
                checksumStore.removeAllDirectoryChecksums(folderIDs);
            } else if (0 < foldersToDelete.size() || 0 < filesToDelete.size()) {
                LOG.debug("Detected {} folder(s) and {} file(s) in temp folder being outdated, cleaning up.", foldersToDelete.size(), filesToDelete.size());
                for (FileStorageFolder folder : foldersToDelete) {
                    FolderID id = new FolderID(folder.getId());
                    folderAccess.deleteFolder(folder.getId(), true);
                    checksumStore.removeFileChecksumsInFolder(id);
                    checksumStore.removeDirectoryChecksum(id);
                }
                if (0 < filesToDelete.size()) {
                    List<String> ids = new ArrayList<String>(filesToDelete.size());
                    long sequenceNumber = 0;
                    for (File file : filesToDelete) {
                        ids.add(file.getId());
                        sequenceNumber = Math.max(sequenceNumber, file.getSequenceNumber());
                    }
                    List<String> notDeleted = fileAccess.removeDocument(ids, sequenceNumber, true);
                    for (File file : filesToDelete) {
                        if (null != notDeleted && notDeleted.contains(file.getId())) {
                            continue;
                        }
                        checksumStore.removeFileChecksums(DriveUtils.getFileID(file));
                    }
                }
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("error during temp cleaner run", t);
        }
    }

}
