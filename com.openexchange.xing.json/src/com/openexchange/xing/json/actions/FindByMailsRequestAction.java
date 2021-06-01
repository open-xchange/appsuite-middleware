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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.WebAuthSession;

/**
 * {@link FindByMailsRequestAction}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FindByMailsRequestAction extends AbstractXingAction {

	/**
	 * Initializes a new {@link FindByMailsRequestAction}.
	 */
	public FindByMailsRequestAction(ServiceLookup services) {
		super(services);
	}

    @Override
    protected AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException {
        Object objData = req.getRequest().getData();
        if (objData == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        if (!(objData instanceof JSONObject)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }

        JSONObject jsonData = (JSONObject) objData;
        JSONArray jsonArray = jsonData.optJSONArray("emails");
        if (jsonArray == null) {
        	throw XingExceptionCodes.MANDATORY_REQUEST_DATA_MISSING.create("emails");
        }

        int length = jsonArray.length();
        List<String> emails = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            String email = jsonArray.getString(i);
            email = validateMailAddress(email);
            emails.add(email);
        }

        String token = req.getParameter("testToken");
        String secret = req.getParameter("testSecret");

        final XingOAuthAccess xingOAuthAccess;
        {
            if (Strings.isNotEmpty(token) && Strings.isNotEmpty(secret)) {
                xingOAuthAccess = getXingOAuthAccess(token, secret, req.getSession());
            } else {
                xingOAuthAccess = getXingOAuthAccess(req);
            }
        }

		XingAPI<WebAuthSession> xingAPI = xingOAuthAccess.getXingAPI();
		Map<String, Object> xingUser = xingAPI.findByEmailsGetXingAttributes(emails);

		if (null == xingUser || xingUser.isEmpty()) {
		    return new AJAXRequestResult(new JSONObject(0));
        }

		final JSONObject result = (JSONObject) JSONCoercion.coerceToJSON(xingUser);
		return new AJAXRequestResult(result);
	}
}
