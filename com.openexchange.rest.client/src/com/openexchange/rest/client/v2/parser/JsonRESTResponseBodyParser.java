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

package com.openexchange.rest.client.v2.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.v2.RESTResponse;

/**
 * {@link JsonRESTResponseBodyParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class JsonRESTResponseBodyParser implements RESTResponseBodyParser {

    private static final String CHARSET = "UTF-8";
    private final Set<String> contentTypes;

    /**
     * Initialises a new {@link JsonRESTResponseBodyParser}.
     */
    public JsonRESTResponseBodyParser() {
        super();
        contentTypes = Collections.singleton("application/json");
    }

    @Override
    public void parse(HttpResponse httpResponse, RESTResponse restResponse) throws OXException {
        try (InputStream inputStream = Streams.bufferedInputStreamFor(httpResponse.getEntity().getContent())) {
            String string = Streams.stream2string(inputStream, CHARSET);
            char c = string.charAt(0);
            switch (c) {
                case '{':
                    restResponse.setResponseBody(new JSONObject(string));
                    return;
                case '[':
                    restResponse.setResponseBody(new JSONArray(string));
                    return;
                default:
                    throw RESTExceptionCodes.JSON_ERROR.create("Unexpected start token detected '" + c + "'");
            }
        } catch (IOException e) {
            throw RESTExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw RESTExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Set<String> getContentTypes() {
        return contentTypes;
    }
}
