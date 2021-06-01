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

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.DriveJSONUtils;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.drive.json.pattern.JsonDirectoryPattern;
import com.openexchange.drive.json.pattern.JsonFilePattern;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link SyncFilesAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncFilesAction extends AbstractDriveAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyncFilesAction.class);

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get request data
         */
        String path = requestData.getParameter("path");
        if (Strings.isEmpty(path)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
        }

        Object data = requestData.getData();
        if (null == data || false == JSONObject.class.isInstance(data)) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        JSONObject dataObject = (JSONObject) data;
        /*
         * get original and current client file versions
         */
        List<FileVersion> originalFiles = null;
        List<FileVersion> clientFiles = null;
        try {
            originalFiles = JsonFileVersion.deserialize(dataObject.optJSONArray("originalVersions"));
            clientFiles = JsonFileVersion.deserialize(dataObject.optJSONArray("clientVersions"));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * extract file- and directory exclusions if present
         */
        try {
            session.setDirectoryExclusions(JsonDirectoryPattern.deserialize(dataObject.optJSONArray("directoryExclusions")));
            session.setFileExclusions(JsonFilePattern.deserialize(dataObject.optJSONArray("fileExclusions")));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * determine sync actions & return json result
         */
        try {
            DriveService driveService = Services.getService(DriveService.class, true);
            boolean includeQuota = requestData.containsParameter("quota") ? requestData.getParameter("quota", Boolean.class).booleanValue() : false;
            SyncResult<FileVersion> syncResult = driveService.syncFiles(session, path, originalFiles, clientFiles);
            if (null != session.isDiagnostics() || includeQuota || session.getApiVersion() >= 8) {
                JSONObject jsonObject = new JSONObject();
                if (null != session.isDiagnostics()) {
                    jsonObject.put("diagnostics", syncResult.getDiagnostics());
                }
                if (includeQuota) {
                    jsonObject.put("quota", DriveJSONUtils.serializeQuota(syncResult.getQuota()));
                }
                jsonObject.put("pathToRoot", syncResult.getPathToRoot());
                jsonObject.put("actions", JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()));
                return new AJAXRequestResult(jsonObject, "json");
            }
            return new AJAXRequestResult(JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()), "json");
        } catch (OXException e) {
            if ("DRV".equals(e.getPrefix())) {
                LOG.debug("Error performing syncFiles request", e);
            } else {
                LOG.warn("Error performing syncFiles request", e);
            }
            throw e;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
}
