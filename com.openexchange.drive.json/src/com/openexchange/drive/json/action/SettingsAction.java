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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveQuota;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveSettings;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Quota;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link SettingsAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SettingsAction extends AbstractDriveAction {

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get request data
         */
        String rootFolderID = requestData.getParameter("root");
        if (Strings.isEmpty(rootFolderID)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("root");
        }
        /*
         * get settings
         */
        DriveService driveService = getDriveService();
        DriveSettings settings = driveService.getSettings(session);
        /*
         * return json result
         */
        JSONObject jsonObject = new JSONObject();
        try {
            if (null != settings) {
                jsonObject.put("pathToRoot", settings.getPathToRoot());
                jsonObject.put("helpLink", settings.getHelpLink());
                DriveQuota driveQuota = settings.getQuota();
                if (null != driveQuota) {
                    jsonObject.put("quotaManageLink", driveQuota.getManageLink());
                    if (null != driveQuota.getQuota()) {
                        JSONArray jsonArray = new JSONArray(2);
                        for (Quota q : driveQuota.getQuota()) {
                            if (Quota.UNLIMITED != q.getLimit()) {
                                JSONObject jsonQuota = new JSONObject();
                                jsonQuota.put("limit", q.getLimit());
                                jsonQuota.put("use", q.getUsage());
                                jsonQuota.put("type", String.valueOf(q.getType()).toLowerCase());
                                jsonArray.put(jsonQuota);
                            }
                        }
                        jsonObject.put("quota", jsonArray);
                    }
                }
                jsonObject.put("serverVersion", settings.getServerVersion());
                jsonObject.put("supportedApiVersion", settings.getSupportedApiVersion());
                jsonObject.put("minApiVersion", settings.getMinApiVersion());
                jsonObject.putOpt("minUploadChunk", settings.getMinUploadChunk());
                jsonObject.put("localizedFolderNames", settings.getLocalizedFolders());
                jsonObject.put("capabilities", settings.getCapabilities());
                jsonObject.put("minSearchChars", settings.getMinSearchChars());
                jsonObject.put("hasTrashFolder", settings.hasTrashFolder());
                jsonObject.put("maxConcurrentSyncFiles", settings.getMaxConcurrentSyncFiles());
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        return new AJAXRequestResult(jsonObject, "json");
    }

}
