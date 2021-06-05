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

package com.openexchange.mail.utils;

import java.io.IOException;


/**
 * {@link MaxBytesExceededIOException} - Thrown if a specified maximum byte count is exceeded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MaxBytesExceededIOException extends IOException {

    private static final long serialVersionUID = -2821126584291903503L;

    private final long maxSize;
    private final long size;

    /**
     * Initializes a new {@link MaxBytesExceededIOException}.
     *
     * @param message The detail message
     * @param maxSize The max. allowed size
     * @param size The actual number of bytes
     */
    public MaxBytesExceededIOException(String message, long maxSize, long size) {
        super(message);
        this.maxSize = maxSize;
        this.size = size;
    }

    /**
     * Gets the max. allowed size
     *
     * @return The max. allowed size
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * Gets the actual size
     *
     * @return The actual size
     */
    public long getSize() {
        return size;
    }

}
