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

package com.openexchange.sql.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.sql.grammar.Expression;
import com.openexchange.sql.grammar.LIST;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SQLTools {

    public static void closeSQLStuff(Connection con, Statement stmt, ResultSet rs) throws SQLException {
        SQLException sqle = null;
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException x) {
                sqle = x;
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException x) {
                if (sqle == null) {
                    sqle = x;
                }
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException x) {
                if (sqle == null) {
                    sqle = x;
                }
            }
        }
        if (sqle != null) {
            throw sqle;
        }
    }

    public static LIST createLIST(int length, Expression expression) {
        List<Expression> list = new ArrayList<Expression>();
        for (int i = 0; i < length; i++) {
            list.add(expression);
        }
        return new LIST(list);
    }

    /**
     * Creates a java.sql.Timestamp object with truncated milliseconds to avoid bad rounding in MySQL versions >= 7.6.4.
     * See: http://dev.mysql.com/doc/refman/5.6/en/fractional-seconds.html
     * 
     * @param date
     * @return
     */
    public static Timestamp toTimestamp(Date date) {
        return new Timestamp((date.getTime() / 1000) * 1000);
    }

    /**
     * Creates a java.sql.Timestamp object with truncated milliseconds to avoid bad rounding in MySQL versions >= 7.6.4.
     * See: http://dev.mysql.com/doc/refman/5.6/en/fractional-seconds.html
     * 
     * @param date
     * @return
     */
    public static Timestamp toTimestamp(long date) {
        return new Timestamp((date / 1000) * 1000);
    }
}
