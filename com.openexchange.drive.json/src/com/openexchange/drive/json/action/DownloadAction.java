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

package com.openexchange.drive.json.action;

import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.drive.json.pattern.JsonDirectoryPattern;
import com.openexchange.drive.json.pattern.JsonFilePattern;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link DownloadAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@DispatcherNotes(defaultFormat = "file")
public class DownloadAction extends AbstractDriveAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DownloadAction.class);

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        try {
            /*
             * no limits & no transformations for download
             */
            enableUnlimitedBodySize(requestData);
            preventTransformations(requestData);
            /*
             * get parameters
             */
            String path = requestData.getParameter("path");
            if (Strings.isEmpty(path)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
            }
            String name = requestData.getParameter("name");
            if (Strings.isEmpty(name)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("name");
            }
            String checksum = requestData.getParameter("checksum");
            if (Strings.isEmpty(checksum)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
            }
            long length = -1;
            if (requestData.containsParameter("length")) {
                length = requestData.getParameter("length", Long.class).longValue();
            }
            long offset = 0;
            if (requestData.containsParameter("offset")) {
                offset = requestData.getParameter("offset", Long.class).longValue();
            }
            /*
             * extract file- and directory exclusions if present
             */
            Object data = requestData.getData();
            if (null != data && JSONObject.class.isInstance(data)) {
                JSONObject dataObject = (JSONObject) data;
                try {
                    session.setDirectoryExclusions(JsonDirectoryPattern.deserialize(dataObject.optJSONArray("directoryExclusions")));
                    session.setFileExclusions(JsonFilePattern.deserialize(dataObject.optJSONArray("fileExclusions")));
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
            /*
             * get data
             */
            DriveService driveService = Services.getService(DriveService.class, true);
            IFileHolder fileHolder = driveService.download(session, path, new JsonFileVersion(checksum, name), offset, length);
            if (null == fileHolder) {
                throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(name, checksum, path);
            }
            /*
             * return file result
             */
            return new AJAXRequestResult(fileHolder, "file");
        } catch (OXException e) {
            /*
             * indicate error by setting HTTP status code
             */
            LOG.warn("Error performing download with parameters: {}", requestData.getParameters(), e);
            throw getHttpError(e);
        } catch (RuntimeException e) {
            /*
             * indicate error by setting HTTP status code
             */
            LOG.error("Unexpected error performing download with parameters: {}", requestData.getParameters(), e);
            throw AjaxExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), e.getMessage());
        }
    }

    private static OXException getHttpError(OXException e) throws OXException {
        int status;
        if (DriveExceptionCodes.FILEVERSION_NOT_FOUND.equals(e) || DriveExceptionCodes.FILE_NOT_FOUND.equals(e) ||
            DriveExceptionCodes.PATH_NOT_FOUND.equals(e) || "FLS-017".equals(e.getErrorCode()) ||
            FileStorageExceptionCodes.FILE_NOT_FOUND.equals(e) || FileStorageExceptionCodes.FOLDER_NOT_FOUND.equals(e) ||
            FileStorageExceptionCodes.NOT_FOUND.equals(e) || FileStorageExceptionCodes.FILE_VERSION_NOT_FOUND.equals(e) ||
            "DROPBOX-0005".equals(e.getErrorCode()) || "GOOGLE_DRIVE-0005".equals(e.getErrorCode())) {
            status = HttpServletResponse.SC_NOT_FOUND;
        } else if (DriveExceptionCodes.INVALID_FILE_OFFSET.equals(e) || "FLS-0018".equals(e.getErrorCode())
            || "FLS-0019".equals(e.getErrorCode()) || "FLS-0020".equals(e.getErrorCode())) {
            status = HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE;
        } else if (DriveExceptionCodes.SERVER_BUSY.equals(e)) {
            status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        } else if (AjaxExceptionCodes.MISSING_PARAMETER.equals(e)) {
            status = HttpServletResponse.SC_BAD_REQUEST;
        } else if (OXException.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            status = HttpServletResponse.SC_FORBIDDEN;
        } else if (OXException.CATEGORY_CONFLICT.equals(e.getCategory())) {
            status = HttpServletResponse.SC_CONFLICT;
        } else if (OXException.CATEGORY_USER_INPUT.equals(e.getCategory())) {
            status = HttpServletResponse.SC_BAD_REQUEST;
        } else {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        throw AjaxExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(status), e.getSoleMessage());
    }

}
