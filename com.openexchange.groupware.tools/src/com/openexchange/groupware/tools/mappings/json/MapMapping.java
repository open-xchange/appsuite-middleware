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

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link MapMapping}
 *
 * JSON specific mapping for a map
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public abstract class MapMapping<O> extends DefaultJsonMapping<Map<String,Object>, O> {

    /**
     * Initializes a new {@link MapMapping}.
     *
     * @param ajaxName The ajaxName
     * @param columnID The columnId
     */
    public MapMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    public void deserialize(JSONObject from, O to) throws JSONException, OXException {
        if (from.isNull(getAjaxName())) {
           set(to, null);
        } else {
            JSONObject jsonObject = from.getJSONObject(getAjaxName());
            HashMap<String, Object> map = new HashMap<>(jsonObject.entrySet().size());
            for(Entry<String, Object> entry : jsonObject.entrySet()) {
               map.put(entry.getKey(), entry.getValue());
            }
            set(to, map);
        }
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) {
        Map<String, Object> value = get(from);
        return value != null ? new JSONObject(value) : null;
    }
}
