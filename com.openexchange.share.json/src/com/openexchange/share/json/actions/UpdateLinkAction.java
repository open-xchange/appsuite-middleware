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

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.LinkUpdate;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareTarget;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateLinkAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class UpdateLinkAction extends AbstractShareAction {

    /**
     * Initializes a new {@link UpdateLinkAction}.
     *
     * @param services The service lookup
     */
    public UpdateLinkAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * parse parameters & target
         */
        Date clientTimestamp = new Date(requestData.getParameter("timestamp", Long.class).longValue());
        JSONObject json = (JSONObject) requestData.requireData();
        ShareTarget target = getParser().parseTarget(json);
        /*
         * update share based on present data in update request
         */
        ShareLink shareLink;
        try {
            LinkUpdate linkUpdate = new LinkUpdate();
            if (json.has("expiry_date")) {
                Date newExpiry = json.isNull("expiry_date") ? null : new Date(getParser().removeTimeZoneOffset(json.getLong("expiry_date"), getTimeZone(requestData, session)));
                linkUpdate.setExpiryDate(newExpiry);
            }
            if (json.has("password")) {
                String newPassword = json.isNull("password") ? null : json.getString("password");
                linkUpdate.setPassword(newPassword);
            }
            if (json.has("includeSubfolders")) {
                linkUpdate.setIncludeSubfolders(json.optBoolean("includeSubfolders"));
            }
            shareLink = getShareService().updateLink(session, target, linkUpdate, clientTimestamp);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
        /*
         * return empty result in case of success
         */
        return new AJAXRequestResult(new JSONObject(), shareLink.getTimestamp(), "json");
    }

}
