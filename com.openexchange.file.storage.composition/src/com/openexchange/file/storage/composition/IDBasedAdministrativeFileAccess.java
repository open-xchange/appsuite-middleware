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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.file.storage.composition;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;


/**
 * An {@link IDBasedAdministrativeFileAccess} can be used for administrative tasks when no
 * user session is available. Implementations will perform the provided calls without any
 * further permission checks then.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface IDBasedAdministrativeFileAccess {

    /**
     * Returns whether the administrative file access is able to handle the file identified by the given id.
     *
     * @param id The id of the file
     * @return <code>true</code> if the file can be handled, <code>false</code> if not.
     * @throws OXException If operation fails
     */
    boolean supports(String id) throws OXException;

    /**
     * Load the metadata about a file<br>
     * <br>
     * Always check if a file is supported with {@link IDBasedAdministrativeFileAccess#supports(String)}. Otherwise
     * {@link FileStorageExceptionCodes#ADMIN_FILE_ACCESS_NOT_AVAILABLE} can be thrown, if the file is not supported.
     *
     * @param id The id of the file
     * @param version The version number of the file. May pass in {@link FileStorageFileAccess#CURRENT_VERSION} to load the current version
     * @return The File Metadata
     * @throws OXException If the file doesn't exist or an error occurs
     */
    File getFileMetadata(String id, String version) throws OXException;

    /**
     * Saves the file metadata. The fields to consider as modified have to be specified with the <code>modifiedColumns</code> parameter.
     * Therefore it is even possible to override the <code>modified by</code> field. Otherwise the context admin will be used. The document
     * must always have the fields <code>folder id</code> and <code>id</code> set.<br>
     * <br>
     * Always check if a file is supported with {@link IDBasedAdministrativeFileAccess#supports(String)}. Otherwise
     * {@link FileStorageExceptionCodes#ADMIN_FILE_ACCESS_NOT_AVAILABLE} can be thrown, if the file is not supported.
     *
     * @param document The metadata to save
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass {@link FileStorageFileAccess#UNDEFINED_SEQUENCE_NUMBER} for new files
     * or {@link FileStorageFileAccess#DISTANT_FUTURE} to circumvent the check.
     * @param modifiedColumns The fields to save. All other fields will be ignored. Use {@link FileStorageFileAccess#ALL_FIELDS} if all fields shall be considered.
     * @throws OXException If the file doesn't exist or an error occurs
     */
    void saveFileMetadata(File document, long sequenceNumber, List<File.Field> modifiedColumns) throws OXException;

    /**
     * Removes the file with the given identifier. This method always results in a hard delete
     * of the according file.<br>
     * <br>
     * Always check if a file is supported with {@link IDBasedAdministrativeFileAccess#supports(String)}. Otherwise
     * {@link FileStorageExceptionCodes#ADMIN_FILE_ACCESS_NOT_AVAILABLE} can be thrown, if the file is not supported.
     *
     * @param id The identifier
     * @throws OXException If the file doesn't exist or an error occurs
     */
    void removeDocument(String id) throws OXException;

    /**
     * Removes the files with the given identifiers. This method always results in a hard delete
     * of the according files.<br>
     * <br>
     * Always check if a file is supported with {@link IDBasedAdministrativeFileAccess#supports(String)}. Otherwise
     * {@link FileStorageExceptionCodes#ADMIN_FILE_ACCESS_NOT_AVAILABLE} can be thrown, if the file is not supported.
     *
     * @param ids The identifiers
     * @throws OXException If one of the files doesn't exist or an error occurs
     */
    void removeDocuments(List<String> ids) throws OXException;

}
