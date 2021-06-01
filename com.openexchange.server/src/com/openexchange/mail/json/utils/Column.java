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

package com.openexchange.mail.json.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import gnu.trove.ConcurrentTIntObjectHashMap;

/**
 * {@link Column} - A column parameter.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class Column {

    private static final ConcurrentTIntObjectHashMap<Column> FIELDS = new ConcurrentTIntObjectHashMap<Column>(24);
    private static final ConcurrentMap<String, Column> HEADERS = new ConcurrentHashMap<String, Column>(32, 0.9f, 1);

    /**
     * Gets the column for given field.
     *
     * @param field The field
     * @return The column
     */
    public static Column field(int field) {
        Column column = FIELDS.get(field);
        if (null == column) {
            // No need for synchronized
            column = new Column(field);
            FIELDS.put(field, column);
        }
        return column;
    }

    /**
     * Gets the column for given header name.
     *
     * @param header The header name
     * @return The column
     */
    public static Column header(String header) {
        Column column = HEADERS.get(header);
        if (null == column) {
            // No need for synchronized
            column = new Column(header);
            HEADERS.put(header, column);
        }
        return column;
    }

    // --------------------------------------------------------------------------------------------------------

    private final int field;
    private final String header;

    /**
     * Initializes a new {@link Column} for given field.
     *
     * @param field The field
     */
    private Column(int field) {
        super();
        this.field = field;
        this.header = null;
    }

    /**
     * Initializes a new {@link Column} for given header.
     *
     * @param header The header
     */
    private Column(String header) {
        super();
        this.field = -1;
        this.header = header;
    }

    /**
     * Gets the field
     *
     * @return The field
     */
    public int getField() {
        return field;
    }

    /**
     * Gets the header
     *
     * @return The header
     */
    public String getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return field > 0 ? Integer.toString(field) : (null == header ? "null" : header);
    }

}
