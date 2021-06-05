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

package com.openexchange.api.client.common.calls.capabilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;

/**
 * {@link AllCall}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class AllCall extends AbstractGetCall<Set<String>> {

    @Override
    @NonNull
    public String getModule() {
        return "capabilities";
    }

    @Override
    protected String getAction() {
        return "all";
    }

    @Override
    public HttpResponseParser<Set<String>> getParser() {
        return new AbstractHttpResponseParser<Set<String>>() {

            @Override
            public Set<String> parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                JSONArray jsonArray = commonResponse.getJSONArray();
                Set<String> capabilities = new HashSet<String>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    capabilities.add(jsonArray.getJSONObject(i).getString("id"));
                }
                return capabilities;
            }
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        // no
    }

}
