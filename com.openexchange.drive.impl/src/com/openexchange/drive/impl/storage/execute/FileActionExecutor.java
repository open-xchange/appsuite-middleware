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

package com.openexchange.drive.impl.storage.execute;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.DownloadFileAction;
import com.openexchange.drive.impl.actions.ErrorFileAction;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.FileChecksum;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.metadata.DriveMetadata;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;


/**
 * {@link FileActionExecutor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileActionExecutor extends BatchActionExecutor<FileVersion> {

    /**
     * When removing to the temp folder, this value is used as limit before a safety check is performed if they already exist in the
     * temp folder.
     */
    private static final int OPTIMISTIC_MOVE_TO_TEMP_THRESHOLD = 5;

    /**
     * When removing to the temp folder, this value is used as hard limit for the number of files - further files will be hard-deleted
     */
    private static final int MOVE_TO_TEMP_LIMIT = 20;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileActionExecutor.class);

    private final String path;

    /**
     * Initializes a new {@link FileActionExecutor}.
     *
     * @param session The sync session
     * @param transactional <code>true</code> to wrap each execution into a transaction, <code>false</code>, otherwise
     * @param allowBatches <code>true</code> to allow batch execution, <code>false</code>, otherwise
     * @param path The path where the file actions should be executed
     */
    public FileActionExecutor(SyncSession session, boolean transactional, boolean allowBatches, String path) {
        super(session, transactional, allowBatches);
        this.path = path;
    }

    @Override
    protected List<AbstractAction<FileVersion>> getActionsForClient(IntermediateSyncResult<FileVersion> syncResult) throws OXException {
        List<AbstractAction<FileVersion>> actionsForClient = super.getActionsForClient(syncResult);
        /*
         * check for possible changes to .drive-meta files that are going to be downloaded by the client
         */
        for (int i = 0; i < actionsForClient.size(); i++) {
            AbstractAction<FileVersion> actionForClient = actionsForClient.get(i);
            if (Action.DOWNLOAD.equals(actionForClient.getAction()) &&
                DriveConstants.METADATA_FILENAME.equals(actionForClient.getNewVersion().getName())) {
                boolean needsUpdate = false;
                for (AbstractAction<FileVersion> actionForServer : syncResult.getActionsForServer()) {
                    Action action = actionForServer.getAction();
                    if (Action.REMOVE.equals(action) || Action.DOWNLOAD.equals(action) || Action.EDIT.equals(action)) {
                        needsUpdate = true;
                        break;
                    }
                }
                if (needsUpdate) {
                    /*
                     * previously executed server action influences .drive-meta file, propagate new checksum in client action
                     */
                    DriveMetadata metadata = new DriveMetadata(session, session.getStorage().getFolder(path));
                    FileChecksum checksum = ChecksumProvider.getChecksum(session, metadata);
                    ServerFileVersion newFileVersion = new ServerFileVersion(metadata, checksum);
                    actionsForClient.set(i, new DownloadFileAction(
                        session, actionForClient.getVersion(), newFileVersion, actionForClient.getComparison(), path));
                }
            }
        }
        return actionsForClient;
    }

    @Override
    protected void batchExecute(Action action, List<AbstractAction<FileVersion>> actions) throws OXException {
        switch (action) {
        case REMOVE:
            batchRemove(actions);
            break;
        default:
            for (AbstractAction<FileVersion> driveAction : actions) {
                execute(driveAction);
            }
            break;
        }
    }

    @Override
    protected void execute(AbstractAction<FileVersion> action) throws OXException {
        switch (action.getAction()) {
        case REMOVE:
            remove(action);
            break;
        case DOWNLOAD:
            try {
                download(action);
            } catch (OXException e) {
                LOG.warn("Got exception during server-side execution of download action: {}\nSession: {}, path: {}, action: {}",
                    e.getMessage(), session, path, action, e);
                if (DriveUtils.indicatesQuotaExceeded(e)) {
                    addNewActionsForClient(DriveUtils.handleQuotaExceeded(session, e, path, action.getVersion(), action.getNewVersion()));
                } else if (DriveUtils.indicatesFailedSave(e)) {
                    addNewActionForClient(new ErrorFileAction(null, action.getNewVersion(), null, path, e, true));
                } else {
                    throw e;
                }
            }
            break;
        case EDIT:
            try {
                edit(action);
            } catch (OXException e) {
                LOG.warn("Got exception during server-side execution of edit action: {}\nSession: {}, path: {}, action: {}",
                    e.getMessage(), session, path, action, e);
                if (DriveUtils.indicatesFailedSave(e)) {
                    addNewActionForClient(new ErrorFileAction(null, action.getNewVersion(), null, path, e, true));
                } else {
                    throw e;
                }
            }
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private void edit(AbstractAction<FileVersion> action) throws OXException {
        /*
         * rename file, update checksum
         */
        ServerFileVersion targetVersion = null != action.getParameters().get("targetVersion") ?
            ServerFileVersion.valueOf((FileVersion)action.getParameters().get("targetVersion"), path, session) : null;
        ServerFileVersion originalVersion = ServerFileVersion.valueOf(action.getVersion(), path, session);
        FileChecksum fileChecksum = originalVersion.getFileChecksum();
        File renamedFile;
        if (null != targetVersion) {
            session.getChecksumStore().removeFileChecksum(targetVersion.getFileChecksum());
            renamedFile = session.getStorage().moveFile(originalVersion.getFile(), targetVersion.getFile());
        } else {
            renamedFile = session.getStorage().renameFile(originalVersion.getFile(), action.getNewVersion().getName());
        }
        fileChecksum.setFileID(DriveUtils.getFileID(renamedFile));
        fileChecksum.setVersion(renamedFile.getVersion());
        fileChecksum.setSequenceNumber(renamedFile.getSequenceNumber());
        FileChecksum updatedFileChecksum = session.getChecksumStore().updateFileChecksum(fileChecksum);
        action.setResultingVersion(new ServerFileVersion(renamedFile, updatedFileChecksum));
    }

    private void download(AbstractAction<FileVersion> action) throws OXException {
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
            FileChecksum insertedFileChecksum = session.getChecksumStore().insertFileChecksum(new FileChecksum(
                DriveUtils.getFileID(createdFile), createdFile.getVersion(), createdFile.getSequenceNumber(), DriveConstants.EMPTY_MD5));
            action.setResultingVersion(new ServerFileVersion(createdFile, insertedFileChecksum));
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
            File file = session.getStorage().getFileByName(path, action.getVersion().getName(), true);
            if (null != file && ChecksumProvider.matches(session, file, action.getVersion().getChecksum())) {
                targetFile = file;
            }
        }
        /*
         * invalidate target file checksum
         */
        if (null != targetFile) {
            session.getChecksumStore().removeFileChecksum(
                DriveUtils.getFileID(targetFile), targetFile.getVersion(), targetFile.getSequenceNumber());
        }
        if (sourceFile.isCurrentVersion() && isFromTemp(session, sourceFile)) {
            /*
             * restore from temp folder possible, move file & update stored checksum
             */
            File movedFile = null != targetFile ? session.getStorage().moveFile(sourceFile, targetFile) :
                session.getStorage().moveFile(sourceFile, action.getNewVersion().getName(), path);
            FileChecksum fileChecksum = sourceVersion.getFileChecksum();
            fileChecksum.setFileID(DriveUtils.getFileID(movedFile));
            fileChecksum.setVersion(movedFile.getVersion());
            fileChecksum.setSequenceNumber(movedFile.getSequenceNumber());
            FileChecksum updatedFileChecksum = session.getChecksumStore().updateFileChecksum(fileChecksum);
            action.setResultingVersion(new ServerFileVersion(movedFile, updatedFileChecksum));
        } else {
            /*
             * copy file, store checksum
             */
            try {
                File copiedFile = null != targetFile ? session.getStorage().copyFile(sourceFile, targetFile) :
                    session.getStorage().copyFile(sourceFile, action.getNewVersion().getName(), path);
                FileChecksum insertedFileChecksum = session.getChecksumStore().insertFileChecksum(new FileChecksum(
                    DriveUtils.getFileID(copiedFile), copiedFile.getVersion(), copiedFile.getSequenceNumber(), sourceVersion.getChecksum()));
                action.setResultingVersion(new ServerFileVersion(copiedFile, insertedFileChecksum));
            } catch (OXException e) {
                if ("FLS-0017".equals(e.getErrorCode()) || "DROPBOX-0005".equals(e.getErrorCode()) ||
                    "FILE_STORAGE-0026".equals(e.getErrorCode()) || "FILE_STORAGE-0039".equals(e.getErrorCode())) {
                    // not found, remove checksum to be safe
                    session.getChecksumStore().removeFileChecksums(Collections.singletonList(sourceVersion.getFileChecksum()));
                }
                throw e;
            }
        }
    }

    private void remove(AbstractAction<FileVersion> action) throws OXException {
        ServerFileVersion versionToRemove = ServerFileVersion.valueOf(action.getVersion(), path, session);
        FileChecksum fileChecksum = versionToRemove.getFileChecksum();
        if (session.getStorage().hasTrashFolder()) {
            /*
             * move to trash if available
             */
            session.getStorage().deleteFile(versionToRemove.getFile(), false);
            session.getChecksumStore().removeFileChecksum(fileChecksum);
        } else if (false == session.getTemp().supported() || DriveConstants.EMPTY_MD5.equals(fileChecksum.getChecksum())) {
            /*
             * hard delete file if applicable
             */
            session.getStorage().deleteFile(versionToRemove.getFile(), true);
            session.getChecksumStore().removeFileChecksum(fileChecksum);
        } else {
            /*
             * try and move to temp folder using the file's checksum as filename
             */
            try {
                File removedFile = session.getStorage().moveFile(
                    versionToRemove.getFile(), versionToRemove.getChecksum(), session.getTemp().getPath(true));
                if (versionToRemove.getChecksum().equals(removedFile.getFileName())) {
                    // moved successfully, update checksum
                    fileChecksum.setFileID(DriveUtils.getFileID(removedFile));
                    fileChecksum.setVersion(removedFile.getVersion());
                    fileChecksum.setSequenceNumber(removedFile.getSequenceNumber());
                    session.getChecksumStore().updateFileChecksum(fileChecksum);
                } else {
                    // file already in trash, cleanup
                    session.getStorage().deleteFile(removedFile, true);
                    session.getChecksumStore().removeFileChecksum(fileChecksum);
                }
            } catch (OXException e) {
                LOG.debug("Error moving file to temp folder - performing hard-delete instead.", e);
                session.getStorage().deleteFile(versionToRemove.getFile(), true);
                session.getChecksumStore().removeFileChecksum(fileChecksum);
            }
        }
    }

    private void batchRemove(List<AbstractAction<FileVersion>> removeActions) throws OXException {
        if (session.getStorage().hasTrashFolder()) {
            /*
             * move all versions to trash if available
             */
            Map<File, FileChecksum> filesToRemove = new HashMap<File, FileChecksum>(removeActions.size());
            for (DriveAction<FileVersion> action : removeActions) {
                if (false == Action.REMOVE.equals(action.getAction())) {
                    throw new IllegalArgumentException(action.getAction().toString());
                }
                ServerFileVersion fileVersion = ServerFileVersion.valueOf(action.getVersion(), path, session);
                filesToRemove.put(fileVersion.getFile(), fileVersion.getFileChecksum());
            }
            List<File> notRemovedFiles = session.getStorage().deleteFiles(new ArrayList<File>(filesToRemove.keySet()), false);
            /*
             * remove stored checksums accordingly
             */
            for (File file : notRemovedFiles) {
                filesToRemove.remove(file);
            }
            if (0 < filesToRemove.size()) {
                session.getChecksumStore().removeFileChecksums(new ArrayList<FileChecksum>(filesToRemove.values()));
            }
        } else {
            /*
             * check if files should be moved to temp folder or deleted
             */
            List<ServerFileVersion> versionsToDelete = new ArrayList<ServerFileVersion>();
            List<ServerFileVersion> versionsToRemove = new ArrayList<ServerFileVersion>();
            Set<String> checksumsToBeRemoved = new HashSet<String>();
            for (DriveAction<FileVersion> action : removeActions) {
                if (false == Action.REMOVE.equals(action.getAction())) {
                    throw new IllegalArgumentException(action.getAction().toString());
                }
                ServerFileVersion fileVersion = ServerFileVersion.valueOf(action.getVersion(), path, session);
                /*
                 * hard-delete if no temp folder available, file is empty, identical file already marked for removal, or hard limit reached
                 */
                if (false == session.getTemp().supported() || DriveConstants.EMPTY_MD5.equals(fileVersion.getChecksum()) ||
                    MOVE_TO_TEMP_LIMIT <= versionsToRemove.size() || checksumsToBeRemoved.contains(fileVersion.getChecksum())) {
                    versionsToDelete.add(fileVersion);
                } else {
                    versionsToRemove.add(fileVersion);
                    checksumsToBeRemoved.add(fileVersion.getChecksum());
                }
            }
            /*
             * check if versions already known in trash if applicable
             */
            if (OPTIMISTIC_MOVE_TO_TEMP_THRESHOLD < versionsToRemove.size() && session.getTemp().exists()) {
                FileStorageFolder tempFolder = session.getStorage().optFolder(session.getTemp().getPath(false));
                if (null != tempFolder) {
                    List<FileChecksum> knownChecksums = session.getChecksumStore().getFileChecksums(new FolderID(tempFolder.getId()));
                    if (null != knownChecksums && 0 < knownChecksums.size()) {
                        Iterator<ServerFileVersion> iterator = versionsToRemove.iterator();
                        while (iterator.hasNext()) {
                            ServerFileVersion versionToRemove = iterator.next();
                            boolean alreadyKnown = false;
                            for (FileChecksum knownChecksum : knownChecksums) {
                                if (knownChecksum.getChecksum().equals(versionToRemove.getChecksum())) {
                                    alreadyKnown = true;
                                    break;
                                }
                            }
                            if (alreadyKnown) {
                                /*
                                 * checksum already known, do hard-delete instead
                                 */
                                versionsToDelete.add(versionToRemove);
                                iterator.remove();
                            }
                        }
                    }
                }
            }
            /*
             * execute move-operations
             */
            if (0 < versionsToRemove.size()) {
                List<FileChecksum> checksumsToUpdate = new ArrayList<FileChecksum>();
                for (ServerFileVersion versionToRemove : versionsToRemove) {
                    FileChecksum fileChecksum = versionToRemove.getFileChecksum();
                    try {
                        File removedFile = session.getStorage().moveFile(
                            versionToRemove.getFile(), versionToRemove.getChecksum(), session.getTemp().getPath(true));
                        if (versionToRemove.getChecksum().equals(removedFile.getFileName())) {
                            // moved successfully, update checksum
                            fileChecksum.setFileID(DriveUtils.getFileID(removedFile));
                            fileChecksum.setVersion(removedFile.getVersion());
                            fileChecksum.setSequenceNumber(removedFile.getSequenceNumber());
                            checksumsToUpdate.add(fileChecksum);
                        } else {
                            // file already in trash, mark for complete removal
                            versionsToDelete.add(new ServerFileVersion(removedFile, fileChecksum));
                        }
                    } catch (OXException e) {
                        LOG.debug("Error moving file to temp folder - performing hard-delete instead.", e);
                        versionsToDelete.add(versionToRemove);
                    }
                }
                /*
                 * update checksums accordingly
                 */
                if (0 < checksumsToUpdate.size()) {
                    session.getChecksumStore().updateFileChecksums(checksumsToUpdate);
                }
            }
            /*
             * execute delete operations
             */
            if (0 < versionsToDelete.size()) {
                List<FileChecksum> checksumsToRemove = new ArrayList<FileChecksum>();
                List<String> ids = new ArrayList<String>();
                long sequenceNumber = 0;
                for (ServerFileVersion versionToDelete : versionsToDelete) {
                    ids.add(versionToDelete.getFile().getId());
                    sequenceNumber = Math.max(sequenceNumber, versionToDelete.getFile().getSequenceNumber());
                    checksumsToRemove.add(versionToDelete.getFileChecksum());
                }
                List<String> notRemovedIDs = session.getStorage().getFileAccess().removeDocument(ids, sequenceNumber, true);
                for (String notRemovedID : notRemovedIDs) {
                    //TODO: keep those checksums?
                }
                /*
                 * remove checksums accordingly
                 */
                if (0 < checksumsToRemove.size()) {
                    session.getChecksumStore().removeFileChecksums(checksumsToRemove);
                }
            }
        }
    }

    private static boolean isFromTemp(SyncSession session, File file) throws OXException {
        if (session.getTemp().exists()) {
            String tempPath = session.getTemp().getPath(true);
            if (null != tempPath) {
                String tempFolderID = session.getStorage().getFolderID(tempPath);
                if (tempFolderID.equals(file.getFolderId())) {
                    return true;
                }
                FileStorageFolder folder = session.getStorage().getFolderAccess().getFolder(file.getFolderId());
                return null != folder && tempFolderID.equals(folder.getParentId());
            }
        }
        return false;
    }

}
