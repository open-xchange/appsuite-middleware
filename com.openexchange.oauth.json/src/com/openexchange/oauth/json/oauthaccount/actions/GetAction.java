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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.association.OAuthAccountAssociationService;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.oauthaccount.AccountWriter;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetAction extends AbstractOAuthAJAXActionService {

    /**
     * Initializes a new {@link GetAction}.
     */
    public GetAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final String accountId = request.getParameter("id");
            if (null == accountId) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
            }
            /*
             * Request account
             */
            final OAuthService oAuthService = getOAuthService();
            final OAuthAccount account = oAuthService.getAccount(session, getUnsignedInteger(accountId));
            /*
             * Write account as a JSON object
             */
            OAuthAccountAssociationService associationService = getOAuthAccountAssociationService();
            final JSONObject jsonObject = AccountWriter.write(account, associationService.getAssociationsFor(account.getId(), session), session);
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(jsonObject);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
