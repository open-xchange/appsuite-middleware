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

package com.openexchange.tools.update;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.tx.SimpleDBProvider;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;

/**
 * This class contains some tools to ease update of database.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Tools {

    /**
     * Prevent instantiation
     */
    private Tools() {
        super();
    }

    public static final boolean isNullable(final Connection con, final String table, final String column) throws SQLException {
        final DatabaseMetaData meta = con.getMetaData();
        ResultSet result = null;
        boolean retval = false;
        try {
            result = meta.getColumns(null, null, table, column);
            if (result.next()) {
                retval = DatabaseMetaData.typeNullable == result.getInt(NULLABLE);
            } else {
                throw new SQLException("Can't get information for column " + column + " in table " + table + '.');
            }
        } finally {
            closeSQLStuff(result);
        }
        return retval;
    }

    /**
     * @param con readable database connection.
     * @param table table name that indexes should be tested.
     * @param columns column names that the index must cover.
     * @return the name of an index that matches the given columns or <code>null</code> if no matching index is found.
     * @throws SQLException if some SQL problem occurs.
     * @throws NullPointerException if one of the columns is <code>null</code>.
     */
    public static final String existsIndex(final Connection con, final String table, final String[] columns) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        final Map<String, ArrayList<String>> indexes = new HashMap<String, ArrayList<String>>();
        ResultSet result = null;
        try {
            result = metaData.getIndexInfo(null, null, table, false, false);
            while (result.next()) {
                final String indexName = result.getString(6);
                final int columnPos = result.getInt(8);
                final String columnName = result.getString(9);
                ArrayList<String> foundColumns = indexes.get(indexName);
                if (null == foundColumns) {
                    foundColumns = new ArrayList<String>();
                    indexes.put(indexName, foundColumns);
                }
                while (foundColumns.size() < columnPos) {
                    foundColumns.add(null);
                }
                foundColumns.set(columnPos - 1, columnName);
            }
        } finally {
            closeSQLStuff(result);
        }
        String foundIndex = null;
        final Iterator<Entry<String, ArrayList<String>>> iter = indexes.entrySet().iterator();
        while (null == foundIndex && iter.hasNext()) {
            final Entry<String, ArrayList<String>> entry = iter.next();
            final ArrayList<String> foundColumns = entry.getValue();
            if (columns.length != foundColumns.size()) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; matches && i < columns.length; i++) {
                matches = columns[i].equals(foundColumns.get(i));
            }
            if (matches) {
                foundIndex = entry.getKey();
            }
        }
        return foundIndex;
    }

    /**
     * This method drops an index with the given name. Beware, this method is vulnerable to SQL injection because table and index name can
     * not be set through a {@link PreparedStatement}.
     * 
     * @param con writable database connection.
     * @param table table name that index should be dropped.
     * @param index name of the index to drop.
     * @throws SQLException if some SQL problem occurs.
     */
    public static final void dropIndex(final Connection con, final String table, final String index) throws SQLException {
        final String sql = "ALTER TABLE `" + table + "` DROP INDEX `" + index + "`";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * This method creates a new index on a table. Beware, this method is vulnerable to SQL injection because table and column names can not
     * be set through a {@link PreparedStatement}.
     * 
     * @param con writable database connection.
     * @param table name of the table that should get a new index.
     * @param columns names of the columns the index should cover.
     * @throws SQLException if some SQL problem occurs.
     */
    public static final void createIndex(final Connection con, final String table, final String[] columns) throws SQLException {
        final StringBuilder sql = new StringBuilder("ALTER TABLE `");
        sql.append(table);
        sql.append("` ADD INDEX (`");
        for (final String column : columns) {
            sql.append(column);
            sql.append("`,`");
        }
        sql.setLength(sql.length() - 2);
        sql.append(')');
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    public static final boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { TABLE });
            retval = (rs.next() && rs.getString("TABLE_NAME").equals(table));
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }
    
    public static boolean columnExists(Connection con, String table, String column) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getColumns(null, null, table, column);
            while (rs.next()) {
                retval = rs.getString(4).equals(column);
            }
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

    private static final int NULLABLE = 11;

    private static final String TABLE = "TABLE";

    public static void removeFile(final int cid, final String fileStoreLocation, final Connection con) throws FileStorageException, FilestoreException, ContextException {
        final Context ctx = ContextStorage.getInstance().loadContext(cid);
        final URI fileStorageURI = FilestoreStorage.createURI(ctx);
        final File file = new File(fileStorageURI);
        if (file.exists()) {
            final FileStorage fs = FileStorage.getInstance(fileStorageURI, ctx, new SimpleDBProvider(con, con));
            fs.deleteFile(fileStoreLocation);
        }
    }

    public static boolean hasSequenceEntry(String sequenceTable, Connection con, int ctxId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM " + sequenceTable + " WHERE cid = "+ctxId);
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(stmt != null) {
                stmt.close();
            }
        }
    }

    public static List<Integer> getContextIDs(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> contextIds = new LinkedList<Integer>();
        try {
            stmt = con.prepareStatement("SELECT DISTINCT cid FROM user");
            rs = stmt.executeQuery();
            while(rs.next()) {
                contextIds.add(rs.getInt(1));
            }
            return contextIds;
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(stmt != null) {
                stmt.close();
            }
        }
    }

    public static void exec(Connection con, String sql, Object...args) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = con.prepareStatement(sql);
            int i = 1;
            for(Object arg : args) {
                statement.setObject(i++, arg);
            }
            statement.execute();
        } finally {
            if(statement != null) {
                statement.close();
            }
        }
        
    }
}
