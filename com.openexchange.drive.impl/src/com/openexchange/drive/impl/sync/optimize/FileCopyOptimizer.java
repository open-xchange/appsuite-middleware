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

package com.openexchange.drive.impl.sync.optimize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.AcknowledgeFileAction;
import com.openexchange.drive.impl.actions.DownloadFileAction;
import com.openexchange.drive.impl.checksum.FileChecksum;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.storage.StorageOperation;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.search.FileMd5SumTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


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
    public IntermediateSyncResult<FileVersion> optimize(SyncSession session, IntermediateSyncResult<FileVersion> result) {
        List<AbstractAction<FileVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForClient());
        List<AbstractAction<FileVersion>> optimizedActionsForServer = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForServer());
        /*
         * for client UPLOADs, check if file already known on server
         */
        List<AbstractAction<FileVersion>> uploadActions = new ArrayList<AbstractAction<FileVersion>>();
        Set<String> checksums = new HashSet<String>();
        for (AbstractAction<FileVersion> clientAction : result.getActionsForClient()) {
            if (Action.UPLOAD == clientAction.getAction()) {
                checksums.add(clientAction.getNewVersion().getChecksum());
                uploadActions.add(clientAction);
            }
        }
        if (0 < uploadActions.size()) {
            Map<String, ServerFileVersion> knownFileVersions = findByChecksum(session, checksums);
            if (0 < knownFileVersions.size()) {
                for (AbstractAction<FileVersion> uploadAction : uploadActions) {
                    ServerFileVersion knownFileVersion = knownFileVersions.get(uploadAction.getNewVersion().getChecksum());
                    if (null != knownFileVersion) {
                        /*
                         * no need to upload, just copy file on server and let client update it's metadata
                         */
                        String path = (String)uploadAction.getParameters().get(DriveAction.PARAMETER_PATH);
                        optimizedActionsForClient.remove(uploadAction);
                        DownloadFileAction serverDownload = new DownloadFileAction(
                            session, uploadAction.getVersion(), uploadAction.getNewVersion(), null, path, null);
                        serverDownload.getParameters().put("sourceVersion", knownFileVersion);
                        AcknowledgeFileAction clientAcknowledge = new AcknowledgeFileAction(
                            session, uploadAction.getVersion(), uploadAction.getNewVersion(), null, path, null);
                        clientAcknowledge.setDependingAction(serverDownload);
                        optimizedActionsForServer.add(serverDownload);
                        optimizedActionsForClient.add(clientAcknowledge);
                    }
                }
            }
        }
        /*
         * return new sync result
         */
        return new IntermediateSyncResult<FileVersion>(optimizedActionsForServer, optimizedActionsForClient);
    }

    private static ServerFileVersion findByChecksum(String checksum, Collection<? extends FileVersion> versions) {
        if (null != versions && 0 < versions.size()) {
            for (FileVersion version : versions) {
                if (ServerFileVersion.class.isInstance(version) && checksum.equals(version.getChecksum()) &&
                    false == DriveConstants.METADATA_FILENAME.equals(version.getName())) {
                    return (ServerFileVersion) version;
                }
            }
        }
        return null;
    }

    private Map<String, ServerFileVersion> findByChecksum(SyncSession session, Set<String> checksums) {
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
                matchingFileVersions.putAll(getMatchingFileVersions(session, checksumsToQuery));
            } catch (OXException e) {
                LOG.warn("unexpected error during file lookup by checksum", e);
            }
            checksumsToQuery.removeAll(matchingFileVersions.keySet());
            /*
             * query file storage for remaining checksums
             */
            if (0 < checksumsToQuery.size()) {
               try {
                   matchingFileVersions.putAll(searchMatchingFileVersions(session, checksumsToQuery));
                } catch (OXException e) {
                    LOG.warn("unexpected error during file lookup by checksum", e);
                }
                checksumsToQuery.removeAll(matchingFileVersions.keySet());
                /*
                 * search in trash, too, if available
                 */
                if (0 < checksumsToQuery.size()) {
                    try {
                        if (session.getStorage().hasTrashFolder() && null != session.getStorage().getTrashFolder()) {
                            String trashFolderID = session.getStorage().getTrashFolder().getId();
                            matchingFileVersions.putAll(searchMatchingFileVersions(
                                session, Collections.singletonList(trashFolderID), checksumsToQuery));
                        }
                    } catch (OXException e) {
                        LOG.warn("unexpected error during file lookup by checksum", e);
                    }
                }
            }
        }
        return matchingFileVersions;
    }

    /**
     * Searches for files matching the supplied checksums in the storage.
     *
     * @param session The sync session
     * @param checksums The checksums to lookup
     * @return The found file versions, each mapped to the matching checksum
     * @throws OXException
     */
    private static Map<String, ServerFileVersion> searchMatchingFileVersions(final SyncSession session, final List<String> checksums) throws OXException {
        return searchMatchingFileVersions(session, null, checksums);
    }

    /**
     * Searches for files matching the supplied checksums in the storage.
     *
     * @param session The sync session
     * @param folderIDs The IDs of the folder to search, or <code>null</code> to search in all visible folders
     * @param checksums The checksums to lookup
     * @return The found file versions, each mapped to the matching checksum
     * @throws OXException
     */
    private static Map<String, ServerFileVersion> searchMatchingFileVersions(final SyncSession session, final List<String> folderIDs, final List<String> checksums) throws OXException {
        Map<String, ServerFileVersion> matchingFileVersions = new HashMap<String, ServerFileVersion>();
        if (0 < checksums.size() && session.getStorage().supports(new FolderID(session.getRootFolderID()), FileStorageCapability.SEARCH_BY_TERM)) {
            List<FileChecksum> checksumsToInsert = new ArrayList<FileChecksum>();
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = session.getStorage().wrapInTransaction(new StorageOperation<SearchIterator<File>>() {

                    @Override
                    public SearchIterator<File> call() throws OXException {
                        return session.getStorage().getFileAccess().search(
                            folderIDs, getSearchTermForChecksums(checksums), DriveConstants.FILE_FIELDS, null, SortDirection.DEFAULT,
                            FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
                    }
                });
                while (searchIterator.hasNext()) {
                    File file = searchIterator.next();
                    String md5 = file.getFileMD5Sum();
                    if (false == Strings.isEmpty(md5)) {
                        FileChecksum fileChecksum = new FileChecksum(
                            DriveUtils.getFileID(file), file.getVersion(), file.getSequenceNumber(), md5);
                        checksumsToInsert.add(fileChecksum);
                        matchingFileVersions.put(fileChecksum.getChecksum(), new ServerFileVersion(file, fileChecksum));
                    }
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
            if (0 < checksumsToInsert.size()) {
                session.getChecksumStore().insertFileChecksums(checksumsToInsert);
            }
        }
        return matchingFileVersions;
    }

    /**
     * Constructs a search term to match any files matching the supplied file checksums.
     *
     * @param checksumsToQuery The checksums to construct the search term for
     * @return The search term, or <code>null</code> if supplied checksums were empty
     */
    private static SearchTerm<?> getSearchTermForChecksums(List<String> checksumsToQuery) {
        if (null == checksumsToQuery || 0 == checksumsToQuery.size()) {
            return null;
        } else if (1 == checksumsToQuery.size()) {
            return new FileMd5SumTerm(checksumsToQuery.get(0));
        } else {
            List<SearchTerm<?>> md5Terms = new ArrayList<SearchTerm<?>>(checksumsToQuery.size());
            for (String checksum : checksumsToQuery) {
                md5Terms.add(new FileMd5SumTerm(checksum));
            }
            return new OrTerm(md5Terms);
        }
    }

    private static boolean indicatesInvalidation(OXException e) {
        if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            return false;
        }
        return true;
    }

    private static Map<String, ServerFileVersion> getMatchingFileVersions(SyncSession session, List<String> checksums) throws OXException {
        Map<String, ServerFileVersion> matchingFileVersions = new HashMap<String, ServerFileVersion>();
        List<FileChecksum> checksumsToInvalidate = new ArrayList<FileChecksum>();
        Map<String, List<FileChecksum>> storedFileChecksums = session.getChecksumStore().getMatchingFileChecksums(checksums);
        Map<FolderID, Integer> folderPermissions = new HashMap<FolderID, Integer>();
        for (List<FileChecksum> storedChecksums : storedFileChecksums.values()) {
            File matchingFile = null;
            for (FileChecksum storedChecksum : storedChecksums) {
                if (DriveConstants.METADATA_FILENAME.equals(storedChecksum.getFileID().getFileId())) {
                    continue;
                }
                FileID fileID = storedChecksum.getFileID();
                /*
                 * try to get parent folder permissions
                 */
                FolderID folderID = new FolderID(fileID.getService(), fileID.getAccountId(),fileID.getFolderId());
                Integer readPermissions = null;
                if (false == folderPermissions.containsKey(folderID)) {
                    FileStorageFolder folder = null;
                    try {
                        folder = session.getStorage().getFolderAccess().getFolder(folderID.toUniqueID());
                        if (null != folder) {
                            readPermissions = Integer.valueOf(folder.getOwnPermission().getReadPermission());
                        }
                    } catch (OXException e) {
                        LOG.debug("Error accessing folder referenced by checksum store: {}", e.getMessage());
                        if (false == indicatesInvalidation(e)) {
                            // mark not accessible
                            readPermissions = Integer.valueOf(FileStoragePermission.NO_PERMISSIONS);
                        }
                    }
                    folderPermissions.put(folderID, readPermissions); // may be null (folder not found)
                } else {
                    readPermissions = folderPermissions.get(folderID);
                }
                if (null == readPermissions) {
                    checksumsToInvalidate.add(storedChecksum);
                    continue;
                }
                if (FileStoragePermission.NO_PERMISSIONS == readPermissions.intValue()) {
                    continue;
                }
                /*
                 * try to get matching file
                 */
                try {
                    matchingFile = session.getStorage().getFile(storedChecksum.getFileID().toUniqueID(), storedChecksum.getVersion());
                } catch (OXException e) {
                    LOG.debug("Error accessing file referenced by checksum store: {}", e.getMessage());
                    if (indicatesInvalidation(e)) {
                        checksumsToInvalidate.add(storedChecksum);
                    }
                }
                if (null != matchingFile) {
                    /*
                     * check if sequence number / version still valid
                     */
                    if (matches(storedChecksum, matchingFile)) {
                        matchingFileVersions.put(storedChecksum.getChecksum(), new ServerFileVersion(matchingFile, storedChecksum));
                        break;
                    } else {
                        checksumsToInvalidate.add(storedChecksum);
                    }
                }
            }
        }
        if (0 < checksumsToInvalidate.size()) {
            session.getChecksumStore().removeFileChecksums(checksumsToInvalidate);
        }
        return matchingFileVersions;
    }

    private static boolean matches(FileChecksum checksum, File file) {
        if (null == checksum) {
            return null == file;
        } else if (null != file) {
            return checksum.getSequenceNumber() == file.getSequenceNumber() &&
                (null == checksum.getVersion() ? null == file.getVersion() : checksum.getVersion().equals(file.getVersion()));
        }
        return false;
    }

}
