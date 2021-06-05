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

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.SecureContentWrapper;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.association.OAuthAccountAssociationService;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.json.oauthaccount.AccountWriter;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CreateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@DispatcherNotes(noSecretCallback = true)
public final class CreateAction extends AbstractOAuthTokenAction {

    /**
     * Initializes a new {@link CreateAction}.
     */
    public CreateAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            // The meta data identifier
            final String serviceId = request.getParameter(AccountField.SERVICE_ID.getName());
            if (serviceId == null) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AccountField.SERVICE_ID.getName());
            }

            // Get service meta data
            final OAuthService oAuthService = getOAuthService();
            final OAuthServiceMetaData service;
            {
                final OAuthServiceMetaDataRegistry registry = oAuthService.getMetaDataRegistry();
                service = registry.getService(serviceId, session.getUserId(), session.getContextId());
            }
            final Map<String, Object> arguments = processOAuthArguments(request, session, service);

            // Get the scopes
            Set<OAuthScope> scopes = getScopes(request, serviceId);

            // By now it doesn't matter which interaction type is passed
            OAuthAccount newAccount;
            try {
                newAccount = oAuthService.createAccount(session, serviceId, scopes, OAuthInteractionType.CALLBACK, arguments);
            } catch (OXException e) {
                // Create attempt failed
                HttpServletResponse response = request.optHttpServletResponse();
                if (null == response) {
                    throw e;
                }

                try {
                    Tools.sendErrorPage(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
                    return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                } catch (IOException ioe) {
                    throw OAuthExceptionCodes.IO_ERROR.create(ioe, ioe.getMessage());
                }
            }

            // Write as JSON
            OAuthAccountAssociationService associationService = getOAuthAccountAssociationService();
            final JSONObject jsonAccount = AccountWriter.write(newAccount, associationService.getAssociationsFor(newAccount.getId(), session), session);
            // Return appropriate result
            return new AJAXRequestResult(new SecureContentWrapper(jsonAccount, "json"), SecureContentWrapper.CONTENT_TYPE);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
}
