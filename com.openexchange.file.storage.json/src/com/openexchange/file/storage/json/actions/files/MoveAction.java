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

package com.openexchange.file.storage.json.actions.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link MoveAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MoveAction extends AbstractWriteAction {

    private final List<Field> fields;

    /**
     * Initializes a new {@link MoveAction}.
     */
    public MoveAction() {
        super();
        fields = Collections.singletonList(Field.FOLDER_ID);
    }

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        List<IdVersionPair> pairs = request.optIdVersionPairs();
        if (null != pairs) {
            return handlePairs(pairs, request);
        }

        // A single file
        request.require(Param.ID, Param.FOLDER_ID, Param.TIMESTAMP);

        DefaultFile file = new DefaultFile();
        file.setId(request.getId());
        file.setFolderId(request.getFolderId());

        FileID id = new FileID(file.getId());
        IDBasedFileAccess fileAccess = request.getFileAccess();

        // Save file metadata without binary payload
        boolean ignoreWarnings = AJAXRequestDataTools.parseBoolParameter("ignoreWarnings", request.getRequestData(), false);
        String newId = fileAccess.saveFileMetadata(file, request.getTimestamp(), fields, ignoreWarnings, false);

        // Construct detailed response as requested including any warnings, treat as error if not forcibly ignored by client
        AJAXRequestResult result;
        if (null != newId && request.extendedResponse()) {
            result = result(fileAccess.getFileMetadata(newId, FileStorageFileAccess.CURRENT_VERSION), request);
        } else {
            result = new AJAXRequestResult(newId, new Date(file.getSequenceNumber()));
        }
        Collection<OXException> warnings = fileAccess.getAndFlushWarnings();
        result.addWarnings(warnings);
        if ((null == newId) && (null != warnings) && (false == warnings.isEmpty()) && (false == ignoreWarnings)) {
            result.setException(FileStorageExceptionCodes.FILE_UPDATE_ABORTED.create(getFilenameSave(id, fileAccess), id.toUniqueID()));
        }
        return result;
    }

    private AJAXRequestResult handlePairs(List<IdVersionPair> pairs, InfostoreRequest request) throws OXException {
        request.require(Param.FOLDER_ID);

        boolean adjustFilenamesAsNeeded = AJAXRequestDataTools.parseBoolParameter("autorename", request.getRequestData(), true);

        IDBasedFileAccess fileAccess = request.getFileAccess();
        IDBasedFolderAccess folderAccess = request.getFolderAccess();
        String destFolder = request.getFolderId();

        List<String> oldFiles = new LinkedList<String>();
        LinkedList<String> deleteableFolders = new LinkedList<String>();

        boolean error = true;
        try {
            List<String> conflicting = new ArrayList<String>(pairs.size());
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
                    List<String> fileIds = new ArrayList<String>();
                    {
                        TimedResult<File> documents = fileAccess.getDocuments(folderId);
                        SearchIterator<File> iter = documents.results();
                        try {
                            while (iter.hasNext()) {
                                File file = iter.next();
                                fileIds.add(file.getId());
                            }
                        } finally {
                            SearchIterators.close(iter);
                        }
                    }

                    deleteableFolders.addLast(newFolderID);
                    conflicting.addAll(fileAccess.move(fileIds, FileStorageFileAccess.DISTANT_FUTURE, newFolderID, adjustFilenamesAsNeeded));

                    deleteableFolders.removeLast();
                    deleteableFolders.addLast(folderId);
                } else {
                    // Resource denotes a file
                    String id = pair.getIdentifier();
                    oldFiles.add(id);
                }
            }

            if (!oldFiles.isEmpty()) {
                conflicting.addAll(fileAccess.move(oldFiles, FileStorageFileAccess.DISTANT_FUTURE, destFolder, adjustFilenamesAsNeeded));
            }

            {
                for (String folderId : deleteableFolders) {
                    try {
                        folderAccess.deleteFolder(folderId, true);
                    } catch (Exception e) {
                        /* ignore */}
                }
            }

            error = false;
            AJAXRequestResult result = result(conflicting, request);

            // Add any warnings to the response
            Collection<OXException> warnings = fileAccess.getAndFlushWarnings();
            result.addWarnings(warnings);

            boolean ignoreWarnings = AJAXRequestDataTools.parseBoolParameter("ignoreWarnings", request.getRequestData(), false);
            if ((warnings != null) && (!warnings.isEmpty()) && (!ignoreWarnings)) {
                result.setException(FileStorageExceptionCodes.FILE_MOVE_ABORTED.create());
            }

            return result;
        } finally {
            if (error) {
                for (String folderId : deleteableFolders) {
                    try {
                        folderAccess.deleteFolder(folderId, true);
                    } catch (Exception e) {
                        /* ignore */}
                }
            }
        }
    }

    private String moveFile(String fileId, String newFolderId, IDBasedFileAccess fileAccess) throws OXException {
        DefaultFile file = new DefaultFile();
        file.setId(fileId);
        file.setFolderId(newFolderId);

        // Save file metadata without binary payload
        return fileAccess.saveFileMetadata(file, FileStorageFileAccess.DISTANT_FUTURE, fields, true, false);
    }

    private static String getFilenameSave(FileID id, IDBasedFileAccess fileAccess) {
        String name = null;
        if (null != id && null != fileAccess) {
            try {
                File metadata = fileAccess.getFileMetadata(id.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
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
