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
import java.util.Arrays;

/**
 * {@link RememberingInputStream} represents an InputStream which remembers the data which has been read
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v2.4.2
 */
class RememberingInputStream extends InputStream {

    private final InputStream in;
    private byte[] buffer = null;

    /**
     * Initializes a new {@link RememberingInputStream}.
     *
     * @param in
     */
    public RememberingInputStream(InputStream inputStream) {
        this.in = inputStream;
    }

    /**
     * Internal method to write the bytes read to the internal buffer
     *
     * @param b The bytes to remember
     */
    private void addToBuffer(byte... b) {
        if (buffer == null) {
            buffer = new byte[b.length];
            System.arraycopy(b, 0, buffer, 0, b.length);
        }
        else {
            byte[] newBuffer = new byte[buffer.length + b.length];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            System.arraycopy(b, 0, newBuffer, buffer.length, b.length);
            buffer = newBuffer;
        }
    }

    /**
     * Gets the internal "remember" buffer
     *
     * @return
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Resets the internal "remember" buffer
     */
    public void resetBuffer() {
        buffer = null;
    }

    /**
     * Gets the underlying InputStream
     *
     * @return The underlying InputStream
     */
    public InputStream getRememberingStream() {
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
        int read = super.read(b, off, len);
        if (read != -1) {
            addToBuffer(Arrays.copyOfRange(b, off, read));
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
        int read = super.read(b);
        if (read != -1) {
            addToBuffer(Arrays.copyOf(b, read));
        }
        return read;
    }

}
