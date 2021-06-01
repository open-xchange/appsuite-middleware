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
