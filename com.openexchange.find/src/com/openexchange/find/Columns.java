/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package com.openexchange.find;

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
public class Columns {

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
            array[i] = intColumns.get(i);
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
