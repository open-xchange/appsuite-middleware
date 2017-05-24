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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.composition;

import java.io.InputStream;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link IDBasedFileAccessListener} - A listener receiving call-backs on invocations of {@link IDBasedFileAccess} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
@Service
public interface IDBasedFileAccessListener {

    /**
     * Invoked before a new file is created.
     *
     * @param document The file meta-data
     * @param data The binary content
     * @param sequenceNumber The sequence number
     * @param modifiedColumns The modified columns
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     */
    void onBeforeNewFile(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before an existing file is modified.
     *
     * @param document The file meta-data
     * @param data The binary content
     * @param sequenceNumber The sequence number
     * @param modifiedColumns The modified columns
     * @param isMove <code>true</code> if update implies a move; otherwise <code>false</code>
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     */
    void onBeforeUpdateFile(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns, boolean isMove, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before multiple files are moved.
     *
     * @param sourceIds The file identifiers
     * @param sequenceNumber The sequence number
     * @param destFolderId The identifier of the destination folder
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     */
    void onBeforeMoveFiles(List<String> sourceIds, long sequenceNumber, String destFolderId, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before an existent file is copied.
     *
     * @param sourceId The file identifier
     * @param version The version to consider for copy
     * @param destFolderId The identifier of the destination folder
     * @param update The updated meta-data
     * @param newData The possibly new binary data
     * @param fields The modified fields
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     */
    void onBeforeCopyFile(String sourceId, String version, String destFolderId, File update, InputStream newData, List<Field> fields, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before multiple files are deleted.
     *
     * @param  The file identifiers
     * @param sequenceNumber The sequence number
     * @param hardDelete <code>true</code> for hard-delete; otherwise <code>false</code>
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     */
    void onBeforeDeleteFiles(List<IDTuple> ids, long sequenceNumber, boolean hardDelete, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before all files of a certain folder are deleted.
     *
     * @param folderId The folder identifier
     * @param sequenceNumber The sequence number
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     */
    void onBeforeDeleteAllFilesInFolder(String folderId, long sequenceNumber, FileStorageFileAccess fileAccess, Session session) throws OXException;

}
