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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ListMapping}
 *
 * JSON specific mapping for list properties.
 *
 * @param <O> The type of the object
 * @param <T> The type of the list elements
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ListMapping<T, O> extends DefaultJsonMapping<List<T>, O> {

    public ListMapping(String ajaxName, Integer columnID) {
		super(ajaxName, columnID);
	}

    protected abstract T deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException;

	@Override
    public void deserialize(JSONObject from, O to, TimeZone timeZone) throws JSONException, OXException {
		if (from.isNull(getAjaxName())) {
            set(to, null);
		} else {
			JSONArray jsonArray = from.getJSONArray(getAjaxName());
			int size = jsonArray.length();
            List<T> array = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
                array.add(deserialize(jsonArray, i, timeZone));
			}
			this.set(to, array);
		}
	}

    @Override
    public void deserialize(JSONObject from, O to) throws JSONException, OXException {
        deserialize(from, to, TimeZone.getTimeZone("UTC"));
    }

	@Override
	public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        List<T> value = get(from);
        return null != value ? new JSONArray(value) : (JSONArray) null;
	}

}
