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

package com.openexchange.database.internal.change.custom;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.database.Databases;
import com.openexchange.java.Strings;
import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * {@link AddIndexForFilestore2UserCustomTaskChange}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class AddIndexForFilestore2UserCustomTaskChange implements CustomTaskChange, CustomTaskRollback {

    /**
     * Initializes a new {@link AddIndexForFilestore2UserCustomTaskChange}.
     */
    public AddIndexForFilestore2UserCustomTaskChange() {
        super();
    }

    @Override
    public String getConfirmationMessage() {
        return "INDEX successfully created for filestore2user table";
    }

    @Override
    public void setUp() throws SetupException {
        // Nothing
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // Ignore
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
        // Ignore
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }

        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AddIndexForFilestore2UserCustomTaskChange.class);
        Connection configDbCon = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        int rollback = 0;
        try {
            if (existsIndex(configDbCon, "filestore2user", new String[] {"filestore_id"}) != null) {
                // INDEX already exists
                return;
            }

            createIndex(configDbCon, "filestore2user", "filestore_id_index", new String[] {"filestore_id"}, false);
        } catch (SQLException e) {
            logger.error("Failed to add INDEX to \"filestore2user\" table", e);
            throw new CustomChangeException("SQL error", e);
        } catch (RuntimeException e) {
            logger.error("Failed to add INDEX to \"filestore2user\" table", e);
            throw new CustomChangeException("Runtime error", e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(configDbCon);
                }
                Databases.autocommit(configDbCon);
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * This method creates a new index on a table. Beware, this method is vulnerable to SQL injection because table and column names can not
     * be set through a {@link PreparedStatement}.
     *
     * @param con writable database connection.
     * @param table name of the table that should get a new index.
     * @param name name of the index or <code>null</code> to let the database define the name.
     * @param columns names of the columns the index should cover.
     * @param unique if this should be a unique index.
     * @throws SQLException if some SQL problem occurs.
     */
    private static final void createIndex(final Connection con, final String table, final String name, final String[] columns, final boolean unique) throws SQLException {
        final StringBuilder sql = new StringBuilder("ALTER TABLE `");
        sql.append(table);
        sql.append("` ADD ");
        if (unique) {
            sql.append("UNIQUE ");
        }
        sql.append("INDEX ");
        if (null != name) {
            sql.append('`');
            sql.append(name);
            sql.append("` ");
        }
        sql.append("(");
        for (final String column : columns) {
            if (column.startsWith("`")) {
                sql.append(column);
                sql.append(",");
            } else {
                sql.append("`");
                sql.append(column);
                sql.append("`,");
            }
        }
        sql.setLength(sql.length() - 1);
        sql.append(')');
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private static final String existsIndex(final Connection con, final String table, final String[] columns) throws SQLException {
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
                matches = columns[i].equalsIgnoreCase(foundColumns.get(i));
            }
            if (matches) {
                foundIndex = entry.getKey();
            }
        }
        return foundIndex;
    }

    private static final void createPrimaryKey(final Connection con, final String table, final String[] columns) throws SQLException {
        final int[] lengths = new int[columns.length];
        Arrays.fill(lengths, -1);
        createPrimaryKey(con, table, columns, lengths);
    }

    private static final void createPrimaryKey(final Connection con, final String table, final String[] columns, final int[] lengths) throws SQLException {
        createKey(con, table, columns, lengths, true, null);
    }

    private static final void createKey(final Connection con, final String table, final String[] columns, final int[] lengths, boolean primary, String name) throws SQLException {
        final StringBuilder sql = new StringBuilder("ALTER TABLE `");
        sql.append(table);
        sql.append("` ADD ");
        if (primary) {
            sql.append("PRIMARY ");
        }
        sql.append("KEY ");
        if (!primary && Strings.isNotEmpty(name)) {
            sql.append('`').append(name).append('`');
        }
        sql.append(" (");
        {
            final String column = columns[0];
            sql.append('`').append(column).append('`');
            final int len = lengths[0];
            if (len > 0) {
                sql.append('(').append(len).append(')');
            }
        }
        for (int i = 1; i < columns.length; i++) {
            final String column = columns[i];
            sql.append(',');
            sql.append('`').append(column).append('`');
            final int len = lengths[i];
            if (len > 0) {
                sql.append('(').append(len).append(')');
            }
        }
        sql.append(')');
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private static List<int[]> partition(int[] original, int partitionSize) {
        checkNotNull(original, "Array must not be null");
        checkArgument(partitionSize > 0);
        int total = original.length;
        if (partitionSize >= total) {
            return Collections.singletonList(original);
        }

        // Create a list of sets to return.
        List<int[]> result = new ArrayList<>((total + partitionSize - 1) / partitionSize);

        // Create each new array.
        int stopIndex = 0;
        for (int startIndex = 0; startIndex + partitionSize <= total; startIndex += partitionSize) {
            stopIndex += partitionSize;
            result.add(java.util.Arrays.copyOfRange(original, startIndex, stopIndex));
        }
        if (stopIndex < total) {
            result.add(java.util.Arrays.copyOfRange(original, stopIndex, total));
        }
        return result;
    }

}
