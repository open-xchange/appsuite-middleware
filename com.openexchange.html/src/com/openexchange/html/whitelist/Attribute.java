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

package com.openexchange.html.whitelist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Attribute} - Represents an attribute inside an HTML tag.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class Attribute extends AbstractNamedObject {

    private static final Map<String, Attribute> CACHE = new ConcurrentHashMap<>(128, 0.9F, 1);

    /**
     * Gets the attribute for specified attribute name
     *
     * @param attributeName The attribute name
     * @return The attribute
     */
    public static Attribute valueOf(String attributeName) {
        if (null == attributeName) {
            return null;
        }

        Attribute a = CACHE.get(attributeName);
        if (null == a) {
            a = new Attribute(attributeName);
            CACHE.put(attributeName, a);
        }
        return a;
    }

    // -----------------------------------------------------------------

    /**
     * Initializes a new {@link Attribute}.
     *
     * @param attributeName The attribute name
     */
    private Attribute(String attributeName) {
        super(attributeName);
    }

}
