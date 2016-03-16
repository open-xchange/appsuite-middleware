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

package com.openexchange.publish.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Transaction {

    private Connection connection;

    private final int contextId;

    public Transaction(final int contextId) {
        this.contextId = contextId;
    }

    public List<Integer> executeStatement(final String sql, final Object... objects) throws OXException, SQLException {
        PreparedStatement statement = null;
        ResultSet keys = null;
        final List<Integer> retval = new ArrayList<Integer>();
        try {
            if (connection == null) {
                connection = Database.get(contextId, true);
                connection.setAutoCommit(false);
            }

            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < objects.length; i++) {
                statement.setObject(i + 1, objects[i]);
            }

            statement.execute();

            keys = statement.getGeneratedKeys();
            while (keys.next()) {
                retval.add(keys.getInt(1));
            }
        } finally {
            closeSQLStuff(null, statement, keys);
        }


        return retval;
    }

    public List<Map<String, Object>> executeQuery(final String sql, final Object... objects) throws OXException, SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        final List<Map<String, Object>> retval = new ArrayList<Map<String, Object>>();

        try {
            if (connection == null) {
                con = Database.get(contextId, false);
            } else {
                con = connection;
            }

            statement = con.prepareStatement(sql);
            for (int i = 0; i < objects.length; i++) {
                statement.setObject(i + 1, objects[i]);
            }

            rs = statement.executeQuery();
            final ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                final Map<String, Object> row = new HashMap<String, Object>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    final String column = rsmd.getColumnName(i);
                    row.put(column, rs.getObject(column));
                }
                retval.add(row);
            }
        } finally {
            closeSQLStuff(null, statement, rs);

            if (connection == null) {
                closeSQLStuff(con, null, null);
                Database.back(contextId, false, con);
            }
        }


        return retval;
    }

    public static List<Integer> commitStatement(final int contextId, final String sql, final Object... objects) throws OXException, SQLException {
        final Transaction transaction = new Transaction(contextId);
        final List<Integer> retval = transaction.executeStatement(sql, objects);
        transaction.commit();
        return retval;
    }

    public static List<Map<String, Object>> commitQuery(final int contextId, final String sql, final Object... objects) throws OXException, SQLException {
        final Transaction transaction = new Transaction(contextId);
        return transaction.executeQuery(sql, objects);
    }

    public void commit() throws SQLException {
        if (connection == null) {
            return;
        }
        connection.commit();
        connection.setAutoCommit(true);
        Database.back(contextId, true, connection);
    }

    public void rollback() throws SQLException {
        if (connection == null) {
            return;
        }
        connection.rollback();
        connection.setAutoCommit(true);
        Database.back(contextId, true, connection);
    }

    private void closeSQLStuff(final Connection con, final Statement stmt, final ResultSet rs) throws SQLException {
        if (rs != null) {
            try {
                rs.close();
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } finally {
                        if (con != null) {
                            con.close();
                        }
                    }
                }
            }
        }
    }


    // Utilities
    /**
     * Tries to turn everything into an int
     */
    public static int INT(final Object result) {
        if(Long.class.isInstance(result)) {
            return (int) (long) (Long)result;
        } else if (Integer.class.isInstance(result)) {
            return (Integer) result;
        } else if (String.class.isInstance(result)) {
            return Integer.valueOf((String) result);
        } else {
            return Integer.valueOf(result.toString());
        }
    }
}
