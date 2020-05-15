/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.storage.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import com.google.json.JsonSanitizer;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link RdbUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
final class RdbUtil {

    /**
     * Reads the specified {@link ResultSet} and parses it to a {@link CalendarAccount}
     *
     * @param resultSet The {@link ResultSet} to parse
     * @return The {@link CalendarAccount}
     * @throws SQLException if an SQL error is occurred
     */
    public static CalendarAccount readAccount(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int user = resultSet.getInt("user");
        String provider = resultSet.getString("provider");
        long lastModified = resultSet.getLong("modified");
        JSONObject internalConfig = readJSON("internalConfig", resultSet);
        JSONObject userConfig = readJSON("userConfig", resultSet);
        return new DefaultCalendarAccount(provider, id, user, internalConfig, userConfig, new Date(lastModified));
    }

    /**
     * Reads the JSON content of the designated column in the current row of specified <code>ResultSet</code> object.
     *
     * @param columnName The column name
     * @param resultSet The <code>ResultSet</code> object
     * @return The JSON content or <code>null</code>
     * @throws SQLException If JSON content cannot be read
     */
    private static JSONObject readJSON(String columnName, ResultSet resultSet) throws SQLException {
        JSONObject retval;
        InputStream inputStream = null;
        try {
            inputStream = resultSet.getBinaryStream(columnName);
            retval = deserialize(inputStream);
        } catch (SQLException e) {
            if (false == JSONException.isParseException(e)) {
                throw e;
            }

            // Try to sanitize corrupt input
            Streams.close(inputStream);
            inputStream = resultSet.getBinaryStream(columnName);
            retval = deserialize(inputStream, true);
        } finally {
            Streams.close(inputStream);
        }
        return retval;
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @return The deserialized JSON object
     */
    private static JSONObject deserialize(InputStream inputStream) throws SQLException {
        return deserialize(inputStream, false);
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @param withSanitize <code>true</code> if JSON content provided by input stream is supposed to be sanitized; otherwise <code>false</code> to read as-is
     * @return The deserialized JSON object
     */
    private static JSONObject deserialize(InputStream inputStream, boolean withSanitize) throws SQLException {
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
     * Serializes a JSON object (as used in an account's configuration) to an input stream.
     *
     * @param data The JSON object serialize, or <code>null</code>
     * @return The serialized JSON object, or <code>null</code> if the passed object was <code>null</code>
     */
    public static InputStream serialize(JSONObject data) {
        if (null == data) {
            return null;
        }
        return new JSONInputStream(data, Charsets.US_ASCII.name());
    }
}
