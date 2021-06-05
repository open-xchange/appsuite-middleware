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

package com.openexchange.ajax.oauth.client.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link InitOAuthAccountRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class InitOAuthAccountRequest extends AbstractOAuthRequest<InitOAuthAccountResponse> {

    private final OAuthService authProvider;

    /**
     * Initializes a new {@link InitOAuthAccountRequest}.
     * 
     * @param oauthUrl
     */
    public InitOAuthAccountRequest(OAuthService oauthProvider) {
        this.authProvider = oauthProvider;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "init"));
        parameterList.add(new Parameter("provider", authProvider.getProvider()));
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends InitOAuthAccountResponse> getParser() {
        return new AbstractAJAXParser<InitOAuthAccountResponse>(true) {

            @Override
            protected InitOAuthAccountResponse createResponse(Response response) throws JSONException {
                return new InitOAuthAccountResponse(response);
            }

        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        // TODO Auto-generated method stub
        return null;
    }

}
