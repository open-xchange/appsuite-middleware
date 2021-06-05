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

package com.openexchange.tools.console;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link TableWriter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TableWriter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    private final PrintStream ps;
    private final ColumnFormat[] formats;
    private final List<List<Object>> data;

    /**
     * Initializes a new {@link TableWriter}.
     */
    public TableWriter(final PrintStream ps, final ColumnFormat[] formats, final List<List<Object>> data) {
        super();
        this.ps = ps;
        this.formats = formats;
        this.data = data;
    }

    public static class ColumnFormat {
        public enum Align {
            LEFT,
            RIGHT
        }
        public enum Conversion {
            STRING,
            DATE
        }
        public static final int AUTO_WIDTH = -1;
        private final Align align;
        private int width;
        private final Conversion conversion;
        public ColumnFormat(final Align align) {
            this(align, AUTO_WIDTH, Conversion.STRING);
        }
        public ColumnFormat(final Align align, final Conversion conversion) {
            this(align, AUTO_WIDTH, conversion);
        }
        public ColumnFormat(final Align align, final int width, final Conversion conversion) {
            super();
            this.align = align;
            this.width = width;
            this.conversion = conversion;
        }
        public Align getAlign() {
            return align;
        }
        public int getWidth() {
            return width;
        }
        public void setWidth(final int width) {
            this.width = width;
        }
        public Conversion getConversion() {
            return conversion;
        }
    }

    private void determineAutoWidth() {
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].getWidth() == ColumnFormat.AUTO_WIDTH) {
                for (final List<Object> row : data) {
                    formats[i].setWidth(Math.max(formats[i].getWidth(), toString(row.get(i)).length()));
                }
            }
        }
    }

    private String generateFormatString() {
        final StringBuilder retval = new StringBuilder();
        for (final ColumnFormat format : formats) {
            retval.append('%');
            switch (format.getAlign()) {
            case LEFT:
                retval.append('-');
                break;
            case RIGHT:
            default:
            }
            retval.append(format.getWidth());
            switch (format.getConversion()) {
            case DATE:
                retval.append("tc");
                break;
            case STRING:
            default:
                retval.append('s');
            }
            retval.append(' ');
        }
        retval.setCharAt(retval.length() - 1, '\n');
        return retval.toString();
    }

    private String toString(final Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Date) {
            synchronized (DATE_FORMAT) {
                return DATE_FORMAT.format((Date) obj);
            }
        }
        return obj.toString();
    }

    public void write() {
        determineAutoWidth();
        final String format = generateFormatString();
        for (final List<Object> row : data) {
            final Object[] args = new Object[row.size()];
            for (int i = 0; i < row.size(); i++) {
                args[i] = toString(row.get(i));
            }
            ps.format(format, args);
        }
    }
}
