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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.checksum.ChecksumStore;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.drive.storage.DriveStorage;
import com.openexchange.drive.storage.StorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link TempCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TempCleaner implements Runnable {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(TempCleaner.class);
    private static final long MILLIS_PER_HOUR = 1000 * 60 * 60;
    private static final String PARAM_LAST_CLEANER_RUN = "com.openexchange.drive.lastCleanerRun";

    /**
     * Initializes and starts a new cleaner run in the background if needed.
     *
     * @param session The sync session
     */
    public static void cleanUpIfNeeded(SyncSession session) {
        ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class);
        if (null == configService) {
            LOG.warn("Unable to access config service, skipping cleanup-thread initialization.");
            return;
        }
        Object parameter = session.getServerSession().getParameter(PARAM_LAST_CLEANER_RUN);
        if (null != parameter && Long.class.isInstance(parameter)) {
            long lastCleanerRun = ((Long)parameter).longValue();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Last cleaner run for session " + session + " at: " +
                    DriveConstants.LOG_DATE_FORMAT.get().format(new Date(lastCleanerRun)));
            }
            String intervalValue = configService.getProperty("com.openexchange.drive.cleaner.interval", "1D");
            long interval = TimeSpanParser.parseTimespan(intervalValue);
            if (MILLIS_PER_HOUR > interval) {
                LOG.warn("The configured interval of '" + intervalValue +
                    "' is smaller than the allowed minimum of one hour. Falling back to '1h' instead.");
                interval = MILLIS_PER_HOUR;
            }
            if (System.currentTimeMillis() - lastCleanerRun < interval) {
                LOG.debug("Cleaner interval time of '" + intervalValue +
                    "' not yet exceeded, not starting new run for session " + session);
                return;
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No previous cleaner run detected for session " + session + '.');
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Starting cleaner run for session " + session + '.');
            try {
                String maxAgeValue = configService.getProperty("com.openexchange.drive.cleaner.maxAge", "1D");
                long maxAge = TimeSpanParser.parseTimespan(maxAgeValue);
                if (MILLIS_PER_HOUR > maxAge) {
                    LOG.warn("The configured maximum age of '" + maxAgeValue +
                        "' is smaller than the allowed minimum of one hour. Falling back to '1h' instead.");
                    maxAge = MILLIS_PER_HOUR;
                }
                TempCleaner tempCleaner = new TempCleaner(new DriveStorage(session), session.getChecksumStore(), maxAge);
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
    }

    private static final List<Field> FILE_FIELDS = Arrays.asList(
        Field.LAST_MODIFIED, Field.CREATED, Field.ID, Field.FOLDER_ID, Field.SEQUENCE_NUMBER, Field.FILENAME);
    private final DriveStorage storage;
    private final ChecksumStore checksumStore;
    private final long minimumTimestamp;

    /**
     * Initializes a new {@link TempCleaner}.
     */
    public TempCleaner(DriveStorage storage, ChecksumStore checksumStore, long maxAge) {
        super();
        this.storage = storage;
        this.checksumStore = checksumStore;
        this.minimumTimestamp = System.currentTimeMillis() - maxAge;
    }

    @Override
    public void run() {
        //TODO: direct file- / folder-access
        try {
            final FileStorageFolder tempFolder = storage.optFolder(DriveConstants.TEMP_PATH, false);
            if (null == tempFolder) {
                return; // nothing to do
            }
            /*
             * check age of each contained file
             */
            boolean deleteAll = true;
            final List<File> filesToDelete = new ArrayList<File>();
            List<File> files = storage.getFilesInFolder(tempFolder.getId(), true, null, FILE_FIELDS);
            if (null != files && 0 < files.size()) {
                for (File file : files) {
                    if ((null == file.getLastModified() || minimumTimestamp > file.getLastModified().getTime()) &&
                        (null == file.getCreated() || minimumTimestamp > file.getCreated().getTime())) {
                        filesToDelete.add(file);
                    } else {
                        deleteAll = false;
                    }
                }
            }
            /*
             * check age of each contained folder
             */
            final List<FileStorageFolder> foldersToDelete = new ArrayList<FileStorageFolder>();
            FileStorageFolder[] subfolders = storage.getFolderAccess().getSubfolders(tempFolder.getId(), true);
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
                /*
                 * cleanup whole '.drive' folder, invalidate checksums
                 */
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Detected all folders (" + foldersToDelete.size() + ") and files (" + filesToDelete.size() +
                        ") in temp folder being outdated, removing '.drive' folder completely.");
                }
                storage.wrapInTransaction(new StorageOperation<Void>() {

                    @Override
                    public Void call() throws OXException {
                        String folderID = storage.getFolderAccess().deleteFolder(tempFolder.getId());
                        checksumStore.removeFileChecksumsInFolder(new FolderID(folderID));
                        for (FileStorageFolder folder : foldersToDelete) {
                            FolderID id = new FolderID(folder.getId());
                            checksumStore.removeFileChecksumsInFolder(id);
                            checksumStore.removeDirectoryChecksum(id);
                        }
                        return null;
                    }
                });
            } else {
                /*
                 * cleanup selected files and folders, invalidate checksums
                 */
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Detected " + foldersToDelete.size() + " folder(s) and " + filesToDelete.size() +
                        " file(s) in temp folder being outdated, cleaning up.");
                }
                storage.wrapInTransaction(new StorageOperation<Void>() {

                    @Override
                    public Void call() throws OXException {
                        for (FileStorageFolder folder : foldersToDelete) {
                            FolderID id = new FolderID(folder.getId());
                            checksumStore.removeFileChecksumsInFolder(id);
                            checksumStore.removeDirectoryChecksum(id);
                        }
                        for (File file : filesToDelete) {
                            storage.deleteFile(file);
                            FileID fileID = new FileID(file.getId());
                            FolderID folderID = new FolderID(file.getFolderId());
                            if (null == fileID.getFolderId()) {
                                // TODO: check
                                fileID.setFolderId(folderID.getFolderId());
                            }
                            checksumStore.removeFileChecksums(fileID);
                        }
                        return null;
                    }
                });
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("error during temp cleaner run", t);
        }
    }

}
