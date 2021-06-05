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
