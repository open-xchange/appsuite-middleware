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

package com.openexchange.xing.json.actions;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.xing.UserField;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.WebAuthSession;

/**
 * {@link UserFeedAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class UserFeedAction extends AbstractXingAction {

    /**
     * Initializes a new {@link UserFeedAction}.
     */
    public UserFeedAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException {
        Date optSince = null;
        Date optUntil = null;
        Collection<UserField> optUserFields = null;
        String address = getMandatoryStringParameter(req, "email");
        address = validateMailAddress(address);
        // Since/Until
        String since = req.getParameter("since");
        String until = req.getParameter("until");

        if (since != null && until != null) {
            throw XingExceptionCodes.MUTUALLY_EXCLUSIVE.create();
        }

        if (since != null) {
            optSince = new Date(Long.parseLong(since));
        } else if (until != null) {
            optUntil = new Date(Long.parseLong(until));
        }

        // User Fields
        optUserFields = getUserFields(req.getParameter("user_fields"));

        final XingOAuthAccess xingOAuthAccess;
        {
        	String token = req.getParameter("testToken");
        	String secret = req.getParameter("testSecret");
        	if (Strings.isNotEmpty(token) && Strings.isNotEmpty(secret)) {
        		xingOAuthAccess = getXingOAuthAccess(token, secret, req.getSession());
        	} else {
        		xingOAuthAccess = getXingOAuthAccess(req);
        	}
        }
        final XingAPI<WebAuthSession> xingAPI = xingOAuthAccess.getXingAPI();
        final String xingId = xingAPI.findByEmail(address);
        if (Strings.isEmpty(xingId)) {
            // Already connected
            throw XingExceptionCodes.NOT_A_MEMBER.create(address);
        }

        Map<String, Object> feed = xingAPI.getUserFeed(xingId, optSince, optUntil, optUserFields);
        JSONObject result = (JSONObject) JSONCoercion.coerceToJSON(feed);

        return new AJAXRequestResult(result);
    }

}
