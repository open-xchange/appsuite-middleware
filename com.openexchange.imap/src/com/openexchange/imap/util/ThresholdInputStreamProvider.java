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
    public ThresholdInputStreamProvider(final int threshold) {
        this(threshold, -1);
    }

    /**
     * Initializes a new {@link ThresholdInputStreamProvider}.
     *
     * @param threshold The threshold
     * @param initalCapacity The initial capacity
     */
    public ThresholdInputStreamProvider(final int threshold, final int initalCapacity) {
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
    public ThresholdInputStreamProvider write(final byte[] bytes, final int off, final int len) throws IOException {
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
    public ThresholdInputStreamProvider write(final byte[] bytes) throws IOException {
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
    public ThresholdInputStreamProvider write(final InputStream in) throws IOException {
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
        } catch (final Exception ignore) {
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
