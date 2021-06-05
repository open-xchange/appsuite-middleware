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

import javax.mail.MessagingException;

/**
 * {@link MaxBytesExceededMessagingException} - Thrown if a specified maximum byte count is exceeded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MaxBytesExceededMessagingException extends MessagingException {

    private static final long serialVersionUID = 656229884485289184L;

    private final long maxSize;
    private final long size;

    /**
     * Initializes a new {@link MaxBytesExceededMessagingException}.
     */
    public MaxBytesExceededMessagingException() {
        super();
        maxSize = -1;
        size = -1;
    }

    /**
     * Initializes a new {@link MaxBytesExceededMessagingException}.
     *
     * @param s The detail message
     */
    public MaxBytesExceededMessagingException(String s) {
        super(s);
        maxSize = -1;
        size = -1;
    }

    /**
     * Initializes a new {@link MaxBytesExceededMessagingException}.
     *
     * @param s The detail message
     * @param e The cause
     */
    public MaxBytesExceededMessagingException(String s, Exception e) {
        super(s, e);
        maxSize = -1;
        size = -1;
    }

    /**
     * Initializes a new {@link MaxBytesExceededMessagingException}.
     *
     * @param e The cause
     * @throws NullPointerException If given <code>MaxBytesExceededIOException</code> is <code>null</code>
     */
    public MaxBytesExceededMessagingException(MaxBytesExceededIOException e) {
        super(e.getMessage(), e);
        maxSize = e.getMaxSize();
        size = e.getSize();
    }

    /**
     * Initializes a new {@link MaxBytesExceededMessagingException}.
     *
     * @param maxSize The max. allowed size
     * @param size The actual number of bytes
     */
    public MaxBytesExceededMessagingException(long maxSize, long size) {
        super();
        this.maxSize = maxSize;
        this.size = size;
    }

    /**
     * Initializes a new {@link MaxBytesExceededMessagingException}.
     *
     * @param s The detail message
     * @param maxSize The max. allowed size
     * @param size The actual number of bytes
     */
    public MaxBytesExceededMessagingException(String s, long maxSize, long size) {
        super(s);
        this.maxSize = maxSize;
        this.size = size;
    }

    /**
     * Initializes a new {@link MaxBytesExceededMessagingException}.
     *
     * @param s The detail message
     * @param e The cause
     * @param maxSize The max. allowed size
     * @param size The actual number of bytes
     */
    public MaxBytesExceededMessagingException(String s, Exception e, long maxSize, long size) {
        super(s, e);
        this.maxSize = maxSize;
        this.size = size;
    }

    /**
     * Gets the max. allowed size.
     *
     * @return The max. allowed size or <code>-1</code> if unknown
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * Gets the actual size.
     *
     * @return The actual size or <code>-1</code> if unknown
     */
    public long getSize() {
        return size;
    }

}
