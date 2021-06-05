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

package com.openexchange.oauth.json.oauthaccount.actions;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteAction extends AbstractOAuthAJAXActionService {

    /**
     * Initializes a new {@link DeleteAction}.
     */
    public DeleteAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        final Object data = request.getData();
        if (data instanceof JSONObject) {
            /*
             * Parse parameters
             */
            final String accountId = request.getParameter("id");
            if (null == accountId) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
            }
            /*
             * Delete account
             */
            final OAuthService oAuthService = getOAuthService();
            oAuthService.deleteAccount(session, getUnsignedInteger(accountId));
        } else if (data instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) data;
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                JSONObject json = jsonArray.getJSONObject(i);
                final String accountId = json.getString("id");
                if (null == accountId) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
                }
                /*
                 * Delete account
                 */
                final OAuthService oAuthService = getOAuthService();
                oAuthService.deleteAccount(session, getUnsignedInteger(accountId));
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e);
                }
            }
        } else if (data == null) {
        	request.require("id");
        	int id = request.getParameter("id", int.class).intValue();
        	final OAuthService oAuthService = getOAuthService();
            oAuthService.deleteAccount(session, id);

        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(Boolean.TRUE);
    }

}
