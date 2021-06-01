/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
