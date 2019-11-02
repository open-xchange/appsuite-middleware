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

package com.openexchange.gdpr.dataexport.impl.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.java.Streams;

/**
 * {@link AppendingFileStorageOutputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class AppendingFileStorageOutputStream extends OutputStream {

    /** The default in-memory threshold of 500 KB. */
    public static final int DEFAULT_IN_MEMORY_THRESHOLD = 500 * 1024; // 500KB

    private final FileStorage fileStorage;
    private String fileStorageLocation;
    private final byte buf[];
    private int count;
    private long bytesWritten;

    /**
     * Initializes a new {@link AppendingFileStorageOutputStream} with an internal buffer size of 500 KB.
     *
     * @param fileStorage The file storage to write to
     */
    public AppendingFileStorageOutputStream(FileStorage fileStorage) {
        this(DEFAULT_IN_MEMORY_THRESHOLD, fileStorage);
    }

    /**
     * Initializes a new {@link AppendingFileStorageOutputStream}.
     *
     * @param size The buffer size
     * @param fileStorage The file storage to write to
     */
    public AppendingFileStorageOutputStream(int size, FileStorage fileStorage) {
        super();
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.fileStorage = fileStorage;
        buf = new byte[size];
        fileStorageLocation = null;
    }

    /** Flush the internal buffer to file storage location */
    private void flushBufferToFileStorage() throws IOException {
        if (count > 0) {
            try {
                if (fileStorageLocation == null) {
                    fileStorageLocation = fileStorage.saveNewFile(Streams.newByteArrayInputStream(buf, 0, count));
                    bytesWritten = count;
                } else {
                    fileStorage.appendToFile(Streams.newByteArrayInputStream(buf, 0, count), fileStorageLocation, bytesWritten);
                    bytesWritten += count;
                }
                count = 0;
            } catch (OXException e) {
                Throwable cause = e.getCause();
                throw cause instanceof IOException ? ((IOException) cause) : new IOException(e);
            }
        }
    }

    /**
     * Gets the file storage location
     *
     * @return The file storage location
     */
    public synchronized Optional<String> getFileStorageLocation() {
        return Optional.ofNullable(fileStorageLocation);
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (count >= buf.length) {
            flushBufferToFileStorage();
        }
        buf[count++] = (byte)b;
    }

    @Override
    public synchronized void write(byte b[], int off, int len) throws IOException {
        if (len >= buf.length) {
            flushBufferToFileStorage();
            return;
        }
        if (len > buf.length - count) {
            flushBufferToFileStorage();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    @Override
    public synchronized void flush() throws IOException {
        flushBufferToFileStorage();
    }

    @Override
    public synchronized void close() throws IOException {
        flushBufferToFileStorage();
        super.close();
    }

}
