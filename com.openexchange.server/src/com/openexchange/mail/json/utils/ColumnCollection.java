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

import java.util.ArrayList;
import java.util.List;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link ColumnCollection} - A column collection.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class ColumnCollection {

    private final List<Column> columns;

    /**
     * Initializes a new {@link ColumnCollection}.
     */
    public ColumnCollection(List<Column> columns) {
        super();
        this.columns = columns;
    }

    /**
     * Gets the columns
     *
     * @return The columns
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Gets the contained fields
     *
     * @return The fields or <code>null</code>
     */
    public int[] getFields() {
        TIntList l = new TIntArrayList(columns.size());
        for (Column column : columns) {
            int field = column.getField();
            if (field > 0) {
                l.add(field);
            }
        }
        return l.isEmpty() ? null : l.toArray();
    }

    /**
     * Gets the contained headers
     *
     * @return The headers or <code>null</code>
     */
    public String[] getHeaders() {
        List<String> l = new ArrayList<String>(columns.size());
        for (Column column : columns) {
            String header = column.getHeader();
            if (null != header) {
                l.add(header);
            }
        }
        return l.isEmpty() ? null : l.toArray(new String[l.size()]);
    }

}
