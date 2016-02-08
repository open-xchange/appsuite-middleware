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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.mailfilter.json.ajax.json.mapper.parser.action;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link ActionCommandParserUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class ActionCommandParserUtil {

    /**
     * Gets the string value from the specified {@link JSONObject}
     * 
     * @param jsonObject The {@link JSONObject}
     * @param key The key
     * @param component The component name
     * @return The string value
     * @throws OXException If specified key is not present on the specified {@link JSONObject}
     * @throws JSONException If the value of the specified key is <code>null</code>
     */
    static final String getString(JSONObject jsonObject, String key, String component) throws OXException, JSONException {
        String retval = null;
        try {
            retval = jsonObject.getString(key);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, "Error while reading ActionCommand '" + component + "': " + e.getMessage());
        }
        if (retval == null) {
            throw new JSONException("The parameter '" + key + "' is missing for the action command '" + component + "'.");
        }
        return retval;
    }

    /**
     * Creates an array of arrays
     * 
     * @param string The string value to encapsulate into an array of arrays
     * @return The created array of arrays
     */
    static final ArrayList<Object> createArrayOfArrays(final String string) {
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
    static final ArrayList<String> stringToList(final String string) {
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
    static final List<String> coerceToStringList(JSONArray jarray) throws JSONException {
        int length = jarray.length();
        List<String> retval = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            retval.add(jarray.getString(i));
        }
        return retval;
    }
}
