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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.DriveFileMetadata;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.json.DriveFieldMapper;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileMetadataAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileMetadataAction extends AbstractDriveAction {

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        try {
            /*
             * get parameters
             */
            String path = requestData.getParameter("path");
            if (Strings.isEmpty(path)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
            }
            String columnsValue = requestData.getParameter("columns");
            if (Strings.isEmpty(columnsValue)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("columns");
            }
            String[] splitted = Strings.splitByComma(columnsValue);
            int[] columnIDs = new int[splitted.length];
            for (int i = 0; i < splitted.length; i++) {
                try {
                    columnIDs[i] = Integer.parseInt(splitted[i]);
                } catch (NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("columns");
                }
            }
            DriveFileField[] fields = DriveFieldMapper.getInstance().getFields(columnIDs);
            List<FileVersion> fileVersions;
            Object data = requestData.getData();
            if (null != data) {
                /*
                 * get requested versions from body
                 */
                if (false == JSONArray.class.isInstance(data)) {
                    throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
                }
                fileVersions = JsonFileVersion.deserialize((JSONArray)data);
            } else if (requestData.containsParameter("name")) {
                /*
                 * get requested version from url
                 */
                String name = requestData.getParameter("name");
                if (Strings.isEmpty(name)) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create("name");
                }
                String checksum = requestData.getParameter("checksum");
                if (Strings.isEmpty(checksum)) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
                }
                fileVersions = new ArrayList<FileVersion>(1);
                fileVersions.add(new JsonFileVersion(checksum, name));
            } else {
                /*
                 * no specific versions specified
                 */
                fileVersions = null;
            }
            /*
             * get & return metadata as json
             */
            List<DriveFileMetadata> fileMetadata = getDriveService().getFileMetadata(session, path, fileVersions, Arrays.asList(fields));
            JSONArray jsonArray = DriveFieldMapper.getInstance().serialize(fileMetadata, fields, TimeZones.UTC, session.getServerSession());
            return new AJAXRequestResult(jsonArray, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
