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

import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.SecureContentWrapper;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.association.OAuthAccountAssociationService;
import com.openexchange.oauth.json.Services;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.json.oauthaccount.AccountWriter;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.policy.retry.ExponentialBackOffRetryPolicy;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CallbackAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CallbackAction extends AbstractOAuthTokenAction {

    private static final String REAUTHORIZE_ACTION_HINT = "reauthorize";

    /**
     * Initialises a new {@link CallbackAction}.
     */
    public CallbackAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        // The meta data identifier
        final String serviceId = requestData.getParameter(AccountField.SERVICE_ID.getName());
        if (serviceId == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AccountField.SERVICE_ID.getName());
        }

        // Get the scopes
        Set<OAuthScope> scopes = getScopes(requestData, serviceId);

        // Get service meta data
        final OAuthService oAuthService = getOAuthService();
        final OAuthServiceMetaData service;
        {
            final OAuthServiceMetaDataRegistry registry = oAuthService.getMetaDataRegistry();
            service = registry.getService(serviceId, session.getUserId(), session.getContextId());
        }
        final Map<String, Object> arguments = processOAuthArguments(requestData, session, service);
        OAuthAccount oauthAccount = oAuthService.upsertAccount(session, serviceId, getAccountId(requestData), OAuthInteractionType.CALLBACK, arguments, scopes);

        // Trigger a reauthorize task if a reauthorize was requested
        String actionHint = (String) arguments.get(OAuthConstants.URLPARAM_ACTION_HINT);
        if (Strings.isNotEmpty(actionHint) && REAUTHORIZE_ACTION_HINT.equals(actionHint)) {
            ClusterLockService clusterLockService = Services.getService(ClusterLockService.class);
            clusterLockService.runClusterTask(new ReauthorizeClusterTask(requestData, session, Integer.toString(oauthAccount.getId()), serviceId), new ExponentialBackOffRetryPolicy());
        }
        try {
            OAuthAccountAssociationService associationService = getOAuthAccountAssociationService();
            final JSONObject jsonAccount = AccountWriter.write(oauthAccount, associationService.getAssociationsFor(oauthAccount.getId(), session), session);
            return new AJAXRequestResult(new SecureContentWrapper(jsonAccount, "json"), SecureContentWrapper.CONTENT_TYPE);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
}
