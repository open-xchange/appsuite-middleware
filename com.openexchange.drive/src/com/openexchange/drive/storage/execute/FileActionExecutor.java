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

package com.openexchange.drive.storage.execute;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.checksum.FileChecksum;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.internal.SyncSession;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;


/**
 * {@link FileActionExecutor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileActionExecutor extends BatchActionExecutor<FileVersion> {

    /**
     * When moving to trash, this value is used as limit before a safety check is performed if they already exist in the temp folder.
     */
    private static final int OPTIMISTIC_MOVE_TO_TRASH_THRESHOLD = 5;

    /**
     * When moving to trash, this value is used as hard limit for the number of files - further files will be hard-deleted
     */
    private static final int MOVE_TO_TRASH_LIMIT = 20;

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
    protected void batchExecute(Action action, List<AbstractAction<FileVersion>> actions) throws OXException {
        switch (action) {
//        case REMOVE:
//            batchRemove(actions);
//            break;
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

    private void batchRemove(List<AbstractAction<FileVersion>> removeActions) throws OXException {
        List<ServerFileVersion> versionsToDelete = new ArrayList<ServerFileVersion>();
        List<ServerFileVersion> versionsToRemove = new ArrayList<ServerFileVersion>();
        /*
         * check if files should be moved to temp folder or deleted
         */
        Set<String> checksumsToBeRemoved = new HashSet<String>();
        for (DriveAction<FileVersion> action : removeActions) {
            if (false == Action.REMOVE.equals(action.getAction())) {
                throw new IllegalArgumentException(action.getAction().toString());
            }
            ServerFileVersion fileVersion = ServerFileVersion.valueOf(action.getVersion(), path, session);
            /*
             * hard-delete if no temp folder available, file is empty, identical file already marked for removal, or hard limit reached
             */
            if (false == session.hasTempFolder() || DriveConstants.EMPTY_MD5.equals(fileVersion.getChecksum()) ||
                MOVE_TO_TRASH_LIMIT <= versionsToRemove.size() || checksumsToBeRemoved.contains(fileVersion.getChecksum())) {
                versionsToDelete.add(fileVersion);
            } else {
                versionsToRemove.add(fileVersion);
                checksumsToBeRemoved.add(fileVersion.getChecksum());
            }
        }
        /*
         * check if versions already known in trash if applicable
         */
        if (OPTIMISTIC_MOVE_TO_TRASH_THRESHOLD < versionsToRemove.size()) {
            FileStorageFolder tempFolder = session.getStorage().optFolder(DriveConstants.TEMP_PATH, false);
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
                    checksumsToUpdate.add(fileChecksum);
                } else {
                    // file already in trash, mark for complete removal
                    versionsToDelete.add(new ServerFileVersion(removedFile, fileChecksum));
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
        if (0 < versionsToRemove.size()) {
            List<FileChecksum> checksumsToRemove = new ArrayList<FileChecksum>();
            List<String> ids = new ArrayList<String>();
            long sequenceNumber = 0;
            for (ServerFileVersion versionToDelete : versionsToDelete) {
                ids.add(versionToDelete.getFile().getId());
                sequenceNumber = Math.max(sequenceNumber, versionToDelete.getFile().getSequenceNumber());
                checksumsToRemove.add(versionToDelete.getFileChecksum());
            }
            List<String> notRemovedIDs = session.getStorage().getFileAccess().removeDocument(ids, sequenceNumber);
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

}
