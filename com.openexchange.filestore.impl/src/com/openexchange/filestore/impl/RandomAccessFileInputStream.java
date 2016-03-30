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

package com.openexchange.filestore.impl;

import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;
import java.io.IOException;

/**
 * {@link RandomAccessFileInputStream}
 *
 * Provides an input stream for part of a file.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RandomAccessFileInputStream extends java.io.InputStream {

    private final EnhancedRandomAccessFile file;
    private final long endPosition;
    private long markedPosition;

    /**
     * Initializes a new {@link RandomAccessFileInputStream}.
     *
     * @param eraf The random access file to create the random access stream for
     * @param offset The offset to begin to read data from, or <code>0</code> to start at the beginning
     * @param length The number of bytes to read starting from the offset, or <code>-1</code> to read until the end
     * @throws IOException if the offset is less than 0 or if an I/O error occurs.
     */
    public RandomAccessFileInputStream(EnhancedRandomAccessFile eraf, long offset, long length) throws IOException {
        super();
        this.file = eraf;
        this.endPosition = -1 == length ? file.length() : Math.min(file.length(), offset + length);
        this.file.seek(offset);
    }

    @Override
    public synchronized int read() throws IOException {
        if (file.getFilePointer() >= endPosition) {
            return -1;
        }
        return file.read();
    }

    @Override
    public synchronized void close() throws IOException {
        this.file.close();
        super.close();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            this.markedPosition = file.getFilePointer();
        } catch (IOException e) {
            // indicate failure in following reset() call
            this.markedPosition = Long.MIN_VALUE;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if (Long.MIN_VALUE == this.markedPosition) {
            throw new IOException("No position marked");
        }
        file.seek(markedPosition);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int available = this.available();
        if (0 >= available) {
            return -1;
        }
        return file.read(b, off, Math.min(available, len));
    }

    @Override
    public synchronized int available() throws IOException {
        return (int)(endPosition - file.getFilePointer());
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            throw new IOException("can skip at maximum " + Integer.MAX_VALUE + " bytes");
        }
        return file.skipBytes((int)(n & 0xFFFFFFFF));
    }

}
