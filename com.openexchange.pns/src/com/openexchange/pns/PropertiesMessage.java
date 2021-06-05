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

package com.openexchange.pns;

import java.util.Map;

/**
 * {@link PropertiesMessage} - A message containing properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PropertiesMessage implements Message<Map<String, Object>> {

    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link PropertiesMessage}.
     *
     * @param properties The properties of this message
     * @throws IllegalArgumentException If properties reference is <code>null</code>
     */
    public PropertiesMessage(Map<String, Object> properties) {
        super();
        if (null == properties) {
            throw new IllegalArgumentException("properties must not be null");
        }
        this.properties = properties;
    }

    @Override
    public Map<String, Object> getMessage() {
        return properties;
    }

    @Override
    public String toString() {
        return properties.toString();
    }

}
