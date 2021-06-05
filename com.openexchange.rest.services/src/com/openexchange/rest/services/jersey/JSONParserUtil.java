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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.json.JSONException;
import org.json.JSONValue;

/**
 * {@link JSONParserUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
final class JSONParserUtil {

    /**
     * Checks whether the specified <code>target</code> {@link MediaType} is applicable for the specified
     * <code>source</code> {@link MediaType} and whether the specified {@link Class} is a {@link JSONValue}
     * 
     * @param type The {@link Class}
     * @param sourceMediaType The source {@link MediaType}
     * @param targetMediaType The target {@link MediaType}
     * @return <code>true</code> if applicable, <code>false</code> otherwise
     */
    static final boolean isApplicable(Class<?> type, MediaType sourceMediaType, MediaType targetMediaType) {
        return targetMediaType.equals(sourceMediaType) && JSONValue.class.isAssignableFrom(type);
    }

    /**
     * Writes to the specified {@link OutputStream} the specified {@link JSONValue} and appends the appropriate Content-Type
     * header to the specified headers map
     * 
     * @param t The {@link JSONValue} to write
     * @param contentType The Content-Type
     * @param httpHeaders The map with the Http Headers
     * @param entityStream The {@link OutputStream} to write to
     * @throws IOException if an I/O error is occurred
     */
    static final void writeTo(JSONValue t, String contentType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, contentType.toString());
        OutputStreamWriter writer = new OutputStreamWriter(entityStream, "UTF-8");
        try {
            t.write(writer);
        } catch (JSONException e) {
            throw JSONParserUtil.convertJSONException(e);
        } finally {
            writer.flush();
        }
    }

    /**
     * Converts the specified {@link JSONException} to an {@link IOException}
     * 
     * @param e The {@link JSONException} to convert
     * @return The {@link IOException}
     */
    static IOException convertJSONException(JSONException e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return new IOException(e);
        }

        if (IOException.class.isAssignableFrom(cause.getClass())) {
            return (IOException) cause;
        }

        return new IOException(cause);
    }
}
