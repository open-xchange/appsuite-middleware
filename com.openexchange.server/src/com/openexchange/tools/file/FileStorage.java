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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.exception.OXException;
import com.openexchange.tools.file.external.FileStorageCodes;
import com.openexchange.tools.file.external.FileStorageFactory;

public class FileStorage {

    private static volatile FileStorageFactory fss;

    private com.openexchange.tools.file.external.FileStorage fs;

    protected FileStorage() {
        super();
    }

    public FileStorage(final com.openexchange.tools.file.external.FileStorage fs) {
        super();
        this.fs = fs;
    }

    public static final FileStorage getInstance(final URI uri) throws OXException {
        if (fss == null) {
            throw FileStorageCodes.INSTANTIATIONERROR.create("No file storage starter registered.");
        }
        return new FileStorage(fss.getFileStorage(uri));
    }

    public static void setFileStorageStarter(final FileStorageFactory fss) {
        FileStorage.fss = fss;
    }

    public boolean deleteFile(final String identifier) throws OXException {
        return fs.deleteFile(identifier);
    }

    public Set<String> deleteFiles(final String[] identifiers) throws OXException {
        return fs.deleteFiles(identifiers);
    }

    public InputStream getFile(final String name) throws OXException {
        return fs.getFile(name);
    }

    public SortedSet<String> getFileList() throws OXException {
        return fs.getFileList();
    }

    public long getFileSize(final String name) throws OXException {
        return fs.getFileSize(name);
    }

    public String getMimeType(final String name) throws OXException {
        return fs.getMimeType(name);
    }

    public void recreateStateFile() throws OXException {
        fs.recreateStateFile();
    }

    public void remove() throws OXException {
        fs.remove();
    }

    public String saveNewFile(final InputStream file) throws OXException {
        return fs.saveNewFile(file);
    }

    public void close() {
        fs = null;
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
        return fs.appendToFile(file, name, offset);
    }

    /**
     * Shortens an existing file to the supplied length.
     *
     * @param length The target file length in bytes
     * @param name The existing file's path in associated file storage
     * @throws OXException
     */
    public void setFileLength(long length, String name) throws OXException {
        fs.setFileLength(length, name);
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
        return fs.getFile(name, offset, length);
    }

}
