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

package com.openexchange.tools.oxfolder.property;

import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;

/**
 * {@link FolderPropertyStorage} - Storage to get user-specific properties per folder
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public interface FolderPropertyStorage {

    /**
     * Deletes all given user-specific properties for a given folder.
     * 
     * @param folderId The ID of the folder
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param propertyKeys The properties to delete. If the {@link Set} is <code>null</code> or empty all properties will be deleted
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void deleteFolderProperties(int folderId, int contextId, int userId, Set<String> propertyKeys) throws OXException;

    /**
     * Deletes a single property for a user from a folder.
     * 
     * @param folderId The ID of the folder to delete
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void deleteFolderProperty(int folderId, int contextId, int userId, String key) throws OXException;

    /**
     * Check if a folder has user-specific properties and therefore exits
     * 
     * @param folderId The ID of the folder to check existence for
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @return <code>true</code> if user-specific folder properties exists,
     *         <code>false</code> otherwise
     * @throws OXException In case of missing service or no connection could be obtained
     */
    boolean exists(int folderId, int contextId, int userId) throws OXException;

    /**
     * Get a {@link Map} with user-specific properties for the folder
     * 
     * @param treeeId The ID of the tree the folder is in
     * @param folderId The ID of the folder
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @return The {@link Folder} or <code>null</code>
     * @throws OXException In case of missing service or no connection could be obtained
     */
    Map<String, String> getFolderProperties(int treeId, int folderId, int contextId, int userId) throws OXException;

    /**
     * Get an user-specific property from a folder
     * 
     * @param folderId The ID of the folder
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @return The value of the property or <code>null</code>
     * @throws OXException In case of missing service or no connection could be obtained
     */
    String getFolderProperty(int folderId, int contextId, int userId, String key) throws OXException;

    /**
     * Insert user-specific values for a folder into the database.
     * 
     * @param folderId The ID of the folder to insert
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to save for the folder. Must be modifiable
     * @return <code>true</code> if the insertion was successful, <code>false</code> otherwise
     * @throws OXException In case of missing service or no connection could be obtained
     */
    boolean insertFolderProperties(int folderId, int contextId, int userId, Map<String, String> properties) throws OXException;

    /**
     * Insert user-specific values for a folder into the database.
     * 
     * @param folderId The ID of the folder to insert
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @param value The value to of the property
     * @return <code>true</code> if the insertion was successful, <code>false</code> otherwise
     * @throws OXException In case of missing service or no connection could be obtained
     */
    boolean insertFolderProperty(int folderId, int contextId, int userId, String key, String value) throws OXException;

    /**
     * Updates a specific property on the folder
     * 
     * @param folderId The ID of the folder to insert
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to save for the folder. Must be modifiable
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void updateFolderProperties(int folderId, int contextId, int userId, Map<String, String> properties) throws OXException;

    /**
     * Updates a specific property on the folder
     * 
     * @param folderId The ID of the folder to insert
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param key The name of the property
     * @param value The value to update the property to
     * @throws OXException In case of missing service or no connection could be obtained
     */
    void updateFolderProperty(int folderId, int contextId, int userId, String key, String value) throws OXException;

}
