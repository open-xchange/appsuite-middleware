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
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.DriveJSONUtils;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.JsonDirectoryVersion;
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.drive.json.pattern.JsonDirectoryPattern;
import com.openexchange.drive.json.pattern.JsonFilePattern;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link SyncFoldersAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncFoldersAction extends AbstractDriveAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyncFoldersAction.class);

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get request data
         */
        Object data = requestData.getData();
        if (null == data || false == JSONObject.class.isInstance(data)) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        JSONObject dataObject = (JSONObject)data;
        /*
         * get original and current client folder versions
         */
        List<DirectoryVersion> originalVersions = null;
        List<DirectoryVersion> clientVersions = null;
        try {
            originalVersions = JsonDirectoryVersion.deserialize(dataObject.optJSONArray("originalVersions"));
            clientVersions = JsonDirectoryVersion.deserialize(dataObject.optJSONArray("clientVersions"));
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
            SyncResult<DirectoryVersion> syncResult = driveService.syncFolders(session, originalVersions, clientVersions);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("actions", JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()));
            jsonObject.put("pathToRoot", syncResult.getPathToRoot());
            if (null != session.isDiagnostics() || session.isIncludeQuota()) {
                if (null != session.isDiagnostics()) { 
                    jsonObject.put("diagnostics", syncResult.getDiagnostics());
                }
                if (session.isIncludeQuota()) {
                    jsonObject.put("quota", DriveJSONUtils.serializeQuota(syncResult.getQuota()));
                }
            }
            return new AJAXRequestResult(jsonObject, "json");
        } catch (OXException e) {
            if ("DRV".equals(e.getPrefix())) {
                LOG.debug("Error performing syncFolders request", e);
            } else {
                LOG.warn("Error performing syncFolders request", e);
            }
            throw e;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
