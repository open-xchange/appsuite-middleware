/**
 * 
 */
package com.openexchange.admin.tools;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * {@link DatabaseTools}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin
 *         Schneider</a>
 * @since v7.8.0
 */

public class DatabaseTools {

    /**
     * Checks if specified column exists.
     *
     * @param con The connection
     * @param table The table name
     * @param column The column name
     * @return <code>true</code> if specified column exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    public static boolean columnExists(final Connection con, final String table, final String column) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getColumns(null, null, table, column);
            while (rs.next()) {
                retval = rs.getString(4).equalsIgnoreCase(column);
            }
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }
}
