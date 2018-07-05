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

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            addToBuffer((byte) b);
        }
        return b;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = in.read(b, off, len);
        if (read > 0) {
            addToBuffer(b, off, read);
        }
        return read;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
}
