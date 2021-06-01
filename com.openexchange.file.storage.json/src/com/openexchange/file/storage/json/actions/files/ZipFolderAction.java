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
import java.util.Collections;
import java.util.List;
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
 * {@link ZipFolderAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@DispatcherNotes(defaultFormat = "file")
public class ZipFolderAction extends AbstractFileAction {

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        IDBasedFileAccess fileAccess = request.getFileAccess();
        IDBasedFolderAccess folderAccess = request.getFolderAccess();

        String folderId = request.getFolderId();
        if (Strings.isEmpty(folderId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(Param.FOLDER_ID.getName());
        }

        boolean recursive = AJAXRequestDataTools.parseBoolParameter(request.getParameter("recursive"));

        String zipFileName;
        {
            String folderName = saneForFileName(folderAccess.getFolder(folderId).getName());
            zipFileName = folderName + ".zip";
        }

        List<IdVersionPair> idVersionPairs = Collections.singletonList(new IdVersionPair(null, null, folderId));
        scan(request, idVersionPairs, fileAccess, folderAccess, recursive);

        // Initialize ZIP maker for folder resource
        ZipMaker zipMaker = new ZipMaker(idVersionPairs, recursive, fileAccess, folderAccess);

        // Check against size threshold
        zipMaker.checkThreshold(threshold());

        AJAXRequestData ajaxRequestData = request.getRequestData();
        if (ZipUtility.setHttpResponseHeaders(zipFileName, ajaxRequestData)) {
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

        // No direct response possible

        // Create archive
        ThresholdFileHolder fileHolder = ZipUtility.prepareThresholdFileHolder(zipFileName);
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

    private static String saneForFileName(final String fileName) {
        if (Strings.isEmpty(fileName)) {
            return "archive";
        }
        final int len = fileName.length();
        final StringBuilder sb = new StringBuilder(len);
        char prev = '\0';
        for (int i = 0; i < len; i++) {
            final char c = fileName.charAt(i);
            if (Strings.isWhitespace(c)) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('/' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('\\' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if (',' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('.' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else {
                prev = '\0';
                sb.append(c);
            }
        }
        String sanitized = sb.toString();
        return Strings.isEmpty(sanitized) ? "archive" : sanitized;
    }

}
