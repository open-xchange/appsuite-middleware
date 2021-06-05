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

package com.openexchange.api.client.common.calls.system;

import java.util.Map;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;

/**
 * {@link WhoamiCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */

public class WhoamiCall extends AbstractGetCall<WhoamiInformation> {

    @Override
    public @NonNull String getModule() {
        return "/system";
    }

    @Override
    protected String getAction() {
        return "whoami";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {}

    @Override
    public HttpResponseParser<WhoamiInformation> getParser() {
        return new AbstractHttpResponseParser<WhoamiInformation>() {

            @Override
            public WhoamiInformation parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                JSONObject data = commonResponse.getJSONObject();
                String sessionId = data.getString("session");
                String user = data.getString("user");
                int userId = data.getInt("user_id");
                int contextId = data.getInt("context_id");
                String locale = data.getString("locale");

                return new WhoamiInformation(sessionId, user, userId, contextId, locale);
            }
        };

    }
}
