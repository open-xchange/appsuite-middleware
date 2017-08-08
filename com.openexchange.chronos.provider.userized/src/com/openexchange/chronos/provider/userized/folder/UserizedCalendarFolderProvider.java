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

package com.openexchange.chronos.provider.userized.folder;

import java.util.Map;
import com.openexchange.chronos.provider.CalendarFolder;

/**
 * {@link UserizedCalendarFolderProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public interface UserizedCalendarFolderProvider {

    /**
     * Deletes a user-specific folder. Does not delete the folder itself! Only deletes the user-specific properties for the folder.
     * 
     * @param folderId The ID of the folder to delete
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     */
    void deleteFolder(int folderId, int contextId, int userId);

    /**
     * Deletes a user-specific folder. Does not delete the folder itself! Only deletes the user-specific properties for the folder.
     * 
     * @param folder The {@link UserizedCalendarFolder} from which to remove the user-specified properties
     */
    void deleteFolder(UserizedCalendarFolder folder);

    /**
     * Deletes a single property from a folder.
     * 
     * @param folderId The ID of the folder to delete
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param name The name of the property
     */
    void deleteFolderProperty(int folderId, int contextId, int userId, String name);

    /**
     * Check if a folder has user-specific properties and therefore exits in terms of a {@link UserizedCalendarFolder}
     * 
     * @param folderId The ID of the folder to check existence for
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @return <code>true</code> if user-specific folder properties exists,
     *         <code>false</code> otherwise
     */
    boolean exists(int folderId, int contextId, int userId);

    /**
     * Get a {@link UserizedCalendarFolder} for a specific user
     * 
     * @param folder The {@link CalendarFolder}
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @return An {@link UserizedCalendarFolder}. If a folder in terms of {@link #exists(CalendarFolder, int)}
     *         does not exist a folder with default values is returned
     */
    UserizedCalendarFolder getFolder(CalendarFolder folder, int contextId, int userId);

    /**
     * Insert user-specific values for a folder into a database.
     * If the value already exists, the value will be updated.
     * Does not insert the folder itself! Only saves the user-specific properties for the folder.
     * 
     * @param folderId The ID of the folder to insert
     * @param contextId The context ID of the user
     * @param userId The ID of the user the user-specific folder belongs to
     * @param properties The properties to save for the folder. Must be modifiable
     */
    void insertFolder(int folderId, int contextId, int userId, Map<String, String> properties);

    /**
     * Insert an user-specific folder into a database. Does not insert the folder itself!
     * Only saves the user-specific properties for the folder.
     * 
     * @param folder The user-specific folder
     */
    void insertFolder(UserizedCalendarFolder folder);

}
