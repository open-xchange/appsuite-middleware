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

    private final static String DELETE      = "DELETE FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=?";
    private final static String DELETE_PROP = "DELETE FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=? AND name=?";
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
    public void deleteFolderProperties(int contextId, int folderId, int userId, Set<String> propertyKeys) throws OXException {
        Connection connection = null;
        DatabaseService dbService = null;
        boolean autocommit = false;
        boolean rollback = false;
        boolean modified = false;

        // Acquire write connection
        dbService = getDatabaseServiceService();
        connection = dbService.getWritable(contextId);

        try {
            Databases.startTransaction(connection);
            autocommit = true;
            rollback = true;

            deleteFolderProperties(contextId, folderId, userId, propertyKeys, connection);
            modified = true;

            connection.commit();
            rollback = false;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(connection);
            }
            if (autocommit) {
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
    public void deleteFolderProperties(int contextId, int folderId, int userId, Set<String> propertyKeys, Connection connection) throws OXException {
        if (null == connection) {
            // Get connection an re-call this function
            deleteFolderProperties(contextId, folderId, userId, propertyKeys);
            return;
        }

        PreparedStatement stmt = null;
        try {
            if (null == propertyKeys || propertyKeys.isEmpty()) {
                // Prepare statement
                stmt = connection.prepareStatement(DELETE);
                stmt.setInt(1, contextId);
                stmt.setInt(2, folderId);
                stmt.setInt(3, userId);

                // Execute
                stmt.executeUpdate();
            } else {
                // Prepare statement for every property
                stmt = connection.prepareStatement(DELETE_PROP);
                stmt.setInt(1, contextId);
                stmt.setInt(2, folderId);
                stmt.setInt(3, userId);
                for (String key : propertyKeys) {
                    stmt.setString(4, key);
                    stmt.addBatch();
                }
                // Execute batch
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void deleteFolderProperty(int contextId, int folderId, int userId, String key) throws OXException {
        deleteFolderProperty(contextId, folderId, userId, key, null);
    }

    @Override
    public void deleteFolderProperty(int contextId, int folderId, int userId, String key, Connection connection) throws OXException {
        if (null != key) {
            deleteFolderProperties(folderId, contextId, userId, Collections.singleton(key), connection);
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

}
