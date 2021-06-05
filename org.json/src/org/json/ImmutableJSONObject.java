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

package org.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ImmutableJSONObject} - An immutable {@link JSONObject}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ImmutableJSONObject extends JSONObject {

    private static final long serialVersionUID = 7348084518800542046L;

    /**
     * Gets the immutable view for specified JSON object.
     *
     * @param jsonObject The source JSON object
     * @return The immutable JSON object
     */
    public static ImmutableJSONObject immutableFor(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        return jsonObject instanceof ImmutableJSONObject ? (ImmutableJSONObject) jsonObject : new ImmutableJSONObject(jsonObject);
    }

    // --------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ImmutableJSONObject}.
     *
     * @param jsonObject The JSON object to copy from
     */
    private ImmutableJSONObject(JSONObject jsonObject) {
        super(createImmutableMapFrom(jsonObject.getMyHashMap()), true);
    }

    /**
     * Creates the immutable view for given map.
     *
     * @param map The map
     * @return The immutable map
     */
    static ImmutableMap<String, Object> createImmutableMapFrom(Map<String, Object> map) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            builder.put(entry.getKey(), ImmutableJSONValues.getImmutableValueFor(entry.getValue()));
        }
        return builder.build();
    }

}
