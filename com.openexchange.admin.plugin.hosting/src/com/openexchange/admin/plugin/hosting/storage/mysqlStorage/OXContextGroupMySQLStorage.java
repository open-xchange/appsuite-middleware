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

package com.openexchange.admin.plugin.hosting.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.plugin.hosting.services.AdminServiceRegistry;
import com.openexchange.admin.plugin.hosting.storage.interfaces.OXContextGroupStorageInterface;
import com.openexchange.admin.plugin.hosting.tools.database.TableColumnObject;
import com.openexchange.admin.plugin.hosting.tools.database.TableObject;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupEvent;
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupRegistry;

/**
 * {@link OXContextGroupMySQLStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OXContextGroupMySQLStorage implements OXContextGroupStorageInterface {

    private static final Logger LOG = LoggerFactory.getLogger(OXContextGroupMySQLStorage.class);

    /**
     * Initialises a new {@link OXContextGroupMySQLStorage}.
     */
    public OXContextGroupMySQLStorage() {
        super();
    }

    @Override
    public void deleteContextGroup(String contextGroupId) throws StorageException, OXException {
        DatabaseService globalDBService = AdminServiceRegistry.getInstance().getService(DatabaseService.class);

        if (!globalDBService.isGlobalDatabaseAvailable(contextGroupId)) {
            throw new StorageException("The global database is not available for the context group with identifier '" + contextGroupId + "'");
        }

        Connection connection;
        try {
            connection = globalDBService.getWritableForGlobal(contextGroupId);
        } catch (OXException e) {
            LOG.error("{}", e.getMessage(), e);
            throw StorageException.wrapForRMI(e);
        }

        try {
            List<TableObject> tableObjects;
            {
                List<TableObject> tables = getTables(connection);
                findReferences(tables, connection);
                //retainRelevantTables(tables);
                tableObjects = sortTables(tables);
            }
            purgeData(contextGroupId, tableObjects, connection);
        } catch (SQLException e) {
            LOG.error("{}", e.getMessage(), e);
            throw new StorageException(e);
        } finally {
            globalDBService.backWritableForGlobal(contextGroupId, connection);
        }

        // TODO: Any caches to invalidate? Any post processing stuff?
    }

    /**
     * Get a list of all tables in the globaldb.
     *
     * @param connection The connection to the globaldb
     * @return A list with tables
     * @throws SQLException If an error occurs
     */
    private List<TableObject> getTables(Connection connection) throws SQLException {
        List<TableObject> tableObjects = new ArrayList<TableObject>();

        LOG.debug("Fetching table metadata");
        ResultSet rs = null;
        ResultSet rsColumns = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            rs = metaData.getTables(null, null, null, null);
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                TableObject tableObject = new TableObject();
                tableObject.setName(tableName);

                LOG.debug("Fetching metadata for {}", tableName);
                rsColumns = metaData.getColumns(connection.getCatalog(), null, tableName, null);

                while (rsColumns.next()) {
                    String columnName = rsColumns.getString("COLUMN_NAME");
                    TableColumnObject tableColumnObject = new TableColumnObject();
                    tableColumnObject.setName(columnName);
                    tableColumnObject.setType(rsColumns.getInt("DATA_TYPE"));
                    tableColumnObject.setColumnSize(rsColumns.getInt("COLUMN_SIZE"));
                    tableObject.addColumn(tableColumnObject);

                    if (columnName.equals("gid")) {
                        tableObjects.add(tableObject);
                    }
                }
                LOG.debug("Table {} has following columns: {}", tableObject.getColumns());
                rsColumns.close();
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            Databases.closeSQLStuff(rs);
            Databases.closeSQLStuff(rsColumns);
        }

        return tableObjects;
    }

    /**
     * Find any foreign key references among the specified tables and update the list.
     *
     * @param tableObjects The list with the table objects
     * @param connection The connection to the globaldb.
     * @throws SQLException
     */
    private void findReferences(List<TableObject> tableObjects, Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        for (TableObject tableObject : tableObjects) {
            String tableName = tableObject.getName();
            ResultSet rsImportedKeys = metaData.getImportedKeys(catalog, null, tableName);
            try {
                while (rsImportedKeys.next()) {
                    String primaryKeyTableName = rsImportedKeys.getString("PKTABLE_NAME");
                    tableObject.addCrossReferenceTable(primaryKeyTableName);
                    int position = Collections.binarySearch(tableObjects, primaryKeyTableName);
                    if (position >= 0) {
                        tableObjects.get(position).addReferencedBy(tableName);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (rsImportedKeys != null) {
                    rsImportedKeys.close();
                }
            }
        }
    }

//    /**
//     * Retains all tables that have a 'gid' column or have cross references to other global db tables.
//     *
//     * @param tableObjects
//     */
//    private void retainRelevantTables(List<TableObject> tableObjects) {
//        List<TableObject> tmp = new ArrayList<TableObject>(tableObjects);
//        boolean hasGidColumn = false;
//        for (TableObject tableObject : tmp) {
//            for (TableColumnObject column : tableObject.getColumns()) {
//                if ("gid".equals(column.getName())) {
//                    hasGidColumn = true;
//                    break;
//                }
//            }
//            if (!hasGidColumn && !tableObject.hasCrossReferences()) {
//                tableObjects.remove(tableObject);
//            }
//            hasGidColumn = false;
//        }
//    }

    /**
     * Sort the specified tables according to their foreign key references.
     * The <a href="http://en.wikipedia.org/wiki/Topological_sorting">topological sorting</a> algorithm is used.
     *
     * @param tableObjects The list with the table objects to sort
     * @return A sorted list with table objects
     */
    private List<TableObject> sortTables(List<TableObject> tableObjects) {
        List<TableObject> sorted = new ArrayList<TableObject>(tableObjects.size());

        int index = 0;
        while (!tableObjects.isEmpty()) {
            TableObject tableObject = tableObjects.get(index++);
            if (!tableObject.hasCrossReferences()) {
                sorted.add(tableObject);
                tableObjects.remove(tableObject);
                for (TableObject to : tableObjects) {
                    to.removeCrossReferenceTable(tableObject.getName());
                }
                index--;
            }
        }

        return sorted;
    }

    /**
     * Perform the actual purge operation on the specified tables.
     *
     * @param contextGroupId The context group identifier
     * @param tables The tables to purge.
     * @param connection The writable connection to the globaldb.
     * @throws StorageException
     */
    private void purgeData(String contextGroupId, List<TableObject> tables, Connection connection) throws StorageException {
        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(false);

            try {
                DeleteContextGroupEvent deleteEvent = new DeleteContextGroupEvent(this, contextGroupId);
                DeleteContextGroupRegistry.getInstance().fireDeleteContextGroupEvent(deleteEvent, connection, connection);
            } catch (OXException e) {
                LOG.error("Some implementation deleting context group specific data failed. Continuing with hard delete from tables using the 'gid' column.", e);
            }

            // Purge data
            for (TableObject tableObject : tables) {
                statement = connection.prepareStatement("DELETE FROM " + tableObject.getName() + " WHERE gid = ?");
                statement.setString(1, contextGroupId);
                statement.executeUpdate();
                statement.close();
            }

            connection.commit();
        } catch (SQLException e) {
            Databases.rollback(connection);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(statement);
            Databases.autocommit(connection);
        }
    }
}
