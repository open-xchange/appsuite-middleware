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

import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * {@link ImmutableJSONArray} - An immutable {@link JSONObject}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ImmutableJSONArray extends JSONArray {

    private static final long serialVersionUID = 1183196320780891929L;

    /**
     * Gets the immutable view for specified JSON array.
     *
     * @param jsonArray The source JSON array
     * @return The immutable JSON array
     */
    public static ImmutableJSONArray immutableFor(JSONArray jsonArray) {
        if (null == jsonArray) {
            return null;
        }
        return jsonArray instanceof ImmutableJSONArray ? (ImmutableJSONArray) jsonArray : new ImmutableJSONArray(jsonArray);
    }

    // --------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ImmutableJSONArray}.
     *
     * @param jsonArray The JSON array to copy from
     */
    private ImmutableJSONArray(JSONArray jsonArray) {
        super(createImmutableListFrom(jsonArray.getMyArrayList()), true);
    }

    /**
     * Creates the immutable view for given list.
     *
     * @param list The list
     * @return The immutable list
     */
    static ImmutableList<Object> createImmutableListFrom(List<Object> list) {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        for (Object object : list) {
            builder.add(ImmutableJSONValues.getImmutableValueFor(object));
        }
        return builder.build();
    }


}
