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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.unitedinternet.smartdrive.client;

import java.io.InputStream;
import java.util.List;

/**
 * {@link SmartDriveStatefulAccess} - Methods require an active session. Requests to those paths will be answered with a HTTP 401
 * Unauthorized if no session identifier or an invalid one is provided.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SmartDriveStatefulAccess extends SmartDriveConstants {

    /**
     * Obtains the download token.
     * 
     * @return The download token
     * @throws SmartDriveException If operation fails
     */
    String obtainDownloadToken() throws SmartDriveException;

    /**
     * Lists the content of specified directory.
     * 
     * @param pathOfDirectory The path of the directory
     * @return The content of specified directory
     * @throws SmartDriveException If list fails
     */
    SmartDriveResponse<List<SmartDriveResource>> list(String pathOfDirectory) throws SmartDriveException;

    /**
     * Lists the content of specified directory.
     * 
     * @param pathOfDirectory The path of the directory
     * @return The <b>extended</b> content of specified directory
     * @throws SmartDriveException If list fails
     */
    SmartDriveResponse<List<SmartDriveResource>> extendedList(String pathOfDirectory) throws SmartDriveException;

    /**
     * Search recursively from specified directory.
     * 
     * @param pathOfDirectory The path to directory from which the recursive search is going to be started
     * @param query The SmartDrive query to perform
     * @param thumbNailFormatIds The allowed identifiers for thumb nails; default is <code>[ 1 ]</code>
     * @return The queried SmartDrive resources
     * @throws SmartDriveException If search fails
     */
    SmartDriveResponse<List<SmartDriveResource>> search(String pathOfDirectory, SmartDriveQuery query, int[] thumbNailFormatIds) throws SmartDriveException;

    /**
     * Search recursively from specified directory.
     * 
     * @param pathOfDirectory The path to directory from which the recursive search is going to be started
     * @param query The SmartDrive query to perform
     * @param thumbNailFormatIds The allowed identifiers for thumb nails; default is <code>[ 1 ]</code>
     * @return The queried <b>extended</b> SmartDrive resources
     * @throws SmartDriveException If search fails
     */
    SmartDriveResponse<List<SmartDriveResource>> extendedSearch(String pathOfDirectory, SmartDriveQuery query, int[] thumbNailFormatIds) throws SmartDriveException;

    /**
     * Retrieves general and custom properties of the resources contained in specified directory.
     * <p>
     * This method is necessary since resource <code>"/"</code> is not reachable via list/extendedList method.
     * 
     * @param resourcePath The path to resource
     * @param thumbNailFormatIds The allowed identifiers for thumb nails; default is <code>[ 1 ]</code>
     * @return The properties of the resources contained in specified directory
     * @throws SmartDriveException If retrieval fails
     */
    SmartDriveResponse<List<SmartDriveResource>> propget(String resourcePath, int[] thumbNailFormatIds) throws SmartDriveException;

    /**
     * Retrieves general and custom properties of the resources contained in specified directory.
     * <p>
     * This method is necessary since resource <code>"/"</code> is not reachable via list/extendedList method.
     * 
     * @param resourcePath The path to resource
     * @param thumbNailFormatIds The allowed identifiers for thumb nails; default is <code>[ 1 ]</code>
     * @return The <b>extended</b> properties of the resources contained in specified directory
     * @throws SmartDriveException If retrieval fails
     */
    SmartDriveResponse<List<SmartDriveResource>> extendedPropget(String resourcePath, int[] thumbNailFormatIds) throws SmartDriveException;

    /**
     * Manipulates the properties (meta data) of a resource.
     * 
     * @param srcPath The path of the resource
     * @param newName The new name; set to <code>null</code> to ignore rename
     * @param deadProperties The dead properties to store
     * @return A <code>null</code> response
     * @throws SmartDriveException If operation fails
     */
    SmartDriveResponse<Object> proppatch(String srcPath, String newName, List<SmartDriveDeadProperty> deadProperties) throws SmartDriveException;

    /**
     * Creates a directory.
     * 
     * @param pathOfDirectory The path of the directory to create
     * @return A <code>null</code> response
     * @throws SmartDriveException If creation fails
     */
    SmartDriveResponse<Object> mkcol(String pathOfDirectory) throws SmartDriveException;

    /**
     * Renames denoted resource.
     * 
     * @param srcPath The path of the resource to rename
     * @param newName The new name
     * @return A <code>null</code> response
     * @throws SmartDriveException If rename fails
     */
    SmartDriveResponse<Object> rename(String srcPath, String newName) throws SmartDriveException;

    /**
     * Copies specified files.
     * 
     * @param srcPath The source directory path
     * @param destPath The destination directory path
     * @param fileNames The file names
     * @param overwrite <code>true</code> to overwrite; otherwise <code>false</code>
     * @return Possible collisions
     * @throws SmartDriveException If operation fails
     */
    SmartDriveResponse<List<SmartDriveCollision>> copy(String srcPath, String destPath, String[] fileNames, boolean overwrite) throws SmartDriveException;

    /**
     * Moves specified files.
     * 
     * @param srcPath The source directory path
     * @param destPath The destination directory path
     * @param fileNames The file names
     * @param overwrite <code>true</code> to overwrite; otherwise <code>false</code>
     * @return Possible collisions
     * @throws SmartDriveException If operation fails
     */
    SmartDriveResponse<List<SmartDriveCollision>> move(String srcPath, String destPath, String[] fileNames, boolean overwrite) throws SmartDriveException;

    /**
     * Deletes specified resources located below given directory.
     * 
     * @param pathOfDirectory The optional directory path; if <code>null</code> paths are considered to be absolute paths
     * @param relativePaths The relative paths of the resources to delete; <code>"/"</code> deletes whole directory
     * @return Possible collisions
     * @throws SmartDriveException If delete fails
     */
    SmartDriveResponse<List<SmartDriveCollision>> delete(String pathOfDirectory, List<String> relativePaths) throws SmartDriveException;

    /**
     * Gets the user information for logged-in user.
     * 
     * @return The user information for logged-in user
     * @throws SmartDriveException If operation fails
     */
    SmartDriveResponse<SmartDriveUserInfo> userInfo() throws SmartDriveException;

}
