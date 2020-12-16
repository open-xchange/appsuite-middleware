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

package com.openexchange.logging;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.java.Strings;

/**
 * {@link ConsoleTable} - A utility class for printing a table to console.
 * <p>
 * Example:
 * <pre>
 * ConsoleTable.Builder builder = ConsoleTable.builder(3, "Row", "Surname", "Lastname", "Age");
 * builder.addRow("1", "Donald Peter", "Duck", "55");
 * builder.addRow("2", "Hegret", "Duck", "43");
 * builder.addRow("3", "Chuck", "Duck", "31");
 * System.out.println(builder.build().buildTable());
 * </pre>
 *
 * Yields the following table:
 * <pre>
 * |-----|--------------|----------|-----|
 * | Row | Surname      | Lastname | Age |
 * |-----|--------------|----------|-----|
 * | 1   | Donald Peter | Duck     | 55  |
 * | 2   | Hegret       | Duck     | 43  |
 * | 3   | Chuck        | Duck     | 31  |
 * |-----|--------------|----------|-----|
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ConsoleTable {

    /**
     * Creates a new builder for creating an instance of <code>ConsoleTable</code>.
     *
     * @param expectedNumberOfRows The expected number of rows
     * @param headers The headers of the table
     * @return The newly created builder
     */
    public static Builder builder(int expectedNumberOfRows, String... headers) {
        return new Builder(expectedNumberOfRows, headers);
    }

    /** The builder for instance of <code>ConsoleTable</code> */
    public static class Builder {

        private final List<List<String>> content;
        private final List<String> headers;

        Builder(int expectedNumberOfRows, String[] headers) {
            super();
            if (headers == null || headers.length <= 0) {
                throw new IllegalArgumentException("headers must not be null or empty");
            }
            if (expectedNumberOfRows < 0) {
                throw new IllegalArgumentException("expectedNumberOfRows must not be negative");
            }
            content = new ArrayList<List<String>>(expectedNumberOfRows);
            List<String> hdrs = new ArrayList<String>(headers.length);
            for (String header : headers) {
                hdrs.add(header);
            }
            this.headers = hdrs;
        }

        /**
         * Adds given row.
         *
         * @param values The values of the row
         * @return This builder
         */
        public Builder addRow(Object... values) {
            if (values == null || values.length <= 0) {
                throw new IllegalArgumentException("values must not be null or empty");
            }
            if (values.length != headers.size()) {
                throw new IllegalArgumentException("Number of value is invalid");
            }

            List<String> row = new ArrayList<String>(values.length);
            for (Object value : values) {
                row.add(value == null ? "null" : value.toString());
            }
            content.add(row);
            return this;
        }

        /**
         * Adds given row.
         *
         * @param values The values of the row
         * @return This builder
         */
        public Builder addRow(List<Object> values) {
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("values must not be null or empty");
            }
            if (values.size() != headers.size()) {
                throw new IllegalArgumentException("Number of value is invalid");
            }

            List<String> row = new ArrayList<String>(values.size());
            for (Object value : values) {
                row.add(value == null ? "null" : value.toString());
            }
            content.add(row);
            return this;
        }

        /**
         * Builds the instance of <code>ConsoleTable</code> from this builder's arguments.
         *
         * @return The resulting instance of <code>ConsoleTable</code>
         */
        public ConsoleTable build() {
            return new ConsoleTable(headers, content);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final int TABLEPADDING = 1;
    private static final char SEPERATOR_CHAR = '-';

    private final List<String> headers;
    private final List<? extends List<String>> table;
    private final List<Integer> maxLength;

    /**
     * Initializes a new {@link ConsoleTable}.
     *
     * @param headers The headers
     * @param content The rows
     */
    ConsoleTable(List<String> headers, List<? extends List<String>> content) {
        super();
        this.headers = ImmutableList.copyOf(headers);
        this.table = ImmutableList.copyOf(content);

        //Set headers length to maxLength at first
        List<Integer> maxLength = new ArrayList<Integer>(headers.size());
        for (int i = 0; i < headers.size(); i++) {
            maxLength.add(Integer.valueOf(headers.get(i).length()));
        }

        // Calculate max. length
        for (List<String> temp : content) {
            for (int j = 0; j < temp.size(); j++) {
                //If the table content was longer then current maxLength - update it
                if (temp.get(j).length() > maxLength.get(j).intValue()) {
                    maxLength.set(j, Integer.valueOf(temp.get(j).length()));
                }
            }
        }
        this.maxLength = ImmutableList.copyOf(maxLength);
    }

    /**
     * Builds the string containing the table.
     */
    public String buildTable() {
        // Create padding string containing just containing spaces
        String padder;
        {
            StringBuilder sbPadder = new StringBuilder();
            for (int i = 0; i < TABLEPADDING; i++) {
                sbPadder.append(' ');
            }
            padder = sbPadder.toString();
            sbPadder = null;
        }

        // Create the rowSeperator
        String rowSeperator;
        {
            StringBuilder sbRowSep = new StringBuilder();
            for (int i = 0; i < maxLength.size(); i++) {
                sbRowSep.append('|');
                for (int j = 0; j < maxLength.get(i).intValue() + (TABLEPADDING << 1); j++) {
                    sbRowSep.append(SEPERATOR_CHAR);
                }
            }
            sbRowSep.append('|');
            rowSeperator = sbRowSep.toString();
            sbRowSep = null;
        }

        // Append headers
        StringBuilder sb = new StringBuilder();
        sb.append(rowSeperator);
        sb.append(Strings.getLineSeparator());
        sb.append('|');
        for (int i = 0; i < headers.size(); i++) {
            sb.append(padder);
            sb.append(headers.get(i));
            // Fill up with empty spaces
            for (int k = 0; k < (maxLength.get(i).intValue() - headers.get(i).length()); k++) {
                sb.append(' ');
            }
            sb.append(padder);
            sb.append('|');
        }
        sb.append(Strings.getLineSeparator());
        sb.append(rowSeperator);
        sb.append(Strings.getLineSeparator());

        // Append rows
        for (int i = 0; i < table.size(); i++) {
            List<String> tempRow = table.get(i);
            // New row
            sb.append('|');
            for (int j = 0; j < tempRow.size(); j++) {
                sb.append(padder);
                sb.append(tempRow.get(j));
                // Fill up with empty spaces
                for (int k = 0; k < (maxLength.get(j).intValue() - tempRow.get(j).length()); k++) {
                    sb.append(" ");
                }
                sb.append(padder);
                sb.append("|");
            }
            sb.append(Strings.getLineSeparator());
        }
        sb.append(rowSeperator);
        sb.append(Strings.getLineSeparator());
        return sb.toString();
    }

}
