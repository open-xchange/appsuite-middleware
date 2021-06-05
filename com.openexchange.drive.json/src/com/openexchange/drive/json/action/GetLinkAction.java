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

import static com.openexchange.java.Autoboxing.B;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveShareLink;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link GetLinkAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class GetLinkAction extends AbstractDriveAction {

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * parse target & get or create the share link
         */
        DriveShareTarget target = getShareParser().parseTarget((JSONObject) requestData.requireData());
        DriveShareLink shareLink = getDriveService().getUtility().getLink(session, target);
        /*
         * return appropriate result
         */
        try {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("url", shareLink.getShareURL(requestData.getHostData()));
            jsonResult.put("is_new", shareLink.isNew());
            Date expiryDate = shareLink.getGuest().getExpiryDate();
            if (null != expiryDate) {
                jsonResult.put("expiry_date", expiryDate.getTime());
            }
            jsonResult.putOpt("password", shareLink.getGuest().getPassword());
            jsonResult.putOpt("includeSubfolders", B(shareLink.isIncludeSubfolders()));
            jsonResult.put("checksum", shareLink.getTarget().getChecksum());
            return new AJAXRequestResult(jsonResult, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

}
