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

package com.openexchange.database;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link IncorrectStringSQLException} - The special SQL exception signaling an attempt to pass an incorrect string to database.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IncorrectStringSQLException extends SQLException {

    private static final long serialVersionUID = 2713082500383087281L;

    private static final Pattern PATTERN_ERROR_MESSAGE = Pattern.compile(Pattern.quote("Incorrect string value:") + " *" + "'([^']+)'" + " for column " + "'([^']+)'" + " at row " + "([0-9]+)");

    /** The (vendor) error code <code>1366</code> that signals an attempt to pass an incorrect string to database */
    public static final int ERROR_CODE = 1366;

    /**
     * Attempts to yield an appropriate {@code UnsupportedCharacterSQLException} instance for specified SQL exception.
     *
     * @param e The SQL exception
     * @return The appropriate {@code UnsupportedCharacterSQLException} instance or <code>null</code>
     */
    public static IncorrectStringSQLException instanceFor(SQLException e) {
        if (null == e) {
            return null;
        }
        if (ERROR_CODE != e.getErrorCode()) {
            return null;
        }

        // E.g. "Incorrect string value: '\xF0\x9F\x92\xA9' for column 'field01' at row 1"
        Matcher m = PATTERN_ERROR_MESSAGE.matcher(e.getMessage());
        if (!m.matches()) {
            return null;
        }

        // Parse incorrect string value
        String incorrect;
        {
            ByteArrayOutputStream buf = Streams.newByteArrayOutputStream(4);
            String ic = m.group(1);
            for (int st = 0; ic.indexOf("\\x", st) >= 0;) {
                int end = st + 4;
                buf.write(Integer.parseInt(ic.substring(st + 2, end), 16));
                st = end;
            }
            incorrect = new String(buf.toByteArray(), Charsets.UTF_8);
        }

        return new IncorrectStringSQLException(incorrect, m.group(2), Integer.parseInt(m.group(3)), e);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private final String incorrectString;
    private final String column;
    private final int row;

    /**
     * Initializes a new {@link IncorrectStringSQLException}.
     *
     * @param incorrectString The incorrect string
     * @param column The column name
     * @param row The row number
     * @param sqlState The SQL state
     * @param vendorCode The vendor code (always <code>1366</code>)
     * @param cause The associated SQL exception
     */
    public IncorrectStringSQLException(String incorrectString, String column, int row, SQLException cause) {
        super(cause.getMessage(), cause.getSQLState(), cause.getErrorCode(), cause);
        this.incorrectString = incorrectString;
        this.column = column;
        this.row = row;
    }

    /**
     * Gets the incorrect string
     *
     * @return The incorrect string
     */
    public String getIncorrectString() {
        return incorrectString;
    }

    /**
     * Gets the column
     *
     * @return The column
     */
    public String getColumn() {
        return column;
    }

    /**
     * Gets the row
     *
     * @return The row
     */
    public int getRow() {
        return row;
    }

}
