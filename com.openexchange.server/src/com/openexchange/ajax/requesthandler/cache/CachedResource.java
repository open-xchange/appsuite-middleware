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

package com.openexchange.ajax.requesthandler.cache;

import java.io.InputStream;

/**
 * {@link CachedResource} - A cached resource.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CachedResource {

    private final byte[] bytes;
    private final InputStream in;
    private final String fileName;
    private final String fileType;
    private final long size;

    /**
     * Initializes a new {@link CachedResource}.
     */
    public CachedResource(final byte[] bytes, final String fileName, final String fileType, final long size) {
        super();
        in = null;
        this.bytes = bytes;
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
    }

    /**
     * Initializes a new {@link CachedResource}.
     */
    public CachedResource(final InputStream in, final String fileName, final String fileType, final long size) {
        super();
        bytes = null;
        this.in = in;
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
    }

    /**
     * Gets the size
     *
     * @return The size or <code>-1</code> if unknown
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the bytes
     * <p>
     * If <code>null</code> check {@link #getInputStream()}.
     *
     * @return The bytes or <code>null</code>
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Gets the input stream
     * <p>
     * If <code>null</code> check {@link #getBytes()}.
     *
     * @return The input stream or <code>null</code>
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * Gets the file name
     *
     * @return The file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the file MIME type
     *
     * @return The file MIME type
     */
    public String getFileType() {
        return fileType;
    }

}
