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

package com.openexchange.ajax.zip;

/**
 * {@link Buffer} - Simple helper class for a byte array buffer.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class Buffer {

    private final int buflen;
    private final byte[] buf;

    /**
     * Initializes a new {@link Buffer} with default capacity of 64K.
     */
    public Buffer() {
        this(65536);
    }

    /**
     * Initializes a new {@link Buffer} with given capacity.
     *
     * @param capacity The buffer's capacity
     * @throws IllegalArgumentException If a capacity is less than or equal to <code>0</code> (zero)
     */
    public Buffer(int capacity) {
        super();
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be 0 (zero) or negative");
        }
        this.buflen = capacity;
        this.buf = new byte[capacity];
    }

    /**
     * Gets the buffer
     *
     * @return The buffer
     */
    public byte[] getBuf() {
        return buf;
    }

    /**
     * Gets the buffer length
     *
     * @return The buffer length
     */
    public int getBuflen() {
        return buflen;
    }

}
