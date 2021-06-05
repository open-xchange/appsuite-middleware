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
