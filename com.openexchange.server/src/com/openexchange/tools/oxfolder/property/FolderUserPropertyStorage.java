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

package com.openexchange.tools.oxfolder.property;

import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link FolderUserPropertyStorage} - Storage to get user-specific properties per folder
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@SingletonService
public interface FolderUserPropertyStorage {

    /**
     * Deletes all given user-specific properties for a given folder.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder
     * @param userId The ID of the user the user-specific folder belongs to
     * @param propertyKeys The properties to delete. If the {@link Set} is <code>null</code> or empty all properties will be deleted
     * @throws OXException In case of missing service or no connection could be obtained
     */
    default void deleteFolderProperties(int contextId, int folderId, int userId, Set<String> propertyKeys) throws OXException {
        deleteFolderProperties(contextId, folderId, new int[] { userId }, propertyKeys);
    }

    /**
     * Deletes all given user-specific properties for a given folder.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder
     * @param userId The ID of the user the user-specific folder belongs to
     * @param propertyKeys The properties to delete. If the {@link Set} is <code>null</code> or empty all properties will be deleted
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    default void deleteFolderProperties(int contextId, int folderId, int userId, Set<String> propertyKeys, Connection connection) throws OXException {
        deleteFolderProperties(contextId, folderId, new int[] { userId }, propertyKeys, connection);
    }

    /**
     * Deletes a single property for a user from a folder.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to delete
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @throws OXException In case of missing service or no connection could be obtained
     */
    default void deleteFolderProperty(int contextId, int folderId, int userId, String key) throws OXException {
        deleteFolderProperty(contextId, folderId, userId, key, null);
    }

    /**
     * Deletes a single property for a user from a folder.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to delete
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    default void deleteFolderProperty(int contextId, int folderId, int userId, String key, Connection connection) throws OXException {
        if (null != key) {
            deleteFolderProperties(contextId, folderId, userId, Collections.singleton(key), connection);
        }
    }

    /**
     * Deletes all given user-specific properties for multiple users for a given folder.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder
     * @param userIds The identifiers of the users to delete the properties for. If the array is <code>null</code>
     *            or empty all properties for the folder will be deleted
     * @param propertyKeys The properties to delete. If the {@link Set} is <code>null</code> or empty all properties will be deleted
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void deleteFolderProperties(int contextId, int folderId, int[] userIds, Set<String> propertyKeys) throws OXException;

    /**
     * Deletes all given user-specific properties for multiple users for a given folder.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder
     * @param userIds The identifiers of the users to delete the properties for. If the array is <code>null</code>
     *            or empty all properties for the folder will be deleted
     * @param propertyKeys The properties to delete. If the {@link Set} is <code>null</code> or empty all properties will be deleted
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void deleteFolderProperties(int contextId, int folderId, int[] userIds, Set<String> propertyKeys, Connection connection) throws OXException;

    /**
     * Check if a folder has user-specific properties and therefore exits
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to check existence for
     * @param userId The ID of the user the user-specific folder belongs to
     * @return <code>true</code> if user-specific folder properties exists,
     *         <code>false</code> otherwise
     * @throws OXException In case of missing service or no connection could be obtained
     */
    boolean exists(int contextId, int folderId, int userId) throws OXException;

    /**
     * Check if a folder has user-specific properties and therefore exits
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to check existence for
     * @param userId The ID of the user the user-specific folder belongs to
     * @param connection The {@link Connection} to to use for the transaction
     * @return <code>true</code> if user-specific folder properties exists,
     *         <code>false</code> otherwise
     * @throws OXException In case of missing service or no connection could be obtained
     */
    boolean exists(int contextId, int folderId, int userId, Connection connection) throws OXException;

    /**
     * Get a {@link Map} with user-specific properties for the folder
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder
     * @param userId The ID of the user the user-specific folder belongs to
     * @return The folder properties.
     * @throws OXException In case of missing service or no connection could be obtained
     */
    Map<String, String> getFolderProperties(int contextId, int folderId, int userId) throws OXException;

    /**
     * Get a {@link Map} with user-specific properties for multiple folders
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder
     * @param userId The ID of the user the user-specific folder belongs to
     * @return The folder properties.
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    Map<String, String> getFolderProperties(int contextId, int folderId, int userId, Connection connection) throws OXException;

    /**
     * Get a {@link Map} with user-specific properties for the folder
     *
     * @param contextId The context ID of the user
     * @param folderIds The IDs of the folder to load
     * @param userId The ID of the user the user-specific folder belongs to
     * @return The folder properties for each given folder.
     * @throws OXException In case of missing service or no connection could be obtained
     */
    Map<Integer, Map<String, String>> getFolderProperties(int contextId, int[] folderIds, int userId) throws OXException;

    /**
     * Get a {@link Map} with user-specific properties for multiple folders
     *
     * @param contextId The context ID of the user
     * @param folderIds The IDs of the folder to load
     * @param userId The ID of the user the user-specific folder belongs to
     * @return The folder properties for each given folder.
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    Map<Integer, Map<String, String>> getFolderProperties(int contextId, int[] folderIds, int userId, Connection connection) throws OXException;

    /**
     * Get an user-specific property from a folder
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @return The value of the property or <code>null</code>
     * @throws OXException In case of missing service or no connection could be obtained
     */
    String getFolderProperty(int contextId, int folderId, int userId, String key) throws OXException;

    /**
     * Get an user-specific property from a folder
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @return The value of the property or <code>null</code>
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    String getFolderProperty(int contextId, int folderId, int userId, String key, Connection connection) throws OXException;

    /**
     * Insert user-specific values for a folder into the database.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to save for the folder. Must be modifiable
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void insertFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException;

    /**
     * Insert user-specific values for a folder into the database.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to save for the folder. Must be modifiable
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void insertFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException;

    /**
     * Insert user-specific values for a folder into the database.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @param value The value to of the property
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void insertFolderProperty(int contextId, int folderId, int userId, String key, String value) throws OXException;

    /**
     * Insert user-specific values for a folder into the database.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @param value The value to of the property
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void insertFolderProperty(int contextId, int folderId, int userId, String key, String value, Connection connection) throws OXException;

    /**
     * Insert user-specific values for the given folder. If a property already exists the value will be updated.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to add to the folder
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void setFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException;

    /**
     * Insert user-specific values for the given folder. If a property already exists the value will be updated.
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to add to the folder
     * @param connection The {@link Connection} to use for the transaction, or <code>null</code> to acquire the connection on demand
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void setFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException;

    /**
     * Updates a specific property on the folder
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to save for the folder. Must be modifiable
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void updateFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException;

    /**
     * Updates a specific property on the folder
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to save for the folder. Must be modifiable
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void updateFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException;

    /**
     * Updates a specific property on the folder
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @param value The value to update the property to
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void updateFolderProperty(int contextId, int folderId, int userId, String key, String value) throws OXException;

    /**
     * Updates a specific property on the folder
     *
     * @param contextId The context ID of the user
     * @param folderId The ID of the folder to insert
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @param value The value to update the property to
     * @param connection The {@link Connection} to to use for the transaction
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void updateFolderProperty(int contextId, int folderId, int userId, String key, String value, Connection connection) throws OXException;

}
