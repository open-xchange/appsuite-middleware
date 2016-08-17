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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.actions.AcknowledgeDirectoryAction;
import com.openexchange.drive.impl.actions.EditDirectoryAction;
import com.openexchange.drive.impl.actions.ErrorDirectoryAction;
import com.openexchange.drive.impl.actions.RemoveDirectoryAction;
import com.openexchange.drive.impl.actions.SyncDirectoryAction;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;


/**
 * {@link DirectorySynchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectorySynchronizer extends Synchronizer<DirectoryVersion> {

    private Map<String, Set<String>> normalizedFilesnamesPerFolder;

    public DirectorySynchronizer(SyncSession session, VersionMapper<DirectoryVersion> mapper) throws OXException {
        super(session, mapper);
    }

    @Override
    public IntermediateSyncResult<DirectoryVersion> sync() throws OXException {
        IntermediateSyncResult<DirectoryVersion> syncResult = super.sync();
        /*
         * handle any conflicting client versions
         */
        if (null != mapper.getMappingProblems().getCaseConflictingClientVersions()) {
            for (DirectoryVersion clientVersion : mapper.getMappingProblems().getCaseConflictingClientVersions()) {
                /*
                 * indicate case-conflicting version as error with quarantine flag
                 */
                ThreeWayComparison<DirectoryVersion> twc = new ThreeWayComparison<DirectoryVersion>();
                twc.setClientVersion(clientVersion);
                OXException e = DriveExceptionCodes.CONFLICTING_PATH.create(clientVersion.getPath());
                LOG.warn("Client change refused due to case conflicting name: {}", clientVersion, e);
                syncResult.addActionForClient(new ErrorDirectoryAction(null, clientVersion, twc, e, true, false));
            }
        }
        if (null != mapper.getMappingProblems().getUnicodeConflictingClientVersions()) {
            for (DirectoryVersion clientVersion : mapper.getMappingProblems().getUnicodeConflictingClientVersions()) {
                /*
                 * indicate unicode-conflicting version as error with quarantine flag
                 */
                ThreeWayComparison<DirectoryVersion> twc = new ThreeWayComparison<DirectoryVersion>();
                twc.setClientVersion(clientVersion);
                OXException e = DriveExceptionCodes.CONFLICTING_PATH.create(clientVersion.getPath());
                LOG.warn("Client change refused due to unicode conflicting name: {}", clientVersion, e);
                syncResult.addActionForClient(new ErrorDirectoryAction(null, clientVersion, twc, e, true, false));
            }
        }
        if (null != mapper.getMappingProblems().getDuplicateClientVersions()) {
            for (DirectoryVersion clientVersion : mapper.getMappingProblems().getDuplicateClientVersions()) {
                /*
                 * indicate duplicate version as error with quarantine flag
                 */
                ThreeWayComparison<DirectoryVersion> twc = new ThreeWayComparison<DirectoryVersion>();
                twc.setClientVersion(clientVersion);
                OXException e = DriveExceptionCodes.CONFLICTING_PATH.create(clientVersion.getPath());
                LOG.warn("Duplicate directory version indicated by client: {}", clientVersion, e);
                syncResult.addActionForClient(new ErrorDirectoryAction(null, clientVersion, twc, e, true, false));
            }
        }
        return syncResult;
    }

    @Override
    protected int getMaxActions() {
        return DriveConfig.getInstance().getMaxDirectoryActions();
    }

    @Override
    protected int processServerChange(IntermediateSyncResult<DirectoryVersion> result, ThreeWayComparison<DirectoryVersion> comparison) throws OXException {
        switch (comparison.getServerChange()) {
        case DELETED:
            /*
             * deleted on server, check for potential conflicts in subfolders
             */
            String normalizedPathToDelete = PathNormalizer.normalize(comparison.getClientVersion().getPath()).toLowerCase();
            for (Entry<String, ThreeWayComparison<DirectoryVersion>> entry : mapper) {
                Change clientChange = entry.getValue().getClientChange();
                if (Change.NONE != clientChange && Change.DELETED != clientChange) {
                    String normalizedPath = PathNormalizer.normalize(entry.getKey()).toLowerCase();
                    if (normalizedPath.startsWith(normalizedPathToDelete) && false == normalizedPath.equals(normalizedPathToDelete)) {
                        /*
                         * conflicting change in one of the subfolders, don't delete directory at client for now
                         */
                        if (session.isTraceEnabled()) {
                            session.trace("Skipping action \""+ new RemoveDirectoryAction(comparison.getClientVersion(), comparison) +
                                "\" due to conflicting client-side changes in subfolders.");
                        }
                        return 0;
                    }
                }
            }
            /*
             * deleted on server, delete directory on client, too
             */
            result.addActionForClient(new RemoveDirectoryAction(comparison.getClientVersion(), comparison));
            return 1;
        case MODIFIED:
            String normalizedClientPath = PathNormalizer.normalize(comparison.getClientVersion().getPath());
            String normalizedServerPath = PathNormalizer.normalize(comparison.getServerVersion().getPath());
            if (normalizedClientPath.equalsIgnoreCase(normalizedServerPath) &&
                false == normalizedClientPath.equals(normalizedServerPath)) {
                /*
                 * case-renamed on server, let client edit the directory first, then synchronize it if needed
                 */
                result.addActionForClient(new EditDirectoryAction(comparison.getClientVersion(), comparison.getServerVersion(), comparison));
                if (false == comparison.getClientVersion().getChecksum().equalsIgnoreCase(comparison.getServerVersion().getChecksum())) {
                    result.addActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
                    return 2;
                } else {
                    return 1;
                }
            } else {
                /*
                 * contents modified on server, let client synchronize the folder
                 */
                result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
                return 1;
            }
        case NEW:
            /*
             * new on server, let client synchronize the folder
             */
            result.addActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
            return 1;
        default:
            return 0;
        }
    }

    @Override
    protected int processClientChange(IntermediateSyncResult<DirectoryVersion> result, ThreeWayComparison<DirectoryVersion> comparison) throws OXException {
        switch (comparison.getClientChange()) {
        case DELETED:
            if (mayDelete(comparison.getServerVersion(), true)) {
                /*
                 * deleted on client, delete on server, too, let client remove it's metadata
                 */
                result.addActionForServer(new RemoveDirectoryAction(comparison.getServerVersion(), comparison));
                result.addActionForClient(new AcknowledgeDirectoryAction(comparison.getOriginalVersion(), null, comparison));
                return 1;
            } else {
                /*
                 * not allowed, let client synchronize the directory again, indicate as error without quarantine flag
                 */
                result.addActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
                OXException e = DriveExceptionCodes.NO_DELETE_DIRECTORY_PERMISSION.create(comparison.getServerVersion().getPath());
                LOG.warn("Client change refused for {}", comparison.getServerVersion(), e);
                result.addActionForClient(new ErrorDirectoryAction(
                    comparison.getClientVersion(), comparison.getServerVersion(), comparison, e, false, false));
                return 2;
            }
        case NEW:
            if (DriveUtils.isInvalidPath(comparison.getClientVersion().getPath())) {
                /*
                 * invalid path, indicate as error with quarantine flag
                 */
                OXException e = DriveExceptionCodes.INVALID_PATH.create(comparison.getClientVersion().getPath());
                LOG.warn("Client change refused due to invalid path: {}", comparison.getClientVersion(), e);
                result.addActionForClient(new ErrorDirectoryAction(
                    null, comparison.getClientVersion(), comparison, e, true, false));
                return 1;
            } else if (DriveUtils.isIgnoredPath(session, comparison.getClientVersion().getPath())) {
                /*
                 * ignored path, indicate as error with quarantine flag
                 */
                OXException e = DriveExceptionCodes.IGNORED_PATH.create(comparison.getClientVersion().getPath());
                LOG.warn("Client change refused due to ignored path: {}", comparison.getClientVersion(), e);
                result.addActionForClient(new ErrorDirectoryAction(
                    null, comparison.getClientVersion(), comparison, e, true, false));
                return 1;
            } else {
                DirectoryVersion lastExistingParentVersion = getLastExistingParentVersion(comparison.getClientVersion().getPath());
                if (mayCreate(lastExistingParentVersion.getPath())) {
                    /*
                     * new on client, check for potential directory name collisions
                     */
                    String directoryName = getFirstDirectoryNameToCreate(lastExistingParentVersion, comparison.getClientVersion().getPath());
                    FileStorageFolder parentFolder = session.getStorage().getFolder(lastExistingParentVersion.getPath());
                    if (getNormalizedFilenames(parentFolder.getId()).contains(PathNormalizer.normalize(directoryName.toLowerCase()))) {
                        /*
                         * collision with file on same level, indicate as error with quarantine flag
                         */
                        OXException e = DriveExceptionCodes.LEVEL_CONFLICTING_PATH.create(
                            comparison.getClientVersion().getPath(), lastExistingParentVersion.getPath());
                        LOG.warn("Client change refused due to conflicting path: {}", comparison.getClientVersion(), e);
                        result.addActionForClient(new ErrorDirectoryAction(
                            null, comparison.getClientVersion(), comparison, e, true, false));
                        return 1;
                    }
                    /*
                     * create directory on server, let client synchronize the directory
                     */
                    result.addActionForServer(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
                    result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
                    return 2;
                } else {
                    /*
                     * not allowed, indicate as error with quarantine flag
                     */
                    OXException e = DriveExceptionCodes.NO_CREATE_DIRECTORY_PERMISSION.create(lastExistingParentVersion.getPath());
                    LOG.warn("Client change refused due to missing permissions: {}", comparison.getClientVersion(), e);
                    result.addActionForClient(new ErrorDirectoryAction(
                        null, comparison.getClientVersion(), comparison, e, true, false));
                    return 1;
                }
            }
        case MODIFIED:
            int nonTrivialChanges = 0;
            String normalizedClientPath = PathNormalizer.normalize(comparison.getClientVersion().getPath());
            String normalizedServerPath = PathNormalizer.normalize(comparison.getServerVersion().getPath());
            if (normalizedClientPath.equalsIgnoreCase(normalizedServerPath) &&
                false == normalizedClientPath.equals(normalizedServerPath)) {
                /*
                 * case-renamed on client, let server edit the directory
                 */
                result.addActionForServer(new EditDirectoryAction(comparison.getServerVersion(), comparison.getClientVersion(), comparison));
                nonTrivialChanges++;
            }
            if (false == comparison.getClientVersion().getChecksum().equalsIgnoreCase(comparison.getServerVersion().getChecksum())) {
                /*
                 * modified on client, let client synchronize the directory
                 */
                result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
                nonTrivialChanges++;
            }
            return nonTrivialChanges;
        default:
            return 0;
        }
    }

    @Override
    protected int processConflictingChange(IntermediateSyncResult<DirectoryVersion> result, ThreeWayComparison<DirectoryVersion> comparison) throws OXException {
        if (Change.DELETED == comparison.getServerChange() && Change.DELETED == comparison.getClientChange()) {
            /*
             * both deleted, just let client remove it's metadata
             */
            result.addActionForClient(new AcknowledgeDirectoryAction(comparison.getOriginalVersion(), null, comparison));
            return 0;
        } else if ((Change.NEW == comparison.getClientChange() || Change.MODIFIED == comparison.getClientChange()) &&
            (Change.NEW == comparison.getServerChange() || Change.MODIFIED == comparison.getServerChange())) {
            /*
             * name clash for new/modified directories, check directory content equivalence
             */
            if (Change.NONE.equals(Change.get(comparison.getClientVersion(), comparison.getServerVersion()))) {
                /*
                 * same directory version, let client update it's metadata
                 */
                result.addActionForClient(new AcknowledgeDirectoryAction(comparison.getOriginalVersion(), comparison.getClientVersion(), comparison));
                if (false == DriveConstants.EMPTY_MD5.equals(comparison.getClientVersion().getChecksum()) &&
                    Change.NEW.equals(comparison.getClientChange()) && Change.NEW.equals(comparison.getServerChange())) {
                    /*
                     * first-time synchronization of identical, but non-empty directory, let client sync directory to acknowledge the contents
                     */
                    result.addActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
                    return 1;
                } else {
                    return 0;
                }
            } else {
                String normalizedClientPath = PathNormalizer.normalize(comparison.getClientVersion().getPath());
                String normalizedServerPath = PathNormalizer.normalize(comparison.getServerVersion().getPath());
                if (normalizedClientPath.equalsIgnoreCase(normalizedServerPath) &&
                    false == normalizedClientPath.equals(normalizedServerPath)) {
                    /*
                     * same directory version with different case, server wins, so let client first rename its version, then sync it if needed
                     */
                    result.addActionForClient(new EditDirectoryAction(comparison.getClientVersion(), comparison.getServerVersion(), comparison));
                    if (false == comparison.getClientVersion().getChecksum().equalsIgnoreCase(comparison.getServerVersion().getChecksum())) {
                        result.addActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
                        return 2;
                    } else {
                        return 1;
                    }
                }
                /*
                 * different contents, let client synchronize the directory
                 */
                result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
                return 1;
            }
        } else if (Change.DELETED == comparison.getClientChange() && (Change.MODIFIED == comparison.getServerChange() || Change.NEW == comparison.getServerChange())) {
            /*
             * delete-edit conflict, let client synchronize the directory
             */
            result.addActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
            return 1;
        } else if ((Change.NEW == comparison.getClientChange() || Change.MODIFIED == comparison.getClientChange()) && Change.DELETED == comparison.getServerChange()) {
            /*
             * edit-delete conflict, leave on server, let client synchronize the directory
             */
            DirectoryVersion lastExistingParentVersion = getLastExistingParentVersion(comparison.getClientVersion().getPath());
            if (mayCreate(lastExistingParentVersion.getPath())) {
                String directoryName = getFirstDirectoryNameToCreate(lastExistingParentVersion, comparison.getClientVersion().getPath());
                FileStorageFolder parentFolder = session.getStorage().getFolder(lastExistingParentVersion.getPath());
                if (getNormalizedFilenames(parentFolder.getId()).contains(PathNormalizer.normalize(directoryName.toLowerCase()))) {
                    /*
                     * collision with file on same level, indicate as error with quarantine flag
                     */
                    OXException e = DriveExceptionCodes.LEVEL_CONFLICTING_PATH.create(
                        comparison.getClientVersion().getPath(), lastExistingParentVersion.getPath());
                    LOG.warn("Client change refused due to conflicting path: {}", comparison.getClientVersion(), e);
                    result.addActionForClient(new ErrorDirectoryAction(
                        null, comparison.getClientVersion(), comparison, e, true, false));
                    return 1;
                }
                /*
                 * let client synchronize the directory
                 */
                result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
                return 1;
            } else {
                OXException e = DriveExceptionCodes.NO_CREATE_DIRECTORY_PERMISSION.create(lastExistingParentVersion.getPath());
                LOG.warn("Client change refused due to missing permissions: {}", comparison.getClientVersion(), e);
                result.addActionForClient(new ErrorDirectoryAction(
                    null, comparison.getClientVersion(), comparison, e, true, false));
                return 1;
            }
        } else {
            throw new UnsupportedOperationException("Not implemented: Server: " + comparison.getServerChange() + ", Client: " + comparison.getClientChange());
        }
    }

    /**
     * Gets a value indicating whether the user is allowed to delete the supplied directory version (and optionally any subfolder
     * recursively), i.e. he has administrative permissions at the folder level and is allowed to delete each contained file.
     *
     * @param version The directory version to check
     * @param checkSubfolders <code>true</code> to check contained subfolders recursively, <code>false</code>, otherwise
     * @return <code>true</code> if deletion is permitted, <code>false</code>, otherwise
     * @throws OXException
     */
    private boolean mayDelete(DirectoryVersion version, boolean checkSubfolders) throws OXException {
        if (DriveConstants.ROOT_PATH.equals(version.getPath())) {
            return false;
        }
        FileStorageFolder folder = session.getStorage().getFolder(version.getPath());
        if (folder.isDefaultFolder()) {
            return false;
        }
        FileStoragePermission ownPermission = folder.getOwnPermission();
        if (false == ownPermission.isAdmin()) {
            return false;
        }
        if (false == DriveConstants.EMPTY_MD5.equals(version.getChecksum())) {
            if (FileStoragePermission.DELETE_OWN_OBJECTS > ownPermission.getDeletePermission()) {
                return false;
            }
            if (FileStoragePermission.DELETE_OWN_OBJECTS == ownPermission.getDeletePermission()) {
                String folderID = session.getStorage().getFolderID(version.getPath());
                List<File> files = session.getStorage().getFilesInFolder(folderID, true, null, Arrays.asList(Field.CREATED_BY));
                for (File file : files) {
                    if (file.getCreatedBy() != session.getServerSession().getUserId()) {
                        return false;
                    }
                }
            }
        }
        if (checkSubfolders) {
            for (DirectoryVersion directoryVersion : getKnownSubDirectories(version)) {
                if (false == mayDelete(directoryVersion, false)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets a value indicating whether the user is allowed to create subdirectories in the supplied path, i.e. he has "create
     * subfolders" permissions in the parent folder.
     *
     * @param parentPath The path to check
     * @return <code>true</code> if directory creation is permitted, <code>false</code>, otherwise
     * @throws OXException
     */
    private boolean mayCreate(String parentPath) throws OXException {
        return FileStoragePermission.CREATE_SUB_FOLDERS <= session.getStorage().getOwnPermission(parentPath).getFolderPermission();
    }

    /**
     * Recursively gets all known subdirectories of a directory version (not including the supplied version itself).
     *
     * @param version The path to get the subdirectories for
     * @return The subdirectories, or an empty list if none were found
     */
    private List<DirectoryVersion> getKnownSubDirectories(DirectoryVersion version) {
        List<DirectoryVersion> subDirectories = new ArrayList<DirectoryVersion>();
        String prefix = version.getPath() + DriveConstants.PATH_SEPARATOR;
        for (DirectoryVersion directoryVersion : mapper.getServerVersions()) {
            String candidatePath = directoryVersion.getPath();
            if (candidatePath.startsWith(prefix)) {
                subDirectories.add(directoryVersion);
            }
        }
        return subDirectories;
    }

    /**
     * Gets the server directory version of the last parent path that exists, i.e. that is already known by the server.
     *
     * @param path The path to get the last existing parent version for
     * @return The last existing parent directory version, down to the root folder if needed
     */
    private DirectoryVersion getLastExistingParentVersion(String path) {
        Collection<? extends DirectoryVersion> serverVersions = mapper.getServerVersions();
        String currentPath = path;
        int idx;
        do {
            idx = currentPath.lastIndexOf(DriveConstants.PATH_SEPARATOR);
            currentPath = 0 < idx ? currentPath.substring(0, idx) : DriveConstants.ROOT_PATH;
            for (DirectoryVersion serverVersion : serverVersions) {
                if (serverVersion.getPath().equals(currentPath)) {
                    return serverVersion;
                }
            }
        } while (0 < idx);
        return null; // not possible
    }

    /**
     * Gets a set of the normalized names contained in a folder.
     *
     * @param folderID The identifier of the folder to get the contained filenames for
     * @return The normalized file names
     * @throws OXException
     */
    private Set<String> getNormalizedFilenames(String folderID) throws OXException {
        if (null == normalizedFilesnamesPerFolder) {
            normalizedFilesnamesPerFolder = new HashMap<String, Set<String>>();
        }
        Set<String> normalizedFilenames = normalizedFilesnamesPerFolder.get(folderID);
        if (null == normalizedFilenames) {
            normalizedFilenames = DriveUtils.getNormalizedFileNames(session.getStorage().getFilesInFolder(
                folderID, true, null, Arrays.asList(File.Field.FILENAME)), true);
            normalizedFilesnamesPerFolder.put(folderID, normalizedFilenames);
        }
        return normalizedFilenames;
    }

    /**
     * Gets the name of the first directory that would be created when creating all folders in a path, based on the last existing
     * directory version.
     *
     * @param lastExistingParentVersion The last existing directory version
     * @param pathToCreate The path that should be created
     * @return The name of the directory that is created right below the parent directory
     */
    private static String getFirstDirectoryNameToCreate(DirectoryVersion lastExistingParentVersion, String pathToCreate) {
        String normalizedParentPath = PathNormalizer.normalize(lastExistingParentVersion.getPath());
        if (false == DriveConstants.ROOT_PATH.equals(normalizedParentPath)) {
            normalizedParentPath = normalizedParentPath + DriveConstants.PATH_SEPARATOR;
        }
        String normalizedPathToCreate = PathNormalizer.normalize(pathToCreate).substring(normalizedParentPath.length());
        int idx = normalizedPathToCreate.indexOf(DriveConstants.PATH_SEPARATOR);
        return 0 < idx ? normalizedPathToCreate.substring(0, idx) : normalizedPathToCreate;
    }

}
