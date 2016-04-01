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

package com.openexchange.filestore;

import java.io.InputStream;
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.exception.OXException;

/**
 * {@link FileStorage} - A file storage or rather the entity-specific view or namespace inside a surrounding file storage;e.g.<br>
 * <code>"file:/mount/disk/1234_ctx_store"</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileStorage {

    /**
     * Saves specified stream as a file.
     *
     * @param file The stream to save as a file
     * @return The file's path in associated file storage
     * @throws OXException If saving file fails
     */
    String saveNewFile(InputStream file) throws OXException;

    /**
     * Gets denoted file's content as a stream.
     *
     * @param name The file name
     * @return The content stream
     * @throws OXException If returning content stream fails
     */
    InputStream getFile(String name) throws OXException;

    /**
     * Lists available files.
     *
     * @return The available file as a sorted set
     * @throws OXException If listing files fails
     */
    SortedSet<String> getFileList() throws OXException;

    /**
     * Gets the size of denoted file.
     *
     * @param name The file name
     * @return The file's size
     * @throws OXException If size cannot be returned
     */
    long getFileSize(String name) throws OXException;

    /**
     * Gets file's MIME type.
     *
     * @param name The file name
     * @return The MIME type
     * @throws OXException If MIME type cannot be returned
     */
    String getMimeType(final String name) throws OXException;

    /**
     * Deletes denoted file
     *
     * @param identifier The file name
     * @return <code>true</code> if deletion was successful; otherwise <code>false</code>
     * @throws OXException If delete attempt fails
     */
    boolean deleteFile(String identifier) throws OXException;

    /**
     * Deletes multiple files.
     *
     * @param identifiers The file identifiers
     * @return The identifiers of those files that could not be deleted
     * @throws OXException If delete attempt fails
     */
    Set<String> deleteFiles(String[] identifiers) throws OXException;

    /**
     * Completely removes/cleans all file storage's content.
     *
     * @throws OXException If remove/clean operation fails
     */
    void remove() throws OXException;

    /**
     * Re-creates file statistics.
     *
     * @throws OXException If create attempt fails
     */
    void recreateStateFile() throws OXException;

    /**
     * Checks if file statistics are correct.
     *
     * @return <code>true</code> if correct; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    boolean stateFileIsCorrect() throws OXException;

    /**
     * Appends specified stream to the supplied file.
     *
     * @param file The stream to append to the file
     * @param name The existing file's path in associated file storage
     * @param offset The offset in bytes where to append the data, must be equal to the file's current length
     * @return The updated length of the file
     * @throws OXException If appending file fails
     */
    long appendToFile(InputStream file, String name, long offset) throws OXException;

    /**
     * Shortens an existing file to the supplied length.
     *
     * @param length The target file length in bytes
     * @param name The existing file's path in associated file storage
     * @throws OXException
     */
    void setFileLength(long length, String name) throws OXException;

    /**
     * Gets (part of) a file's input stream.
     *
     * @param name The existing file's path in associated file storage
     * @param offset The requested start offset of the file stream in bytes
     * @param length The requested length in bytes, starting from the offset
     * @return The file stream
     * @throws OXException
     */
    InputStream getFile(String name, long offset, long length) throws OXException;

}
