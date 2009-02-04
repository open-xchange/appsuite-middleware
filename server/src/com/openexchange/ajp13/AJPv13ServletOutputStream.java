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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.locks.Lock;
import javax.servlet.ServletOutputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link AJPv13ServletOutputStream} - The AJP's servlet output stream.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13ServletOutputStream extends ServletOutputStream implements Synchronizable {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13ServletOutputStream.class);

    private static final String ERR_OUTPUT_CLOSED = "OutputStream is closed";

    private final AJPv13Connection ajpCon;

    private final UnsynchronizedByteArrayOutputStream byteBuffer;

    private boolean isClosed;

    private final Synchronizer synchronizer;

    /**
     * Initializes a new {@link AJPv13ServletOutputStream}.
     * 
     * @param ajpCon The associated AJP connection
     */
    public AJPv13ServletOutputStream(final AJPv13Connection ajpCon) {
        super();
        this.ajpCon = ajpCon;
        byteBuffer = new UnsynchronizedByteArrayOutputStream(AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE);
        synchronizer = new NonBlockingSynchronizer();
    }

    /**
     * Resets this output stream's buffer
     */
    public void resetBuffer() {
        final Lock l = synchronizer.acquire();
        try {
            byteBuffer.reset();
        } finally {
            synchronizer.release(l);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        flushByteBuffer();
    }

    @Override
    public void close() throws IOException {
        flushByteBuffer();
    }

    @Override
    public void write(final int i) throws IOException {
        final Lock l = synchronizer.acquire();
        try {
            if (isClosed) {
                throw new IOException(ERR_OUTPUT_CLOSED);
            }
            if (byteBuffer.size() >= (AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE)) {
                responseToWebServer();
            }
            byteBuffer.write(i);
        } finally {
            synchronizer.release(l);
        }
    }

    /**
     * Checks if this output stream currently holds any data outstanding for being written to Web Server.
     * 
     * @return <code>true</code> if this output stream currently holds any data; otherwise <code>false</code>.
     * @throws IOException If stream is already closed
     */
    public boolean hasData() throws IOException {
        final Lock l = synchronizer.acquire();
        try {
            if (isClosed) {
                throw new IOException(ERR_OUTPUT_CLOSED);
            }
            return byteBuffer.size() > 0;
        } finally {
            synchronizer.release(l);
        }
    }

    /**
     * Gets current data held in this output stream outstanding for being written to Web Server.
     * 
     * @return Current data held in this output stream
     * @throws IOException If stream is already closed
     */
    public byte[] getData() throws IOException {
        final Lock l = synchronizer.acquire();
        try {
            if (isClosed) {
                throw new IOException(ERR_OUTPUT_CLOSED);
            }
            /*
             * try { byteBuffer.flush(); } catch (IOException e) { LOG.error(e.getMessage(), e); }
             */
            return byteBuffer.toByteArray();
        } finally {
            synchronizer.release(l);
        }
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        final Lock l = synchronizer.acquire();
        try {
            if (isClosed) {
                throw new IOException(ERR_OUTPUT_CLOSED);
            } else if (b == null) {
                throw new NullPointerException("AJPv13ServletOutputStream.write(byte[], int, int): Byte array is null");
            } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException("AJPv13ServletOutputStream.write(byte[], int, int): Invalid arguments");
            } else if (len == 0) {
                return;
            }
            final int restCapacity = AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE - byteBuffer.size();
            if (len <= restCapacity) {
                /*
                 * Everything fits into buffer!
                 */
                byteBuffer.write(b, off, len);
                return;
            }
            /*
             * Write fitting bytes into buffer
             */
            byteBuffer.write(b, off, restCapacity);
            /*
             * Write full byte buffer
             */
            responseToWebServer();
            /*
             * Write rest of byte array
             */
            int numOfWrittenBytes = restCapacity;
            int numOfWithheldBytes = len - restCapacity;
            while (numOfWithheldBytes > AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE) {
                /*
                 * As long as withheld bytes exceed max body chunk size, write them cut into MAX_BODY_CHUNK_SIZE pieces
                 */
                final byte[] responseBodyChunk = new byte[AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE];
                System.arraycopy(b, off + numOfWrittenBytes, responseBodyChunk, 0, responseBodyChunk.length);
                byteBuffer.write(responseBodyChunk, 0, responseBodyChunk.length);
                responseToWebServer();
                numOfWrittenBytes += AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE;
                numOfWithheldBytes -= AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE;
            }
            /*
             * Extract remaining bytes
             */
            final byte[] withheldBytes = new byte[numOfWithheldBytes];
            System.arraycopy(b, off + numOfWrittenBytes, withheldBytes, 0, withheldBytes.length);
            /*
             * Fill byte buffer with withheld bytes
             */
            byteBuffer.write(withheldBytes, 0, withheldBytes.length);
        } finally {
            synchronizer.release(l);
        }
    }

    private static final String ERR_BROKEN_PIPE = "Broken pipe";

    /**
     * Sends response headers to web server if not done before and writes all buffered bytes cut into AJP SEND_BODY_CHUNK packages to web
     * server.
     * 
     * @throws IOException If an I/O error occurs
     */
    private void responseToWebServer() throws IOException {
        try {
            final OutputStream out = ajpCon.getOutputStream();
            /*
             * Ensure headers are written first
             */
            ajpCon.getAjpRequestHandler().doWriteHeaders(out);
            /*
             * Send data cut into MAX_BODY_CHUNK_SIZE pieces
             */
            while (byteBuffer.size() > AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE) {
                out.write(AJPv13Response.getSendBodyChunkBytes(byteBuffer.toByteArray(0, AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE)));
                out.flush();
                byteBuffer.discard(AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE);
                /*-
                 * final byte[] currentData = new byte[AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE];
                 * final byte[] tmp = new byte[byteBuffer.size() - AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE];
                 * final byte[] bufferedBytes = byteBuffer.toByteArray();
                 * System.arraycopy(bufferedBytes, 0, currentData, 0, currentData.length);
                 * System.arraycopy(bufferedBytes, currentData.length, tmp, 0, tmp.length);
                 * ajpCon.getOutputStream().write(AJPv13Response.getSendBodyChunkBytes(currentData));
                 * ajpCon.getOutputStream().flush();
                 * byteBuffer.reset();
                 * byteBuffer.write(tmp, 0, tmp.length);
                 */
            }
            if (byteBuffer.size() > 0) {
                out.write(AJPv13Response.getSendBodyChunkBytes(byteBuffer.toByteArray()));
                out.flush();
            }
            /*
             * Since we do not expect any answer here, request handler's processPackage() method need not to be called.
             */
            byteBuffer.reset();
        } catch (final SocketException e) {
            if (e.getMessage().indexOf(ERR_BROKEN_PIPE) == -1) {
                LOG.error(e.getMessage(), e);
            } else {
                LOG.warn(new StringBuilder("Underlying (TCP) protocol communication aborted: ").append(e.getMessage()).toString(), e);
            }
            ajpCon.discardAll();
            final IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            final IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    /**
     * Flushes the byte buffer into stream.
     * 
     * @throws IOException If an I/O error occurs
     */
    public void flushByteBuffer() throws IOException {
        final Lock l = synchronizer.acquire();
        try {
            if (isClosed) {
                throw new IOException(ERR_OUTPUT_CLOSED);
            }
            responseToWebServer();
        } finally {
            synchronizer.release(l);
        }
    }

    /**
     * Clears the byte buffer.
     * 
     * @throws IOException If an I/O error occurs
     */
    public void clearByteBuffer() throws IOException {
        final Lock l = synchronizer.acquire();
        try {
            if (isClosed) {
                throw new IOException(ERR_OUTPUT_CLOSED);
            }
            byteBuffer.reset();
        } finally {
            synchronizer.release(l);
        }
    }

    public void synchronize() {
        synchronizer.synchronize();
    }

    public void unsynchronize() {
        synchronizer.unsynchronize();
    }

}
