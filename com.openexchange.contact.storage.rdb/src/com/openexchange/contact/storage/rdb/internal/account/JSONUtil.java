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

package com.openexchange.contact.storage.rdb.internal.account;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import com.google.json.JsonSanitizer;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link JSONUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public final class JSONUtil {

    /**
     * Serializes a JSON object (as used in an account's configuration) to an input stream.
     *
     * @param data The JSON object serialise, or <code>null</code>
     * @return The serialized JSON object, or <code>null</code> if the passed object was <code>null</code>
     */
    public static InputStream serialise(JSONObject data) {
        if (null == data) {
            return null;
        }
        return new JSONInputStream(data, Charsets.US_ASCII.name());
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @return The deserialized JSON object
     */
    public static JSONObject deserialise(InputStream inputStream) throws SQLException {
        return deserialise(inputStream, false);
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @param withSanitize <code>true</code> if JSON content provided by input stream is supposed to be sanitized; otherwise <code>false</code> to read as-is
     * @return The deserialized JSON object
     */
    public static JSONObject deserialise(InputStream inputStream, boolean withSanitize) throws SQLException {
        if (null == inputStream) {
            return null;
        }

        try {
            if (withSanitize) {
                String jsonish = JsonSanitizer.sanitize(Streams.reader2string(new AsciiReader(inputStream)));
                return new JSONObject(jsonish);
            }

            return new JSONObject(new AsciiReader(inputStream));
        } catch (JSONException e) {
            throw new SQLException(e);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Reads the JSON content of the designated column in the current row of specified <code>ResultSet</code> object.
     *
     * @param columnName The column name
     * @param resultSet The <code>ResultSet</code> object
     * @return The JSON content or <code>null</code>
     * @throws SQLException If JSON content cannot be read
     */
    public static JSONObject readJSON(String columnName, ResultSet resultSet) throws SQLException {
        JSONObject retval;
        InputStream inputStream = null;
        try {
            inputStream = resultSet.getBinaryStream(columnName);
            retval = deserialise(inputStream);
        } catch (SQLException e) {
            if (false == JSONException.isParseException(e)) {
                throw e;
            }

            // Try to sanitize corrupt input
            Streams.close(inputStream);
            inputStream = resultSet.getBinaryStream(columnName);
            retval = deserialise(inputStream, true);
        } finally {
            Streams.close(inputStream);
        }
        return retval;
    }
}
