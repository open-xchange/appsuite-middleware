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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.NotificationParameters;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.json.JsonDirectoryVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UpdateFolderAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class UpdateFolderAction extends AbstractDriveWriteAction {

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * parse parameters & directory metadata
         */
        String path = requestData.getParameter("path");
        if (Strings.isEmpty(path)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
        }
        String checksum = requestData.getParameter("checksum");
        if (Strings.isEmpty(checksum)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
        }
        boolean cascadePermissions = Boolean.parseBoolean(requestData.getParameter("cascadePermissions"));
        JSONObject json = (JSONObject) requestData.requireData();
        NotificationParameters parameters = new NotificationParameters();
        JSONObject jsonFolder;
        try {
            jsonFolder = json.getJSONObject("folder");
            if (null == jsonFolder) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("folder");
            }
            JSONObject jsonNotification = json.optJSONObject("notification");
            if (null != jsonNotification) {
                parameters.setNotificationTransport(getShareParser().parseNotificationTransport(jsonNotification));
                parameters.setNotificationMessage(jsonNotification.optString("message", null));
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * update the directory, return empty result in case of success
         */
        getDriveService().getUtility().updateDirectory(session, new JsonDirectoryVersion(checksum, path), jsonFolder, cascadePermissions, parameters);
        AJAXRequestResult result = new AJAXRequestResult(new JSONObject(), "json");
        if (null != parameters.getWarnings()) {
            result.addWarnings(parameters.getWarnings());
        }
        return result;
    }

}
