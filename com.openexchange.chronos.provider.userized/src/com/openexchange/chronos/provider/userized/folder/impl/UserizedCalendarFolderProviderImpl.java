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

package com.openexchange.chronos.provider.userized.folder.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.userized.folder.UserizedCalendarFolder;
import com.openexchange.chronos.provider.userized.folder.UserizedCalendarFolderProvider;
import com.openexchange.chronos.provider.userized.folder.UserizedFolderField;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link UserizedCalendarFolderProviderImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class UserizedCalendarFolderProviderImpl implements UserizedCalendarFolderProvider {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserizedCalendarFolderProviderImpl.class);

    private final static String GET         = "SELECT name, value FROM oxfolder_userized WHERE cid=? AND fuid=? AND userid=?;";
    private final static String EXIST       = "SELECT EXISTS(SELECT 1 FROM oxfolder_userized WHERE cid=? AND fuid=? AND userid=? LIMIT 1);";
    private final static String EXIST_PROP  = "SELECT EXISTS(SELECT 1 FROM oxfolder_userized WHERE cid=? AND fuid=? AND userid=? AND name=? LIMIT 1);";
    private final static String DELETE      = "DELETE FROM oxfolder_userized WHERE cid=? AND fuid=? AND userid=?;";
    private final static String DELETE_PROP = "DELETE FROM oxfolder_userized WHERE cid=? AND fuid=? AND userid=? AND name=?;";
    private final static String INSERT      = "INSERT INTO oxfolder_userized (cid,fuid,userid,name,value) VALUES (?,?,?,?,?);";
    private final static String UPDATE      = "UPDATE oxfolder_userized SET value=? WHERE cid=? AND fuid=? AND userid=? AND name=?;";

    private final ServiceLookup service;

    /**
     * Initializes a new {@link UserizedCalendarFolderProviderImpl}.
     * 
     */
    public UserizedCalendarFolderProviderImpl(ServiceLookup service) {
        super();
        this.service = service;

    }

    @Override
    public boolean exists(int folderId, int contextId, int userId) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = acquireDatabaseService();
            connection = dbService.getReadOnly(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(EXIST);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);

            // Execute & check result
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
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
    public UserizedCalendarFolder getFolder(CalendarFolder folder, int contextId, int userId) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = acquireDatabaseService();
            connection = dbService.getReadOnly(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(GET);
            stmt.setInt(1, contextId);
            stmt.setInt(2, Integer.valueOf(folder.getId()));
            stmt.setInt(3, userId);

            // Execute & convert result
            ResultSet result = stmt.executeQuery();
            Map<String, String> properties = new HashMap<>();
            while (result.next()) {
                properties.put(result.getString(1), result.getString(2));
            }
            return fill(folder, contextId, userId, properties);
        } catch (Exception e) {
            LOG.debug("Couldn't get userized folder", e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backReadOnly(connection);
            }
        }
        return new DefaultUserizedCalendarFolder(folder, contextId, userId);
    }

    @Override
    public void deleteFolder(int folderId, int contextId, int userId) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = acquireDatabaseService();
            connection = dbService.getWritable(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(DELETE);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);

            // Execute
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.debug("Couldn't delete userized folder", e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backWritable(connection);
            }
        }
    }

    @Override
    public void deleteFolder(UserizedCalendarFolder folder) {
        deleteFolder(Integer.valueOf(folder.getId()), folder.getContextId(), folder.getUserId());
    }

    @Override
    public void deleteFolderProperty(int folderId, int contextId, int userId, String name) {
        Connection connection = null;
        DatabaseService dbService = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            dbService = acquireDatabaseService();
            connection = dbService.getWritable(contextId);

            // Prepare statement
            stmt = connection.prepareStatement(DELETE_PROP);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            stmt.setString(4, name);

            // Execute
            stmt.executeUpdate();
        } catch (Exception e) {
            LOG.debug("Couldn't delete userized folder", e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (null != dbService) {
                dbService.backWritable(connection);
            }
        }

    }

    @Override
    public void insertFolder(int folderId, int contextId, int userId, Map<String, String> properties) {
        if (null == properties || properties.isEmpty()) {
            LOG.debug("Properties missing. Going to skipp insertion of userized folder.");
            return;
        }
        Connection connection = null;
        DatabaseService dbService = null;
        try {
            // Acquire connection
            dbService = acquireDatabaseService();
            connection = dbService.getReadOnly(contextId);
            connection.setAutoCommit(false);
            PreparedStatement stmt = null;
            for (String propertyName : properties.keySet()) {

                if (exists(folderId, contextId, userId, propertyName, connection)) {
                    // Update entry   
                    stmt = connection.prepareStatement(UPDATE);
                    stmt.setString(1, properties.get(propertyName));
                    stmt.setInt(2, contextId);
                    stmt.setInt(3, folderId);
                    stmt.setInt(4, userId);
                    stmt.setString(5, propertyName);
                } else {
                    // New entry 
                    stmt = connection.prepareStatement(INSERT);
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, folderId);
                    stmt.setInt(3, userId);
                    stmt.setString(4, propertyName);
                    stmt.setString(5, properties.get(propertyName));
                }
                // Execute
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
            }
            connection.setAutoCommit(true);
        } catch (Exception e) {
            LOG.debug("Couldn't delete userized folder", e);
        } finally {
            if (null != dbService) {
                dbService.backReadOnly(connection);
            }
        }
    }

    @Override
    public void insertFolder(UserizedCalendarFolder folder) {
        insertFolder(Integer.valueOf(folder.getId()), folder.getContextId(), folder.getUserId(), folder.additionalProperties());
    }

    /**
     * Get the {@link DatabaseService}
     * 
     * @return The {@link DatabaseService}
     * @throws OXException If the {@link DatabaseService} is not available
     */
    private DatabaseService acquireDatabaseService() throws OXException {
        DatabaseService dbService = service.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        return dbService;
    }

    /**
     * Fills a {@link UserizedCalendarFolder} with its properties
     * 
     * @param cFolder The original {@link CalendarFolder}
     * @param userId The ID of the user the folder belongs to
     * @param properties The properties of the folder
     * @return A {@link UserizedCalendarFolder}
     */
    private UserizedCalendarFolder fill(CalendarFolder cFolder, int contextId, int userId, Map<String, String> properties) {
        DefaultUserizedCalendarFolder folder = new DefaultUserizedCalendarFolder(cFolder, contextId, userId);
        // Parser for every entry
        for (String propertyName : properties.keySet()) {
            parseFolderFields(folder, propertyName, properties.get(propertyName));
        }
        return folder;
    }

    /**
     * Parses for known fields. Any additional fields will be available throughÏ to {@link UserizedCalendarFolder#additionalProperties()}
     * 
     * @param folder {@link DefaultUserizedCalendarFolder} to add the values to
     * @param name The name to compare to known fields
     * @param value The value of the name
     */
    private void parseFolderFields(DefaultUserizedCalendarFolder folder, String name, String value) {
        if (UserizedFolderField.ALTERNATIVE_DESCRIPTION.equalsField(name)) {
            folder.setAlternativeDescription(value);
        } else if (UserizedFolderField.ALTERNATIVE_NAME.equalsField(name)) {
            folder.setAlternativeName(value);
        } else if (UserizedFolderField.SUBSCRIBED.equalsField(name)) {
            folder.setSubscribed(Boolean.valueOf(value));
        } else if (UserizedFolderField.SYNC.equalsField(name)) {
            folder.setSync(Boolean.valueOf(value));
        } else {
            folder.setAdditionalProperty(name, value);
        }
    }

    /**
     * Check if a property of a folder exists
     * 
     * @param folderId The ID of the folder to check existence for
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param connection The {@link Connection} used for executing
     * @return <code>true</code> if the properties exists,
     *         <code>false</code> otherwise
     * @throws Exception in case of SQL error
     */
    private boolean exists(int folderId, int contextId, int userId, String name, Connection connection) throws Exception {
        // Prepare statement
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(EXIST_PROP);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            stmt.setString(4, name);

            // Execute & check result
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                // If 1 is returned there is an element
                return result.getInt(1) == 1;
            }
            return false;
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }
}
