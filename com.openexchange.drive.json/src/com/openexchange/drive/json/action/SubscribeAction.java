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
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link SubscribeAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SubscribeAction extends AbstractDriveWriteAction {

    @Override
    protected boolean requiresRootFolderID() {
        return false;
    }

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get parameters
         */
        String token = requestData.getParameter("token");
        if (Strings.isEmpty(token)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("token");
        }
        String serviceID = requestData.getParameter("service");
        if (Strings.isEmpty(serviceID)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("service");
        }
        /*
         * get optional root folder identifiers, falling back to the session's default root folder id
         */
        List<String> rootFolderIDs = null;
        Object data = requestData.getData();
        if (null != data) {
            if (false == JSONObject.class.isInstance(data)) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }
            JSONObject dataObject = (JSONObject) data;
            JSONArray rootArray = dataObject.optJSONArray("root");
            if (null != rootArray && 0 < rootArray.length()) {
                rootFolderIDs = new ArrayList<String>(rootArray.length());
                try {
                    for (int i = 0; i < rootArray.length(); i++) {
                        rootFolderIDs.add(rootArray.getString(i));
                    }
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        }
        if (null == rootFolderIDs) {
            if (Strings.isEmpty(session.getRootFolderID())) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("root");
            }
            rootFolderIDs = Collections.singletonList(session.getRootFolderID());
        }
        /*
         * get subscription mode if set
         */
        SubscriptionMode mode = extractSubscriptionMode(requestData);
        /*
         * add subscription
         */
        DriveSubscriptionStore subscriptionStore = Services.getService(DriveSubscriptionStore.class, true);
        subscriptionStore.subscribe(session.getServerSession(), serviceID, token, rootFolderIDs, mode);
        /*
         * return empty json object to indicate success
         */
        return new AJAXRequestResult(new JSONObject(0), "json");
    }

}
