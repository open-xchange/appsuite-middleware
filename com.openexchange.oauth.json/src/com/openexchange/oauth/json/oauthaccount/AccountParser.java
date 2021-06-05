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

package com.openexchange.oauth.json.oauthaccount;

import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.json.Services;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.server.ServiceExceptionCode;

/**
 * Parses the JSON representation of an OAuth account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AccountParser {

    /**
     * Initializes a new {@link AccountParser}.
     */
    private AccountParser() {
        super();
    }

    /**
     * Parses the OAuth account from specified JSON representation of an OAuth account.
     *
     * @param accountJSON The JSON representation of the OAuth account
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The parsed OAuth account
     * @throws OXException If an OPen-Xchange error occurs
     * @throws JSONException If an error occurs while parsing/reading JSON data
     */
    public static DefaultOAuthAccount parse(JSONObject accountJSON, int userId, int contextId) throws OXException, JSONException {
        DefaultOAuthAccount account = new DefaultOAuthAccount();

        if (accountJSON.hasAndNotNull(AccountField.ID.getName())) {
            account.setId(accountJSON.getInt(AccountField.ID.getName()));
        }
        if (accountJSON.hasAndNotNull(AccountField.DISPLAY_NAME.getName())) {
            account.setDisplayName(accountJSON.getString(AccountField.DISPLAY_NAME.getName()));
        }
        if (accountJSON.hasAndNotNull(AccountField.TOKEN.getName())) {
            account.setToken(accountJSON.getString(AccountField.TOKEN.getName()));
        }
        if (accountJSON.hasAndNotNull(AccountField.SECRET.getName())) {
            account.setSecret(accountJSON.getString(AccountField.SECRET.getName()));
        }
        if (accountJSON.hasAndNotNull(AccountField.SERVICE_ID.getName())) {
            String serviceId = accountJSON.getString(AccountField.SERVICE_ID.getName());
            OAuthServiceMetaDataRegistry registry = Services.getService(OAuthService.class).getMetaDataRegistry();
            account.setMetaData(registry.getService(serviceId, userId, contextId));
        }
        if (accountJSON.hasAndNotNull(AccountField.ENABLED_SCOPES.getName()) && account.getMetaData() != null) {
            JSONArray enabledScopesArray = accountJSON.getJSONArray(AccountField.ENABLED_SCOPES.getName());
            int length = enabledScopesArray.length();
            if (length > 0) {
                OAuthScopeRegistry scopeRegistry = Services.optService(OAuthScopeRegistry.class);
                if (null == scopeRegistry) {
                    throw ServiceExceptionCode.absentService(OAuthScopeRegistry.class);
                }

                API api = account.getMetaData().getAPI();
                Set<OAuthScope> enabledScopes = new LinkedHashSet<>(length);
                for (int i = 0; i < length; i++) {
                    String scope = enabledScopesArray.getString(i);
                    enabledScopes.add(scopeRegistry.getScope(api, OXScope.valueOf(scope)));
                }
                account.setEnabledScopes(enabledScopes);
            }
        }

        return account;
    }

}
