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

package com.openexchange.tools.update;

/**
 * {@link Column} - Immutable column representation providing column name and column definition.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Column {

    /** The column name; e.g. <code>"intfield01"</code> */
    public final String name;

    /** The column definition; e.g. <code>"INT4 unsigned NOT NULL"</code> */
    public final String definition;

    /**
     * Initializes a new {@link Column}.
     *
     * @param name The name; e.g. <code>"intfield01"</code>
     * @param definition The definition; e.g. <code>"INT4 unsigned NOT NULL"</code>
     */
    public Column(String name, String definition) {
        super();
        this.name = name;
        this.definition = definition;
    }

    /**
     * Gets the column name; e.g. <code>"intfield01"</code>.
     *
     * @return The column name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the column definition; e.g. <code>"INT4 unsigned NOT NULL"</code>.
     *
     * @return The column definition
     */
    public String getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32);
        if (name != null) {
            builder.append(name);
        }
        if (definition != null) {
            builder.append(" ").append(definition);
        }
        return builder.toString();
    }

}
