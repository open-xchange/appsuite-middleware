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

package com.openexchange.tools.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.tools.Collections;
import com.openexchange.tools.Collections.Filter;

/**
 * This class models database indexes (in mysql).An index operates on a table, has a name and a list of columns it indexes. You can use the
 * find-methods to load indexes from the database. Use the instance methods to drop or create an index.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.org">Francisco Laguna</a>
 */
public class Index {

    /**
     * Returns a list of all indexes defined on a certain table.
     *
     * @param con - The Connection to the Database.
     * @param tableName - The name of the table on which the indexes have to be defined.
     * @return A list of all indexes on this table.
     * @throws SQLException
     */

    public static List<Index> findAllIndexes(final Connection con, final String tableName) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SHOW INDEX FROM `" + tableName + '`');
            rs = stmt.executeQuery();

            final List<Index> retval = new ArrayList<Index>();
            final Map<String, Index> indexes = new HashMap<String, Index>();

            while (rs.next()) {
                Index id;
                final String name = rs.getString("Key_name");
                if (indexes.containsKey(name)) {
                    id = indexes.get(name);
                } else {
                    id = new Index();
                    id.setTable(tableName);
                    id.setName(name);
                    indexes.put(name, id);
                    retval.add(id);
                }

                id._setColumnAt(rs.getInt("Seq_in_index") - 1, rs.getString("Column_name"));

            }
            return retval;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }

    /**
     * Tries to find an index with a certain name on a table. If no index with the given name can be found, this query will throw an
     * IndexNotFoundException
     */
    public static Index findByName(final Connection con, final String tableName, final String indexName) throws IndexNotFoundException, SQLException {
        final List<Index> allIndexes = findAllIndexes(con, tableName);
        final Index id = Collections.findFirst(allIndexes, new NameFilter(indexName));
        if (id == null) {
            throw new IndexNotFoundException("Couldn't find index with name " + indexName);
        }
        return id;
    }

    /**
     * Tries to find all indexes on a table that contain a certain column, no matter on which posistion, and no matter if other indexes are
     * present. Consider a table with indexes on cid, id and cid, field01 and cid and last_modified. When you use this method to search for
     * indexes for the field cid, then the first three indexes (all except the one on last_modified) will be returned by this method. If you
     * need a more restrictive search (give me all indexes matching exactly some columns) use #findWithColumns instead.
     */
    public static List<Index> findContainingColumns(final Connection con, final String tableName, final String... columns) throws SQLException {
        final List<Index> allIndexes = findAllIndexes(con, tableName);
        final List<Index> retval = new ArrayList<Index>();
        Collections.collect(allIndexes, new ContainsColumnFilter(columns), retval);
        return retval;
    }

    /**
     * Tries to find all indexes on a table that match a certain column signature, Consider a table with indexes on cid, id and cid, field01
     * and cid and last_modified. When you use this method to search for indexes for the fields cid, id, then the first index (containing
     * exactly cid, id in that order) will be returned by this method. If you need a less restrictive search (give me all indexes containing
     * some columns) use #findContainingColumns instead.
     */
    public static List<Index> findWithColumns(final Connection con, final String tableName, final String... columns) throws SQLException {
        final List<Index> allIndexes = findAllIndexes(con, tableName);
        final List<Index> retval = new ArrayList<Index>();
        Collections.collect(allIndexes, new ExactColumnFilter(columns), retval);
        return retval;
    }

    private String name;

    private List<String> columns;

    private String table;

    public Index() {
        columns = new ArrayList<String>();
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(final String... columns) {
        setColumns(Arrays.asList(columns));
    }

    public void setColumns(final List<String> columns) {
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Remove this index from the table
     *
     * @param con
     * @throws SQLException
     */
    public void drop(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DROP INDEX `" + name + "` ON `" + table + '`');
            stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Creates an index on the table. Make sure table, name and columns all contain values.
     *
     * @param con
     * @throws SQLException
     */
    public void create(final Connection con) throws SQLException {
        if (name == null || table == null || columns.size() == 0) {
            throw new IllegalStateException("You must set name and table to create an index. Also the index must span at least one column");
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("CREATE INDEX `" + name + "` ON `" + table + "` (" + _join_cols() + ')');
            stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public String getTable() {
        return table;
    }

    public void setTable(final String table) {
        this.table = table;
    }

    public void _setColumnAt(final int index, final String column) {
        _ensureSize(index + 1);
        columns.set(index, column);
    }

    public void _ensureSize(final int size) {
        while (size > columns.size()) {
            columns.add(null);
        }
    }

    public String _join_cols() {
        final StringBuilder b = new StringBuilder();
        for (final String col : columns) {
            b.append(col).append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder(50).append("Index ");
        b.append(name);
        b.append(" (").append(table).append(") with Columns: ").append(_join_cols()).append(')');

        return b.toString();
    }

    public static class NameFilter implements Filter<Index> {

        private final String name;

        public NameFilter(final String name) {
            this.name = name;
        }

        @Override
        public boolean accept(final Index object) {
            return object.getName().equalsIgnoreCase(name);
        }

    }

    public static class ContainsColumnFilter implements Filter<Index> {

        private final String[] columns;

        public ContainsColumnFilter(final String... columns) {
            this.columns = columns;
        }

        @Override
        public boolean accept(final Index object) {
            final Set<String> needed = new HashSet<String>(Arrays.asList(columns));
            for (final String col : object.getColumns()) {
                for (final String need : new HashSet<String>(needed)) {
                    if (need.equalsIgnoreCase(col)) {
                        needed.remove(need);
                    }
                }
                if (needed.isEmpty()) {
                    return true;
                }
            }
            return needed.isEmpty();
        }

    }

    public static class ExactColumnFilter implements Filter<Index> {

        private final String[] columns;

        public ExactColumnFilter(final String... columns) {
            this.columns = columns;
        }

        @Override
        public boolean accept(final Index object) {
            if (columns.length != object.getColumns().size()) {
                return false;
            }
            final List<String> colObject = object.getColumns();
            for (int i = 0; i < columns.length; i++) {
                if (!columns[i].equalsIgnoreCase(colObject.get(i))) {
                    return false;
                }
            }
            return true;
        }

    }

}
