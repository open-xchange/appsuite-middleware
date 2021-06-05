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

package com.openexchange.ajax.oauth.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.oauth.types.OAuthService;
import com.openexchange.ajax.writer.ResponseWriter;

/**
 * {@link OAuthServicesParser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class OAuthServicesParser extends AbstractAJAXParser<OAuthServicesResponse> {

    /**
     * Initializes a new {@link OAuthServicesParser}.
     * 
     * @param failOnError
     */
    protected OAuthServicesParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected OAuthServicesResponse createResponse(Response response) throws JSONException {
        List<OAuthService> services = new ArrayList<OAuthService>();
        JSONObject json = ResponseWriter.getJSON(response);
        if (json.has("data")) {
            JSONValue data = (JSONValue) json.get("data");
            if (data.isArray()) {
                JSONArray arr = data.toArray();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    services.add(parseService(obj));
                }
            } else {
                services.add(parseService(data.toObject()));
            }
        }

        return new OAuthServicesResponse(response, services);
    }

    protected OAuthService parseService(JSONObject obj) throws JSONException {
        OAuthService service = new OAuthService(obj.getString("id"), obj.getString("displayName"));
        return service;
    }

}
