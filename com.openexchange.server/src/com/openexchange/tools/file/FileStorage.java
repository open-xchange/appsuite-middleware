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

package com.openexchange.tools.file;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.FileStorageService;

/**
 * {@link FileStorage} - The legacy file storage delegating to new <code>com.openexchange.filestore</code> API.
 *
 * @deprecated Please use new <code>com.openexchange.filestore</code> API
 */
@Deprecated
public class FileStorage {

    private static volatile FileStorageService fss;

    /**
     * Gets the legacy file storage for given URI
     *
     * @param uri The URI
     * @return The legacy file storage
     * @throws OXException If file storage cannot be returned
     */
    public static final FileStorage getInstance(final URI uri) throws OXException {
        FileStorageService fss = FileStorage.fss;
        if (fss == null) {
            throw FileStorageCodes.INSTANTIATIONERROR.create("No file storage starter registered.");
        }
        return new FileStorage(fss.getFileStorage(uri));
    }

    /**
     * Sets the service instance.
     *
     * @param fss The service instance
     */
    public static void setFileStorageStarter(final FileStorageService fss) {
        FileStorage.fss = fss;
    }

    // ---------------------------------------------------------------------------------------------------------------

    private com.openexchange.filestore.FileStorage delegate;

    /**
     * Initializes a new {@link FileStorage}.
     */
    protected FileStorage() {
        super();
    }

    /**
     * Initializes a new {@link FileStorage}.
     *
     * @param fs The delegate file storage
     */
    public FileStorage(final com.openexchange.filestore.FileStorage fs) {
        super();
        this.delegate = fs;
    }

    public boolean deleteFile(final String identifier) throws OXException {
        return delegate.deleteFile(identifier);
    }

    public Set<String> deleteFiles(final String[] identifiers) throws OXException {
        if (null == identifiers || 0 == identifiers.length) {
            return Collections.emptySet();
        }
        return delegate.deleteFiles(identifiers);
    }

    public InputStream getFile(final String name) throws OXException {
        Thread thread = Thread.currentThread();

        return delegate.getFile(name);
    }

    public SortedSet<String> getFileList() throws OXException {
        return delegate.getFileList();
    }

    public long getFileSize(final String name) throws OXException {
        return delegate.getFileSize(name);
    }

    public String getMimeType(final String name) throws OXException {
        return delegate.getMimeType(name);
    }

    public void recreateStateFile() throws OXException {
        delegate.recreateStateFile();
    }

    public void remove() throws OXException {
        delegate.remove();
    }

    public String saveNewFile(final InputStream file) throws OXException {
        return delegate.saveNewFile(file);
    }

    public void close() {
        delegate = null;
    }

    /**
     * Appends specified stream to the supplied file.
     *
     * @param file The stream to append to the file
     * @param name The existing file's path in associated file storage
     * @param offset The offset in bytes where to append the data, must be equal to the file's current length
     * @return The updated length of the file
     * @throws OXException If appending file fails
     */
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        return delegate.appendToFile(file, name, offset);
    }

    /**
     * Shortens an existing file to the supplied length.
     *
     * @param length The target file length in bytes
     * @param name The existing file's path in associated file storage
     * @throws OXException
     */
    public void setFileLength(long length, String name) throws OXException {
        delegate.setFileLength(length, name);
    }

    /**
     * Gets (part of) a file's input stream.
     *
     * @param name The existing file's path in associated file storage
     * @param offset The requested start offset of the file stream in bytes
     * @param length The requested length in bytes, starting from the offset
     * @return The file stream
     * @throws OXException
     */
    public InputStream getFile(String name, long offset, long length) throws OXException {
        return delegate.getFile(name, offset, length);
    }

}
