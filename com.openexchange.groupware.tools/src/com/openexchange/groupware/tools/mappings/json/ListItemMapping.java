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

package com.openexchange.groupware.tools.mappings.json;

import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.session.Session;

/**
 * {@link ListItemMapping}
 *
 * @param <O> The type of the object
 * @param <T> The type of the list elements
 * @param <I> The type of the the json list element
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public abstract class ListItemMapping<T,O,I> extends ListMapping<T,O> {

    /**
     * Initializes a new {@link ListItemMapping}.
     * @param ajaxName
     * @param columnID
     */
    public ListItemMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    /**
     * Deserializes a list item from a json list element.
     *
     * @param from The source
     * @param timeZone The {@link TimeZone} to use
     * @return The deserialized object
     * @throws JSONException
     */
    public abstract T deserialize(I from, TimeZone timeZone) throws JSONException;

    /**
     * Serializes a list item to a json list element.
     *
     * @param from The source
     * @param timeZone The {@link TimeZone} to use
     * @return The serialized object
     * @throws JSONException
     */
    public abstract I serialize(T from, TimeZone timeZone) throws JSONException;

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        List<T> value = get(from);
        if (null == value) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(value.size());
        for (int i = 0; i < value.size(); i++) {
            jsonArray.add(i, serialize(value.get(i), timeZone));
        }
        return jsonArray;
    }

}
