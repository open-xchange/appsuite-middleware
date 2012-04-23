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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13;

import java.io.IOException;
import javax.servlet.ServletInputStream;
import com.openexchange.ajp13.exception.AJPv13Exception;

/**
 * {@link AJPv13ServletInputStream} - The AJP's servlet input stream which may be accessed by only one dedicated {@link Thread thread}. The
 * one which actually created this servlet input stream. Otherwise an {@link IOException I/O exception} will be thrown complaining about
 * illegal access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13ServletInputStream extends ServletInputStream {

    private static final byte[] EMPTY_BYTES = new byte[0];

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13ServletInputStream.class));

    private final AJPv13Connection ajpCon;

    private final Thread owner;

    private byte[] data;

    private int pos;

    private boolean dataSet;

    private boolean isClosed;

    private static final String EXC_MSG = "No data found in servlet's input stream!";

    /**
     * Initializes a new {@link AJPv13ServletInputStream}
     *
     * @param ajpCon The associated AJP connection
     */
    public AJPv13ServletInputStream(final AJPv13Connection ajpCon) {
        super();
        this.ajpCon = ajpCon;
        owner = Thread.currentThread();
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }

    private void ensureAccess() throws IOException {
        if (!Thread.currentThread().equals(owner)) {
            throw new IOException(new StringBuilder(128).append("Illegal access to input stream through thread \"").append(
                Thread.currentThread().getName()).append("\" but should be \"").append(owner.getName()).append('"').toString());
        }
    }

    /**
     * This method is called to set or append new data. If new data is <code>null</code> then value <code>-1</code> will be returned on
     * invocations of any read method
     *
     * @param newData The new data
     * @throws IOException If an I/O error occurs
     */
    public void setData(final byte[] newData) throws IOException {
        ensureAccess();
        if (isClosed) {
            throw new IOException("InputStream is closed");
        }
        if (data != null && pos < data.length) {
            /*
             * Copy rest of previous data
             */
            final byte[] temp = new byte[data.length - pos];
            System.arraycopy(data, pos, temp, 0, temp.length);
            /*
             * Append new data
             */
            if (newData == null) {
                data = temp;
            } else {
                data = new byte[temp.length + newData.length];
                System.arraycopy(temp, 0, data, 0, temp.length);
                System.arraycopy(newData, 0, data, temp.length, newData.length);
            }
            pos = 0;
        } else {
            if (newData == null) {
                /*
                 * Data is set to null and dataSet is left to false
                 */
                data = null;
                pos = 0;
                dataSet = false;
                return;
            }
            data = new byte[newData.length];
            System.arraycopy(newData, 0, data, 0, newData.length);
            pos = 0;
        }
        dataSet = true;
    }

    @Override
    public int read() throws IOException {
        ensureAccess();
        if (isClosed) {
            throw new IOException("AJPv13ServletInputStream.read(): InputStream is closed");
        } else if (!dataSet) {
            if (data == null || pos >= data.length) {
                return -1;
            }
            throw new IOException(new StringBuilder("AJPv13ServletInputStream.read(): ").append(EXC_MSG).toString());
        }
        if (pos >= data.length) {
            dataSet = false;
            if (!requestMoreDataFromWebServer()) {
                /*
                 * Web server sent an empty data package to indicate no more available data
                 */
                return -1;
            }
        }
        return (data[pos++] & 0xff);
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        ensureAccess();
        if (isClosed) {
            throw new IOException("AJPv13ServletInputStream.read(byte[], int, int): InputStream is closed");
        }
        if (!dataSet) {
            if (data == null || pos >= data.length) {
                return -1;
            }
            throw new IOException(new StringBuilder("AJPv13ServletInputStream.read(byte[], int, int): ").append(EXC_MSG).toString());
        }
        if (b == null) {
            throw new NullPointerException("AJPv13ServletInputStream.read(byte[], int, int): Byte array is null");
        }
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException("AJPv13ServletInputStream.read(byte[], int, int): Invalid arguments");
        }
        if (len == 0) {
            return 0;
        }
        final int numOfAvailableBytes = data.length - pos;
        /*
         * Number of available bytes is greater than or equal to requested length (len)
         */
        if (numOfAvailableBytes >= len) {
            System.arraycopy(data, pos, b, off, len);
            pos += len;
            return len;
        }
        /*
         * Caller requests more than currently available bytes. First copy all available bytes into byte array.
         */
        if (numOfAvailableBytes > 0) {
            System.arraycopy(data, pos, b, off, numOfAvailableBytes);
            pos = data.length;
        }
        dataSet = false;
        int remainingLen = len - numOfAvailableBytes;
        int numOfFilledBytes = numOfAvailableBytes;
        while (remainingLen > 0 && requestMoreDataFromWebServer()) {
            if (data.length >= remainingLen) {
                /*
                 * New data size is equal to or greater than remaining len
                 */
                System.arraycopy(data, pos, b, off + numOfFilledBytes, remainingLen);
                pos += remainingLen;
                return len;
            }
            /*
             * Copy data from web server into byte array
             */
            System.arraycopy(data, pos, b, off + numOfFilledBytes, data.length);
            pos = data.length;
            dataSet = false;
            numOfFilledBytes += data.length;
            remainingLen -= data.length;
        }
        return numOfFilledBytes == 0 ? -1 : numOfFilledBytes;
    }

    @Override
    public long skip(final long n) throws IOException {
        ensureAccess();
        if (!dataSet) {
            if (data == null || pos >= data.length) {
                return 0;
            }
            throw new IOException("AJPv13ServletInputStream.skip(long): No data found");
        }
        if (isClosed) {
            throw new IOException("AJPv13ServletInputStream.skip(long): InputStream is closed");
        }
        if (n > Integer.MAX_VALUE) {
            throw new IOException("AJPv13ServletInputStream.skip(long): Too many bytes to skip: " + n);
        }
        final byte[] tmp = new byte[(int) n];
        return (read(tmp, 0, tmp.length));
    }

    @Override
    public int available() throws IOException {
        ensureAccess();
        if (!dataSet) {
            if (data == null || pos >= data.length) {
                return 0;
            }
            throw new IOException("AJPv13ServletInputStream.available(): No data found");
        }
        if (isClosed) {
            throw new IOException("AJPv13ServletInputStream.available(): InputStream is closed");
        }
        return (data.length - pos);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Peeks (and thus does not consume) available data from this input stream
     *
     * @return Peeked available data
     * @throws IOException If an I/O error occurs
     */
    public byte[] peekData() throws IOException {
        final int len = available();
        if (0 == len) {
            return EMPTY_BYTES;
        }
        final byte[] retval = new byte[len];
        System.arraycopy(data, pos, retval, 0, len);
        return retval;
    }

    /**
     * Requests more data from web server. Note: If web server transmits an empty data package the setData() method sets data to
     * <code>null</code>. This should be checked after calling this method.
     *
     * @return <code>true</code> if new data could be read successfully, <code>false</code> if no more data is expected or an empty data
     *         package has been sent from web server
     */
    private boolean requestMoreDataFromWebServer() throws IOException {
        try {
            final AJPv13RequestHandler ajpRequestHandler = ajpCon.getAjpRequestHandler();
            if (ajpRequestHandler.isAllDataRead()) {
                /*
                 * No more data expected
                 */
                return false;
            }
            {
                final BlockableBufferedOutputStream ajpOut = ajpCon.getOutputStream();
                ajpOut.acquire();
                try {
                    ajpOut.write(AJPv13Response.getGetBodyChunkBytes(ajpRequestHandler.getNumOfBytesToRequestFor()));
                    ajpOut.flush();
                } finally {
                    ajpOut.release();
                }
            }
            /*
             * Trigger request handler to process expected incoming data package which in turn calls the setData() method.
             */
            ajpRequestHandler.processPackage();
            return (data != null);
        } catch (final AJPv13Exception e) {
            LOG.error(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }
}
