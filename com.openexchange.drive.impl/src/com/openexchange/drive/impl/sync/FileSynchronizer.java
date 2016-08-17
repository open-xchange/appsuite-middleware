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

package com.openexchange.drive.impl.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.actions.AcknowledgeFileAction;
import com.openexchange.drive.impl.actions.DownloadFileAction;
import com.openexchange.drive.impl.actions.EditFileAction;
import com.openexchange.drive.impl.actions.ErrorFileAction;
import com.openexchange.drive.impl.actions.RemoveFileAction;
import com.openexchange.drive.impl.actions.UploadFileAction;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.FileChecksum;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.internal.UploadHelper;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.drive.impl.metadata.DriveMetadata;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.file.storage.composition.FolderID;


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
    private Set<String> normalizedDirectoryNames;

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
        /*
         * handle any conflicting client versions
         */
        if (null != mapper.getMappingProblems().getCaseConflictingClientVersions()) {
            for (FileVersion clientVersion : mapper.getMappingProblems().getCaseConflictingClientVersions()) {
                /*
                 * let client first rename it's file...
                 */
                FileVersion renamedVersion = getRenamedVersion(session, clientVersion);
                ThreeWayComparison<FileVersion> twc = new ThreeWayComparison<FileVersion>();
                twc.setClientVersion(clientVersion);
                syncResult.addActionForClient(new EditFileAction(clientVersion, renamedVersion, twc, path, false));
                /*
                 * ... then upload it if possible
                 */
                if (mayCreate()) {
                    UploadFileAction uploadAction = new UploadFileAction(null, renamedVersion, twc, path, 0);
                    uploadActions.add(uploadAction);
                    syncResult.addActionForClient(uploadAction);
                } else {
                    OXException e = DriveExceptionCodes.NO_CREATE_FILE_PERMISSION.create(path);
                    LOG.warn("Client upload not allowed for {}", clientVersion, e);
                    syncResult.addActionForClient(new ErrorFileAction(clientVersion, renamedVersion, null, path, e , true));
                }
            }
        }
        if (null != mapper.getMappingProblems().getUnicodeConflictingClientVersions()) {
            for (FileVersion clientVersion : mapper.getMappingProblems().getUnicodeConflictingClientVersions()) {
                /*
                 * indicate as error with quarantine flag
                 */
                ThreeWayComparison<FileVersion> twc = new ThreeWayComparison<FileVersion>();
                twc.setClientVersion(clientVersion);
                OXException e = DriveExceptionCodes.CONFLICTING_FILENAME.create(clientVersion.getName());
                LOG.warn("Client upload not allowed due to unicode conflicts: {}", clientVersion, e);
                syncResult.addActionForClient(new ErrorFileAction(null, clientVersion, twc, path, e, true));
            }
        }
        if (null != mapper.getMappingProblems().getDuplicateClientVersions()) {
            for (FileVersion clientVersion : mapper.getMappingProblems().getDuplicateClientVersions()) {
                /*
                 * indicate as error with quarantine flag
                 */
                ThreeWayComparison<FileVersion> twc = new ThreeWayComparison<FileVersion>();
                twc.setClientVersion(clientVersion);
                OXException e = DriveExceptionCodes.CONFLICTING_FILENAME.create(clientVersion.getName());
                LOG.warn("Duplicate file version indicated by client: {}", clientVersion, e);
                syncResult.addActionForClient(new ErrorFileAction(null, clientVersion, twc, path, e, true));
            }
        }
        return syncResult;
    }

    @Override
    protected int getMaxActions() {
        return DriveConfig.getInstance().getMaxFileActions();
    }

    @Override
    protected int processServerChange(IntermediateSyncResult<FileVersion> result, ThreeWayComparison<FileVersion> comparison) throws OXException {
        switch (comparison.getServerChange()) {
        case DELETED:
            /*
             * deleted on server, delete file on client, too
             */
            result.addActionForClient(new RemoveFileAction(comparison.getClientVersion(), comparison, path));
            return 1;
        case MODIFIED:
            if (comparison.getServerVersion().getChecksum().equalsIgnoreCase(comparison.getClientVersion().getChecksum()) &&
                comparison.getServerVersion().getName().equalsIgnoreCase(comparison.getClientVersion().getName()) &&
                false == comparison.getServerVersion().getName().equals(comparison.getClientVersion().getName())) {
                /*
                 * just renamed on server, let client edit the file
                 */
                result.addActionForClient(new EditFileAction(
                    comparison.getClientVersion(), comparison.getServerVersion(), comparison, path));
                return 1;
            } else {
                /*
                 * modified on server, let client download the file
                 */

                result.addActionForClient(createDownloadAction(comparison.getClientVersion(),
                    ServerFileVersion.valueOf(comparison.getServerVersion(), path, session), comparison));
                return 1;
            }
        case NEW:
            /*
             * new on server, let client download the file
             */
            result.addActionForClient(createDownloadAction(comparison.getClientVersion(),
                ServerFileVersion.valueOf(comparison.getServerVersion(), path, session), comparison));
            return 1;
        default:
            return 0;
        }
    }

    @Override
    protected int processClientChange(IntermediateSyncResult<FileVersion> result, ThreeWayComparison<FileVersion> comparison) throws OXException {
        switch (comparison.getClientChange()) {
        case DELETED:
            if (mayDelete(comparison.getServerVersion())) {
                /*
                 * delete on server, too, let client remove it's metadata
                 */
                result.addActionForServer(new RemoveFileAction(comparison.getServerVersion(), comparison, path));
                result.addActionForClient(new AcknowledgeFileAction(session, comparison.getOriginalVersion(), null, comparison, path));
                return 1;
            } else {
                /*
                 * not allowed, let client re-download the file, indicate as error without quarantine flag
                 */
                OXException e = DriveExceptionCodes.NO_DELETE_FILE_PERMISSION.create(comparison.getServerVersion().getName(), path);
                LOG.warn("Client change refused for {}", comparison.getServerVersion(), e);
                result.addActionForClient(createDownloadAction(comparison.getClientVersion(),
                    ServerFileVersion.valueOf(comparison.getServerVersion(), path, session), comparison));
                result.addActionForClient(new ErrorFileAction(comparison.getClientVersion(), comparison.getServerVersion(), comparison,
                    path, e, false));
                return 2;
            }
        case MODIFIED:
            if (mayModify(comparison.getServerVersion())) {
                if (comparison.getClientVersion().getChecksum().equalsIgnoreCase(comparison.getServerVersion().getChecksum()) &&
                    comparison.getClientVersion().getName().equalsIgnoreCase(comparison.getServerVersion().getName()) &&
                    false == comparison.getClientVersion().getName().equals(comparison.getServerVersion().getName())) {
                    /*
                     * just renamed on client, let server edit the file, acknowledge rename to client
                     */
                    ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session);
                    EditFileAction serverEdit = new EditFileAction(serverFileVersion, comparison.getClientVersion(), comparison, path);
                    AcknowledgeFileAction clientAcknowledge = new AcknowledgeFileAction(session,
                        comparison.getOriginalVersion(), comparison.getClientVersion(), comparison, path, serverFileVersion.getFile());
                    clientAcknowledge.setDependingAction(serverEdit);
                    result.addActionForServer(serverEdit);
                    result.addActionForClient(clientAcknowledge);
                    return 1;
                } else {
                    /*
                     * modified on client, let client upload the modified file
                     */
                    UploadFileAction uploadAction = new UploadFileAction(
                        comparison.getServerVersion(), comparison.getClientVersion(), comparison, path, 0);
                    uploadActions.add(uploadAction);
                    result.addActionForClient(uploadAction);
                    return 1;
                }
            } else if (session.getDriveSession().useDriveMeta() && DriveConstants.METADATA_FILENAME.equals(comparison.getServerVersion().getName())) {
                /*
                 * checksum mismatch for .drive-meta file, enforce server-side re-calculation of .drive-meta file's checksum to be safe
                 */
                ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(comparison.getServerVersion(), path, session);
                session.getChecksumStore().removeFileChecksum(serverFileVersion.getFileChecksum());
                FileChecksum recalculatedChecksum = ChecksumProvider.getChecksum(session, serverFileVersion.getFile());
                if (false == recalculatedChecksum.getChecksum().equals(serverFileVersion.getChecksum())) {
                    LOG.debug("Using re-calculated checksum for .drive-meta file: {}", recalculatedChecksum);
                    session.getChecksumStore().removeDirectoryChecksum(new FolderID(serverFileVersion.getFile().getFolderId()));
                    serverFileVersion = new ServerFileVersion(serverFileVersion.getFile(), recalculatedChecksum);
                    if (serverFileVersion.getChecksum().equals(comparison.getClientVersion().getChecksum())) {
                        /*
                         * client version matches, send acknowledge
                         */
                        result.addActionForClient(new AcknowledgeFileAction(session, comparison.getClientVersion(), serverFileVersion, comparison, path));
                        return 1;
                    }
                }
                /*
                 * not allowed, let client re-download the file, indicate as error without quarantine flag
                 */
                OXException e = DriveExceptionCodes.NO_MODIFY_FILE_PERMISSION.create(comparison.getServerVersion().getName(), path);
                LOG.warn("Client change refused for " + comparison.getServerVersion(), e);
                result.addActionForClient(createDownloadAction(comparison.getClientVersion(), serverFileVersion, comparison));
                result.addActionForClient(new ErrorFileAction(comparison.getClientVersion(), serverFileVersion, comparison, path, e, false));
                return 2;
            } else if (mayCreate()) {
                /*
                 * not allowed, keep both client- and server versions, let client first rename it's file...
                 */
                FileVersion renamedVersion = getRenamedVersion(session, comparison.getClientVersion());
                result.addActionForClient(new EditFileAction(comparison.getClientVersion(), renamedVersion, comparison, path, false));
                /*
                 * ... then mark that file as error (without quarantine)...
                 */
                OXException e = DriveExceptionCodes.NO_MODIFY_FILE_PERMISSION.create(comparison.getServerVersion().getName(), path);
                LOG.warn("Client change refused for {}", comparison.getServerVersion(), e);
                result.addActionForClient(new ErrorFileAction(
                    comparison.getClientVersion(), comparison.getServerVersion(), comparison, path, e, false));
                /*
                 * ... then upload it, and download the server version afterwards
                 */
                UploadFileAction uploadAction = new UploadFileAction(null, renamedVersion, comparison, path, 0);
                uploadActions.add(uploadAction);
                result.addActionForClient(uploadAction);
                result.addActionForClient(createDownloadAction(null, ServerFileVersion.valueOf(comparison.getServerVersion(), path, session), comparison));
                return 4;
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
                result.addActionForClient(createDownloadAction(null, ServerFileVersion.valueOf(comparison.getServerVersion(), path, session), comparison));
                return 3;
            }
        case NEW:
            /*
             * new on client
             */
            if (mayCreate()) {
                    if (FilenameValidationUtils.isInvalidFileName(comparison.getClientVersion().getName())) {
                    /*
                     * invalid name, indicate as error with quarantine flag
                     */
                    result.addActionForClient(new ErrorFileAction(null, comparison.getClientVersion(), comparison, path,
                        DriveExceptionCodes.INVALID_FILENAME.create(comparison.getClientVersion().getName()), true));
                    return 1;
                } else if (DriveUtils.isIgnoredFileName(session.getDriveSession(), path, comparison.getClientVersion().getName())) {
                    /*
                     * ignored file, indicate as error with quarantine flag
                     */
                    result.addActionForClient(new ErrorFileAction(null, comparison.getClientVersion(), comparison, path,
                        DriveExceptionCodes.IGNORED_FILENAME.create(comparison.getClientVersion().getName()), true));
                    return 1;
                } else {
                    /*
                     * new on client, check for potential directory name collisions
                     */
                    if (getNormalizedDirectoryNames().contains(PathNormalizer.normalize(comparison.getClientVersion().getName()))) {
                        /*
                         * collision with directory on same level, indicate as error with quarantine flag
                         */
                        result.addActionForClient(new ErrorFileAction(null, comparison.getClientVersion(), comparison, path,
                            DriveExceptionCodes.LEVEL_CONFLICTING_FILENAME.create(comparison.getClientVersion().getName(), path), true));
                        return 1;
                    }
                    /*
                     * let client upload the file
                     */
                    UploadFileAction uploadAction = new UploadFileAction(
                        comparison.getServerVersion(), comparison.getClientVersion(), comparison, path, 0);
                    uploadActions.add(uploadAction);
                    result.addActionForClient(uploadAction);
                    return 1;
                }
            } else {
                /*
                 * not allowed, indicate as error with quarantine flag
                 */
                result.addActionForClient(new ErrorFileAction(null, comparison.getClientVersion(), comparison, path,
                    DriveExceptionCodes.NO_CREATE_FILE_PERMISSION.create(path), true));
                return 1;
            }
        default:
            return 0;
        }
    }

    @Override
    protected int processConflictingChange(IntermediateSyncResult<FileVersion> result, ThreeWayComparison<FileVersion> comparison) throws OXException {
        if (Change.DELETED == comparison.getServerChange() && Change.DELETED == comparison.getClientChange()) {
            /*
             * both deleted, just let client remove it's metadata
             */
            result.addActionForClient(new AcknowledgeFileAction(session, comparison.getOriginalVersion(), null, comparison, path));
            return 0;
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
                result.addActionForClient(new AcknowledgeFileAction(session,
                    comparison.getOriginalVersion(), comparison.getClientVersion(), comparison, path, serverFileVersion.getFile()));
                return 0;
            } else if (session.getDriveSession().useDriveMeta() && DriveConstants.METADATA_FILENAME.equals(comparison.getServerVersion().getName())) {
                /*
                 * server's metadata file always wins, let client re-download the file
                 */
                result.addActionForClient(createDownloadAction(comparison.getClientVersion(),
                    ServerFileVersion.valueOf(comparison.getServerVersion(), path, session), comparison));
                return 1;
            }  else if (comparison.getClientVersion().getChecksum().equalsIgnoreCase(comparison.getServerVersion().getChecksum()) &&
                comparison.getClientVersion().getName().equalsIgnoreCase(comparison.getServerVersion().getName()) &&
                false == comparison.getClientVersion().getName().equals(comparison.getServerVersion().getName())) {
                /*
                 * same file version with different case, server wins
                 */
                result.addActionForClient(new EditFileAction(
                    comparison.getClientVersion(), comparison.getServerVersion(), comparison, path));
                return 1;
            } else {
                /*
                 * keep both client- and server versions, let client first rename it's file...
                 */
                FileVersion renamedVersion = getRenamedVersion(session, comparison.getClientVersion());
                result.addActionForClient(new EditFileAction(comparison.getClientVersion(), renamedVersion, comparison, path, false));
                /*
                 * ... then upload it if possible...
                 */
                if (mayCreate()) {
                    UploadFileAction uploadAction = new UploadFileAction(null, renamedVersion, comparison, path, 0);
                    uploadActions.add(uploadAction);
                    result.addActionForClient(uploadAction);
                } else {
                    OXException e = DriveExceptionCodes.NO_CREATE_FILE_PERMISSION.create(path);
                    LOG.warn("Client upload not allowed for {}", comparison.getClientVersion(), e);
                    result.addActionForClient(new ErrorFileAction(
                        comparison.getClientVersion(), renamedVersion, comparison, path, e, true));
                }
                /*
                 * ... and download the server version aftwerwards
                 */
                result.addActionForClient(createDownloadAction(null, ServerFileVersion.valueOf(comparison.getServerVersion(), path, session), comparison));
                return 3;
            }
        } else if (Change.DELETED == comparison.getClientChange() && (Change.MODIFIED == comparison.getServerChange() || Change.NEW == comparison.getServerChange())) {
            /*
             * delete-edit conflict, let client download server version
             */
            result.addActionForClient(createDownloadAction(null, ServerFileVersion.valueOf(comparison.getServerVersion(), path, session), comparison));
            return 1;
        } else if ((Change.NEW == comparison.getClientChange() || Change.MODIFIED == comparison.getClientChange()) && Change.DELETED == comparison.getServerChange()) {
            /*
             * edit-delete conflict, let client upload it's file
             */
            if (mayCreate()) {
                UploadFileAction uploadAction = new UploadFileAction(null, comparison.getClientVersion(), comparison, path, 0);
                result.addActionForClient(uploadAction);
                uploadActions.add(uploadAction);
                return 1;
            } else {
                /*
                 * not allowed, indicate as error with quarantine flag
                 */
                result.addActionForClient(new ErrorFileAction(null, comparison.getClientVersion(), comparison, path,
                    DriveExceptionCodes.NO_CREATE_FILE_PERMISSION.create(path), true));
                return 1;
            }
        } else {
            throw new UnsupportedOperationException("Not implemented: Server: " + comparison.getServerChange() + ", Client: " + comparison.getClientChange());
        }
    }

    private boolean mayDelete(FileVersion version) throws OXException {
        if (session.getDriveSession().useDriveMeta() && DriveConstants.METADATA_FILENAME.equals(version.getName())) {
            return false;
        }
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
        if (session.getDriveSession().useDriveMeta() && DriveConstants.METADATA_FILENAME.equals(version.getName())) {
            return false;
        }
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
     * Creates a new DOWNLOAD file action using the supplied parameters, implicitly validating the checksum again for any slipstreamed
     * <code>.drive-meta</code> file.
     *
     * @param fileVersion The original file version
     * @param newVersion The new file version
     * @param comparison The underlying comparison
     * @return The download file action
     */
    private DownloadFileAction createDownloadAction(FileVersion fileVersion, ServerFileVersion newVersion, ThreeWayComparison<FileVersion> comparison) throws OXException {
        if (null != newVersion) {
            if (session.getDriveSession().useDriveMeta() && DriveMetadata.class.isInstance(newVersion.getFile())) {
                DriveMetadata metadata = (DriveMetadata) newVersion.getFile();
                String fileMD5Sum = metadata.getFileMD5Sum();
                if (false == newVersion.getChecksum().equals(fileMD5Sum)) {
                    /*
                     * stored checksum no longer valid, invalidate any affected checksums & propagate new checksum in client action
                     */
                    session.trace("Checksum " + newVersion.getChecksum() + " different from actual checksum " +
                        fileMD5Sum + " of .drive-meta file, invalidating stored checksums.");
                    session.getChecksumStore().removeFileChecksums(DriveUtils.getFileID(metadata));
                    session.getChecksumStore().removeDirectoryChecksum(new FolderID(metadata.getFolderId()));
                    newVersion = new ServerFileVersion(metadata, ChecksumProvider.getChecksum(session, metadata));
                }
            }
        }
        return new DownloadFileAction(session, fileVersion, newVersion, comparison, path);
    }

    /**
     * Gets a set of the normalized directory names contained in the synchronized folder.
     *
     * @return The normalized folder names
     * @throws OXException
     */
    private Set<String> getNormalizedDirectoryNames() throws OXException {
        if (null == normalizedDirectoryNames) {
            normalizedDirectoryNames = DriveUtils.getNormalizedFolderNames(session.getStorage().getSubfolders(path).values());
        }
        return normalizedDirectoryNames;
    }

}
