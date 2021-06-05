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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.EnqueuableAJAXActionService;
import com.openexchange.ajax.requesthandler.jobqueue.JobKey;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

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
        String destFolder = request.getFolderId();

        boolean ignoreWarnings = AJAXRequestDataTools.parseBoolParameter("ignoreWarnings", request.getRequestData(), false);
        List<String> conflictingFiles = new ArrayList<>(pairs.size());
        List<String> filesToMove = pairs.stream().map(p -> p.getIdentifier()).collect(Collectors.toList());

        if (!filesToMove.isEmpty()) {
            conflictingFiles.addAll(fileAccess.move(filesToMove, FileStorageFileAccess.DISTANT_FUTURE, destFolder, adjustFilenamesAsNeeded, ignoreWarnings));
        }

        AJAXRequestResult result = result(conflictingFiles, request);

        // Add any warnings to the response
        Collection<OXException> warnings = fileAccess.getAndFlushWarnings();
        result.addWarnings(warnings);
        if ((warnings != null) && (!warnings.isEmpty()) && (!ignoreWarnings)) {
            result.setException(FileStorageExceptionCodes.FILE_MOVE_ABORTED.create(conflictingFiles.stream().collect(Collectors.joining(","))));
        }
        return result;
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

    @Override
    protected Result isEnqueueable(InfostoreRequest request) throws OXException {
        ServerSession session = request.getSession();
        List<IdVersionPair> pairs = request.optIdVersionPairs();
        if (null != pairs) {
            return isEnqueueable(request, pairs);
        }

        // A single file
        request.require(Param.ID, Param.FOLDER_ID, Param.TIMESTAMP);
        String id = request.getId();
        String folderId = request.getFolderId();

        JSONObject jKeyDesc = new JSONObject(4);
        try {
            jKeyDesc.put("module", "files");
            jKeyDesc.put("action", "move");
            jKeyDesc.put("id", id);
            jKeyDesc.put("parent", folderId);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

        return EnqueuableAJAXActionService.resultFor(true, new JobKey(session.getUserId(), session.getContextId(), jKeyDesc.toString()), this);
    }

    private Result isEnqueueable(InfostoreRequest request, List<IdVersionPair> pairs) throws OXException {
        ServerSession session = request.getSession();
        request.require(Param.FOLDER_ID);
        JSONArray folders = new JSONArray();
        JSONArray files = new JSONArray();
        for (IdVersionPair pair : pairs) {
            if (pair.getIdentifier() == null) {
                // Resource denotes a folder
                folders.put(pair.getFolderId());
            } else {
                // Resource denotes a file
                files.put(pair.getIdentifier());
            }
        }

        String folderId = request.getFolderId();

        JSONObject jKeyDesc = new JSONObject(4);
        try {
            jKeyDesc.put("module", "files");
            jKeyDesc.put("action", "move");
            jKeyDesc.put("files", files);
            jKeyDesc.put("folders", folders);
            jKeyDesc.put("parent", folderId);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

        return EnqueuableAJAXActionService.resultFor(true, new JobKey(session.getUserId(), session.getContextId(), jKeyDesc.toString()), this);
    }

}
