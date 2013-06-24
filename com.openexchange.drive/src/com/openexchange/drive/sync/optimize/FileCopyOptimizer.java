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

package com.openexchange.drive.sync.optimize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DownloadFileAction;
import com.openexchange.drive.checksum.FileChecksum;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.sync.SyncResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.FolderID;


/**
 * {@link FileCopyOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileCopyOptimizer extends FileActionOptimizer {

    public FileCopyOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    @Override
    public SyncResult<FileVersion> optimize(DriveSession session, SyncResult<FileVersion> result) {
        List<AbstractAction<FileVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForClient());
        List<AbstractAction<FileVersion>> optimizedActionsForServer = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForServer());
        List<AbstractAction<FileVersion>> uploadActions = new ArrayList<AbstractAction<FileVersion>>();
        /*
         * for client UPLOADs, check if file already known on server
         */
        List<String> checksums = new ArrayList<String>();
        for (AbstractAction<FileVersion> clientAction : result.getActionsForClient()) {
            if (Action.UPLOAD == clientAction.getAction()) {
                checksums.add(clientAction.getNewVersion().getChecksum());
                uploadActions.add(clientAction);
            }
        }
        if (0 < uploadActions.size()) {
            Map<String, ServerFileVersion> knownFileVersions = findByChecksum(session, checksums);
            for (AbstractAction<FileVersion> uploadAction : uploadActions) {
                ServerFileVersion knownFileVersion = knownFileVersions.get(uploadAction.getNewVersion().getChecksum());
                if (null != knownFileVersion) {
                    /*
                     * no need to upload, just copy file on server and let client update it's metadata
                     */
                    String path = (String)uploadAction.getParameters().get(DriveAction.PARAMETER_PATH);
                    optimizedActionsForClient.remove(uploadAction);
                    DownloadFileAction copyAction = new DownloadFileAction(
                        uploadAction.getVersion(), uploadAction.getNewVersion(), null, path, -1, null);
                    copyAction.getParameters().put("sourceVersion", knownFileVersion);
                    optimizedActionsForServer.add(copyAction);
                    optimizedActionsForClient.add(
                        new AcknowledgeFileAction(uploadAction.getVersion(), uploadAction.getNewVersion(), null, path));
                }
            }
        }


//
//        for (AbstractAction<FileVersion> clientAction : result.getActionsForClient()) {
//            /*
//             * for client UPLOADs, check if file already known on server
//             */
//            if (Action.UPLOAD == clientAction.getAction()) {
//                ServerFileVersion knownFile = findByChecksum(session, clientAction.getNewVersion().getChecksum());
//                if (null != knownFile) {
//                    /*
//                     * no need to upload, just copy file on server and let client update it's metadata
//                     */
//                    String path = (String)clientAction.getParameters().get(DriveAction.PARAMETER_PATH);
//                    optimizedActionsForClient.remove(clientAction);
//                    DownloadFileAction copyAction = new DownloadFileAction(
//                        clientAction.getVersion(), clientAction.getNewVersion(), null, path, -1, null);
//                    copyAction.getParameters().put("sourceVersion", knownFile);
//                    optimizedActionsForServer.add(copyAction);
//                    optimizedActionsForClient.add(new AcknowledgeFileAction(clientAction.getVersion(), clientAction.getNewVersion(), null,
//                        (String)clientAction.getParameters().get(DriveAction.PARAMETER_PATH)));
//                }
//            }
//        }
        /*
         * return new sync result
         */
        return new SyncResult<FileVersion>(optimizedActionsForServer, optimizedActionsForClient);
    }

    private static ServerFileVersion findByChecksum(String checksum, Collection<? extends FileVersion> versions) {
        if (null != versions && 0 < versions.size()) {
            for (FileVersion version : versions) {
                if (ServerFileVersion.class.isInstance(version) && checksum.equals(version.getChecksum())) {
                    return (ServerFileVersion)version;
                }
            }
        }
        return null;
    }

    private Map<String, ServerFileVersion> findByChecksum(DriveSession session, List<String> checksums) {
        if (null == checksums || 0 == checksums.size()) {
            return Collections.emptyMap();
        }
        Map<String, ServerFileVersion> matchingFileVersions = new HashMap<String, ServerFileVersion>();
        /*
         * check which checksums are already known in mapped server files
         */
        List<String> checksumsToQuery = new ArrayList<String>(checksums.size());
        Collection<? extends FileVersion> serverVersions = mapper.getServerVersions();
        for (String checksum : checksums) {
            ServerFileVersion fileVersion = findByChecksum(checksum, serverVersions);
            if (null == fileVersion) {
                checksumsToQuery.add(checksum);
            } else {
                matchingFileVersions.put(checksum, fileVersion);
            }
        }
        /*
         * query checksum store for remaining checksums
         */
        if (0 < checksumsToQuery.size()) {
            try {
                Map<String, List<FileChecksum>> matchingFileChecksums = session.getChecksumStore().getMatchingFileChecksums(checksumsToQuery);
                for (Entry<String, List<FileChecksum>> entry : matchingFileChecksums.entrySet()) {
                    for (FileChecksum fileChecksum : entry.getValue()) {
                        ServerFileVersion storageVersion = getStorageVersion(session, fileChecksum);
                        if (null != storageVersion) {
                            matchingFileVersions.put(entry.getKey(), storageVersion);
                            break;
                        }
                    }
                }
            } catch (OXException e) {
                LOG.warn("unexpected error during file lookup by checksum", e);
            }
        }
        return matchingFileVersions;
    }

    private ServerFileVersion findByChecksum(DriveSession session, String checksum) {
        /*
         * check server file versions known by mapper
         */
        Collection<? extends FileVersion> versions = mapper.getServerVersions();
        if (null != versions && 0 < versions.size()) {
            for (FileVersion version : versions) {
                if (checksum.equals(version.getChecksum()) && ServerFileVersion.class.isInstance(version)) {
                    return (ServerFileVersion) version;
                }
            }
        }
        /*
         * check files known by checksum store
         */
        try {
            List<FileChecksum> fileChecksums = session.getChecksumStore().getMatchingFileChecksums(checksum);
            for (FileChecksum fileChecksum : fileChecksums) {
                File storageFile = null;
                boolean folderNotFound = false;
                FolderID folderID = new FolderID(fileChecksum.getFileID().getService(), fileChecksum.getFileID().getAccountId(),
                    fileChecksum.getFileID().getFolderId());
                try {
                    String path = session.getStorage().getPath(folderID.toUniqueID());
                    storageFile = session.getStorage().getFile(path, fileChecksum.getFileID().toUniqueID(), fileChecksum.getVersion());
                } catch (OXException e) {
                    LOG.debug("Error accessing file referenced by checksum store: " + e.getMessage());
                    if ("FLD-0008".equals(e.getErrorCode())) {
                        folderNotFound = true;
                    }
                }
                if (null == storageFile || storageFile.getSequenceNumber() != fileChecksum.getSequenceNumber()) {
                    if (folderNotFound) {
                        LOG.debug("Invalidating stored file checksums for folder: " + folderID);
                        session.getChecksumStore().removeFileChecksumsInFolder(folderID);
                    } else {
                        LOG.debug("Invalidating stored file checksum: " + fileChecksum);
                        session.getChecksumStore().removeFileChecksum(fileChecksum);
                    }
                } else {
                    LOG.debug("Found matching file in storage for stored checksum: " + storageFile);
                    return new ServerFileVersion(storageFile, fileChecksum);
                }
            }
        } catch (OXException e) {
            LOG.warn("unexpected error during file lookup by checksum", e);
        }
        /*
         * not found
         */
        return null;
    }

    private static ServerFileVersion getStorageVersion(DriveSession session, FileChecksum fileChecksum) throws OXException {
        File storageFile = null;
        boolean folderNotFound = false;
        FolderID folderID = new FolderID(fileChecksum.getFileID().getService(), fileChecksum.getFileID().getAccountId(),
            fileChecksum.getFileID().getFolderId());
        try {
            String path = session.getStorage().getPath(folderID.toUniqueID());
            storageFile = session.getStorage().getFile(path, fileChecksum.getFileID().getFileId(), fileChecksum.getVersion());
        } catch (OXException e) {
            LOG.debug("Error accessing file referenced by checksum store: " + e.getMessage());
            if ("FLD-0008".equals(e.getErrorCode())) {
                folderNotFound = true;
            }
        }
        if (null == storageFile || storageFile.getSequenceNumber() != fileChecksum.getSequenceNumber()) {
            if (folderNotFound) {
                LOG.debug("Invalidating stored file checksums for folder: " + folderID);
                session.getChecksumStore().removeFileChecksumsInFolder(folderID);
            } else {
                LOG.debug("Invalidating stored file checksum: " + fileChecksum);
                session.getChecksumStore().removeFileChecksum(fileChecksum);
            }
        } else {
            LOG.debug("Found matching file in storage for stored checksum: " + storageFile);
            return new ServerFileVersion(storageFile, fileChecksum);
        }
        /*
         * not found
         */
        return null;
    }

}
