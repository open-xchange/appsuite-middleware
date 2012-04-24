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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.ajax.tools;

import java.util.Iterator;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * {@link JSONUtil} - Provides JSON utility methods.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONUtil {

    /**
     * Initializes a new {@link JSONUtil}.
     */
    private JSONUtil() {
        super();
    }

    /**
     * Creates a {@link JSONObject} containing the merged view on given JSON objects.
     * 
     * @param jObject1 The first JSON object
     * @param jObjects The other JSON objects to merge with
     * @return The merged JSON object
     * @throws JSONException If composing merged JSON object fails for any reason
     */
    public static JSONObject merge(final JSONObject jObject1, final JSONObject... jObjects) throws JSONException {
        if (null == jObject1) {
            return jObject1;
        }
        if ((null == jObjects) || (0 == jObjects.length)) {
            final JSONObject ret = new JSONObject();
            for (final Entry<String,Object> entry : jObject1.entrySet()) {
                ret.put(entry.getKey(), entry.getValue());
            }
            return ret;
        }
        final JSONObject merged = new JSONObject();
        for (final Iterator<String> iter = jObject1.keys(); iter.hasNext();) {
            final String key = iter.next();
            merged.put(key, jObject1.get(key));
        }
        // Iterate others
        for (final JSONObject obj : jObjects) {
            for (final Iterator<String> iter = obj.keys(); iter.hasNext();) {
                final String key = iter.next();
                merged.put(key, obj.get(key));
            }
        }
        return merged;
    }

    /**
     * Gets the appropriate JSON value for specified string.
     * <p>
     * The value can be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * 
     * @param value The value
     * @return The resulting object
     * @throws JSONException If String cannot be transformed to any object according to JSON specification
     */
    public static Object toObject(final String value) throws JSONException {
        return new JSONTokener(value).nextValue();
    }

}
