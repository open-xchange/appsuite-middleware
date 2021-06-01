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

import java.util.List;

/**
 * {@link OutputHelper} - Output helper class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class OutputHelper {

    /**
     * Initializes a new {@link OutputHelper}.
     */
    private OutputHelper() {
        super();
    }

    /**
     * Formats the output for given data.
     *
     * @param sizeAndAlignments The optional size and alignment for each column; e.g. <code>new String[] { "r", "l", "l" }</code>
     * @param names The column names; e.g. <code>new String[] { "ID", "Name", "Display-Name" }</code>
     * @param dataRows The actual data to fill the rows
     * @throws IllegalArgumentException If specified parameters are invalid
     */
    public static void doOutput(final String[] sizeAndAlignments, String[] names, List<List<String>> dataRows) {
        if (sizeAndAlignments.length != names.length) {
            throw new IllegalArgumentException("The sizes of columnsizes and columnnames aren't equal");
        }
        int[] columnsizes = new int[sizeAndAlignments.length];
        char[] alignments = new char[sizeAndAlignments.length];
        StringBuilder formatsb = new StringBuilder();
        char dividechar = ' ';
        for (int i = 0; i < sizeAndAlignments.length; i++) {
            // fill up part
            try {
                columnsizes[i] = Integer.parseInt(sizeAndAlignments[i].substring(0, sizeAndAlignments[i].length() - 1));
            } catch (NumberFormatException x) {
                // there's no number, so use longest line as alignment value
                columnsizes[i] = longestLine(dataRows,names,i);
            }
            alignments[i] = sizeAndAlignments[i].charAt(sizeAndAlignments[i].length() - 1);

            // check part
            if (names[i].length() > columnsizes[i]) {
                throw new IllegalArgumentException("Columnsize for column " + names[i] + " is too small for columnname");
            }

            // formatting part
            formatsb.append("%");
            if (alignments[i] == 'l') {
                formatsb.append('-');
            }
            formatsb.append(columnsizes[i]);
            formatsb.append('s');
            formatsb.append(dividechar);
        }
        formatsb.deleteCharAt(formatsb.length() - 1);
        formatsb.append('\n');
        System.out.format(formatsb.toString(), (Object[]) names);
        for (List<String> row : dataRows) {
            if (row.size() != sizeAndAlignments.length) {
                throw new IllegalArgumentException("The size of one of the rows isn't correct");
            }
            final Object[] outputrow = new Object[sizeAndAlignments.length];
            for (int i = 0; i < sizeAndAlignments.length; i++) {
                final String value = row.get(i);
                outputrow[i] = stripString(value, columnsizes[i], "~");
            }
            System.out.format(formatsb.toString(), outputrow);
        }
    }

    private static String stripString(String text, int length, String lastmark) {
        if (null != text && text.length() > length) {
            int stringlength = length - lastmark.length();
            return new StringBuffer(text.substring(0, stringlength)).append(lastmark).toString();
        } else if (text == null) {
            return "";
        } else {
            return text;
        }
    }

    private static int longestLine(List<List<String>> data, String[] columnnames, int column) {
        //long start = System.currentTimeMillis();
        int max = columnnames[column].length();
        for (int row = 0; row < data.size(); row++) {
            List<String> arrayList = data.get(row);
            if (columnnames.length != arrayList.size()) {
                throw new IllegalArgumentException("The sizes of columnnames and the columns in line " + row + " of the data aren't equal");
            }
            String value = arrayList.get(column);
            if (value != null) {
                int curLength = arrayList.get(column).length();
                if (curLength > max) {
                    max = curLength;
                }
            }
        }
        //System.out.println("calc took " + (System.currentTimeMillis()-start) + "ms");
        return max;
    }

}
