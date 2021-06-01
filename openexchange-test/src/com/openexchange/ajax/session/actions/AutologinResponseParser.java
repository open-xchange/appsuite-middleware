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
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link AutologinResponseParser}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.2
 */
public class AutologinResponseParser extends AbstractAJAXParser<AutologinResponse> {

    /**
     * Initializes a new {@link AutologinResponseParser}.
     *
     * @param failOnError
     */
    public AutologinResponseParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected AutologinResponse createResponse(Response response) throws JSONException {
        AutologinResponse autologinResponse = new AutologinResponse(response);
        JSONObject json = response.getJSON();
        if (response.hasError()) {
            response.setData(null);
        } else {
            autologinResponse.setSessionId(json.getString(LoginServlet.PARAMETER_SESSION));
            autologinResponse.setUser(json.optString(LoginServlet.PARAMETER_USER, null));
            autologinResponse.setUserId(json.getInt(LoginServlet.PARAMETER_USER_ID));
            autologinResponse.setContextId(json.getInt("context_id"));
        }
        if (isFailOnError()) {
            assertFalse(response.getErrorMessage(), response.hasError());
        }
        return autologinResponse;
    }

}
