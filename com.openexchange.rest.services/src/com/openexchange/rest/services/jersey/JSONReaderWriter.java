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

package com.openexchange.rest.services.jersey;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.rest.services.CommonMediaType;

/**
 * A converter for request and response bodies producing/writing JSON objects
 * based on {@link JSONValue}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public class JSONReaderWriter implements MessageBodyReader<JSONValue>, MessageBodyWriter<JSONValue> {

    /**
     * Initialises a new {@link JSONReaderWriter}.
     */
    public JSONReaderWriter() {
        super();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JSONValue.class.isAssignableFrom(type);
    }

    @Override
    public JSONValue readFrom(Class<JSONValue> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        String charset = mediaType.getParameters().get("charset");
        if (charset == null || charset.length() == 0) {
            charset = "UTF-8";
        }

        try {
            return JSONObject.parse(new InputStreamReader(entityStream, charset));
        } catch (JSONException e) {
            // In case the payload is missing or malformed, return with an empty object
            // and let the framework handle the error case.
            if (e.getMessage().contains("Neither a JSONObject nor a JSONArray")) {
                return new JSONObject();
            }
            throw JSONParserUtil.convertJSONException(e);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JSONParserUtil.isApplicable(type, mediaType, MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public long getSize(JSONValue t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(JSONValue t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        JSONParserUtil.writeTo(t, CommonMediaType.APPLICATION_JSON + ";charset=UTF-8", httpHeaders, entityStream);
    }
}
