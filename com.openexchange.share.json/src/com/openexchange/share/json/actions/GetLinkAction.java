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

package com.openexchange.share.json.actions;

import static com.openexchange.java.Autoboxing.B;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareTarget;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetLinkAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GetLinkAction extends AbstractShareAction {

    /**
     * Initializes a new {@link GetLinkAction}.
     *
     * @param services A service lookup reference
     */
    public GetLinkAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * parse target & get or create the share link
         */
        ShareTarget target = getParser().parseTarget((JSONObject) requestData.requireData());
        ShareLink shareLink = getShareService().getLink(session, target);
        /*
         * return appropriate result
         */
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("url", shareLink.getShareURL(requestData.getHostData()));
            jsonResult.put("entity", shareLink.getGuest().getGuestID());
            jsonResult.put("is_new", shareLink.isNew());
            Date expiryDate = shareLink.getGuest().getExpiryDate();
            if (null != expiryDate) {
                jsonResult.put("expiry_date", getParser().addTimeZoneOffset(expiryDate.getTime(), getTimeZone(requestData, session)));
            }
            jsonResult.putOpt("password", shareLink.getGuest().getPassword());
            jsonResult.putOpt("includeSubfolders", B(shareLink.isIncludeSubfolders()));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
        return new AJAXRequestResult(jsonResult, shareLink.getTimestamp(), "json");
    }

}
