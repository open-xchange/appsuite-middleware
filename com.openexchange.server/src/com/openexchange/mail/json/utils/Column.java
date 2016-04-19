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
