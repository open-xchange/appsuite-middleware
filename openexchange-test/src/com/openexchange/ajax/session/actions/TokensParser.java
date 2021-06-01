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

package com.openexchange.ajax.session.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link TokensParser}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TokensParser extends AbstractAJAXParser<TokensResponse> {

    TokensParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected TokensResponse createResponse(Response response) throws JSONException {
        TokensResponse retval = new TokensResponse(response);
        final JSONObject json = (JSONObject) response.getData();
        if (isFailOnError()) {
            assertFalse(response.getErrorMessage(), response.hasError());
            assertTrue("Session ID is missing.", json.has(LoginServlet.PARAMETER_SESSION));
            assertFalse("Random should be missing.", json.has(LoginFields.RANDOM_PARAM));
        }
        if (!response.hasError()) {
            retval.setSessionId(json.getString(LoginServlet.PARAMETER_SESSION));
        }
        return retval;
    }
}
