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

package com.openexchange.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * {@link JSON} - helpers for typical JSON tasks
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class JSON {

    /**
     * Takes a JSONArray and transforms it to a list
     *
     * @param array JSONArray to transform
     * @return list that is result of transformation
     * @throws JSONException in case JSON cannot be read
     */
    public static List<String> jsonArray2list(final JSONArray array) throws JSONException {
        if (null == array) {
            return new LinkedList<String>();
        }
        final int length = array.length();
        if (0 == length) {
            return new LinkedList<String>();
        }
        final List<String> list = new ArrayList<String>(length);
        for (int i = 0, size = length; i < size; i++) {
            list.add(array.getString(i));
        }
        return list;
    }

    /**
     * Takes a collection and transforms it to a JSONArray
     *
     * @param coll Collection to transform
     * @return array that is result of transformation
     */
    public static JSONArray collection2jsonArray(final Collection<? extends Object> coll) {
        if (null == coll) {
            return new JSONArray(1);
        }
        final JSONArray array = new JSONArray(coll.size());
        for (final Object obj : coll) {
            array.put(obj);
        }
        return array;
    }

}
