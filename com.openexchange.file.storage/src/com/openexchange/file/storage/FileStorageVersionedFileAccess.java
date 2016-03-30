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

package com.openexchange.file.storage;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.groupware.results.TimedResult;

/**
 * {@link FileStorageVersionedFileAccess}.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface FileStorageVersionedFileAccess {

    /**
     * Removes a certain version of a file
     *
     * @param folderId The folder identifier
     * @param id The file id whose version is to be removed
     * @param versions The versions to be remvoed. The versions that couldn't be removed are returned again.
     * @return The IDs of versions that could not be deleted due to an edit-delete conflict
     * @throws OXException If operation fails
     */
    String[] removeVersion(String folderId, String id, String[] versions) throws OXException;

    /**
     * Lists all versions of a file
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @return All versions of a file
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id) throws OXException;

    /**
     * List all versions of a file loading the given fields
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @param fields The fields to load
     * @return All versions of a file with given fields loaded
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id, List<File.Field> fields) throws OXException;

    /**
     * Lists all versions of a file loading the given fields sorted according to the given field in a given order
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @param fields The fields to load
     * @return All sorted versions of a file with given fields loaded
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id, List<File.Field> fields, File.Field sort, SortDirection order) throws OXException;

}
