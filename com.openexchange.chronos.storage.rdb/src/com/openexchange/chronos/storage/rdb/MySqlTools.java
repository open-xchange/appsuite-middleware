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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.chronos.storage.rdb.fields.Field;
import com.openexchange.exception.OXException;

/**
 * {@link MySqlTools}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class MySqlTools {

    public static String toACE(String string) throws OXException {
        try {
            return IDNA.toACE(string);
        } catch (AddressException e) {
            throw EventExceptionCode.ENCODING.create(e);
        }
    }

    public static String toIDN(String string) {
        return IDNA.toIDN(string);
    }

    public static void setInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }

    public static void setString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value);
        }
    }

    public static void setDateTime(PreparedStatement stmt, int index, Date value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.TIMESTAMP);
        } else {
            stmt.setTimestamp(index, new Timestamp(value.getTime()));
        }
    }

    public static void setTimeZone(PreparedStatement stmt, int index, Calendar value) throws SQLException {
        if (value == null || value.getTimeZone() == null) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value.getTimeZone().getID());
            ;
        }
    }

    public static void setBool(PreparedStatement stmt, int index, Boolean value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value ? 1 : 0);
        }
    }

    public static <T> String getColumnList(Collection<Field<T>> fields) {
        if (fields.size() < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder("(");
        for (Field<T> field : fields) {
            sb.append("`" + field.getSqlName() + "`, ");
        }
        return sb.substring(0, sb.length() - 2) + ")";
    }

    /**
     * Creates a String with a list of placeholders for {@link PreparedStatement}s.
     * 
     * @param count The amount of necessary placeholders
     * @return The list of placeholders with brackets
     */
    public static String getParameterList(int count) {
        if (count < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < count; i++) {
            sb.append("?, ");
        }
        return sb.substring(0, sb.length() - 2) + ")";
    }

    /**
     * Creates a String containing the columns to be updated along with the necessary {@link PreparedStatement} placeholders.
     * Indices must be provided to specify the exact range of columns.
     * 
     * @param fields
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static <T> String getUpdateList(Map<Integer, Field<T>> fields, int startIndex, int endIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i <= endIndex; i++) {
            sb.append(fields.get(i).getSqlName()).append("=?, ");
        }
        return sb.substring(0, sb.length() - 2);
    }

}
