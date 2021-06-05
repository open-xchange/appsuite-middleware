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

package com.openexchange.tools.oxfolder.property.impl;

import static com.openexchange.tools.oxfolder.property.sql.CreateFolderUserPropertyTable.TABLE_NAME;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;

/**
 * {@link RdbFolderUserPropertyStorage}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class RdbFolderUserPropertyStorage implements FolderUserPropertyStorage {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RdbFolderUserPropertyStorage.class);

    private final static String DELETE_USER_PROPS      = "DELETE FROM " + TABLE_NAME + " WHERE cid=? AND userid=?";
    private final static String DELETE_CONTEXT_PROPS      = "DELETE FROM " + TABLE_NAME + " WHERE cid=?";
    private final static String DELETE_FOLDER_PROPS      = "DELETE FROM " + TABLE_NAME + " WHERE cid=? AND fuid=?";
    private final static String EXIST       = "SELECT EXISTS(SELECT 1 FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=? LIMIT 1)";
    private final static String GET         = "SELECT name, value FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=?";
    private final static String GET_PROP    = "SELECT value FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=? AND name=? LIMIT 1";
    private final static String INSERT      = "INSERT INTO " + TABLE_NAME + " (cid,fuid,userid,name,value) VALUES (?,?,?,?,?)";
    private final static String SET         = "INSERT INTO " + TABLE_NAME + " (cid,fuid,userid,name,value) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE value=?";
    private final static String UPDATE      = "UPDATE " + TABLE_NAME + " SET value=? WHERE cid=? AND fuid=? AND userid=? AND name=?";

    private final ServiceLookup services;

    /**
     * Initializes a new {@link RdbFolderUserPropertyStorage}.
     */
    public RdbFolderUserPropertyStorage(ServiceLookup service) {
        super();
        this.services = service;
    }

    @Override
    public void deleteFolderProperties(int contextId, int folderId, int[] userIds, Set<String> propertyKeys) throws OXException {
        Connection connection = null;
        DatabaseService dbService = null;
        int rollback = 0;
        boolean modified = false;

        // Acquire write connection
        dbService = getDatabaseServiceService();
        connection = dbService.getWritable(contextId);

        try {
            Databases.startTransaction(connection);
            rollback = 1;

            deleteFolderProperties(contextId, folderId, userIds, propertyKeys, connection);
            modified = true;

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            if (modified) {
                dbService.backWritable(contextId, connection);
            } else {
                dbService.backWritableAfterReading(contextId, connection);
            }
        }
    }

    @Override
    public void deleteFolderProperties(int contextId, int folderId, int[] userIds, Set<String> propertyKeys, Connection connection) throws OXException {
        if (null == connection) {
            // Get connection an re-call this function
            deleteFolderProperties(contextId, folderId, userIds, propertyKeys);
            return;
        }

        String sql;
        {
            StringBuilder stringBuilder = new StringBuilder().append("DELETE FROM ").append(TABLE_NAME).append(" WHERE cid=? AND fuid=?");
            if (null != userIds && 0 < userIds.length) {
                stringBuilder.append(" AND userid").append(Databases.getPlaceholders(userIds.length));
            }
            if (null != propertyKeys && 0 < propertyKeys.size()) {
                stringBuilder.append(" AND name").append(Databases.getPlaceholders(propertyKeys.size()));
            }
            sql = stringBuilder.toString();
        }

        PreparedStatement stmt = null;
        try {
            int parameterIndex = 1;
            stmt = connection.prepareStatement(sql);
            stmt.setInt(parameterIndex++, contextId);
            stmt.setInt(parameterIndex++, folderId);
            if (null != userIds && 0 < userIds.length) {
                for (int userId : userIds) {
                    stmt.setInt(parameterIndex++, userId);
                }
            }
            if (null != propertyKeys && 0 < propertyKeys.size()) {
                for (String key : propertyKeys) {
                    stmt.setString(parameterIndex++, key);
                }
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean exists(int contextId, int folderId, int userId) throws OXException {
        Connection connection = null;
        DatabaseService dbService = null;

        // Acquire read connection
        dbService = getDatabaseServiceService();
        connection = dbService.getReadOnly(contextId);
        try {
            return exists(contextId, folderId, userId, connection);
        } finally {
            dbService.backReadOnly(contextId, connection);
        }
    }

    @Override
    public boolean exists(int contextId, int folderId, int userId, Connection connection) throws OXException {
        if (null == connection) {
            // Get connection an re-call this function
            return exists(contextId, folderId, userId);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Prepare statement
            stmt = connection.prepareStatement(EXIST);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);

            // Execute & check result
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 1) {
                return true;
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
        return false;
    }

    @Override
    public Map<String, String> getFolderProperties(int contextId, int folderId, int userId) throws OXException {
        // Acquire connection
        DatabaseService dbService = getDatabaseServiceService();
        Connection connection = dbService.getReadOnly(contextId);
        try {
            return getFolderProperties(contextId, folderId, userId, connection);
        } finally {
            dbService.backReadOnly(contextId, connection);
        }
    }

    @Override
    public Map<String, String> getFolderProperties(int contextId, int folderId, int userId, Connection connection) throws OXException {
        if (null == connection) {
            // Get connection an re-call this function
            return getFolderProperties(contextId, folderId, userId);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Prepare statement
            stmt = connection.prepareStatement(GET);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);

            // Execute & convert result
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyMap();
            }

            Map<String, String> properties = new HashMap<>();
            do {
                properties.put(rs.getString(1), rs.getString(2));
            } while (rs.next());
            return properties;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Map<Integer, Map<String, String>> getFolderProperties(int contextId, int[] folderIds, int userId) throws OXException {
        if (null == folderIds || folderIds.length < 1) {
            LOGGER.debug("Can't iterate over an empty array");
            return Collections.emptyMap();
        }

        DatabaseService dbService = getDatabaseServiceService();
        Connection connection = dbService.getReadOnly(contextId);
        try {
            return getFolderProperties(contextId, folderIds, userId, connection);
        } finally {
            dbService.backReadOnly(contextId, connection);
        }
    }

    @Override
    public Map<Integer, Map<String, String>> getFolderProperties(int contextId, int[] folderIds, int userId, Connection connection) throws OXException {
        if (null == folderIds || folderIds.length < 1) {
            IllegalArgumentException e = new IllegalArgumentException("Can't iterate over an empty array");
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, String.valueOf(contextId));
        }
        if (null == connection) {
            // Get connection an re-call this function
            return getFolderProperties(contextId, folderIds, userId);
        }

        Map<Integer, Map<String, String>> properties = new HashMap<>(folderIds.length);

        // Prepare statement
        for (int folderId : folderIds) {
            properties.put(Integer.valueOf(folderId), getFolderProperties(contextId, folderId, userId, connection));
        }
        return properties;
    }

    @Override
    public String getFolderProperty(int contextId, int folderId, int userId, String key) throws OXException {
        if (null == key) {
            return null;
        }

        DatabaseService dbService = getDatabaseServiceService();
        Connection connection = dbService.getReadOnly(contextId);
        try {
            return getFolderProperty(contextId, folderId, userId, key, connection);
        } finally {
            dbService.backReadOnly(contextId, connection);
        }
    }

    @Override
    public String getFolderProperty(int contextId, int folderId, int userId, String key, Connection connection) throws OXException {
        if (null == key) {
            return null;
        }

        if (null == connection) {
            // Get connection an re-call this function
            return getFolderProperty(contextId, folderId, userId, key);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Prepare statement
            stmt = connection.prepareStatement(GET_PROP);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            stmt.setString(4, key);

            // Execute & convert result
            rs = stmt.executeQuery();
            if (rs.next()) {
                String retval = rs.getString(1);
                return retval;
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
        return null;
    }

    @Override
    public void insertFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException {
        if (null == properties || properties.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("User properties for folder missing!");
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, String.valueOf(contextId));
        }

        int rollback = 0;
        boolean modified = false;

        // Acquire connection
        DatabaseService dbService = getDatabaseServiceService();
        Connection connection = dbService.getWritable(contextId);
        try {
            Databases.startTransaction(connection);
            rollback = 1;

            insertFolderProperties(contextId, folderId, userId, properties, connection);
            modified = true;

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            if (modified) {
                dbService.backWritable(contextId, connection);
            } else {
                dbService.backWritableAfterReading(contextId, connection);
            }
        }
    }

    @Override
    public void insertFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException {
        if (null == properties || properties.isEmpty()) {
            // Nothing to insert
            return;
        }

        if (null == connection) {
            // Get connection and re-call this function
            insertFolderProperties(contextId, folderId, userId, properties);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(INSERT);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            for (Map.Entry<String, String> propertyName : properties.entrySet()) {
                // New entry
                stmt.setString(4, propertyName.getKey());
                stmt.setString(5, propertyName.getValue());
                stmt.addBatch();
            }

            // Execute & close
            stmt.executeBatch();
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void insertFolderProperty(int contextId, int folderId, int userId, String key, String value) throws OXException {
        insertFolderProperty(contextId, folderId, userId, key, value, null);
    }

    @Override
    public void insertFolderProperty(int contextId, int folderId, int userId, String key, String value, Connection connection) throws OXException {
        if (null == key || null == value) {
            return;
        }

        insertFolderProperties(contextId, folderId, userId, Collections.singletonMap(key, value), connection);
    }

    @Override
    public void setFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException {
        if (null == properties || properties.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("User properties for folder missing!");
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, String.valueOf(contextId));
        }

        int rollback = 0;
        boolean modified = false;

        // Acquire connection
        DatabaseService dbService = getDatabaseServiceService();
        Connection connection = dbService.getWritable(contextId);
        try {
            Databases.startTransaction(connection);
            rollback = 1;

            setFolderProperties(contextId, folderId, userId, properties, connection);
            modified = true;

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            if (modified) {
                dbService.backWritable(contextId, connection);
            } else {
                dbService.backWritableAfterReading(contextId, connection);
            }
        }
    }

    @Override
    public void setFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException {
        if (null == properties || properties.isEmpty()) {
            return;
        }

        if (null == connection) {
            // Get connection an re-call this function
            setFolderProperties(contextId, folderId, userId, properties);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SET);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                // Update entry
                stmt.setString(4, entry.getKey());
                stmt.setString(5, entry.getValue());
                stmt.setString(6, entry.getValue());
                stmt.addBatch();
            }
            // Execute
            stmt.executeBatch();
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void updateFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException {
        if (null == properties || properties.isEmpty()) {
            LOGGER.debug("Properties missing!");
            return;
        }

        int rollback = 0;
        boolean modified = false;

        // Acquire connection
        DatabaseService dbService = getDatabaseServiceService();
        Connection connection = dbService.getWritable(contextId);
        try {
            Databases.startTransaction(connection);
            rollback = 1;

            updateFolderProperties(contextId, folderId, userId, properties, connection);
            modified = true;

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            if (modified) {
                dbService.backWritable(contextId, connection);
            } else {
                dbService.backWritableAfterReading(contextId, connection);
            }
        }
    }

    @Override
    public void updateFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException {
        if (null == properties || properties.isEmpty()) {
            LOGGER.debug("Properties missing!");
            return;
        }
        if (null == connection) {
            // Get connection an re-call this function
            updateFolderProperties(contextId, folderId, userId, properties);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(UPDATE);
            stmt.setInt(2, contextId);
            stmt.setInt(3, folderId);
            stmt.setInt(4, userId);
            for (Map.Entry<String, String> propertyName : properties.entrySet()) {
                // Update entry
                stmt.setString(1, propertyName.getValue());
                stmt.setString(5, propertyName.getKey());

                // Execute
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void updateFolderProperty(int contextId, int folderId, int userId, String key, String value) throws OXException {
        updateFolderProperty(contextId, folderId, userId, key, value, null);
    }

    @Override
    public void updateFolderProperty(int contextId, int folderId, int userId, String key, String value, Connection connection) throws OXException {
        if (null == key || null == value) {
            LOGGER.debug("Flawed key-value pair!");
            return;
        }

        updateFolderProperties(folderId, contextId, userId, Collections.singletonMap(key, value), connection);
    }

    private DatabaseService getDatabaseServiceService() throws OXException {
        DatabaseService service = this.services.getService(DatabaseService.class);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        return service;
    }

    /**
     * Deletes all properties for the given folder
     *
     * @param contextId The context id
     * @param folderId The folder id
     * @throws OXException
     */
    public void deleteFolderProperties(int contextId, int folderId) throws OXException {
        Connection connection = null;
        DatabaseService dbService = null;
        boolean modified = false;

        // Acquire write connection
        dbService = getDatabaseServiceService();
        connection = dbService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            // Prepare statement
            stmt = connection.prepareStatement(DELETE_FOLDER_PROPS);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            // Execute
            modified = stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (modified) {
                dbService.backWritable(contextId, connection);
            } else {
                dbService.backWritableAfterReading(contextId, connection);
            }
        }

    }

    /**
     * Deletes all properties for the given context
     *
     * @param contextId The context id
     * @param writeCon The write connection
     * @throws OXException
     */
    public void deleteContextProperties(int contextId, Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            // Prepare statement
            stmt = writeCon.prepareStatement(DELETE_CONTEXT_PROPS);
            stmt.setInt(1, contextId);
            // Execute
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Deletes all properties for the given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @param writeCon The write {@link Connection} to use
     * @throws OXException
     */
    public void deleteUserProperties(int contextId, int userId, Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            // Prepare statement
            stmt = writeCon.prepareStatement(DELETE_USER_PROPS);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            // Execute
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
