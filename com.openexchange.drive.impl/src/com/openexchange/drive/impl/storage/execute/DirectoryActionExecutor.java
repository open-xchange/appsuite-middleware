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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.ErrorDirectoryAction;
import com.openexchange.drive.impl.actions.SyncDirectoryAction;
import com.openexchange.drive.impl.checksum.FileChecksum;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.composition.FolderID;


/**
 * {@link DirectoryActionExecutor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryActionExecutor extends BatchActionExecutor<DirectoryVersion> {

    /**
     * Initializes a new {@link DirectoryActionExecutor}.
     *
     * @param session The sync session
     * @param transactional <code>true</code> to wrap each execution into a transaction, <code>false</code>, otherwise
     * @param allowBatches <code>true</code> to allow batch execution, <code>false</code>, otherwise
     */
    public DirectoryActionExecutor(SyncSession session, boolean transactional, boolean allowBatches) {
        super(session, transactional, allowBatches);
    }

    @Override
    protected void batchExecute(Action action, List<AbstractAction<DirectoryVersion>> actions) throws OXException {
        switch (action) {
        case REMOVE:
            batchRemove(actions);
            break;
        default:
            for (AbstractAction<DirectoryVersion> driveAction : actions) {
                execute(driveAction);
            }
            break;
        }
    }

    @Override
    protected void execute(AbstractAction<DirectoryVersion> action) throws OXException {
        switch (action.getAction()) {
        case EDIT:
            edit(action);
            break;
        case REMOVE:
            remove(action);
            break;
        case SYNC:
            sync(action);
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private void sync(AbstractAction<DirectoryVersion> action) throws OXException {
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
                FileStorageFolder folder = session.getStorage().optFolder(action.getVersion().getPath());
                if (null != folder) {
                    FolderID id = new FolderID(folder.getId());
                    session.getChecksumStore().removeDirectoryChecksum(id);
                    session.getChecksumStore().removeFileChecksumsInFolder(id);
                }
            }
        } else if (null != action.getVersion()) {
            /*
             * create new, empty directory at server
             */
            try {
                session.getStorage().getFolderID(action.getVersion().getPath(), true);
            } catch (OXException e) {
                if (DriveUtils.indicatesFailedSave(e)) {
                    addNewActionForClient(new ErrorDirectoryAction(null, action.getVersion(), null, e, true, false));
                } else {
                    throw e;
                }
            }
        } else {
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private void edit(AbstractAction<DirectoryVersion> action) throws OXException {
        /*
         * check for new, empty folder that simply can be created on server
         */
        if (null == action.getNewVersion() && null != action.getVersion() &&
            DriveConstants.EMPTY_MD5.equals(action.getVersion().getChecksum())) {
            try {
                session.getStorage().getFolderID(action.getVersion().getPath(), true);
                return;
            } catch (OXException e) {
                if (DriveUtils.indicatesFailedSave(e)) {
                    DirectoryVersion clientVersion;
                    if (null != action.getComparison() && null != action.getComparison().getClientVersion()) {
                        clientVersion = action.getComparison().getClientVersion();
                    } else {
                        clientVersion = action.getVersion();
                    }
                    addNewActionForClient(new ErrorDirectoryAction(null, clientVersion, null, e, true, false));
                    return;
                }
                throw e;
            }
        }
        /*
         * edit folder name and/or path
         */
        String folderID = session.getStorage().getFolderID(action.getVersion().getPath());
        String newFolderID;
        try {
            newFolderID = session.getStorage().moveFolder(action.getVersion().getPath(), action.getNewVersion().getPath());
        } catch (OXException e) {
            if (DriveUtils.indicatesFailedSave(e)) {
                addNewActionForClient(new ErrorDirectoryAction(action.getVersion(), action.getNewVersion(), null, e, true, false));
                return;
            }
            throw e;
        }
        /*
         * update stored checksums if needed
         */
        if (false == folderID.equals(newFolderID)) {
            session.getChecksumStore().updateFileChecksumFolders(new FolderID(folderID), new FolderID(newFolderID));
            session.getChecksumStore().updateDirectoryChecksumFolder(new FolderID(folderID), new FolderID(newFolderID));
        }
    }

    private void remove(AbstractAction<DirectoryVersion> action) throws OXException {
        try {
            if (session.getStorage().hasTrashFolder()) {
                /*
                 * move to trash if available
                 */
                String folderID = session.getStorage().deleteFolder(action.getVersion().getPath(), false);
                session.getChecksumStore().removeDirectoryChecksum(new FolderID(folderID));
                if (false == DriveConstants.EMPTY_MD5.equals(action.getVersion().getChecksum())) {
                    session.getChecksumStore().removeFileChecksumsInFolder(new FolderID(folderID));
                }
            } else if (DriveConstants.EMPTY_MD5.equals(action.getVersion().getChecksum())) {
                /*
                 * just delete empty directory
                 */
                String folderID = session.getStorage().deleteFolder(action.getVersion().getPath(), true);
                session.getChecksumStore().removeDirectoryChecksum(new FolderID(folderID));
            } else if (session.getTemp().supported()) {
                /*
                 * move to temp
                 */
                FileStoragePermission sourceFolderPermission = session.getStorage().getOwnPermission(action.getVersion().getPath());
                FileStoragePermission targetFolderPermission = session.getStorage().getOwnPermission(session.getTemp().getPath(true));
                if (FileStoragePermission.CREATE_SUB_FOLDERS <= targetFolderPermission.getFolderPermission() &&
                    FileStoragePermission.MAX_PERMISSION <= sourceFolderPermission.getFolderPermission()) {
                    /*
                     * try to move whole directory to temp folder
                     */
                    String targetPath = DriveUtils.combine(session.getTemp().getPath(true), action.getVersion().getChecksum());
                    FileStorageFolder targetFolder = session.getStorage().optFolder(targetPath);
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
                    } else {
                        /*
                         * identical folder already in trash, hard-delete the directory
                         */
                        FolderID deletedFolderID = new FolderID(session.getStorage().deleteFolder(action.getVersion().getPath(), true));
                        session.getChecksumStore().removeDirectoryChecksum(deletedFolderID);
                        session.getChecksumStore().removeFileChecksumsInFolder(deletedFolderID);
                    }
                } else {
                    /*
                     * no permissions to move whole directory, try and preserve at least each file separately
                     */
                    List<FileChecksum> checksumsToUpdate = new ArrayList<FileChecksum>();
                    List<FileChecksum> checksumsToRemove = new ArrayList<FileChecksum>();
                    List<File> filesToRemove = new ArrayList<File>();
                    for (ServerFileVersion versionToRemove : session.getServerFiles(action.getVersion().getPath())) {
                        FileChecksum fileChecksum = versionToRemove.getFileChecksum();
                        File removedFile = session.getStorage().moveFile(
                            versionToRemove.getFile(), versionToRemove.getChecksum(), session.getTemp().getPath(true));
                        if (versionToRemove.getChecksum().equals(removedFile.getFileName())) {
                            // moved successfully, update checksum
                            fileChecksum.setFileID(DriveUtils.getFileID(removedFile));
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
                        session.getStorage().deleteFiles(filesToRemove, true);
                    }
                    /*
                     * delete (empty) directory
                     */
                    String folderID = session.getStorage().deleteFolder(action.getVersion().getPath(), true);
                    session.getChecksumStore().removeDirectoryChecksum(new FolderID(folderID));
                }
            } else {
                /*
                 * no temp folder available, hard-delete directory + contents
                 */
                String folderID = session.getStorage().deleteFolder(action.getVersion().getPath(), true);
                session.getChecksumStore().removeDirectoryChecksum(new FolderID(folderID));
                session.getChecksumStore().removeFileChecksumsInFolder(new FolderID(folderID));
            }
        } catch (OXException e) {
            if (DriveUtils.indicatesFailedRemove(e) && null != action.getComparison()) {
                ThreeWayComparison<DirectoryVersion> comparison = action.getComparison();
                addNewActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
                addNewActionForClient(new ErrorDirectoryAction(comparison.getClientVersion(), comparison.getServerVersion(), comparison, e, false, false));
                return;
            }
            throw e;
        }
    }

    private void batchRemove(List<AbstractAction<DirectoryVersion>> removeActions) throws OXException {
        List<FolderID> removedFolderIDs = new ArrayList<FolderID>();
        List<FolderID[]> updatedFolderIDs = new ArrayList<FolderID[]>();
        for (AbstractAction<DirectoryVersion> action : removeActions) {
            /*
             * check action
             */
            if (false == Action.REMOVE.equals(action.getAction())) {
                throw new IllegalStateException("Can't perform action " + action + " on server");
            }
            if (session.getStorage().hasTrashFolder()) {
                /*
                 * move to trash if available
                 */
                String folderID = session.getStorage().deleteFolder(action.getVersion().getPath(), false);
                removedFolderIDs.add(new FolderID(folderID));
            } else if (DriveConstants.EMPTY_MD5.equals(action.getVersion().getChecksum()) || false == session.getTemp().supported() ||
                false == mayMove(action.getVersion().getPath(), session.getTemp().getPath(true))) {
                /*
                 * just delete empty directory
                 */
                String folderID = session.getStorage().deleteFolder(action.getVersion().getPath());
                removedFolderIDs.add(new FolderID(folderID));
            } else {
                /*
                 * try to move whole directory to temp folder
                 */
                String targetPath = DriveUtils.combine(session.getTemp().getPath(true), action.getVersion().getChecksum());
                FileStorageFolder targetFolder = session.getStorage().optFolder(targetPath);
                if (null == targetFolder) {
                    String currentFolderID = session.getStorage().getFolderID(action.getVersion().getPath());
                    String movedFolderID = session.getStorage().moveFolder(action.getVersion().getPath(), targetPath);
                    /*
                     * update stored checksums if needed
                     */
                    if (false == currentFolderID.equals(movedFolderID)) {
                        updatedFolderIDs.add(new FolderID[] { new FolderID(currentFolderID), new FolderID(movedFolderID) });
                    }
                } else {
                    /*
                     * identical folder already in temp folder, hard-delete the directory
                     */
                    String folderID = session.getStorage().deleteFolder(action.getVersion().getPath());
                    removedFolderIDs.add(new FolderID(folderID));
                }
            }
        }
        /*
         * update checksums
         */
        if (0 < removedFolderIDs.size()) {
            session.getChecksumStore().removeAllDirectoryChecksums(removedFolderIDs);
            session.getChecksumStore().removeFileChecksumsInFolders(removedFolderIDs);
        }
        if (0 < updatedFolderIDs.size()) {
            for (FolderID[] folderIDs : updatedFolderIDs) {
                session.getChecksumStore().updateDirectoryChecksumFolder(folderIDs[0], folderIDs[1]);
                session.getChecksumStore().updateFileChecksumFolders(folderIDs[0], folderIDs[1]);
            }
        }
    }

    private boolean mayMove(String fromPath, String toPath) throws OXException {
        return false == DriveConstants.ROOT_PATH.equals(fromPath) &&
            session.getStorage().getOwnPermission(fromPath).isAdmin() &&
            FileStoragePermission.CREATE_SUB_FOLDERS <= session.getStorage().getOwnPermission(toPath).getFolderPermission();
    }

}
