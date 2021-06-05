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

package com.openexchange.capabilities.json;

import java.util.Collection;
import java.util.Iterator;
import org.json.ImmutableJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.capabilities.Capability;

/**
 * {@link CapabilitiesJsonWriter} - A simple JSON writer for capabilities.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class CapabilitiesJsonWriter {

    /**
     * Initializes a new {@link CapabilitiesJsonWriter}.
     */
    private CapabilitiesJsonWriter() {
        super();
    }

    private static final JSONObject EMPTY_JSON = ImmutableJSONObject.immutableFor(new JSONObject(0));

    /**
     * Converts given capability to its JSON representation.
     *
     * @param capability The capability
     * @return The capability's JSON representation
     * @throws JSONException If JSON representation cannot be returned
     */
    public static JSONObject toJson(Capability capability) throws JSONException {
        final JSONObject object = new JSONObject(3);
        object.put("id", capability.getId());
        object.put("attributes", EMPTY_JSON);
        return object;
    }

    /**
     * Converts given capabilities collection to its JSON representation.
     *
     * @param capabilities The capabilities collection
     * @return The JSON representation for the capabilities collection
     * @throws JSONException If JSON representation cannot be returned
     */
    public static JSONArray toJson(Collection<Capability> capabilities) throws JSONException {
        int size = capabilities.size();
        JSONArray array = new JSONArray(size);
        Iterator<Capability> iterator = capabilities.iterator();
        for (int i = size; i-- > 0;) {
            array.put(toJson(iterator.next()));
        }
        return array;
    }

}
