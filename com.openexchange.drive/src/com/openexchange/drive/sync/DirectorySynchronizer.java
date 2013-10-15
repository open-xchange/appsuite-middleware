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

import java.util.Collection;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveConstants;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.actions.AcknowledgeDirectoryAction;
import com.openexchange.drive.actions.EditDirectoryAction;
import com.openexchange.drive.actions.ErrorDirectoryAction;
import com.openexchange.drive.actions.RemoveDirectoryAction;
import com.openexchange.drive.actions.SyncDirectoryAction;
import com.openexchange.drive.comparison.Change;
import com.openexchange.drive.comparison.ThreeWayComparison;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.drive.internal.PathNormalizer;
import com.openexchange.drive.internal.SyncSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.java.Strings;


/**
 * {@link DirectorySynchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectorySynchronizer extends Synchronizer<DirectoryVersion> {

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
                syncResult.addActionForClient(new ErrorDirectoryAction(null, clientVersion, twc,
                    DriveExceptionCodes.CONFLICTING_PATH.create(clientVersion.getPath()), true));
            }
        }
        if (null != mapper.getMappingProblems().getUnicodeConflictingClientVersions()) {
            for (DirectoryVersion clientVersion : mapper.getMappingProblems().getUnicodeConflictingClientVersions()) {
                /*
                 * indicate unicode-conflicting version as error with quarantine flag
                 */
                ThreeWayComparison<DirectoryVersion> twc = new ThreeWayComparison<DirectoryVersion>();
                twc.setClientVersion(clientVersion);
                syncResult.addActionForClient(new ErrorDirectoryAction(null, clientVersion, twc,
                    DriveExceptionCodes.CONFLICTING_PATH.create(clientVersion.getPath()), true));
            }
        }
        return syncResult;
    }

    @Override
    protected int getMaxActions() {
        int defaultValue = 1000;
        ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class);
        if (null != configService) {
            return configService.getIntProperty("com.openexchange.drive.maxDirectoryActions", defaultValue);
        }
        return defaultValue;
    }

    @Override
    protected int processServerChange(IntermediateSyncResult<DirectoryVersion> result, ThreeWayComparison<DirectoryVersion> comparison) throws OXException {
        switch (comparison.getServerChange()) {
        case DELETED:
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
            if (mayDelete(comparison.getServerVersion())) {
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
                result.addActionForClient(new ErrorDirectoryAction(comparison.getClientVersion(), comparison.getServerVersion(), comparison,
                    DriveExceptionCodes.NO_DELETE_DIRECTORY_PERMISSION.create(comparison.getServerVersion().getPath()), false));
                return 2;
            }
        case NEW:
            if (isInvalidPath(comparison.getClientVersion().getPath())) {
                /*
                 * invalid path, indicate as error with quarantine flag
                 */
                result.addActionForClient(new ErrorDirectoryAction(null, comparison.getClientVersion(), comparison,
                    DriveExceptionCodes.INVALID_PATH.create(comparison.getClientVersion().getPath()), true));
                return 1;
            } else if (isIgnoredPath(comparison.getClientVersion().getPath())) {
                /*
                 * ignored path, indicate as error with quarantine flag
                 */
                result.addActionForClient(new ErrorDirectoryAction(null, comparison.getClientVersion(), comparison,
                    DriveExceptionCodes.IGNORED_PATH.create(comparison.getClientVersion().getPath()), true));
                return 1;
            } else {
                String parentPath = getLastExistingParentPath(comparison.getClientVersion().getPath());
                if (mayCreate(parentPath)) {
                    /*
                     * new on client, let client synchronize the directory
                     */
                    result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
                    return 1;
                } else {
                    /*
                     * not allowed, indicate as error with quarantine flag
                     */
                    result.addActionForClient(new ErrorDirectoryAction(null, comparison.getClientVersion(), comparison,
                        DriveExceptionCodes.NO_CREATE_DIRECTORY_PERMISSION.create(parentPath), true));
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
             * edit-delete conflict, create on server, let client synchronize the directory
             */
            String parentPath = getLastExistingParentPath(comparison.getClientVersion().getPath());
            if (mayCreate(parentPath)) {
                result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
                return 1;
            } else {
                result.addActionForClient(new ErrorDirectoryAction(null, comparison.getClientVersion(), comparison,
                    DriveExceptionCodes.NO_CREATE_DIRECTORY_PERMISSION.create(parentPath), true));
                return 1;
            }
        } else {
            throw new UnsupportedOperationException("Not implemented: Server: " + comparison.getServerChange() + ", Client: " + comparison.getClientChange());
        }
    }

    private boolean mayDelete(DirectoryVersion version) throws OXException {
        if (DriveConstants.ROOT_PATH.equals(version.getPath())) {
            return false;
        }
        return session.getStorage().getOwnPermission(version.getPath()).isAdmin();
    }

    private boolean mayCreate(String parentPath) throws OXException {
        return FileStoragePermission.CREATE_SUB_FOLDERS <= session.getStorage().getOwnPermission(parentPath).getFolderPermission();
    }

    private String getLastExistingParentPath(String path) {
        Collection<? extends DirectoryVersion> serverVersions = mapper.getServerVersions();
        String currentPath = path;
        int idx;
        do {
            idx = currentPath.lastIndexOf(DriveConstants.PATH_SEPARATOR);
            if (0 < idx) {
                currentPath = currentPath.substring(0, idx);
                for (DirectoryVersion serverVersion : serverVersions) {
                    if (serverVersion.getPath().equals(currentPath)) {
                        return currentPath;
                    }
                }
            }
        } while (0 < idx);
        return DriveConstants.ROOT_PATH;
    }

    /**
     * Gets a value indicating whether the supplied path is invalid, i.e. it contains illegal characters or is not supported for
     * other reasons.
     *
     * @param path The path to check
     * @return <code>true</code> if the path is considered invalid, <code>false</code>, otherwise
     * @throws OXException
     */
    private static boolean isInvalidPath(String path) throws OXException {
        if (Strings.isEmpty(path)) {
            return true; // no empty paths
        }
        if (false == DriveConstants.PATH_VALIDATION_PATTERN.matcher(path).matches()) {
            return true; // no invalid paths
        }
        return false;
    }

    /**
     * Gets a value indicating whether the supplied path is ignored, i.e. it is excluded from synchronization by definition.
     *
     * @param path The path to check
     * @return <code>true</code> if the path is considered to be ignored, <code>false</code>, otherwise
     * @throws OXException
     */
    private static boolean isIgnoredPath(String path) throws OXException {
        if (DriveConstants.TEMP_PATH.equalsIgnoreCase(path)) {
            return true; // no temp path
        }
        return false;
    }

}
