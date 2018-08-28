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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.microsoft.graph.onedrive;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link MicrosoftGraphDriveService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface MicrosoftGraphDriveService {

    /**
     * Checks whether the folder with the specified path exists.
     * 
     * @param accessToken The oauth access token
     * @param folderPath The absolute folder path
     * @return <code>true</code> if the folder exists; <code>false</code> otherwise.
     * @throws OXException If an error is occurred
     */
    boolean existsFolder(String accessToken, String folderPath) throws OXException;

    /**
     * Returns the root folder of the user's default drive account
     * 
     * @param userId The user identifier
     * @param accessToken The oauth access token
     * @return The root folder
     * @throws OXException If an error is occurred
     */
    OneDriveFolder getRootFolder(int userId, String accessToken) throws OXException;

    /**
     * Retrieves the folder with the specified identifier for the specified user
     * 
     * @param userId The user identifier
     * @param accessToken The oauth access token
     * @param folderId the folder identifier
     * @return The folder
     * @throws OXException if an error is occurred
     */
    OneDriveFolder getFolder(int userId, String accessToken, String folderId) throws OXException;

    /**
     * Returns all sub-folders of the specified folder
     * 
     * @param userId The user identifier
     * @param accessToken The oauth access token
     * @param folderId The folder identifier for which to retrieve all sub-folders
     * @return A {@link List} with all sub-folders
     * @throws OXException If an error is occurred
     */
    List<OneDriveFolder> getSubFolders(int userId, String accessToken, String folderId) throws OXException;

    /**
     * Returns all files with in the specified folder
     * 
     * @param userId The user identifier
     * @param accessToken The oauth access token
     * @param folderId The folder identifier for which to retrieve all sub-folders
     * @return A {@link List} with all files in the specified folder
     * @throws OXException If an error is occurred
     */
    List<OneDriveFile> getFiles(int userId, String accessToken, String folderId) throws OXException;

    /**
     * 
     * @param userId
     * @param accessToken
     * @param itemId
     * @return
     * @throws OXException
     */
    OneDriveFile getFile(int userId, String accessToken, String itemId) throws OXException;

    /**
     * 
     * @param userId
     * @param accessToken
     * @param itemIds
     * @return
     * @throws OXException
     */
    List<OneDriveFile> getFiles(int userId, String accessToken, List<String> itemIds) throws OXException;
}
