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

package com.openexchange.tools;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * {@link CSVWriter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CSVWriter {

    private static final char ROW_DELIMITER = '\n';

    private static final char CELL_DELIMITER = ',';

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final List<List<Object>> data;

    private final PrintStream ps;

    /**
     * Initializes a new {@link CSVWriter}.
     */
    public CSVWriter(final PrintStream ps, final List<List<Object>> data) {
        super();
        this.ps = ps;
        this.data = data;
    }

    public void write() {
        final StringBuilder sb = new StringBuilder();
        for (final List<Object> row : data) {
            for (final Object entry : row) {
                sb.append(quote(toString(entry)));
                sb.append(CELL_DELIMITER);
            }
            sb.setCharAt(sb.length() - 1, ROW_DELIMITER);
            ps.print(sb.toString());
            sb.setLength(0);
        }
    }

    private static String toString(final Object obj) {
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

    private String quote(final String s) {
        final StringBuilder tmp = new StringBuilder();
        tmp.append('"');
        tmp.append(s.replaceAll("\"", "\"\""));
        tmp.append('"');
        return tmp.toString();
    }
}
