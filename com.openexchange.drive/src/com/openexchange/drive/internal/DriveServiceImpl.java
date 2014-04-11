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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryMetadata;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveClientVersion;
import com.openexchange.drive.DriveConstants;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.DriveFileMetadata;
import com.openexchange.drive.DriveQuota;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveSettings;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.drive.actions.AbstractFileAction;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DownloadFileAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.actions.ErrorDirectoryAction;
import com.openexchange.drive.actions.ErrorFileAction;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.checksum.DirectoryChecksum;
import com.openexchange.drive.checksum.FileChecksum;
import com.openexchange.drive.comparison.Change;
import com.openexchange.drive.comparison.DirectoryVersionMapper;
import com.openexchange.drive.comparison.FileVersionMapper;
import com.openexchange.drive.comparison.ServerDirectoryVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.internal.tracking.SyncTracker;
import com.openexchange.drive.management.DriveConfig;
import com.openexchange.drive.storage.execute.DirectoryActionExecutor;
import com.openexchange.drive.storage.execute.FileActionExecutor;
import com.openexchange.drive.sync.DefaultSyncResult;
import com.openexchange.drive.sync.IntermediateSyncResult;
import com.openexchange.drive.sync.RenameTools;
import com.openexchange.drive.sync.SimpleFileVersion;
import com.openexchange.drive.sync.Synchronizer;
import com.openexchange.drive.sync.optimize.OptimizingDirectorySynchronizer;
import com.openexchange.drive.sync.optimize.OptimizingFileSynchronizer;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.composition.FolderID;

/**
 * {@link DriveServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveServiceImpl implements DriveService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveServiceImpl.class);

    /**
     * Initializes a new {@link DriveServiceImpl}.
     */
    public DriveServiceImpl() {
        super();
        LOG.debug("initialized.");
    }

    @Override
    public SyncResult<DirectoryVersion> syncFolders(DriveSession session, List<DirectoryVersion> originalVersions,
        List<DirectoryVersion> clientVersions) throws OXException {
        /*
         * check (hard) version restrictions
         */
        if (session.getApiVersion() < DriveConfig.getInstance().getMinApiVersion()) {
            OXException error = DriveExceptionCodes.CLIENT_OUTDATED.create();
            LOG.warn("Client synchronization aborted for " + session, error);
            List<AbstractAction<DirectoryVersion>> actionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(1);
            actionsForClient.add(new ErrorDirectoryAction(null, null, null, error, false, true));
            return new DefaultSyncResult<DirectoryVersion>(actionsForClient, error.getLogMessage());
        }
        DriveClientVersion clientVersion = session.getClientVersion();
        if (null != clientVersion) {
            DriveClientVersion hardVersionLimit = DriveConfig.getInstance().getHardMinimumVersion(session.getClientType());
            if (0 > clientVersion.compareTo(hardVersionLimit)) {
                OXException error = DriveExceptionCodes.CLIENT_VERSION_OUTDATED.create(clientVersion, hardVersionLimit);
                LOG.warn("Client synchronization aborted for " + session, error);
                List<AbstractAction<DirectoryVersion>> actionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(1);
                actionsForClient.add(new ErrorDirectoryAction(null, null, null, error, false, true));
                return new DefaultSyncResult<DirectoryVersion>(actionsForClient, error.getLogMessage());
            }
        }
        /*
         * sync folders
         */
        long start = System.currentTimeMillis();
        DriveVersionValidator.validateDirectoryVersions(originalVersions);
        DriveVersionValidator.validateDirectoryVersions(clientVersions);
        int retryCount = 0;
        while (true) {
            /*
             * sync
             */
            final SyncSession driveSession = new SyncSession(session);
            IntermediateSyncResult<DirectoryVersion> syncResult = syncDirectories(
                driveSession, originalVersions, clientVersions, driveSession.getServerDirectories());
            /*
             * track & check sync result for cycles
             */
            if (0 == retryCount) {
                syncResult = new SyncTracker(driveSession).trackAndCheck(syncResult);
            }
            try {
                /*
                 * execute actions on server
                 */
                DirectoryActionExecutor executor = new DirectoryActionExecutor(driveSession, true, retryCount < DriveConstants.MAX_RETRIES);
                executor.execute(syncResult.getActionsForServer());
            } catch (OXException e) {
                if (0 == retryCount || (tryAgain(e) && retryCount <= DriveConstants.MAX_RETRIES)) {
                    retryCount++;
                    int delay = DriveConstants.RETRY_BASEDELAY * retryCount;
                    driveSession.trace("Got exception during execution of server actions (" + e.getMessage() + "), trying again in " +
                        delay + "ms (" + retryCount + '/' + DriveConstants.MAX_RETRIES + ")...");
                    delay(delay);
                    continue;
                }
                driveSession.trace("Got exception during execution of server actions (" + e.getMessage() + ")");
                LOG.warn("Got exception during execution of server actions\nPrevious sync result:\n{}", syncResult, e);
                throw e;
            }
            /*
             * start cleaner run if applicable
             */
            if (syncResult.isEmpty()) {
                TempCleaner.cleanUpIfNeeded(driveSession);
            }
            /*
             * check (soft) version restrictions
             */
            if (null != clientVersion) {
                DriveClientVersion softVersionLimit = DriveConfig.getInstance().getSoftMinimumVersion(session.getClientType());
                if (0 > clientVersion.compareTo(softVersionLimit)) {
                    OXException error = DriveExceptionCodes.CLIENT_VERSION_UPDATE_AVAILABLE.create(clientVersion, softVersionLimit);
                    LOG.trace("Client upgrade available for " + session, error);
                    syncResult.addActionForClient(new ErrorDirectoryAction(null, null, null, error, false, false));
                }
            }
            /*
             * return actions for client
             */
            if (driveSession.isTraceEnabled()) {
                driveSession.trace("syncFolders completed after " + (System.currentTimeMillis() - start) + "ms.");
            }
            return new DefaultSyncResult<DirectoryVersion>(syncResult.getActionsForClient(), driveSession.getDiagnosticsLog());
        }
    }

    @Override
    public SyncResult<FileVersion> syncFiles(DriveSession session, final String path, List<FileVersion> originalVersions, List<FileVersion> clientVersions) throws OXException {
        long start = System.currentTimeMillis();
        DriveVersionValidator.validateFileVersions(originalVersions);
        DriveVersionValidator.validateFileVersions(clientVersions);
        int retryCount = 0;
        while (true) {
            /*
             * sync
             */
            final SyncSession driveSession = new SyncSession(session);
            driveSession.getStorage().createFolder(path);
            IntermediateSyncResult<FileVersion> syncResult = syncFiles(
                driveSession, path, originalVersions, clientVersions, driveSession.getServerFiles(path));
            /*
             * track sync result
             */
            if (0 == retryCount) {
                syncResult = new SyncTracker(driveSession).track(syncResult, path);
            }
            try {
                /*
                 * execute actions on server
                 */
                FileActionExecutor executor = new FileActionExecutor(driveSession, true, retryCount < DriveConstants.MAX_RETRIES, path);
                executor.execute(syncResult.getActionsForServer());
            } catch (OXException e) {
                if (0 == retryCount || (tryAgain(e) && retryCount <= DriveConstants.MAX_RETRIES)) {
                    retryCount++;
                    int delay = DriveConstants.RETRY_BASEDELAY * retryCount;
                    driveSession.trace("Got exception during execution of server actions (" + e.getMessage() + "), trying again in " +
                        delay + "ms (" + retryCount + '/' + DriveConstants.MAX_RETRIES + ")...");
                    delay(delay);
                    continue;
                }
                throw e;
            }
            /*
             * return actions for client
             */
            if (driveSession.isTraceEnabled()) {
                driveSession.trace("syncFiles completed after " + (System.currentTimeMillis() - start) + "ms.");
            }
            return new DefaultSyncResult<FileVersion>(syncResult.getActionsForClient(), driveSession.getDiagnosticsLog());
        }
    }

    @Override
    public IFileHolder download(DriveSession session, String path, FileVersion fileVersion, long offset, long length) throws OXException {
        DriveVersionValidator.validateFileVersion(fileVersion);
        SyncSession driveSession = new SyncSession(session);
        LOG.debug("Handling download: file version: {}, offset: {}, length: {}", fileVersion, offset, length);
        /*
         * track sync result to represent the download as performed by client
         */
        AbstractAction<FileVersion> action = new AbstractFileAction(null, fileVersion, null) {

            @Override
            public Action getAction() {
                return Action.DOWNLOAD;
            }
        };
        action.getParameters().put(DriveAction.PARAMETER_OFFSET, Long.valueOf(offset));
        action.getParameters().put(DriveAction.PARAMETER_LENGTH, Long.valueOf(length));
        new SyncTracker(driveSession).track(new IntermediateSyncResult<FileVersion>(
            Collections.<AbstractAction<FileVersion>>emptyList(), Collections.<AbstractAction<FileVersion>>singletonList(action)), path);
        /*
         * return file holder for download
         */
        return new DownloadHelper(driveSession).perform(path, fileVersion, offset, length);
    }

    @Override
    public SyncResult<FileVersion> upload(DriveSession session, String path, InputStream uploadStream, FileVersion originalVersion,
        FileVersion newVersion, String contentType, long offset, long totalLength, Date created, Date modified) throws OXException {
        DriveVersionValidator.validateFileVersion(newVersion);
        if (null != originalVersion) {
            DriveVersionValidator.validateFileVersion(originalVersion);
        }
        SyncSession driveSession = new SyncSession(session);
        if (driveSession.isTraceEnabled()) {
            driveSession.trace("Handling upload: original version: " + originalVersion + ", new version: " + newVersion +
                ", offset: " + offset + ", total length: " + totalLength +
                ", created: " + (null != created ? DriveConstants.LOG_DATE_FORMAT.get().format(created) : "") +
                ", modified: " + (null != modified ? DriveConstants.LOG_DATE_FORMAT.get().format(modified) : ""));
        }
        IntermediateSyncResult<FileVersion> syncResult = new IntermediateSyncResult<FileVersion>();
        File createdFile = null;
        try {
            createdFile = new UploadHelper(driveSession).perform(path, originalVersion, newVersion, uploadStream, contentType, offset, totalLength, created, modified);
        } catch (OXException e) {
            LOG.warn("Got exception during upload ({})\nSession: {}, path: {}, original version: {}, new version: {}, offset: {}, total length: {}",
                e.getMessage(), driveSession, path, originalVersion, newVersion, offset, totalLength);
            if ("FLS-0024".equals(e.getErrorCode()) || FileStorageExceptionCodes.QUOTA_REACHED.equals(e) ||
                "SMARTDRIVEFILE_STORAGE-0008".equals(e.getErrorCode())) {
                /*
                 * quota reached
                 */
                OXException quotaException = DriveExceptionCodes.QUOTA_REACHED.create(e, (Object[])null);
                if (null != originalVersion) {
                    /*
                     * upload should have replaced an existing file, let client first rename it's file and mark as error with quarantine flag...
                     */
                    String alternativeName = RenameTools.findRandomAlternativeName(originalVersion.getName());
                    FileVersion renamedVersion = new SimpleFileVersion(alternativeName, originalVersion.getChecksum());
                    syncResult.addActionForClient(new EditFileAction(newVersion, renamedVersion, null, path));
                    syncResult.addActionForClient(new ErrorFileAction(newVersion, renamedVersion, null, path, quotaException, true));
                    /*
                     * ... then download the server version afterwards
                     */
                    ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(originalVersion, path, driveSession);
                    syncResult.addActionForClient(new DownloadFileAction(driveSession, null, serverFileVersion, null, path));
                } else {
                    /*
                     * upload of new file, mark as error with quarantine flag
                     */
                    syncResult.addActionForClient(new ErrorFileAction(null, newVersion, null, path, quotaException, true));
                }
            } else if ("IFO-0100".equals(e.getErrorCode())) {
                /*
                 * database fields (filename/title/comment?) too long - put into quarantine to prevent repeated errors
                 */
                syncResult.addActionForClient(new ErrorFileAction(null, newVersion, null, path, e, true));
            } else {
                throw e;
            }
        }
        if (null != createdFile) {
            /*
             * store checksum, invalidate parent directory checksum
             */
            FileChecksum fileChecksum = driveSession.getChecksumStore().insertFileChecksum(new FileChecksum(
                IDUtil.getFileID(createdFile), createdFile.getVersion(), createdFile.getSequenceNumber(), newVersion.getChecksum()));
            driveSession.getChecksumStore().removeDirectoryChecksum(new FolderID(createdFile.getFolderId()));
            /*
             * check if created file still equals uploaded one
             */
            ServerFileVersion createdVersion = new ServerFileVersion(createdFile, fileChecksum);
            if (newVersion.getName().equals(createdFile.getFileName())) {
                syncResult.addActionForClient(new AcknowledgeFileAction(driveSession, originalVersion, createdVersion, null, path));
            } else {
                syncResult.addActionForClient(new EditFileAction(newVersion, createdVersion, null, path));
            }
        }
        if (driveSession.isTraceEnabled()) {
            driveSession.trace(syncResult);
        }
        /*
         * track & return sync result
         */
        syncResult = new SyncTracker(driveSession).track(syncResult, path);
        return new DefaultSyncResult<FileVersion>(syncResult.getActionsForClient(), driveSession.getDiagnosticsLog());
    }

    @Override
    public DriveQuota getQuota(DriveSession session) throws OXException {
        return getSettings(session).getQuota();
    }

    @Override
    public DriveSettings getSettings(DriveSession session) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        LOG.debug("Handling get-settings for '{}'", session);
        DriveSettings settings = new DriveSettings();
        Quota[] quota = syncSession.getStorage().getQuota();
        LOG.debug("Got quota for root folder '{}': {}", session.getRootFolderID(), quota);
        settings.setQuota(new DriveQuotaImpl(quota, syncSession.getLinkGenerator().getQuotaLink()));
        settings.setHelpLink(syncSession.getLinkGenerator().getHelpLink());
        settings.setServerVersion(com.openexchange.version.Version.getInstance().getVersionString());
        return settings;
    }

    @Override
    public List<DriveFileMetadata> getFileMetadata(DriveSession session, String path, List<FileVersion> fileVersions, List<DriveFileField> fields) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        if (null == fileVersions) {
            return DriveMetadataFactory.getFileMetadata(syncSession, syncSession.getServerFiles(path), fields);
        } else if (1 == fileVersions.size()) {
            ServerFileVersion serverFile = ServerFileVersion.valueOf(fileVersions.get(0), path, syncSession);
            return Collections.singletonList(DriveMetadataFactory.getFileMetadata(syncSession, serverFile, fields));
        } else {
            List<DriveFileMetadata> metadata = new ArrayList<DriveFileMetadata>(fileVersions.size());
            List<ServerFileVersion> serverFiles = syncSession.getServerFiles(path);
            for (FileVersion requestedVersion : fileVersions) {
                ServerFileVersion matchingVersion = null;
                for (ServerFileVersion serverFileVersion : serverFiles) {
                    if (Change.NONE.equals(Change.get(serverFileVersion, requestedVersion))) {
                        matchingVersion = serverFileVersion;
                        break;
                    }
                }
                if (null == matchingVersion) {
                    throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(requestedVersion.getName(), requestedVersion.getChecksum(), path);
                }
                metadata.add(DriveMetadataFactory.getFileMetadata(syncSession, matchingVersion, fields));
            }
            return metadata;
        }
    }

    @Override
    public DirectoryMetadata getDirectoryMetadata(DriveSession session, String path) throws OXException {
        SyncSession driveSession = new SyncSession(session);
        String folderID = driveSession.getStorage().getFolderID(path);
        List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(driveSession, Arrays.asList(new String[] { folderID }));
        if (null == checksums || 0 == checksums.size()) {
            throw DriveExceptionCodes.PATH_NOT_FOUND.create(path);
        }
        return new DefaultDirectoryMetadata(driveSession, new ServerDirectoryVersion(path, checksums.get(0)));
    }

    private static IntermediateSyncResult<DirectoryVersion> syncDirectories(SyncSession session, List<? extends DirectoryVersion> originalVersions,
        List<? extends DirectoryVersion> clientVersions, List<? extends DirectoryVersion> serverVersions) throws OXException {
        /*
         * map directories
         */
        DirectoryVersionMapper mapper = new DirectoryVersionMapper(originalVersions, clientVersions, serverVersions);
        if (session.isTraceEnabled()) {
            StringBuilder allocator = new StringBuilder("Directory versions mapped to:\n");
            allocator.append(mapper).append('\n');
            session.trace(allocator);
        }
        /*
         * determine sync actions
         */
        Synchronizer<DirectoryVersion> synchronizer = new OptimizingDirectorySynchronizer(session, mapper);
        IntermediateSyncResult<DirectoryVersion> syncResult = synchronizer.sync();
        if (session.isTraceEnabled()) {
            session.trace(syncResult);
        }
        return syncResult;
    }

    private static IntermediateSyncResult<FileVersion> syncFiles(SyncSession session, String path, List<? extends FileVersion> originalVersions,
        List<? extends FileVersion> clientVersions, List<? extends FileVersion> serverVersions) throws OXException {
        /*
         * map files
         */
        FileVersionMapper mapper = new FileVersionMapper(originalVersions, clientVersions, serverVersions);
        if (session.isTraceEnabled()) {
            StringBuilder allocator = new StringBuilder("File versions in directory " + path + " mapped to:\n");
            allocator.append(mapper).append('\n');
            session.trace(allocator);
        }
        /*
         * determine sync actions
         */
        Synchronizer<FileVersion> synchronizer = new OptimizingFileSynchronizer(session, mapper, path);
        IntermediateSyncResult<FileVersion> syncResult = synchronizer.sync();
        if (session.isTraceEnabled()) {
            session.trace(syncResult);
        }
        return syncResult;
    }

    private static boolean tryAgain(OXException e) {
        if (null == e) {
            return false;
        }
        return
            Category.CATEGORY_TRY_AGAIN.equals(e.getCategory()) ||
            Category.CATEGORY_CONFLICT.equals(e.getCategory()) ||
            "FLD-0008".equals(e.getErrorCode()) || // 'Folder 123 does not exist in context 1'
            "DRV-0007".equals(e.getErrorCode()) // The file "123.txt" with checksum "8fc1a2f5e9a2dbd1d5f4f9e330bd1563" was not found at "/"
        ;
    }

    private static void delay(long millis) throws OXException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OXException(e);
        }
    }

}
