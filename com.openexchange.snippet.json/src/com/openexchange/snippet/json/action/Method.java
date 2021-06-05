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

package com.openexchange.snippet.json.action;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * An enumeration for HTTP methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Method {
    GET, PUT, POST, DELETE;

    private static final Map<String, Method> MAP;

    static {
        Method[] values = Method.values();
        Map<String, Method> m = new HashMap<String, Method>(values.length);
        for (Method method : values) {
            m.put(method.name(), method);
        }
        MAP = ImmutableMap.copyOf(m);
    }

    /**
     * Gets the appropriate method.
     *
     * @param method The method identifier
     * @return The appropriate method or <code>null</code>
     */
    public static Method methodFor(final String method) {
        if (null == method) {
            return null;
        }
        return MAP.get(method.toUpperCase(Locale.US));
    }
}
