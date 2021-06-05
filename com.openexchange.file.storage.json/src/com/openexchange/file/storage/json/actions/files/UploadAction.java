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
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UploadAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class UploadAction extends AbstractWriteAction {

    /**
     * Initializes a new {@link UploadAction}.
     */
    public UploadAction() {
        super();
    }

    @Override
    protected AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        if (Strings.isEmpty(request.getParameter(File.Field.FOLDER_ID.getName()))) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(File.Field.FOLDER_ID.getName());
        }
        if (Strings.isEmpty(request.getParameter(File.Field.FILENAME.getName()))) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(File.Field.FILENAME.getName());
        }

        IDBasedFileAccess fileAccess = request.getFileAccess();
        File file = request.getFile();
        String originalFileName = file.getFileName();

        if (file.getId() == null) {
            file.setId(FileStorageFileAccess.NEW);
        }
        boolean ignoreWarnings = AJAXRequestDataTools.parseBoolParameter("ignoreWarnings", request.getRequestData(), false);
        boolean tryAddVersion = AJAXRequestDataTools.parseBoolParameter("try_add_version", request.getRequestData(), false);
        long timestamp = request.getTimestamp();

        String offsetString = request.getParameter("offset");
        Long offset = Strings.isEmpty(offsetString) ? null : Long.valueOf(offsetString);

        // Save the file
        String newId;
        if (offset == null) {
            newId = fileAccess.saveDocument(file, request.getUploadedFileData(), timestamp, request.getSentColumns(), false, ignoreWarnings, tryAddVersion);
        } else {
            newId = fileAccess.saveDocument(file, request.getUploadedFileData(), timestamp, request.getSentColumns(), offset.longValue());
        }

        List<OXException> warnings = new ArrayList<>(fileAccess.getAndFlushWarnings());
        if (request.notifyPermissionEntities() && file.getObjectPermissions() != null && file.getObjectPermissions().size() > 0) {
            File modified = fileAccess.getFileMetadata(newId, FileStorageFileAccess.CURRENT_VERSION);
            warnings.addAll(sendNotifications(request.getNotificationTransport(), request.getNotifiactionMessage(), null, modified, request.getSession(), request.getRequestData().getHostData()));
        }

        // Construct detailed response as requested including any warnings, treat as error if not forcibly ignored by client
        AJAXRequestResult result;
        String saveAction = "none";
        if (null != newId && request.extendedResponse()) {
            File metadata = fileAccess.getFileMetadata(newId, FileStorageFileAccess.CURRENT_VERSION);
            FileID id = new FileID(newId);
            if (null != originalFileName && !originalFileName.equals(metadata.getFileName())) {
                saveAction = "rename";
            } else if (fileAccess.supports(id.getService(), id.getAccountId(), FileStorageCapability.FILE_VERSIONS) && 1 < metadata.getNumberOfVersions()) {
                saveAction = "new_version";
            }
            result = result(metadata, (AJAXInfostoreRequest) request, saveAction);
        } else {
            result = new AJAXRequestResult(newId, new Date(file.getSequenceNumber()));
        }

        result.addWarnings(warnings);
        if (null == newId && false == warnings.isEmpty() && false == ignoreWarnings) {
            String name = getFilenameSave(file, null, fileAccess);
            result.setException(FileStorageExceptionCodes.FILE_SAVE_ABORTED.create(name, name));
        }
        return result;
    }

}
