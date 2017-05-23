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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AsciiTable} - Helper class to output an ASCII table to stdout.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class AsciiTable {

    private final List<Column> columns = new ArrayList<>();
    private final List<Row> data = new ArrayList<>();
    private int maxColumnWidth = Integer.MAX_VALUE;

    /**
     * Initializes a new {@link AsciiTable}.
     */
    public AsciiTable() {
        super();
    }

    /**
     * Calculates the width for each column
     */
    public void calculateColumnWidth() {

        for (Column column : columns) {
            column.width = column.name.length() + 1;
        }

        for (Row row : data) {
            int colIdx = 0;
            for (String value : row.values) {
                Column column = columns.get(colIdx);
                if (value == null) {
                    continue;
                }

                column.width = Math.max(column.width, value.length() + 1);
                colIdx++;
            }
        }

        for (Column column : columns) {
            column.width = Math.min(column.width, maxColumnWidth);
        }
    }

    /**
     * Renders the table to stdout.
     */
    public void render() {
        StringBuilder sb = new StringBuilder();

        writeSeparator(columns, sb);
        writeColumnNames(columns, sb);
        writeSeparator(columns, sb);

        // values
        writeValues(columns, data, sb);

        writeSeparator(columns, sb);

        System.out.println(sb.toString());
    }

    private void writeColumnNames(final List<Column> columns, final StringBuilder sb) {
        sb.append("|");
        for (Column column : columns) {
            sb.append(String.format(" %-" + (column.width) + "s", column.name));
            sb.append("|");
        }
        sb.append("\n");
    }

    private void writeSeparator(final List<Column> columns, final StringBuilder sb) {
        sb.append("+");
        for (Column column : columns) {
            sb.append(String.format("%-" + (column.width + 1) + "s", "").replace(' ', '-'));
            sb.append("+");
        }
        sb.append("\n");
    }

    private void writeValues(final List<Column> columns, final List<Row> rows, final StringBuilder sb) {
        for (Row row : rows) {
            int columnIdx = 0;
            sb.append("|");
            for (String value : row.values) {

                if (value != null && value.length() > maxColumnWidth) {
                    value = value.substring(0, maxColumnWidth - 1);
                }

                sb.append(String.format(" %-" + columns.get(columnIdx).width + "s", value));
                sb.append("|");

                columnIdx++;
            }
            sb.append("\n");
        }
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public void addData(Row row) {
        data.add(row);
    }

    public int getMaxColumnWidth() {
        return maxColumnWidth;
    }

    public void setMaxColumnWidth(final int maxColumnWidth) {
        this.maxColumnWidth = maxColumnWidth;
    }

    // --------------------------------------------------------------------------

    public static class Column {

        final String name;
        int width;

        public Column(final String name) {
            super();
            this.name = name;
        }

        @Override
        public String toString() {
            return "Column{" +
                    "name='" + name + '\'' +
                    ", width=" + width +
                    '}';
        }
    }

    public static class Row {

        final List<String> values = new ArrayList<>();

        public void addValue(String value) {
            values.add(value);
        }

        @Override
        public String toString() {
            return "Row{values=" + values + '}';
        }
    }

}
