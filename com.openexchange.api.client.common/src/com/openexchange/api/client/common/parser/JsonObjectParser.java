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

package com.openexchange.api.client.common.parser;

import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;

/**
 * {@link JsonObjectParser} - Utilizes a mapper for parsing a JSON object
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The type of the object to return
 * @param <E> The enum
 * @since v7.10.5
 */
public class JsonObjectParser<T, E extends Enum<E>> extends AbstractHttpResponseParser<T> {

    private final DefaultJsonMapper<T, E> mapper;

    /**
     * Initializes a new {@link JsonObjectParser}.
     * 
     * @param mapper The mapper to deserialize the JSON
     */
    public JsonObjectParser(DefaultJsonMapper<T, E> mapper) {
        super();
        this.mapper = mapper;
    }

    @Override
    public T parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
        if (commonResponse.isJSONObject()) {
            JSONObject jsonObject = commonResponse.getJSONObject();
            return mapper.deserialize(jsonObject, mapper.getMappedFields());
        }
        throw ApiClientExceptions.JSON_ERROR.create("Not an JSON object");
    }

}
