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

package com.openexchange.api.client.common.calls;

import java.util.Map;
import java.util.Objects;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;

/**
 * {@link GetJsonObjectDataCall}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class GetJsonObjectDataCall extends AbstractGetCall<JSONObject> {

    private final String module;
    private final String action;
    private final Map<String, String> parameters;

    /**
     * Initializes a new {@link GetJsonObjectDataCall}.
     * 
     * @param module The module of the HTTP API to call
     * @param action The action in the module of the HTTP API to call
     * @param parameters Additional URL parameters to include in the call
     */
    public GetJsonObjectDataCall(String module, String action, Map<String, String> parameters) {
        super();
        this.module = Objects.requireNonNull(module);
        this.action = action;
        this.parameters = parameters;
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull String getModule() {
        return module;
    }

    @Override
    protected String getAction() {
        return action;
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        if (null != this.parameters) {
            parameters.putAll(this.parameters);
        }
    }

    @Override
    public HttpResponseParser<JSONObject> getParser() {
        return new AbstractHttpResponseParser<JSONObject>() {

            @Override
            public JSONObject parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                if (null == commonResponse || false == commonResponse.isJSONObject()) {
                    throw ApiClientExceptions.UNEXPECTED_ERROR.create("Unexpected response format");
                }                
                return commonResponse.getJSONObject();
            }
        };
    }

}
