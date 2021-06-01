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

package com.openexchange.ajax.oauth.provider.actions;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.oauth.provider.authorizationserver.client.DefaultClient;
import com.openexchange.oauth.provider.authorizationserver.grant.DefaultGrantView;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantView;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;

/**
 * {@link AllResponse}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AllResponse extends AbstractAJAXResponse {

    /**
     * Initializes a new {@link AllResponse}.
     * 
     * @param response
     */
    protected AllResponse(Response response) {
        super(response);
    }

    public List<GrantView> getGrantViews() throws JSONException {
        List<GrantView> grants = new LinkedList<>();
        JSONArray data = (JSONArray) getData();
        for (int i = 0; i < data.length(); i++) {
            JSONObject jGrant = data.getJSONObject(i);
            JSONObject jClient = jGrant.getJSONObject("client");
            DefaultClient client = new DefaultClient();
            client.setId(jClient.getString("id"));
            client.setName(jClient.getString("name"));
            client.setDescription(jClient.getString("description"));
            client.setWebsite(jClient.getString("website"));

            List<String> scopeTokens = new LinkedList<>();
            JSONObject jScopes = jGrant.getJSONObject("scopes");
            scopeTokens.addAll(jScopes.keySet());
            Scope scope = Scope.newInstance(scopeTokens);
            Date latestGrantDate = new Date(jGrant.getLong("date"));

            DefaultGrantView grant = new DefaultGrantView();
            grant.setClient(client);
            grant.setScope(scope);
            grant.setLatestGrantDate(latestGrantDate);
            grants.add(grant);

        }
        return grants;
    }

}
