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

package com.openexchange.pgp.core.packethandling;

import java.io.IOException;
import java.io.InputStream;
import gnu.trove.list.TByteList;
import gnu.trove.list.array.TByteArrayList;

/**
 * {@link RememberingInputStream} represents an InputStream which remembers the data which has been read
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class RememberingInputStream extends InputStream {

    private final InputStream in;
    private TByteList buffer = null;
    private boolean remember;

    /**
     * Initializes a new {@link RememberingInputStream}.
     *
     * @param in
     */
    public RememberingInputStream(InputStream inputStream) {
        this.in = inputStream;
        this.remember = false;
    }

    /**
     * Internal method to write the bytes read to the internal buffer
     *
     * @param b The bytes to remember
     */
    private void addToBuffer(byte b) {
        if (remember) {
            if (buffer == null) {
                buffer = new TByteArrayList();
            }
            buffer.add(b);
        }
    }

    /**
     * Internal method to write the bytes read to the internal buffer
     *
     * @param b The bytes to remember
     */
    private void addToBuffer(byte[] b, int off, int len) {
        if (remember) {
            if (buffer == null) {
                buffer = new TByteArrayList(len);
            }
            buffer.add(b, off, len);
        }
    }

    /**
     * Gets the internal "remember" buffer
     *
     * @return
     */
    public byte[] getBuffer() {
        if (buffer == null) {
            return new byte[] {};
        }
        return buffer.toArray();
    }

    /**
     * Resets the internal "remember" buffer
     */
    public void resetBuffer() {
        if (buffer != null) {
            buffer.clear();
            buffer = null;
        }
    }

    /**
     * Starts remembering all bytes read from the InputStream
     */
    public void startRemembering() {
        this.remember = true;
    }

    /**
     * Stops remembering
     */
    public void stopRemembering() {
        this.remember = false;
    }

    /**
     * Gets the underlying InputStream
     *
     * @return The underlying InputStream
     */
    public InputStream getRememberedStream() {
        return this.in;
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            addToBuffer((byte) b);
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = in.read(b, off, len);
        if (read > 0) {
            addToBuffer(b, off, read);
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
}
