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
package com.openexchange.find;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the requested columns that shall be set within search result objects.
 * If no columns have been requested, {@link #isUnset()} will return <code>true</code>.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Columns implements Serializable{

    private static final long serialVersionUID = -3440984967716322340L;

    private final List<Integer> intColumns;

    private final List<String> stringColumns;

    private final String[] columns;

    /**
     * Initializes a new {@link Columns}.
     *
     * @param columns The columns or <code>null</code>
     */
    public Columns(String[] columns) {
        super();
        this.columns = columns;
        if (columns != null && columns.length > 0) {
            intColumns = new ArrayList<Integer>(columns.length);
            stringColumns = new ArrayList<String>(columns.length);
            for (String c : columns) {
                try {
                    intColumns.add(Integer.valueOf(c));
                } catch (NumberFormatException e) {
                    stringColumns.add(c);
                }
            }
        } else {
            intColumns = Collections.emptyList();
            stringColumns = Collections.emptyList();
        }
    }

    /**
     * Gets whether a certain set of columns has been requested or not.
     *
     * @return <code>true</code> if columns have been requested.
     */
    public boolean isSet() {
        return columns != null && columns.length > 0;
    }

    /**
     * Gets whether a certain set of columns has been requested or not.
     *
     * @return <code>true</code> if no columns have been requested.
     */
    public boolean isUnset() {
        return columns == null || columns.length == 0;
    }

    /**
     * Gets all contained integer columns as an array of ints.
     *
     * @return The array; if no integer columns exist, the array is empty
     */
    public int[] getIntColumns() {
        int[] array = new int[intColumns.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = intColumns.get(i).intValue();
        }
        return array;
    }

    /**
     * Gets all contained string columns as an array of strings.
     *
     * @return The array; if no integer columns exist, the array is empty
     */
    public String[] getStringColumns() {
        return stringColumns.toArray(new String[stringColumns.size()]);
    }

    /**
     * Gets the originally requested columns as string array.
     *
     * @return The array; if no columns have been requested, <code>null</code> is returned
     */
    public String[] getOriginalColumns() {
        return columns;
    }

    @Override
    public String toString() {
        return "Columns [columns=" + Arrays.toString(columns) + "]";
    }

}
