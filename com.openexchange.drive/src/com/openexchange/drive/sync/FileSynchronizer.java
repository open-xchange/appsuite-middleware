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

package com.openexchange.drive.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DownloadFileAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.actions.ErrorFileAction;
import com.openexchange.drive.actions.RemoveFileAction;
import com.openexchange.drive.actions.UploadFileAction;
import com.openexchange.drive.comparison.Change;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.comparison.ThreeWayComparison;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.SyncSession;
import com.openexchange.drive.internal.UploadHelper;
import com.openexchange.drive.storage.filter.SynchronizedFileFilter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStoragePermission;


/**
 * {@link FileSynchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileSynchronizer extends Synchronizer<FileVersion> {

    private Set<String> usedFilenames;
    private List<UploadFileAction> uploadActions;
    private final String path;
    private FileStoragePermission folderPermission;

    public FileSynchronizer(SyncSession session, VersionMapper<FileVersion> mapper, String path) throws OXException {
        super(session, mapper);
        this.path = path;
    }

    @Override
    public IntermediateSyncResult<FileVersion> sync() throws OXException {
        usedFilenames = new HashSet<String>(mapper.getKeys());
        uploadActions = new ArrayList<UploadFileAction>();
        IntermediateSyncResult<FileVersion> syncResult = super.sync();
        /*
         * inject upload offsets if needed
         */
        if (0 < uploadActions.size()) {
            List<FileVersion> versionsToUpload = new ArrayList<FileVersion>(uploadActions.size());
            for (UploadFileAction uploadAction : uploadActions) {
                versionsToUpload.add(uploadAction.getNewVersion());
            }
            List<Long> uploadOffsets = new UploadHelper(session).getUploadOffsets(path, versionsToUpload);
            for (int i = 0; i < uploadOffsets.size(); i++) {
                uploadActions.get(i).getParameters().put(DriveAction.PARAMETER_OFFSET, uploadOffsets.get(i));
            }
        }
        return syncResult;
    }

    @Override
    protected void processServerChange(IntermediateSyncResult<FileVersion> result, ThreeWayComparison<FileVersion> comparison) throws OXException {
        switch (comparison.getServerChange()) {
        case DELETED:
            /*
             * deleted on server, delete file on client, too
             */
            result.addActionForClient(new RemoveFileAction(comparison.getClientVersion(), comparison, path));
            break;
        case MODIFIED:
            if (comparison.getServerVersion().getChecksum().equalsIgnoreCase(comparison.getClientVersion().getChecksum()) &&
                comparison.getServerVersion().getName().equalsIgnoreCase(comparison.getClientVersion().getName()) &&
                false == comparison.getServerVersion().getName().equals(comparison.getClientVersion().getName())) {
                /*
                 * just renamed on server, let client edit the file
                 */
                result.addActionForClient(new EditFileAction(
                    comparison.getClientVersion(), comparison.getServerVersion(), comparison, path));
            } else {
                /*
                 * modified on server, let client download the file
                 */
                File serverFile = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session).getFile();
                result.addActionForClient(new DownloadFileAction(
                    comparison.getClientVersion(), comparison.getServerVersion(), comparison, path, serverFile));
            }
            break;
        case NEW:
            /*
             * new on server, let client download the file
             */
            File serverFile = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session).getFile();
            result.addActionForClient(new DownloadFileAction(
                comparison.getClientVersion(), comparison.getServerVersion(), comparison, path, serverFile));
            break;
        default:
            break;
        }
    }

    @Override
    protected void processClientChange(IntermediateSyncResult<FileVersion> result, ThreeWayComparison<FileVersion> comparison) throws OXException {
        switch (comparison.getClientChange()) {
        case DELETED:
            if (mayDelete(comparison.getServerVersion())) {
                /*
                 * delete on server, too, let client remove it's metadata
                 */
                result.addActionForServer(new RemoveFileAction(comparison.getServerVersion(), comparison, path));
                result.addActionForClient(new AcknowledgeFileAction(comparison.getOriginalVersion(), null, comparison, path));
            } else {
                /*
                 * not allowed, let client re-download the file, indicate as error without quarantine flag
                 */
                File serverFile = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session).getFile();
                result.addActionForClient(new DownloadFileAction(
                    comparison.getClientVersion(), comparison.getServerVersion(), comparison, path, serverFile));
                result.addActionForClient(new ErrorFileAction(comparison.getClientVersion(), comparison.getServerVersion(), comparison,
                    path, DriveExceptionCodes.NO_DELETE_FILE_PERMISSION.create(comparison.getServerVersion().getName(), path), false));
            }
            break;
        case MODIFIED:
            if (mayModify(comparison.getServerVersion())) {
                if (comparison.getClientVersion().getChecksum().equalsIgnoreCase(comparison.getServerVersion().getChecksum()) &&
                    comparison.getClientVersion().getName().equalsIgnoreCase(comparison.getServerVersion().getName()) &&
                    false == comparison.getClientVersion().getName().equals(comparison.getServerVersion().getName())) {
                    /*
                     * just renamed on client, let server edit the file, acknowledge rename to client
                     */
                    ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session);
                    result.addActionForServer(new EditFileAction(serverFileVersion, comparison.getClientVersion(), comparison, path));
                    result.addActionForClient(new AcknowledgeFileAction(
                        comparison.getOriginalVersion(), comparison.getClientVersion(), comparison, path, serverFileVersion.getFile()));
                } else {
                    /*
                     * modified on client, let client upload the modified file
                     */
                    UploadFileAction uploadAction = new UploadFileAction(
                        comparison.getServerVersion(), comparison.getClientVersion(), comparison, path, 0);
                    uploadActions.add(uploadAction);
                    result.addActionForClient(uploadAction);
                }
            } else if (mayCreate()) {
                /*
                 * not allowed, keep both client- and server versions, let client first rename it's file...
                 */
                FileVersion renamedVersion = getRenamedVersion(session, comparison.getClientVersion());
                result.addActionForClient(new EditFileAction(comparison.getClientVersion(), renamedVersion, comparison, path));
                /*
                 * ... then mark that file as error (without quarantine)...
                 */
                result.addActionForClient(new ErrorFileAction(comparison.getClientVersion(), comparison.getServerVersion(), comparison,
                    path, DriveExceptionCodes.NO_MODIFY_FILE_PERMISSION.create(comparison.getServerVersion().getName(), path), false));
                /*
                 * ... then upload it, and download the server version afterwards
                 */
                UploadFileAction uploadAction = new UploadFileAction(null, renamedVersion, comparison, path, 0);
                uploadActions.add(uploadAction);
                result.addActionForClient(uploadAction);
                File serverFile = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session).getFile();
                result.addActionForClient(new DownloadFileAction(null, comparison.getServerVersion(), comparison, path, serverFile));
            } else {
                /*
                 * not allowed, let client first rename it's file and mark as error with quarantine flag...
                 */
                FileVersion renamedVersion = getRenamedVersion(session, comparison.getClientVersion());
                result.addActionForClient(new EditFileAction(comparison.getClientVersion(), renamedVersion, comparison, path));
                result.addActionForClient(new ErrorFileAction(comparison.getClientVersion(), renamedVersion, comparison,
                    path, DriveExceptionCodes.NO_MODIFY_FILE_PERMISSION.create(comparison.getServerVersion().getName(), path), true));
                /*
                 * ... then download the server version afterwards
                 */
                File serverFile = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session).getFile();
                result.addActionForClient(new DownloadFileAction(null, comparison.getServerVersion(), comparison, path, serverFile));
            }
            break;
        case NEW:
            /*
             * new on client
             */
            if (mayCreate()) {
                if (isInvalidName(comparison.getClientVersion().getName())) {
                    /*
                     * invalid name, indicate as error with quarantine flag
                     */
                    result.addActionForClient(new ErrorFileAction(null, comparison.getClientVersion(), comparison, path,
                        DriveExceptionCodes.INVALID_FILENAME.create(comparison.getClientVersion().getName()), true));
                } else {
                    /*
                     * let client upload the file
                     */
                    UploadFileAction uploadAction = new UploadFileAction(
                        comparison.getServerVersion(), comparison.getClientVersion(), comparison, path, 0);
                    uploadActions.add(uploadAction);
                    result.addActionForClient(uploadAction);
                }
            } else {
                /*
                 * not allowed, indicate as error with quarantine flag
                 */
                result.addActionForClient(new ErrorFileAction(null, comparison.getClientVersion(), comparison, path,
                    DriveExceptionCodes.NO_CREATE_FILE_PERMISSION.create(path), true));
            }
            break;
        default:
            break;
        }
    }

    @Override
    protected void processConflictingChange(IntermediateSyncResult<FileVersion> result, ThreeWayComparison<FileVersion> comparison) throws OXException {
        if (Change.DELETED == comparison.getServerChange() && Change.DELETED == comparison.getClientChange()) {
            /*
             * both deleted, just let client remove it's metadata
             */
            result.addActionForClient(new AcknowledgeFileAction(comparison.getOriginalVersion(), null, comparison, path));
        } else if ((Change.NEW == comparison.getClientChange() || Change.MODIFIED == comparison.getClientChange()) &&
            (Change.NEW == comparison.getServerChange() || Change.MODIFIED == comparison.getServerChange())) {
            /*
             * name clash for new/modified files, check file equivalence
             */
            if (Change.NONE.equals(Change.get(comparison.getClientVersion(), comparison.getServerVersion()))) {
                /*
                 * same file version, let client update it's metadata
                 */
                ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session);
                result.addActionForClient(new AcknowledgeFileAction(
                    comparison.getOriginalVersion(), comparison.getClientVersion(), comparison, path, serverFileVersion.getFile()));
            } else if (comparison.getClientVersion().getChecksum().equalsIgnoreCase(comparison.getServerVersion().getChecksum()) &&
                comparison.getClientVersion().getName().equalsIgnoreCase(comparison.getServerVersion().getName()) &&
                false == comparison.getClientVersion().getName().equals(comparison.getServerVersion().getName())) {
                /*
                 * same file version with different case, server wins
                 */
                result.addActionForClient(new EditFileAction(
                    comparison.getClientVersion(), comparison.getServerVersion(), comparison, path));
            } else {
                /*
                 * keep both client- and server versions, let client first rename it's file...
                 */
                FileVersion renamedVersion = getRenamedVersion(session, comparison.getClientVersion());
                result.addActionForClient(new EditFileAction(comparison.getClientVersion(), renamedVersion, comparison, path));
                /*
                 * ... then upload it if possible...
                 */
                if (mayCreate()) {
                    UploadFileAction uploadAction = new UploadFileAction(null, renamedVersion, comparison, path, 0);
                    uploadActions.add(uploadAction);
                    result.addActionForClient(uploadAction);
                } else {
                    result.addActionForClient(new ErrorFileAction(comparison.getClientVersion(), renamedVersion, comparison, path,
                        DriveExceptionCodes.NO_CREATE_FILE_PERMISSION.create(path), true));
                }
                /*
                 * ... and download the server version aftwerwards
                 */
                File serverFile = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session).getFile();
                result.addActionForClient(new DownloadFileAction(null, comparison.getServerVersion(), comparison, path, serverFile));
            }
        } else if (Change.DELETED == comparison.getClientChange() && (Change.MODIFIED == comparison.getServerChange() || Change.NEW == comparison.getServerChange())) {
            /*
             * delete-edit conflict, let client download server version
             */
            File serverFile = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session).getFile();
            result.addActionForClient(new DownloadFileAction(null, comparison.getServerVersion(), comparison, path, serverFile));
        } else if ((Change.NEW == comparison.getClientChange() || Change.MODIFIED == comparison.getClientChange()) && Change.DELETED == comparison.getServerChange()) {
            /*
             * edit-delete conflict, let client upload it's file
             */
            if (mayCreate()) {
                UploadFileAction uploadAction = new UploadFileAction(null, comparison.getClientVersion(), comparison, path, 0);
                result.addActionForClient(uploadAction);
                uploadActions.add(uploadAction);
            } else {
                /*
                 * not allowed, indicate as error with quarantine flag
                 */
                result.addActionForClient(new ErrorFileAction(null, comparison.getClientVersion(), comparison, path,
                    DriveExceptionCodes.NO_CREATE_FILE_PERMISSION.create(path), true));
            }
        } else {
            throw new UnsupportedOperationException("Not implemented: Server: " + comparison.getServerChange() + ", Client: " + comparison.getClientChange());
        }
    }

    private boolean mayDelete(FileVersion version) throws OXException {
        int deletePermission = getPermission().getDeletePermission();
        if (FileStoragePermission.DELETE_ALL_OBJECTS <= deletePermission) {
            return true;
        } else if (FileStoragePermission.DELETE_OWN_OBJECTS > deletePermission) {
            return false;
        } else if (FileStoragePermission.DELETE_OWN_OBJECTS == deletePermission) {
            ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(version, path, session);
            return serverFileVersion.getFile().getCreatedBy() == session.getServerSession().getUserId();
        } else {
            throw new UnsupportedOperationException("unknown permission: " + deletePermission);
        }
    }

    protected FileVersion getRenamedVersion(SyncSession session, FileVersion conflictingVersion) {
        String alternativeName = RenameTools.findAlternativeName(conflictingVersion.getName(), usedFilenames, session.getDeviceName());
        if (null != usedFilenames) {
            usedFilenames.add(alternativeName);
        }
        return new SimpleFileVersion(alternativeName, conflictingVersion.getChecksum());
    }

    private boolean mayCreate() throws OXException {
        FileStoragePermission permission = getPermission();
        return FileStoragePermission.CREATE_OBJECTS_IN_FOLDER <= permission.getFolderPermission() &&
            FileStoragePermission.WRITE_OWN_OBJECTS <= permission.getWritePermission();
    }

    private boolean mayModify(FileVersion version) throws OXException {
        int writePermission = getPermission().getWritePermission();
        if (FileStoragePermission.WRITE_ALL_OBJECTS <= writePermission) {
            return true;
        } else if (FileStoragePermission.WRITE_OWN_OBJECTS > writePermission) {
            return false;
        } else if (FileStoragePermission.WRITE_OWN_OBJECTS == writePermission) {
            ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(version, path, session);
            return serverFileVersion.getFile().getCreatedBy() == session.getServerSession().getUserId();
        } else {
            throw new UnsupportedOperationException("unknown permission: " + writePermission);
        }
    }

    private FileStoragePermission getPermission() throws OXException {
        if (null == folderPermission) {
            folderPermission = session.getStorage().getOwnPermission(path);
        }
        return folderPermission;
    }

    /**
     * Gets a value indicating whether the supplied filename is invalid, i.e. it contains illegal characters, is not supported for
     * other reasons, or is excluded from synchronization by definition.
     *
     * @param fileName The filename to check
     * @return <code>true</code> if the filename is considered invalid, <code>false</code>, otherwise
     * @throws OXException
     */
    private static boolean isInvalidName(String fileName) throws OXException {
        return false == SynchronizedFileFilter.getInstance().accept(fileName);
    }

}
