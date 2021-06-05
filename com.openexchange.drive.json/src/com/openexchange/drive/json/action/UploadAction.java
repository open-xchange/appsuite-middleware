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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link UploadAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UploadAction extends AbstractDriveWriteAction {

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * no limits for upload
         */
        enableUnlimitedBodySize(requestData);
        /*
         * get parameters
         */
        String path = requestData.getParameter("path");
        if (Strings.isEmpty(path)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
        }
        String newName = requestData.getParameter("newName");
        if (Strings.isEmpty(newName)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("newName");
        }
        String newChecksum = requestData.getParameter("newChecksum");
        if (Strings.isEmpty(newChecksum)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("newChecksum");
        }
        String checksum = null;
        if (requestData.containsParameter("checksum")) {
            checksum = requestData.getParameter("checksum");
        }
        String name = null;
        if (requestData.containsParameter("name")) {
            name = requestData.getParameter("name");
        }
        long totalLength = -1L;
        if (requestData.containsParameter("totalLength")) {
            totalLength = requestData.getParameter("totalLength", Long.class).longValue();
        }
        long offset = 0L;
        if (requestData.containsParameter("offset")) {
            offset = requestData.getParameter("offset", Long.class).longValue();
        }
        String contentType = null;
        if (requestData.containsParameter("contentType")) {
            contentType = requestData.getParameter("contentType");
        }
        if (Strings.isEmpty(contentType)) {
            contentType = MimeType2ExtMap.getContentType(newName); // as fallback
        }
        Date created = null;
        if (requestData.containsParameter("created")) {
            Long value = requestData.getParameter("created", Long.class);
            created = new Date(value.longValue());
        }
        Date modified = null;
        if (requestData.containsParameter("modified")) {
            Long value = requestData.getParameter("modified", Long.class);
            modified = new Date(value.longValue());
        }
        /*
         * construct referenced file versions from parameters
         */
        FileVersion newFile = new JsonFileVersion(newChecksum, newName);
        FileVersion originalFile = null == checksum ? null : new JsonFileVersion(checksum, null == name ? newName : name);
        /*
         * hand over upload stream
         */
        DriveService driveService = Services.getService(DriveService.class, true);
        SyncResult<FileVersion> syncResult = null;
        InputStream uploadStream = null;
        try {
            uploadStream = requestData.getUploadStream();
            if (null == uploadStream) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }
            syncResult = driveService.upload(session, path, uploadStream, originalFile, newFile, contentType, offset, totalLength, created, modified);
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            if ("DRV-0038".equals(e.getErrorCode())) {
                // The connected client closed the connection unexpectedly
                org.slf4j.LoggerFactory.getLogger(UploadAction.class).debug("", e);
                return AJAXRequestResult.EMPTY_REQUEST_RESULT;
            }
            throw e;
        } finally {
            Streams.close(uploadStream);
        }
        /*
         * return json result
         */
        try {
            if (null != session.isDiagnostics()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("diagnostics", syncResult.getDiagnostics());
                jsonObject.put("actions", JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()));
                return new AJAXRequestResult(jsonObject, "json");
            }
            return new AJAXRequestResult(JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
