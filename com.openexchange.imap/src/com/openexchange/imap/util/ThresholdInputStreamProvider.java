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

package com.openexchange.imap.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;

/**
 * {@link ThresholdInputStreamProvider} - Backs data in a <code>byte</code> array as long as specified threshold is not exceeded, but
 * streams data to a temporary file otherwise.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThresholdInputStreamProvider implements Closeable, InputStreamProvider {

    /** The backing file holder */
    private final ThresholdFileHolder sink;

    /**
     * Initializes a new {@link ThresholdInputStreamProvider} with default threshold and default initial capacity.
     */
    public ThresholdInputStreamProvider() {
        this(-1, -1);
    }

    /**
     * Initializes a new {@link ThresholdInputStreamProvider} with default initial capacity.
     *
     * @param threshold The threshold
     */
    public ThresholdInputStreamProvider(int threshold) {
        this(threshold, -1);
    }

    /**
     * Initializes a new {@link ThresholdInputStreamProvider}.
     *
     * @param threshold The threshold
     * @param initalCapacity The initial capacity
     */
    public ThresholdInputStreamProvider(int threshold, int initalCapacity) {
        super();
        sink = new ThresholdFileHolder(threshold, initalCapacity, true);
    }

    /**
     * Gets the {@link OutputStream} view on this file holder.
     *
     * @return An {@link OutputStream} that writes data into this file holder
     */
    public OutputStream asOutputStream() {
        return sink.asOutputStream();
    }

    /**
     * Writes the specified content to this file holder.
     *
     * @param bytes The content to be written.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @return This file holder with content written
     * @throws IOException If write attempt fails
     * @throws IndexOutOfBoundsException If illegal arguments are specified
     */
    public ThresholdInputStreamProvider write(byte[] bytes, int off, int len) throws IOException {
        try {
            sink.write(bytes, off, len);
            return this;
        } catch (OXException e) {
            Throwable cause = e.getCause();
            throw (cause instanceof IOException) ? (IOException) cause : new IOException(null == cause ? e : cause);
        }
    }

    /**
     * Writes the specified content to this file holder.
     *
     * @param bytes The content to be written.
     * @return This file holder with content written
     * @throws IOException If write attempt fails
     */
    public ThresholdInputStreamProvider write(byte[] bytes) throws IOException {
        try {
            sink.write(bytes);
            return this;
        } catch (OXException e) {
            Throwable cause = e.getCause();
            throw (cause instanceof IOException) ? (IOException) cause : new IOException(null == cause ? e : cause);
        }
    }

    /**
     * Writes the specified content to this file holder.
     * <p>
     * Orderly closes specified {@link InputStream} instance.
     *
     * @param in The content to be written.
     * @return This file holder with content written
     * @throws IOException If write attempt fails
     */
    public ThresholdInputStreamProvider write(InputStream in) throws IOException {
        try {
            sink.write(in);
            return this;
        } catch (OXException e) {
            Throwable cause = e.getCause();
            throw (cause instanceof IOException) ? (IOException) cause : new IOException(null == cause ? e : cause);
        }
    }

    /**
     * Gets the number of valid bytes written to this file holder.
     *
     * @return The number of bytes
     */
    public long getCount() {
        return sink.getCount();
    }

    @Override
    public void close() {
        sink.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            close();
        } catch (Exception ignore) {
            // Ignore
        }
    }

    /**
     * Gets this file holder content as a byte array.
     *
     * @return The byte array
     * @throws OXException If byte array cannot be returned for any reason
     */
    public byte[] toByteArray() throws OXException {
        return sink.toByteArray();
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return sink.getStream();
    }

    /**
     * Gets the length.
     *
     * @return The length or <code>-1</code>
     */
    public long getLength() {
        return sink.getLength();
    }

}
