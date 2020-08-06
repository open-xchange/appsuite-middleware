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
