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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.property.FolderPropertyStorage;
import static com.openexchange.tools.oxfolder.property.sql.CreateFolderPropertyTable.TABLE_NAME;

/**
 * {@link FolderPropertyStorageImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */

public class FolderPropertyStorageImpl implements FolderPropertyStorage {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderPropertyStorageImpl.class);

    private final static String GET         = "SELECT name, value FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=?;";
    private final static String GET_PROP    = "SELECT value FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=? AND name=? LIMIT 1;";
    private final static String EXIST       = "SELECT EXISTS(SELECT 1 FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=? LIMIT 1);";
    private final static String DELETE      = "DELETE FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=?;";
    private final static String DELETE_PROP = "DELETE FROM " + TABLE_NAME + " WHERE cid=? AND fuid=? AND userid=? AND name=?;";
    private final static String INSERT      = "INSERT INTO " + TABLE_NAME + " (cid,fuid,userid,name,value) VALUES (?,?,?,?,?);";
    private final static String UPDATE      = "UPDATE " + TABLE_NAME + " SET value=? WHERE cid=? AND fuid=? AND userid=? AND name=?;";

    private final ServiceLookup services;

    /**
     * Initializes a new {@link FolderPropertyStorageImpl}.
     * 
     */
    public FolderPropertyStorageImpl(ServiceLookup service) {
        super();
        this.services = service;
    }

    @Override
    public void deleteFolderProperties(int folderId, int contextId, int userId) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = getService(DatabaseService.class);
            connection = dbService.getWritable(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(DELETE);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);

            // Execute
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.debug("Couldn't delete folder properties", e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backWritable(connection);
            }
        }
    }

    @Override
    public void deleteFolderProperty(int folderId, int contextId, int userId, String key) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = getService(DatabaseService.class);
            connection = dbService.getWritable(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(DELETE_PROP);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            stmt.setString(4, key);

            // Execute
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.debug("Couldn't delete folder property", e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backWritable(connection);
            }
        }

    }

    @Override
    public boolean exists(int folderId, int contextId, int userId) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = getService(DatabaseService.class);
            connection = dbService.getReadOnly(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(EXIST);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);

            // Execute & check result
            ResultSet result = stmt.executeQuery();
            if (result.next() && result.getInt(1) == 1) {
                return true;
            }
        } catch (Exception e) {
            LOG.debug("Couldn't check if the folder exists", e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backReadOnly(connection);
            }

        }
        return false;
    }

    @Override
    public Map<String, String> getFolderProperties(int treeId, int folderId, int contextId, int userId) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = getService(DatabaseService.class);
            connection = dbService.getReadOnly(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(GET);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);

            // Execute & convert result
            ResultSet result = stmt.executeQuery();
            Map<String, String> properties = new HashMap<>();
            while (result.next()) {
                properties.put(result.getString(1), result.getString(2));
            }
            return properties;
        } catch (Exception e) {
            LOG.debug("Couldn't get folder", e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backReadOnly(connection);
            }
        }
        return null;
    }

    @Override
    public String getFolderProperty(int folderId, int contextId, int userId, String key) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = getService(DatabaseService.class);
            connection = dbService.getReadOnly(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(GET_PROP);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            stmt.setString(4, key);

            // Execute & convert result
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString(1);
            }
        } catch (Exception e) {
            LOG.debug("Couldn't get folder property", e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backReadOnly(connection);
            }
        }
        return null;
    }

    @Override
    public boolean insertFolderProperties(int folderId, int contextId, int userId, Map<String, String> properties) {
        if (null == properties || properties.isEmpty()) {
            LOG.debug("Properties missing!");
            return false;
        }
        Connection connection = null;
        DatabaseService dbService = null;
        try {
            // Acquire connection
            dbService = getService(DatabaseService.class);
            connection = dbService.getReadOnly(contextId);
            connection.setAutoCommit(false);
            PreparedStatement stmt = null;
            for (String propertyName : properties.keySet()) {
                // New entry 
                stmt = connection.prepareStatement(INSERT);
                stmt.setInt(1, contextId);
                stmt.setInt(2, folderId);
                stmt.setInt(3, userId);
                stmt.setString(4, propertyName);
                stmt.setString(5, properties.get(propertyName));

                // Execute
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
            }
            connection.setAutoCommit(true);
        } catch (Exception e) {
            LOG.debug("Couldn't insert folder properties", e);
            return false;
        } finally {
            if (null != dbService) {
                dbService.backReadOnly(connection);
            }
        }
        return true;
    }

    @Override
    public boolean insertFolderProperty(int folderId, int contextId, int userId, String key, String value) {
        if (null == key || null == value) {
            LOG.debug("Flawed key-value pair!");
            return false;
        }
        Map<String, String> map = new HashMap<>(1);
        map.put(key, value);
        return insertFolderProperties(folderId, contextId, userId, map);
    }

    @Override
    public void updateFolderProperties(int folderId, int contextId, int userId, Map<String, String> properties) {
        if (null == properties || properties.isEmpty()) {
            LOG.debug("Properties missing!");
            return;
        }
        Connection connection = null;
        DatabaseService dbService = null;
        try {
            // Acquire connection
            dbService = getService(DatabaseService.class);
            connection = dbService.getWritable(contextId);
            connection.setAutoCommit(false);
            PreparedStatement stmt = null;
            for (String propertyName : properties.keySet()) {

                // Update entry   
                stmt = connection.prepareStatement(UPDATE);
                stmt.setString(1, properties.get(propertyName));
                stmt.setInt(2, contextId);
                stmt.setInt(3, folderId);
                stmt.setInt(4, userId);
                stmt.setString(5, propertyName);

                // Execute
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
            }
            connection.setAutoCommit(true);
        } catch (Exception e) {
            LOG.debug("Couldn't delete userized folder", e);
        } finally {
            if (null != dbService) {
                dbService.backWritable(connection);
            }
        }

    }

    @Override
    public void updateFolderProperty(int folderId, int contextId, int userId, String key, String value) {
        if (null == key || null == value) {
            LOG.debug("Flawed key-value pair!");
            return;
        }
        Map<String, String> map = new HashMap<>(1);
        map.put(key, value);
        updateFolderProperties(folderId, contextId, userId, map);
    }

    private <T> T getService(Class<T> clazz) throws OXException {
        T service = this.services.getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }
        return service;
    }
}
