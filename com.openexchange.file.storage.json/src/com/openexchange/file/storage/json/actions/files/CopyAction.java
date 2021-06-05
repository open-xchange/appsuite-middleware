/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.file.storage.json.actions.files;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link CopyAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CopyAction extends AbstractWriteAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        List<IdVersionPair> pairs = request.optIdVersionPairs();
        if (null != pairs) {
            return handlePairs(pairs, request);
        }

        // The old way...
        request.require(Param.ID).requireFileMetadata();

        IDBasedFileAccess fileAccess = request.getFileAccess();
        String id = request.getId();
        File file = request.getFile();
        String folder = null != file.getFolderId() ? file.getFolderId() : request.getFolderId();
        String version = request.getVersion();

        String newId;
        if (request.hasUploads()) {
            newId = fileAccess.copy(id, version, folder, file, request.getUploadedFileData(), request.getSentColumns());
            request.uploadFinished();
        } else {
            newId = fileAccess.copy(id, version, folder, file, null, request.getSentColumns());
        }

        if (newId != null) {
            return new AJAXRequestResult(newId, new Date(file.getSequenceNumber()));
        }

        boolean ignoreWarnings = AJAXRequestDataTools.parseBoolParameter("ignoreWarnings", request.getRequestData(), false);
        if (ignoreWarnings) {
            newId = fileAccess.saveDocument(file, null, file.getSequenceNumber(), request.getSentColumns(), false, ignoreWarnings, false);
            return new AJAXRequestResult(newId, new Date(file.getSequenceNumber()));
        }
        AJAXRequestResult result = new AJAXRequestResult(id);
        Collection<OXException> warnings = fileAccess.getAndFlushWarnings();
        result.addWarnings(warnings);
        result.setException(FileStorageExceptionCodes.FILE_UPDATE_ABORTED.create(getFilenameSave(id, fileAccess), id));

        return result;
    }

    private AJAXRequestResult handlePairs(List<IdVersionPair> pairs, InfostoreRequest request) throws OXException {
        request.require(Param.FOLDER_ID);

        IDBasedFileAccess fileAccess = request.getFileAccess();
        IDBasedFolderAccess folderAccess = request.getFolderAccess();
        String destFolder = request.getFolderId();

        List<String> newFiles = new LinkedList<>();
        List<String> newFolders = new LinkedList<>();

        boolean error = true;
        try {
            for (IdVersionPair pair : pairs) {
                if (pair.getIdentifier() == null) {
                    // Resource denotes a folder
                    String folderId = pair.getFolderId();
                    FileStorageFolder srcFolder = folderAccess.getFolder(new FolderID(folderId));

                    DefaultFileStorageFolder newFolder = new DefaultFileStorageFolder();
                    newFolder.setName(srcFolder.getName());
                    newFolder.setParentId(destFolder);
                    newFolder.setSubscribed(srcFolder.isSubscribed());
                    for (FileStoragePermission permission : srcFolder.getPermissions()) {
                        newFolder.addPermission(permission);
                    }

                    String newFolderID = folderAccess.createFolder(newFolder);
                    newFolders.add(newFolderID);

                    TimedResult<File> documents = fileAccess.getDocuments(folderId);
                    SearchIterator<File> iter = documents.results();
                    try {
                        while (iter.hasNext()) {
                            File file = iter.next();
                            fileAccess.copy(file.getId(), file.getVersion(), newFolderID, null, null, null);
                        }
                    } finally {
                        SearchIterators.close(iter);
                    }
                } else {
                    // Resource denotes a file
                    String id = pair.getIdentifier();
                    String version = pair.getVersion();
                    String newFileId = fileAccess.copy(id, version, destFolder, null, null, null);
                    newFiles.add(newFileId);
                }
            }
            error = false;
            return new AJAXRequestResult(Boolean.TRUE, "native");
        } finally {
            if (error) {
                for (String folderId : newFolders) {
                    try {
                        folderAccess.deleteFolder(folderId, true);
                    } catch (Exception e) {
                        /* ignore */}
                }
                for (String fileId : newFiles) {
                    try {
                        fileAccess.removeDocument(Collections.singletonList(fileId), FileStorageFileAccess.DISTANT_FUTURE, true);
                    } catch (Exception e) {
                        /* ignore */}
                }
            }
        }
    }

    private static String getFilenameSave(String id, IDBasedFileAccess fileAccess) {
        String name = null;
        if (null != id && null != fileAccess) {
            try {
                File metadata = fileAccess.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);
                if (null != metadata) {
                    name = metadata.getFileName();
                    if (null == name) {
                        name = metadata.getTitle();
                    }
                }
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(UpdateAction.class).debug("Error getting name for file {}: {}", id, e.getMessage(), e);
            }
        }
        return name;
    }

}
