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
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryMetadata;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveQuota;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.FileMetadata;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.drive.actions.AbstractFileAction;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DownloadFileAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.actions.ErrorFileAction;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.checksum.DirectoryChecksum;
import com.openexchange.drive.checksum.FileChecksum;
import com.openexchange.drive.comparison.DirectoryVersionMapper;
import com.openexchange.drive.comparison.FileVersionMapper;
import com.openexchange.drive.comparison.ServerDirectoryVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.internal.tracking.SyncTracker;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.drive.storage.StorageOperation;
import com.openexchange.drive.sync.DefaultSyncResult;
import com.openexchange.drive.sync.IntermediateSyncResult;
import com.openexchange.drive.sync.RenameTools;
import com.openexchange.drive.sync.SimpleFileVersion;
import com.openexchange.drive.sync.Synchronizer;
import com.openexchange.drive.sync.optimize.OptimizingDirectorySynchronizer;
import com.openexchange.drive.sync.optimize.OptimizingFileSynchronizer;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;

/**
 * {@link DriveServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveServiceImpl implements DriveService {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(DriveServiceImpl.class);

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
                driveSession, originalVersions, clientVersions, getServerDirectories(driveSession));
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
                for (final DriveAction<DirectoryVersion> action : syncResult.getActionsForServer()) {

                    driveSession.getStorage().wrapInTransaction(new StorageOperation<Void>() {

                        @Override
                        public Void call() throws OXException {
                            execute(driveSession, action);
                            return null;
                        }
                    });
                }
            } catch (OXException e) {
                if (tryAgain(e) && retryCount <= DriveConstants.MAX_RETRIES) {
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
             * start cleaner run if applicable
             */
            if (syncResult.isEmpty()) {
                TempCleaner.cleanUpIfNeeded(driveSession);
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
                driveSession, path, originalVersions, clientVersions, getServerFiles(driveSession, path));
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
                for (final DriveAction<FileVersion> action : syncResult.getActionsForServer()) {

                    driveSession.getStorage().wrapInTransaction(new StorageOperation<Void>() {

                        @Override
                        public Void call() throws OXException {
                            execute(driveSession, path, action);
                            return null;
                        }
                    });
                }
            } catch (OXException e) {
                if (tryAgain(e) && retryCount <= DriveConstants.MAX_RETRIES) {
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling download: file version: " + fileVersion + ", offset: " + offset + ", length: " + length);
        }
        IFileHolder fileHolder = new DownloadHelper(driveSession).perform(path, fileVersion, offset, length);
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
        List<AbstractAction<FileVersion>> actionsForServer = Collections.emptyList();
        List<AbstractAction<FileVersion>> actionsForClient = Collections.singletonList(action);
        new SyncTracker(driveSession).track(new IntermediateSyncResult<FileVersion>(actionsForServer, actionsForClient), path);
        /*
         * return file holder for download
         */
        return fileHolder;
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
                ", offset: " + offset + ", total length: " + totalLength + ", created: " + created + ", modified: " + modified);
        }
        IntermediateSyncResult<FileVersion> syncResult = new IntermediateSyncResult<FileVersion>();
        File createdFile = null;
        try {
            createdFile = new UploadHelper(driveSession).perform(path, originalVersion, newVersion, uploadStream, contentType, offset, totalLength, created, modified);
        } catch (OXException e) {
            if ("FLS-0024".equals(e.getErrorCode())) {
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
                    File serverFile = ServerFileVersion.valueOf(originalVersion, path, driveSession).getFile();
                    syncResult.addActionForClient(new DownloadFileAction(null, originalVersion, null, path, serverFile));
                } else {
                    /*
                     * upload of new file, mark as error with quarantine flag
                     */
                    syncResult.addActionForClient(new ErrorFileAction(null, newVersion, null, path, quotaException, true));
                }
            } else {
                throw e;
            }
        }
        if (null != createdFile) {
            /*
             * store checksum, invalidate parent directory checksum
             */
            FileID fileID = new FileID(createdFile.getId());
            FolderID folderID = new FolderID(createdFile.getFolderId());
            if (null == fileID.getFolderId()) {
                // TODO: check
                fileID.setFolderId(folderID.getFolderId());
            }
            FileChecksum fileChecksum = driveSession.getChecksumStore().insertFileChecksum(
                fileID, createdFile.getVersion(), createdFile.getSequenceNumber(), newVersion.getChecksum());
            driveSession.getChecksumStore().removeDirectoryChecksum(folderID);
            /*
             * check if created file still equals uploaded one
             */
            FileVersion createdVersion = new ServerFileVersion(createdFile, fileChecksum);
            if (newVersion.getName().equals(createdVersion.getName())) {
                syncResult.addActionForClient(new AcknowledgeFileAction(originalVersion, createdVersion, null, path, createdFile));
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
        final SyncSession driveSession = new SyncSession(session);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling get-quota for root folder '" + session.getRootFolderID() + "'");
        }
        final Quota[] quota = driveSession.getStorage().getQuota();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got quota for root folder '" + session.getRootFolderID() + "': " + quota);
        }
        final String manageLink = new DirectLinkGenerator(driveSession).getQuotaLink();
        return new DriveQuota() {

            @Override
            public Quota[] getQuota() {
                return quota;
            }

            @Override
            public String getManageLink() {
                return manageLink;
            }
        };
    }

    @Override
    public List<FileMetadata> getFileMetadata(DriveSession session, String path, List<FileVersion> fileVersions) throws OXException {
        SyncSession driveSession = new SyncSession(session);
        List<FileMetadata> fileMetadata = new ArrayList<FileMetadata>();
        if (null == fileVersions) {
            List<ServerFileVersion> serverFiles = getServerFiles(driveSession, path);
            for (ServerFileVersion fileVersion : serverFiles) {
                fileMetadata.add(new DefaultFileMetadata(driveSession, fileVersion));
            }
        } else if (1 == fileVersions.size()) {
            ServerFileVersion fileVersion = ServerFileVersion.valueOf(fileVersions.get(0), path, driveSession);
            fileMetadata.add(new DefaultFileMetadata(driveSession, fileVersion));
        } else {
            List<ServerFileVersion> serverFiles = getServerFiles(driveSession, path);
            for (FileVersion requestedVersion : fileVersions) {
                ServerFileVersion matchingVersion = null;
                for (ServerFileVersion serverFileVersion : serverFiles) {
                    if (serverFileVersion.getName().equals(requestedVersion.getName()) &&
                        serverFileVersion.getChecksum().equals(requestedVersion.getChecksum())) {
                        matchingVersion = serverFileVersion;
                        break;
                    }
                }
                if (null == matchingVersion) {
                    throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(requestedVersion.getName(), requestedVersion.getChecksum(), path);
                }
                fileMetadata.add(new DefaultFileMetadata(driveSession, matchingVersion));
            }
        }
        return fileMetadata;
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
            StringAllocator allocator = new StringAllocator("Directory versions mapped to:\n");
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
            StringAllocator allocator = new StringAllocator("File versions in directory " + path + " mapped to:\n");
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

    private static void execute(SyncSession session, DriveAction<DirectoryVersion> action) throws OXException {
        switch (action.getAction()) {
        case EDIT:
            /*
             * check for new, empty folder tht simply can be created on server
             */
            if (null == action.getNewVersion() && null != action.getVersion() &&
                DriveConstants.EMPTY_MD5.equals(action.getVersion().getChecksum())) {
                session.getStorage().getFolderID(action.getVersion().getPath(), true);
                return;
            }
            /*
             * edit folder name and/or path
             */
            String folderID = session.getStorage().getFolderID(action.getVersion().getPath());
            String newFolderID = session.getStorage().moveFolder(action.getVersion().getPath(), action.getNewVersion().getPath());
            /*
             * update stored checksums if needed
             */
            if (false == folderID.equals(newFolderID)) {
                session.getChecksumStore().updateFileChecksumFolders(new FolderID(folderID), new FolderID(newFolderID));
                session.getChecksumStore().updateDirectoryChecksumFolder(new FolderID(folderID), new FolderID(newFolderID));
            }
            break;
        case REMOVE:
            if (DriveConstants.EMPTY_MD5.equals(action.getVersion().getChecksum())) {
                /*
                 * just delete empty directory
                 */
                folderID = session.getStorage().deleteFolder(action.getVersion().getPath());
                session.getChecksumStore().removeDirectoryChecksum(new FolderID(folderID));
            } else if (session.hasTempFolder()) {
                /*
                 * move to temp
                 */
                FileStoragePermission sourceFolderPermission = session.getStorage().getOwnPermission(action.getVersion().getPath());
                FileStoragePermission targetFolderPermission = session.getStorage().getOwnPermission(DriveConstants.TEMP_PATH);
                if (FileStoragePermission.CREATE_SUB_FOLDERS <= targetFolderPermission.getDeletePermission() &&
                    FileStoragePermission.MAX_PERMISSION <= sourceFolderPermission.getFolderPermission()) {
                    /*
                     * try to move whole directory to temp folder
                     */
                    String targetPath = DriveConstants.TEMP_PATH + '/' + action.getVersion().getChecksum();
                    FileStorageFolder targetFolder = session.getStorage().optFolder(targetPath, false);
                    if (null == targetFolder) {
                        String currentFolderID = session.getStorage().getFolderID(action.getVersion().getPath());
                        String movedFolderID = session.getStorage().moveFolder(action.getVersion().getPath(), targetPath);
                        /*
                         * update stored checksums if needed
                         */
                        if (false == currentFolderID.equals(movedFolderID)) {
                            session.getChecksumStore().updateFileChecksumFolders(new FolderID(currentFolderID), new FolderID(movedFolderID));
                            session.getChecksumStore().updateDirectoryChecksumFolder(new FolderID(currentFolderID), new FolderID(movedFolderID));
                        }
                    }
                } else {
                    /*
                     * no permissions to move whole directory, try and preserve at least each file separately
                     */
                    List<FileChecksum> checksumsToUpdate = new ArrayList<FileChecksum>();
                    List<FileChecksum> checksumsToRemove = new ArrayList<FileChecksum>();
                    List<File> filesToRemove = new ArrayList<File>();
                    for (ServerFileVersion versionToRemove : getServerFiles(session, action.getVersion().getPath())) {
                        FileChecksum fileChecksum = versionToRemove.getFileChecksum();
                        File removedFile = session.getStorage().moveFile(
                            versionToRemove.getFile(), versionToRemove.getChecksum(), DriveConstants.TEMP_PATH);
                        if (versionToRemove.getChecksum().equals(removedFile.getFileName())) {
                            // moved successfully, update checksum
                            FileID removedFileID = new FileID(removedFile.getId());
                            FolderID removedFolderID = new FolderID(removedFile.getFolderId());
                            if (null == removedFileID.getFolderId()) {
                                // TODO: check
                                removedFileID.setFolderId(removedFolderID.getFolderId());
                            }
                            fileChecksum.setFileID(removedFileID);
                            fileChecksum.setVersion(removedFile.getVersion());
                            fileChecksum.setSequenceNumber(removedFile.getSequenceNumber());
                            checksumsToUpdate.add(fileChecksum);
                        } else {
                            // file already in trash, cleanup
                            checksumsToRemove.add(fileChecksum);
                            filesToRemove.add(removedFile);
                        }
                    }
                    /*
                     * update checksums, cleanup
                     */
                    if (0 < checksumsToUpdate.size()) {
                        session.getChecksumStore().updateFileChecksums(checksumsToUpdate);
                    }
                    if (0 < checksumsToRemove.size()) {
                        session.getChecksumStore().removeFileChecksums(checksumsToRemove);
                    }
                    if (0 < filesToRemove.size()) {
                        long sequenceNumber = 0;
                        List<String> ids = new ArrayList<String>(filesToRemove.size());
                        for (File file : filesToRemove) {
                            sequenceNumber = Math.max(sequenceNumber, file.getSequenceNumber());
                            ids.add(file.getId());
                        }
                        session.getStorage().getFileAccess().removeDocument(ids, sequenceNumber);
                    }
                    /*
                     * delete (empty) directory
                     */
                    folderID = session.getStorage().deleteFolder(action.getVersion().getPath());
                    session.getChecksumStore().removeDirectoryChecksum(new FolderID(folderID));
                }
            } else {
                /*
                 * no temp folder available, hard-delete directory + contents
                 */
                folderID = session.getStorage().deleteFolder(action.getVersion().getPath());
                session.getChecksumStore().removeDirectoryChecksum(new FolderID(folderID));
                session.getChecksumStore().removeFileChecksumsInFolder(new FolderID(folderID));
            }
            break;
        case SYNC:
            if (Boolean.TRUE.equals(action.getParameters().get(DriveAction.PARAMETER_RESET))) {
                if (null == action.getVersion()) {
                    /*
                     * Clear all stored file- and directory-checksums of all folders
                     */
                    for (Entry<String, FileStorageFolder> entry : session.getStorage().getFolders().entrySet()) {
                        FolderID id = new FolderID(entry.getValue().getId());
                        session.getChecksumStore().removeDirectoryChecksum(id);
                        session.getChecksumStore().removeFileChecksumsInFolder(id);
                    }
                } else {
                    /*
                     * Clear all stored file- and directory-checksums of referenced folder
                     */
                    FileStorageFolder folder = session.getStorage().optFolder(action.getVersion().getPath(), false);
                    if (null != folder) {
                        FolderID id = new FolderID(folder.getId());
                        session.getChecksumStore().removeDirectoryChecksum(id);
                        session.getChecksumStore().removeFileChecksumsInFolder(id);
                    }
                }
            } else {
                throw new IllegalStateException("Can't perform action " + action + " on server");
            }
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private static void execute(SyncSession session, String path, DriveAction<FileVersion> action) throws OXException {
        switch (action.getAction()) {
        case REMOVE:
            /*
             * move to temp folder
             */
            ServerFileVersion versionToRemove = ServerFileVersion.valueOf(action.getVersion(), path, session);
            FileChecksum fileChecksum = versionToRemove.getFileChecksum();
            if (DriveConstants.EMPTY_MD5.equals(fileChecksum.getChecksum())) {
                // don't preserve empty files
                session.getStorage().deleteFile(versionToRemove.getFile());
                session.getChecksumStore().removeFileChecksum(fileChecksum);
            } else {
                File removedFile = session.getStorage().moveFile(
                    versionToRemove.getFile(), versionToRemove.getChecksum(), DriveConstants.TEMP_PATH);
                if (versionToRemove.getChecksum().equals(removedFile.getFileName())) {
                    // moved successfully, update checksum
                    FileID fileID = new FileID(removedFile.getId());
                    FolderID folderID = new FolderID(removedFile.getFolderId());
                    if (null == fileID.getFolderId()) {
                        // TODO: check
                        fileID.setFolderId(folderID.getFolderId());
                    }
                    fileChecksum.setFileID(fileID);
                    fileChecksum.setVersion(removedFile.getVersion());
                    fileChecksum.setSequenceNumber(removedFile.getSequenceNumber());
                    session.getChecksumStore().updateFileChecksum(fileChecksum);
                } else {
                    // file already in trash, cleanup
                    session.getStorage().deleteFile(removedFile);
                    session.getChecksumStore().removeFileChecksum(fileChecksum);
                }
            }
            break;
        case DOWNLOAD:
            /*
             * check for empty file that simply can be 'touched'
             */
            if (null == action.getVersion() && DriveConstants.EMPTY_MD5.equals(action.getNewVersion().getChecksum())) {
                File metadata = new DefaultFile();
                metadata.setFileSize(0);
                metadata.setFileMD5Sum(DriveConstants.EMPTY_MD5);
                metadata.setVersion("1");
                metadata.setVersionComment(session.getStorage().getVersionComment());
                InputStream data = new UnsynchronizedByteArrayInputStream(new byte[0]);
                File createdFile = session.getStorage().createFile(path, action.getNewVersion().getName(), metadata, data);
                FileID fileID = new FileID(createdFile.getId());
                FolderID folderID = new FolderID(createdFile.getFolderId());
                if (null == fileID.getFolderId()) {
                    // TODO: check
                    fileID.setFolderId(folderID.getFolderId());
                }
                session.getChecksumStore().insertFileChecksum(fileID, createdFile.getVersion(),
                    createdFile.getSequenceNumber(), DriveConstants.EMPTY_MD5);
                return;
            }
            /*
             * check source and target files
             */
            ServerFileVersion sourceVersion = (ServerFileVersion)action.getParameters().get("sourceVersion");
            File sourceFile = sourceVersion.getFile();
            if (null == sourceFile.getVersion()) {
                /*
                 * no versioning support, re-check sequence number within this transaction
                 */
                File reloadedSourceFile = session.getStorage().getFile(sourceFile.getId(), sourceFile.getVersion());
                if (null == reloadedSourceFile || sourceFile.getSequenceNumber() != reloadedSourceFile.getSequenceNumber()) {
                    throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(sourceVersion.getName(), sourceVersion.getChecksum(), path);
                }
            }
            File targetFile = null;
            if (null != action.getVersion()) {
                File file = session.getStorage().findFileByName(path, action.getVersion().getName());
                if (null != file && ChecksumProvider.matches(session, file, action.getVersion().getChecksum())) {
                    targetFile = file;
                }
            }
            /*
             * invalidate target file checksum
             */
            if (null != targetFile) {
                FileID fileID = new FileID(targetFile.getId());
                FolderID folderID = new FolderID(targetFile.getFolderId());
                if (null == fileID.getFolderId()) {
                    // TODO: check
                    fileID.setFolderId(folderID.getFolderId());
                }
                session.getChecksumStore().removeFileChecksum(
                    fileID, targetFile.getVersion(), targetFile.getSequenceNumber());
            }
            if (sourceFile.isCurrentVersion() && isFromTemp(session, sourceFile)) {
                /*
                 * restore from temp folder possible, move file & update stored checksum
                 */
                File movedFile = null != targetFile ? session.getStorage().moveFile(sourceFile, targetFile) :
                    session.getStorage().moveFile(sourceFile, action.getNewVersion().getName(), path);
                fileChecksum = sourceVersion.getFileChecksum();
                FileID fileID = new FileID(movedFile.getId());
                FolderID folderID = new FolderID(movedFile.getFolderId());
                if (null == fileID.getFolderId()) {
                    // TODO: check
                    fileID.setFolderId(folderID.getFolderId());
                }
                fileChecksum.setFileID(fileID);
                fileChecksum.setVersion(movedFile.getVersion());
                fileChecksum.setSequenceNumber(movedFile.getSequenceNumber());
                session.getChecksumStore().updateFileChecksum(fileChecksum);
            } else {
                /*
                 * copy file, store checksum
                 */
                try {
                    File copiedFile = null != targetFile ? session.getStorage().copyFile(sourceFile, targetFile) :
                        session.getStorage().copyFile(sourceFile, action.getNewVersion().getName(), path);
                    FileID fileID = new FileID(copiedFile.getId());
                    FolderID folderID = new FolderID(copiedFile.getFolderId());
                    if (null == fileID.getFolderId()) {
                        // TODO: check
                        fileID.setFolderId(folderID.getFolderId());
                    }
                    session.getChecksumStore().insertFileChecksum(fileID, copiedFile.getVersion(),
                        copiedFile.getSequenceNumber(), sourceVersion.getChecksum());
                } catch (OXException e) {
                    if ("FLS-0017".equals(e.getErrorCode())) {
                        // not found
                        FileID fileID = new FileID(sourceFile.getId());
                        FolderID folderID = new FolderID(sourceFile.getFolderId());
                        if (null == fileID.getFolderId()) {
                            // TODO: check
                            fileID.setFolderId(folderID.getFolderId());
                        }
                        session.getChecksumStore().removeFileChecksums(fileID);
                    }
                    throw e;
                }
            }
            break;
        case EDIT:
            /*
             * rename file, update checksum
             */
            ServerFileVersion targetVersion = null != action.getParameters().get("targetVersion") ?
                ServerFileVersion.valueOf((FileVersion)action.getParameters().get("targetVersion"), path, session) : null;
            ServerFileVersion originalVersion = ServerFileVersion.valueOf(action.getVersion(), path, session);
            fileChecksum = originalVersion.getFileChecksum();
            File renamedFile;
            if (null != targetVersion) {
                session.getChecksumStore().removeFileChecksum(targetVersion.getFileChecksum());
                renamedFile = session.getStorage().moveFile(originalVersion.getFile(), targetVersion.getFile());
            } else {
                renamedFile = session.getStorage().renameFile(originalVersion.getFile(), action.getNewVersion().getName());
            }
            FileID fileID = new FileID(renamedFile.getId());
            FolderID folderID = new FolderID(renamedFile.getFolderId());
            if (null == fileID.getFolderId()) {
                // TODO: check
                fileID.setFolderId(folderID.getFolderId());
            }
            fileChecksum.setFileID(fileID);
            fileChecksum.setVersion(renamedFile.getVersion());
            fileChecksum.setSequenceNumber(renamedFile.getSequenceNumber());
            session.getChecksumStore().updateFileChecksum(fileChecksum);
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private static boolean isFromTemp(SyncSession session, File file) throws OXException {
        if (session.hasTempFolder()) {
            String tempFolderID = session.getStorage().getFolderID(DriveConstants.TEMP_PATH);
            if (tempFolderID.equals(file.getFolderId())) {
                return true;
            }
            FileStorageFolder folder = session.getStorage().getFolderAccess().getFolder(file.getFolderId());
            return null != folder && tempFolderID.equals(folder.getParentId());
        }
        return false;
    }

    private static List<ServerFileVersion> getServerFiles(SyncSession session, String path) throws OXException {
        String folderID = session.getStorage().getFolderID(path);
        List<File> files = session.getStorage().getFilesInFolder(folderID);
        List<FileChecksum> checksums = ChecksumProvider.getChecksums(session, folderID, files);
        List<ServerFileVersion> serverFiles = new ArrayList<ServerFileVersion>(files.size());
        for (int i = 0; i < files.size(); i++) {
            serverFiles.add(new ServerFileVersion(files.get(i), checksums.get(i)));
        }
        return serverFiles;
    }

    private static List<ServerDirectoryVersion> getServerDirectories(final SyncSession session) throws OXException {
        return session.getStorage().wrapInTransaction(new StorageOperation<List<ServerDirectoryVersion>>() {

            @Override
            public List<ServerDirectoryVersion> call() throws OXException {
                StringAllocator stringAllocator = session.isTraceEnabled() ? new StringAllocator("Server directories:\n") : null;
                Map<String, FileStorageFolder> folders = session.getStorage().getFolders();
                List<String> folderIDs = new ArrayList<String>(folders.size());
                for (Map.Entry<String, FileStorageFolder> entry : folders.entrySet()) {
                    if (false == DriveConstants.PATH_VALIDATION_PATTERN.matcher(entry.getKey()).matches()) {
                        session.trace("Skipping invalid server directory: " + entry.getKey());
                    } else {
                        folderIDs.add(entry.getValue().getId());
                    }
                }
                List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(session, folderIDs);
                List<ServerDirectoryVersion> serverDirectories = new ArrayList<ServerDirectoryVersion>(folderIDs.size());
                for (int i = 0; i < folderIDs.size(); i++) {
                    ServerDirectoryVersion directoryVersion = new ServerDirectoryVersion(
                        session.getStorage().getPath(folderIDs.get(i)), checksums.get(i));
                    serverDirectories.add(directoryVersion);
                    if (session.isTraceEnabled()) {
                        stringAllocator.append(" [").append(directoryVersion.getDirectoryChecksum().getFolderID()).append("] ")
                            .append(directoryVersion.getPath()).append(" | ").append(directoryVersion.getChecksum())
                            .append(" (").append(directoryVersion.getDirectoryChecksum().getSequenceNumber()).append(")\n");
                    }
                }
                if (session.isTraceEnabled()) {
                    session.trace(stringAllocator);
                }
                return serverDirectories;
            }
        });
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
