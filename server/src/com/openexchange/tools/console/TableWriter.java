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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final PrintStream ps;
    private final ColumnFormat[] formats;
    private final List<List<Object>> data;

    /**
     * Initializes a new {@link TableWriter}.
     */
    public TableWriter(PrintStream ps, ColumnFormat[] formats, List<List<Object>> data) {
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
        public ColumnFormat(Align align) {
            this(align, AUTO_WIDTH, Conversion.STRING);
        }
        public ColumnFormat(Align align, Conversion conversion) {
            this(align, AUTO_WIDTH, conversion);
        }
        public ColumnFormat(Align align, int width, Conversion conversion) {
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
        public void setWidth(int width) {
            this.width = width;
        }
        public Conversion getConversion() {
            return conversion;
        }
    }

    private void determineAutoWidth() {
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].getWidth() == ColumnFormat.AUTO_WIDTH) {
                for (List<Object> row : data) {
                    formats[i].setWidth(Math.max(formats[i].getWidth(), toString(row.get(i)).length()));
                }
            }
        }
    }

    private String generateFormatString() {
        StringBuilder retval = new StringBuilder();
        for (ColumnFormat format : formats) {
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
                retval.append('t');
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

    private String toString(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Date) {
            return DATE_FORMAT.format((Date) obj);
        }
        return obj.toString();
    }

    public void write() {
        determineAutoWidth();
        String format = generateFormatString();
        for (List<Object> row : data) {
            Object[] args = new Object[row.size()];
            for (int i = 0; i < row.size(); i++) {
                args[i] = toString(row.get(i));
            }
            ps.format(format, args);
        }
    }
}
