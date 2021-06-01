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

import static com.openexchange.java.Autoboxing.L;
import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.zip.ZipUtility;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.ziputil.ZipMaker;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ZipDocumentsAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true)
public class ZipDocumentsAction extends AbstractFileAction {

    /**
     * Initializes a new {@link ZipDocumentsAction}.
     */
    public ZipDocumentsAction() {
        super();
    }

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        // Get/parse IDs
        List<IdVersionPair> idVersionPairs;
        try {
            Object data = request.getRequestData().getData();
            if (data instanceof JSONArray) {
                idVersionPairs = parsePairs((JSONArray) data);
            } else {
                String value = request.getParameter("body");
                if (Strings.isEmpty(value)) {
                    idVersionPairs = request.getIdVersionPairs();
                } else {
                    idVersionPairs = parsePairs(new JSONArray(value));
                }
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "body", e.getMessage());
        }

        boolean recursive;
        {
            String tmp = request.getParameter("recursive");
            recursive = AJAXRequestDataTools.parseBoolParameter(tmp);
        }

        // Get file/folder access
        IDBasedFileAccess fileAccess = request.getFileAccess();
        IDBasedFolderAccess folderAccess = request.getFolderAccess();
        // Perform scan
        scan(request, idVersionPairs, fileAccess, folderAccess, recursive);
        // Initialize ZIP maker for folder resource
        ZipMaker zipMaker = new ZipMaker(idVersionPairs, recursive, fileAccess, folderAccess);

        // Check against size threshold
        zipMaker.checkThreshold(threshold());

        AJAXRequestData ajaxRequestData = request.getRequestData();
        if (ZipUtility.setHttpResponseHeaders("documents.zip", ajaxRequestData)) {
            // Write ZIP archive
            long bytesWritten = 0;
            try {
                bytesWritten = zipMaker.writeZipArchive(ajaxRequestData.optOutputStream());
            } catch (IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }

            // Signal direct response
            AJAXRequestResult result = new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
            if (bytesWritten != 0) {
                result.setResponseProperty("X-Content-Size", L(bytesWritten));
            }
            return result;
        }

        // No direct response possible. Create ThresholdFileHolder...
        ThresholdFileHolder fileHolder = ZipUtility.prepareThresholdFileHolder("documents.zip");
        try {
            // Create ZIP archive
            zipMaker.writeZipArchive(fileHolder.asOutputStream());

            ajaxRequestData.setFormat("file");
            AJAXRequestResult requestResult = new AJAXRequestResult(fileHolder, "file");
            fileHolder = null;
            return requestResult;
        } finally {
            Streams.close(fileHolder);
        }
    }

}
