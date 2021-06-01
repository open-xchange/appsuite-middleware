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

package com.openexchange.find.facet;

/**
 * {@link FacetInfo} - Provides basic information for a denoted facet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class FacetInfo {

    private final String type;
    private final String value;

    /**
     * Initializes a new {@link FacetInfo}.
     *
     * @param type The identifier of the facet type
     * @param value The value
     */
    public FacetInfo(String type, String value) {
        super();
        this.type = type;
        this.value = value;
    }

    /**
     * Gets the type identifier
     *
     * @return The type identifier
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        if (type != null) {
            builder.append("type=").append(type).append(", ");
        }
        if (value != null) {
            builder.append("value=").append(value);
        }
        builder.append("]");
        return builder.toString();
    }

}
