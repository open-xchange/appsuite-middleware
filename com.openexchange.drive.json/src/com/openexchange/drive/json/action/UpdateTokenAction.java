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

import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link UpdateTokenAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpdateTokenAction extends AbstractDriveWriteAction {

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
        String newToken = requestData.getParameter("newToken");
        if (Strings.isEmpty(newToken)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("newToken");
        }
        String serviceID = requestData.getParameter("service");
        // workaround for bug #33652
        // if (Strings.isEmpty(serviceID)) {
        //     throw AjaxExceptionCodes.MISSING_PARAMETER.create("service");
        // }
        /*
         * update token
         */
        getSubscriptionStore().updateToken(session.getServerSession(), serviceID, token, newToken);
        /*
         * return empty json object to indicate success
         */
        return new AJAXRequestResult(new JSONObject(0), "json");
    }

}
