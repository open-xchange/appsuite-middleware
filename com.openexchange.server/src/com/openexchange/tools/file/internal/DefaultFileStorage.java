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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.tools.file.internal;

import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;
import javax.activation.MimetypesFileTypeMap;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageCodes;

/**
 * {@link DefaultFileStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultFileStorage implements FileStorage {

    private static final String READ = "r";
    private static final String READ_WRITE = "rw";

    protected final File storage;

    /**
     * Initializes a new {@link DefaultFileStorage}.
     *
     * @param storage A file pointing to parent directory of the storage
     */
    public DefaultFileStorage(File storage) {
        super();
        this.storage = storage;
    }

    /**
     * Initializes a new {@link File} reference for the supplied filename.
     *
     * @param name The filename, relative to the storage's parent directory
     * @return A new {@link File} instance
     */
    protected File file(String name) {
        return new File(storage, name);
    }

    /**
     * Initializes a new {@link EnhancedRandomAccessFile} reference for the supplied filename.
     *
     * @param name The filename, relative to the storage's parent directory
     * @param readOnly <code>true</code> if read-only access is sufficient, <code>false</code> to use read-write access
     * @return A new {@link EnhancedRandomAccessFile} instance
     * @throws OXException If the denoted file was not found or an I/O-error occured
     */
    protected EnhancedRandomAccessFile eraf(String name, boolean readOnly) throws OXException {
        File file = file(name);
        try {
            return new EnhancedRandomAccessFile(file, readOnly ? READ : READ_WRITE);
        } catch (FileNotFoundException e) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(e, file.getAbsolutePath());
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        }
    }

    /**
     * Initializes a new {@link RandomAccessFile} reference for the supplied filename.
     *
     * @param name The filename, relative to the storage's parent directory
     * @param readOnly <code>true</code> if read-only access is sufficient, <code>false</code> to use read-write access
     * @return A new {@link RandomAccessFile} instance
     * @throws OXException If the denoted file was not found
     */
    protected RandomAccessFile raf(String name, boolean readOnly) throws OXException {
        File file = file(name);
        try {
            return new RandomAccessFile(file, readOnly ? READ : READ_WRITE);
        } catch (FileNotFoundException e) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(file.getAbsolutePath());
        }
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        return file(identifier).delete();
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        Set<String> notDeleted = new HashSet<String>();
        for (String identifier : identifiers) {
            if (false == deleteFile(identifier)) {
                notDeleted.add(identifier);
            }
        }
        return notDeleted;
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        try {
            return new BufferedInputStream(new FileInputStream(file(name)));
        } catch (FileNotFoundException e) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }
    }

    @Override
    public long getFileSize(String name) throws OXException {
        File file = file(name);
        if (false == file.exists()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }
        return file.length();
    }

    @Override
    public String getMimeType(String name) throws OXException {
        MimetypesFileTypeMap map = new MimetypesFileTypeMap();
        return map.getContentType(file(name));
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        EnhancedRandomAccessFile eraf = eraf(name, true);
        try {
            if (offset > eraf.length()) {
                throw FileStorageCodes.IOERROR.create("invalid offset: " + offset);
            }
            if (-1 != length && length > eraf.length() - offset) {
                throw FileStorageCodes.IOERROR.create("invalid length: " + length);
            }
            return new RandomAccessFileInputStream(eraf, offset, length);
        } catch (FileNotFoundException e) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        }
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        EnhancedRandomAccessFile eraf = null;
        try {
            eraf = eraf(name, false);
            if (offset != eraf.length()) {
                throw FileStorageCodes.IOERROR.create("invalid offset: " + offset);
            }
            eraf.seek(eraf.length());
            byte[] buffer = new byte[8192];
            int read;
            while (0 < (read = file.read(buffer, 0, buffer.length))) {
                eraf.write(buffer, 0, read);
            }
            eraf.flush();
            return eraf.length();
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            if (null != eraf) {
                try {
                    eraf.close();
                } catch (IOException e) {
                    com.openexchange.log.Log.valueOf(LogFactory.getLog(HashingFileStorage.class))
                        .warn("error closing random access file", e);
                }
            }
        }
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        RandomAccessFile raf = null;
        try {
            raf = raf(name, false);
            if (length > raf.length()) {
                throw FileStorageCodes.IOERROR.create("invalid length: " + length);
            }
            raf.setLength(length);
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(raf);
        }
    }

}
