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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.xing.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;


/**
 * {@link JSONCoercion} - Turns JSON data to its Java representation and vice versa.
 * <p>
 * A {@link JSONObject} is coerced to a {@link Map}, a {@link JSONArray} is coerced to a {@link Collection}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JSONCoercion {

    /**
     * Coerces given JSON data to its Java representation.
     *
     * @param object The JSON data to coerce
     * @return The resulting Java representation.
     * @throws JSONException If coercion fails
     */
    public static Object coerceToNative(final JSONValue object) throws JSONException {
        if (null == object) {
            return null;
        }
        if (object.isArray()) {
            final JSONArray jsonArray = object.toArray();
            final int length = jsonArray.length();
            final List<Object> list = new ArrayList<Object>(length);
            for (int i = 0; i < length; i++) {
                list.add(coerceToNative(jsonArray.get(i)));
            }
            return list;
        }
        if (object.isObject()) {
            final JSONObject jsonObject = object.toObject();
            final Map<String, Object> map = new LinkedHashMap<String, Object>(jsonObject.length());
            for (final String key : jsonObject.keySet()) {
                map.put(key, coerceToNative(jsonObject.get(key)));
            }
            return map;
        }
        return object;
    }

    /**
     * Coerces given JSON data to its Java representation.
     *
     * @param object The JSON data to coerce
     * @return The resulting Java representation.
     */
    public static Object coerceToNative(final Object object) {
        if (object instanceof JSONValue) {
            JSONValue jValue = (JSONValue) object;

            // Check for a JSON array
            if (jValue.isArray()) {
                return jValue.toArray().asList();
            }

            // Otherwise a JSON object
            return jValue.toObject().asMap();
        }
        if (JSONObject.NULL == object) {
            return null;
        }
        return object;
    }

    /**
     * Checks if specified object needs to be coerced to JSON.
     *
     * @param value The object to check
     * @return <code>true</code> if specified object needs to be coerced to JSON; otherwise <code>false</code>
     */
    public static boolean needsJSONCoercion(final Object value) {
        return value instanceof Map || value instanceof Collection || isArray(value);
    }

    /**
     * Checks if specified object needs to be coerced to a native Java object.
     *
     * @param value The object to check
     * @return <code>true</code> if specified object needs to be coerced to a native Java object; otherwise <code>false</code>
     */
    public static boolean needsNativeCoercion(final Object value) {
        return value instanceof JSONValue;
    }

    /**
     * Coerces given Java object to its JSON representation.
     *
     * @param value The Java object to coerce
     * @return The resulting JSON representation
     * @throws JSONException If coercion fails
     */
    public static Object coerceToJSON(final Object value) throws JSONException {
        if (null == value || JSONObject.NULL.equals(value)) {
            return JSONObject.NULL;
        }
        if (value instanceof JSONValue) {
            return value;
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked") final Map<String, ?> map = (Map<String, ?>) value;
            final JSONObject jsonObject = new JSONObject(map.size());
            for (final Map.Entry<String, ?> entry : map.entrySet()) {
                jsonObject.put(entry.getKey(), coerceToJSON(entry.getValue()));
            }
            return jsonObject;
        }
        if (value instanceof Collection) {
            final Collection<?> collection = (Collection<?>) value;
            final JSONArray jsonArray = new JSONArray(collection.size());
            for (final Object object : collection) {
                jsonArray.put(coerceToJSON(object));
            }
            return jsonArray;
        }
        if (isArray(value)) {
            final int length = Array.getLength(value);
            final JSONArray jsonArray = new JSONArray(length);
            for (int i = 0; i < length; i++) {
                final Object object = Array.get(value, i);
                jsonArray.put(coerceToJSON(object));
            }
            return jsonArray;
        }
        /*
         * Return directly
         */
        return value;
    }

    /**
     * Checks if specified object is an array.
     *
     * @param object The object to check
     * @return <code>true</code> if specified object is an array; otherwise <code>false</code>
     */
    public static boolean isArray(final Object object) {
        /*-
         * getClass().isArray() is significantly slower on Sun Java 5 or 6 JRE than on IBM.
         * So much that using clazz.getName().charAt(0) == '[' is faster on Sun JVM.
         */
        // return (null != object && object.getClass().isArray());
        return null != object && '[' == object.getClass().getName().charAt(0);
    }
}
