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
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.actions.RemoveFileAction;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.checksum.DirectoryChecksum;
import com.openexchange.drive.checksum.FileChecksum;
import com.openexchange.drive.comparison.DirectoryVersionMapper;
import com.openexchange.drive.comparison.FileVersionMapper;
import com.openexchange.drive.comparison.ServerDirectoryVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.drive.sync.SyncResult;
import com.openexchange.drive.sync.Synchronizer;
import com.openexchange.drive.sync.optimize.OptimizingDirectorySynchronizer;
import com.openexchange.drive.sync.optimize.OptimizingFileSynchronizer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.StringAllocator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DriveServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveServiceImpl implements DriveService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveServiceImpl.class);

    /**
     * Initializes a new {@link DriveServiceImpl}.
     */
    public DriveServiceImpl() {
        super();
        LOG.debug("initialized.");
    }

    @Override
    public List<DriveAction<DirectoryVersion>> syncFolders(ServerSession session, String rootFolderID,
        List<DirectoryVersion> originalDirectories, List<DirectoryVersion> clientDirectories) throws OXException {
        long start = System.currentTimeMillis();
        DriveSession driveSession = createSession(session, rootFolderID);
        /*
         * map directories
         */
        List<ServerDirectoryVersion> serverDirectories = getServerDirectories(driveSession);
        DirectoryVersionMapper mapper = new DirectoryVersionMapper(originalDirectories, clientDirectories, serverDirectories);
        if (LOG.isDebugEnabled()) {
            StringAllocator allocator = new StringAllocator("Directory versions mapped to:\n");
            allocator.append(mapper).append('\n');
            LOG.debug(allocator);
        }
        /*
         * determine sync actions
         */
        Synchronizer<DirectoryVersion> synchronizer = new OptimizingDirectorySynchronizer(driveSession, mapper);
        SyncResult<DirectoryVersion> syncResult = synchronizer.sync();
        if (LOG.isDebugEnabled()) {
            LOG.debug(syncResult);
        }
        /*
         * execute actions on server
         */
        for (DriveAction<DirectoryVersion> action : syncResult.getActionsForServer()) {
            execute(driveSession, action);
        }
        /*
         * return actions for client
         */
        if (LOG.isDebugEnabled()) {
            LOG.debug("syncFolders completed after " + (System.currentTimeMillis() - start) + "ms.");
        }
        return syncResult.getActionsForClient();
    }

    @Override
    public List<DriveAction<FileVersion>> syncFiles(ServerSession session, String rootFolderID, String path,
        List<FileVersion> originalFiles, List<FileVersion> clientFiles) throws OXException {
        long start = System.currentTimeMillis();
        DriveSession driveSession = createSession(session, rootFolderID);
        /*
         * map files
         */
        driveSession.getStorage().createFolder(path);
        List<ServerFileVersion> serverFiles = getServerFiles(driveSession, path);
        FileVersionMapper mapper = new FileVersionMapper(originalFiles, clientFiles, serverFiles);
        if (LOG.isDebugEnabled()) {
            StringAllocator allocator = new StringAllocator("File versions mapped to:\n");
            allocator.append(mapper).append('\n');
            LOG.debug(allocator);
        }
        /*
         * determine sync actions
         */
        Synchronizer<FileVersion> synchronizer = new OptimizingFileSynchronizer(driveSession, mapper, path);
        SyncResult<FileVersion> syncResult = synchronizer.sync();
        if (LOG.isDebugEnabled()) {
            LOG.debug(syncResult);
        }
        /*
         * execute actions on server
         */
        for (DriveAction<FileVersion> action : syncResult.getActionsForServer()) {
            execute(driveSession, path, action);
        }
        /*
         * return actions for client
         */
        if (LOG.isDebugEnabled()) {
            LOG.debug("syncFiles completed after " + (System.currentTimeMillis() - start) + "ms.");
        }
        return syncResult.getActionsForClient();
    }

    @Override
    public IFileHolder download(ServerSession session, String rootFolderID, String path, FileVersion fileVersion,
        long offset, long length) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling download: " + fileVersion);
        }
        DownloadHelper downloadHelper = new DownloadHelper(driveSession);
        return downloadHelper.perform(path, fileVersion, offset, length);
    }

    @Override
    public List<DriveAction<FileVersion>> upload(ServerSession session, String rootFolderID, String path, InputStream uploadStream,
        FileVersion originalVersion, FileVersion newVersion, long offset, long totalLength) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling upload: " + newVersion);
        }
        SyncResult<FileVersion> syncResult = new SyncResult<FileVersion>();
        File createdFile = new UploadHelper(driveSession).perform(path, originalVersion, newVersion, uploadStream, offset, totalLength);
        if (null != createdFile) {
            /*
             * store checksum
             */
            FileChecksum fileChecksum = driveSession.getChecksumStore().insertFileChecksum(createdFile.getFolderId(),
                createdFile.getId(), createdFile.getVersion(), createdFile.getSequenceNumber(), newVersion.getChecksum());
            /*
             * check if created file still equals uploaded one
             */
            FileVersion createdVersion = new ServerFileVersion(createdFile, fileChecksum);
            if (newVersion.getName().equals(createdVersion.getName())) {
                syncResult.addActionForClient(new AcknowledgeFileAction(originalVersion, createdVersion, path));
            } else {
                syncResult.addActionForClient(new EditFileAction(newVersion, createdVersion));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(syncResult);
        }
        return syncResult.getActionsForClient();
    }

    private static void execute(DriveSession session, DriveAction<DirectoryVersion> action) throws OXException {
        switch (action.getAction()) {
        case EDIT:
            /*
             * edit folder name and/or path
             */
            String folderID = session.getStorage().getFolderID(action.getVersion().getPath());
            String newFolderID = session.getStorage().moveFolder(action.getVersion().getPath(), action.getNewVersion().getPath());
            /*
             * update stored checksums if needed
             */
            if (false == folderID.equals(newFolderID)) {
                session.getChecksumStore().updateFileChecksumFolders(folderID, newFolderID);
                session.getChecksumStore().updateDirectoryChecksumFolder(folderID, newFolderID);
            }
            break;
        case REMOVE:
            if (false == DriveConstants.EMPTY_MD5.equals(action.getVersion().getChecksum())) {
                // move all files to temp path
                // TODO: optimize
                List<ServerFileVersion> serverFileVersions = getServerFiles(session, action.getVersion().getPath());
                for (ServerFileVersion serverFileVersion : serverFileVersions) {
                    execute(session, action.getVersion().getPath(), new RemoveFileAction(serverFileVersion, action.getVersion().getPath()));
                }
            }
            // delete empty directory
            folderID = session.getStorage().deleteFolder(action.getVersion().getPath());
            session.getChecksumStore().removeDirectoryChecksum(folderID);
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private static void execute(DriveSession session, String path, DriveAction<FileVersion> action) throws OXException {
        switch (action.getAction()) {
        case REMOVE:
            /*
             * move to temp folder
             */
            ServerFileVersion versionToRemove = ServerFileVersion.valueOf(action.getVersion(), path, session);
            FileChecksum fileChecksum = versionToRemove.getFileChecksum();
            File removedFile = session.getStorage().moveFile(
                versionToRemove.getFile(), versionToRemove.getChecksum(), DriveConstants.TEMP_PATH);
            if (versionToRemove.getChecksum().equals(removedFile.getFileName())) {
                // moved successfully, update checksum
                fileChecksum.setFolderID(removedFile.getFolderId());
                fileChecksum.setFileID(removedFile.getId());
                fileChecksum.setVersion(removedFile.getVersion());
                fileChecksum.setSequenceNumber(removedFile.getSequenceNumber());
                session.getChecksumStore().updateFileChecksum(fileChecksum);
            } else {
                // file already in trash, cleanup
                session.getStorage().deleteFile(removedFile);
                session.getChecksumStore().removeFileChecksum(fileChecksum);
            }
            break;
        case DOWNLOAD:
            /*
             * check source and target files
             */
            ServerFileVersion sourceVersion = (ServerFileVersion)action.getParameters().get("sourceVersion");
            File sourceFile = sourceVersion.getFile();
            boolean isFromTemp = sourceFile.getFolderId().equals(session.getStorage().getFolderID(DriveConstants.TEMP_PATH));
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
                session.getChecksumStore().removeFileChecksum(
                    targetFile.getFolderId(), targetFile.getId(), targetFile.getVersion(), targetFile.getSequenceNumber());
            }
            if (isFromTemp) {
                /*
                 * move file, update stored checksum
                 */
                File movedFile = null != targetFile ? session.getStorage().moveFile(sourceFile, targetFile) :
                    session.getStorage().moveFile(sourceFile, action.getNewVersion().getName(), path);
                fileChecksum = sourceVersion.getFileChecksum();
                fileChecksum.setFolderID(movedFile.getFolderId());
                fileChecksum.setFileID(movedFile.getId());
                fileChecksum.setVersion(movedFile.getVersion());
                fileChecksum.setSequenceNumber(movedFile.getSequenceNumber());
                session.getChecksumStore().updateFileChecksum(fileChecksum);
            } else {
                /*
                 * copy file, store checksum
                 */
                File copiedFile = null != targetFile ? session.getStorage().copyFile(sourceFile, targetFile) :
                    session.getStorage().copyFile(sourceFile, action.getNewVersion().getName(), path);
                session.getChecksumStore().insertFileChecksum(copiedFile.getFolderId(), copiedFile.getId(), copiedFile.getVersion(),
                    copiedFile.getSequenceNumber(), sourceVersion.getChecksum());
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
            fileChecksum.setFolderID(renamedFile.getFolderId());
            fileChecksum.setFileID(renamedFile.getId());
            fileChecksum.setVersion(renamedFile.getVersion());
            fileChecksum.setSequenceNumber(renamedFile.getSequenceNumber());
            session.getChecksumStore().updateFileChecksum(fileChecksum);
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private static List<ServerFileVersion> getServerFiles(DriveSession session, String path) throws OXException {
        String folderID = session.getStorage().getFolderID(path);
        List<File> files = session.getStorage().getFilesInFolder(folderID);
        List<FileChecksum> checksums = ChecksumProvider.getChecksums(session, folderID, files);
        List<ServerFileVersion> serverFiles = new ArrayList<ServerFileVersion>(files.size());
        for (int i = 0; i < files.size(); i++) {
            serverFiles.add(new ServerFileVersion(files.get(i), checksums.get(i)));
        }
        return serverFiles;
    }

    private static List<ServerDirectoryVersion> getServerDirectories(DriveSession session) throws OXException {
        Map<String, FileStorageFolder> folders = session.getStorage().getFolders();
        List<String> folderIDs = new ArrayList<String>(folders.size());
        for (Map.Entry<String, FileStorageFolder> entry : folders.entrySet()) {
            folderIDs.add(entry.getValue().getId());
        }
        List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(session, folderIDs);
        List<ServerDirectoryVersion> serverDirectories = new ArrayList<ServerDirectoryVersion>(folderIDs.size());
        for (int i = 0; i < folderIDs.size(); i++) {
            serverDirectories.add(new ServerDirectoryVersion(session.getStorage().getPath(folderIDs.get(i)), checksums.get(i)));
        }
        return serverDirectories;
    }

    private static DriveSession createSession(ServerSession session, String rootFolderID) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating new drive session for user " + session.getLoginName() + " (" + session.getUserId() +
                ") in context " + session.getContextId() + ", root folder ID is " + rootFolderID);
        }
        return new DriveSession(session, rootFolderID);
    }

}
