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
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.json.oauthaccount.AccountParser;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateAction extends AbstractOAuthAJAXActionService {

    /**
     * Initializes a new {@link UpdateAction}.
     */
    public UpdateAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final String accountId = request.getParameter("id");
            final JSONObject data = (JSONObject) request.requireData();
            final int id;
            if (null == accountId) {
                if (!data.has(AccountField.ID.getName())) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create( "id");
                }
                id = data.getInt(AccountField.ID.getName());
            } else {
                id = getUnsignedInteger(accountId);
            }
            if (id < 0) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("id", accountId);
            }
            final DefaultOAuthAccount account = AccountParser.parse(data, session.getUserId(), session.getContextId());
            /*
             * Update account
             */
            final OAuthService oAuthService = getOAuthService();
            final Map<String, Object> arguments = new HashMap<>(4);
            {
                String displayName = account.getDisplayName();
                if (null != displayName) {
                    arguments.put(OAuthConstants.ARGUMENT_DISPLAY_NAME, displayName);
                }
            }
            {
                String token = account.getToken();
                String secret = account.getSecret();
                if (null != token && null != secret) {
                    arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, account);
                }
            }
            if (account.isEnabledScopesSet()) {
                arguments.put(OAuthConstants.ARGUMENT_SCOPES, account.getEnabledScopes());
            }

            if (!arguments.isEmpty()) {
                arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
                oAuthService.updateAccount(session, id, arguments);
            }

            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(Boolean.TRUE);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

}
