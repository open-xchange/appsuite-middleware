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

package com.openexchange.mail.filter.json.v2.json.mapper.parser;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link CommandParserJSONUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class CommandParserJSONUtil {

    /**
     * Gets the string value from the specified {@link JSONObject}
     *
     * @param jsonObject The {@link JSONObject}
     * @param key The key
     * @param commandName The command name
     * @return The string value
     * @throws OXException If specified key is not present in the specified {@link JSONObject}
     */
    public static final String getString(JSONObject jsonObject, String key, String commandName) throws OXException {
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, "Error while reading command '" + commandName + "'. The parameter '" + key + "' is missing: " + e.getMessage());
        }
    }

    /**
     * Gets the JSONArray from the specified {@link JSONObject}
     *
     * @param jobj The {@link JSONObject}
     * @param key The key
     * @param commandName The command name
     * @return The {@link JSONArray}
     * @throws OXException If specified key is not present in the specified {@link JSONObject}@throws OXException
     */
    public static JSONArray getJSONArray(final JSONObject jobj, final String key, final String commandName) throws OXException {
        try {
            return jobj.getJSONArray(key);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, "Error while reading command " + commandName + ". The parameter '" + key + "' is missing: : " + e.getMessage());
        }
    }

    /**
     * Creates an array of arrays
     *
     * @param string The string value to encapsulate into an array of arrays
     * @return The created array of arrays
     */
    public static final ArrayList<Object> createArrayOfArrays(final String string) {
        final ArrayList<Object> retval = new ArrayList<Object>(1);
        final ArrayList<String> strings = new ArrayList<String>(1);
        strings.add(string);
        retval.add(strings);
        return retval;
    }

    /**
     * Creates a singleton {@link ArrayList} with the specified string
     *
     * @param string The string
     * @return A singleton {@link ArrayList}
     */
    public static final ArrayList<String> stringToList(final String string) {
        final ArrayList<String> retval = new ArrayList<String>(1);
        retval.add(string);
        return retval;
    }

    /**
     * Coerces the specified {@link JSONArray} to a {@link List}
     *
     * @param jarray The {@link JSONArray} to coerce
     * @return The {@link List}
     * @throws JSONException if a JSON parsing error occurs
     */
    public static final List<String> coerceToStringList(JSONArray jarray) throws JSONException {
        int length = jarray.length();
        List<String> retval = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            retval.add(jarray.getString(i));
        }
        return retval;
    }

}
