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

package com.openexchange.tokenlogin.json.actions;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tokenlogin.TokenLoginService;
import com.openexchange.tokenlogin.json.TokenLoginRequest;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AcquireTokenAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AcquireTokenAction extends TokenLoginAction {

    /**
     * Initializes a new {@link AcquireTokenAction}.
     * @param lookup
     */
    public AcquireTokenAction(ServiceLookup lookup) {
        super(lookup);
    }

    @Override
    protected AJAXRequestResult perform(TokenLoginRequest request) throws OXException, JSONException {
        TokenLoginService service = getTokenLoginService();
        ServerSession session = request.getSession();
        String token = service.acquireToken(session);

        final JSONObject response = new JSONObject(2);
        response.put("token", token);
        return new AJAXRequestResult(response, new Date(), "json");
    }

}
