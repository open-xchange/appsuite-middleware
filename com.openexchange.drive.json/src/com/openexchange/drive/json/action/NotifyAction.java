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
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.NotificationParameters;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link NotifyAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class NotifyAction extends AbstractDriveWriteAction {

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * parse parameters & target
         */
        JSONObject json = (JSONObject) requestData.requireData();
        DriveShareTarget target = getShareParser().parseTarget(json);
        NotificationParameters parameters = new NotificationParameters();
        int[] entityIDs;
        try {
            JSONArray jsonArray = json.getJSONArray("entities");
            if (null == jsonArray || 0 == jsonArray.length()) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("entities");
            }
            entityIDs = new int[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                entityIDs[i] = jsonArray.getInt(i);
            }
            JSONObject jsonNotification = json.optJSONObject("notification");
            if (null != jsonNotification) {
                parameters.setNotificationTransport(getShareParser().parseNotificationTransport(jsonNotification));
                parameters.setNotificationMessage(jsonNotification.optString("message", null));
            } else {
                parameters.setNotificationTransport(Transport.MAIL);
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * notify entities, return empty result in case of success
         */
        getDriveService().getUtility().notify(session, target, entityIDs, parameters);
        AJAXRequestResult result = new AJAXRequestResult(new JSONObject(), "json");
        if (null != parameters.getWarnings()) {
            result.addWarnings(parameters.getWarnings());
        }
        return result;
    }

}
