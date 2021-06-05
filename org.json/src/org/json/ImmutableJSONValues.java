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

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ImmutableJSONValues} - Utility class for immutable JSON instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
class ImmutableJSONValues {

    /**
     * Initializes a new {@link ImmutableJSONValues}.
     */
    private ImmutableJSONValues() {
        super();
    }

    private static interface ImmutableValueProvider {

        Object getValue(Object source);
    }

    private static final Map<Class<?>, ImmutableValueProvider> IMMUTABLE_PROVIDERS;
    static {
        Map<Class<?>, ImmutableValueProvider> m = new HashMap<>(4);

        m.put(JSONArray.class, new ImmutableValueProvider() {

            @Override
            public Object getValue(Object source) {
                return ImmutableJSONArray.immutableFor((JSONArray) source);
            }
        });

        m.put(JSONObject.class, new ImmutableValueProvider() {

            @Override
            public Object getValue(Object source) {
                return ImmutableJSONObject.immutableFor((JSONObject) source);
            }
        });

        IMMUTABLE_PROVIDERS = ImmutableMap.copyOf(m);
    }

    /**
     * Gets the immutable view for given value.
     *
     * @param value The value
     * @return The immutable value
     */
    static Object getImmutableValueFor(Object value) {
        if (null == value) {
            return JSONObject.NULL;
        }

        ImmutableValueProvider immutableProvider = IMMUTABLE_PROVIDERS.get(value.getClass());
        return null == immutableProvider ? value : immutableProvider.getValue(value);
    }

}
