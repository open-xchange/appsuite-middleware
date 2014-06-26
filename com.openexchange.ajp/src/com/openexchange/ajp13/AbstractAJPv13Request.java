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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import com.openexchange.ajp13.exception.AJPv13BrokenCycleException;
import com.openexchange.ajp13.exception.AJPv13Exception;

/**
 * {@link AbstractAJPv13Request} - Abstract super class for AJP requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractAJPv13Request implements AJPv13Request {

    private static final byte[] EMPTY_BYTES = new byte[0];

    /**
     * Max size of an incoming request body:<br>
     * 8192 (8K) - 4 bytes (0x12 + 0x34 + data length integer).
     *
     * @value 8188
     */
    protected final static int MAX_REQUEST_BODY_CHUNK_SIZE = 8188;

    private static final AtomicReference<String> ECHO_HEADER_NAME = new AtomicReference<String>();

    /**
     * Sets the name of the echo header.
     *
     * @param headerName The header name
     */
    public static void setEchoHeaderName(final String headerName) {
        ECHO_HEADER_NAME.set(headerName);
    }

    /**
     * Gets the name of the echo header.
     *
     * @return The name or <code>null</code> if not set
     */
    public static String getEchoHeaderName() {
        return ECHO_HEADER_NAME.get();
    }

    /**
     * The payload data.
     */
    protected final byte[] payloadData;

    /**
     * The current read position.
     */
    protected int pos;

    /**
     * The total data length.
     */
    protected final int dataLength;

    /**
     * Initializes a new {@link AbstractAJPv13Request}.
     *
     * @param payloadData The payload data
     * @param dataLength The AJP package's data length
     */
    protected AbstractAJPv13Request(final byte[] payloadData, final int dataLength) {
        super();
        this.payloadData = payloadData;
        this.dataLength = dataLength;
        pos = 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13Request#processRequest(com.openexchange.ajp13.AJPv13RequestHandler)
     */
    @Override
    public abstract void processRequest(AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, IOException;

    /* (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13Request#response(com.openexchange.ajp13.AJPv13RequestHandler)
     */
    @Override
    public void response(final AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, ServletException, IOException {
        final AJPv13Connection ajpConnection = ajpRequestHandler.getAJPConnection();
        if (!ajpRequestHandler.isServiceMethodCalled()) {
            /*
             * Ensure completeness in case of form data
             */
            if (ajpRequestHandler.isFormData()) {
                if (!ajpRequestHandler.isAllDataRead()) {
                    final BlockableBufferedOutputStream ajpOut = ajpConnection.getOutputStream();
                    ajpOut.acquire();
                    try {
                        do {
                            ajpOut.write(AJPv13Response.getGetBodyChunkBytes(ajpRequestHandler.getNumOfBytesToRequestFor()));
                            ajpOut.flush();
                            /*
                             * Trigger request handler to process expected incoming data package
                             */
                            ajpRequestHandler.processPackage();
                        } while (!ajpRequestHandler.isAllDataRead());
                    } finally {
                        ajpOut.release();
                    }
                }
                /*
                 * Turn form's post data into request parameters
                 */
                ajpRequestHandler.doParseQueryString(ajpRequestHandler.peekData());
            }
            /*-
             * Call servlet's service() method which will then request all receivable data chunks from client through servlet input stream
             *
             * TODO: javax.servlet.Filter invocation here
             */
            final String headerName = ECHO_HEADER_NAME.get();
            if (null != headerName) {
                final String echoValue = ajpRequestHandler.getServletRequest().getHeader(headerName);
                if (null != echoValue) {
                    ajpRequestHandler.getServletResponse().setHeader(headerName, echoValue);
                }
            }
            ajpRequestHandler.doServletService();
        }
        final BlockableBufferedOutputStream out = ajpConnection.getOutputStream();
        /*
         * Send response headers first.
         */
        ajpRequestHandler.doWriteHeaders(out);
        /*
         * Get remaining data
         */
        byte[] remainingData = null;
        try {
            remainingData = ajpRequestHandler.getAndClearResponseData();
        } catch (final IOException e) {
            remainingData = EMPTY_BYTES;
        }
        if (remainingData.length > 0) {
            /*
             * Send rest of data cut into MAX_BODY_CHUNK_SIZE pieces
             */
            writeChunked(remainingData, out);
        }
        /*-
         *
        else {
            writeEmpty(out);
        }
         */
        /*
         * Write END_RESPONSE package
         */
        writeResponse(AJPv13Response.getEndResponseBytes(), out, true);
        ajpRequestHandler.setEndResponseSent();
    }

    /**
     * Writes specified data chunked as SEND_BODY packages.
     *
     * @param data The data to write
     * @param out The AJP connection's output stream to write to
     * @throws IOException If an I/O error occurs
     * @throws AJPv13Exception If an AJP error occurs
     */
    public static void writeChunked(final byte[] data, final BlockableBufferedOutputStream out) throws IOException, AJPv13Exception {
        out.acquire();
        try {
            int offset = 0;
            final int maxLen = AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE;
            while (offset < data.length) {
                final int b = (data.length - offset);
                final int curLen = ((maxLen <= b) ? maxLen : b);
                out.write(AJPv13Response.getSendBodyChunkBytes(data, offset, curLen));
                out.flush();
                offset += curLen;
            }
        } finally {
            out.release();
        }
    }

    /**
     * Writes an empty SEND_BODY package.
     *
     * @param out The AJP connection's output stream to write to
     * @throws IOException If an I/O error occurs
     * @throws AJPv13Exception If an AJP error occurs
     */
    public static void writeEmpty(final BlockableBufferedOutputStream out) throws IOException, AJPv13Exception {
        out.acquire();
        try {
            out.write(AJPv13Response.getSendBodyChunkBytes(EMPTY_BYTES));
            out.flush();
        } finally {
            out.release();
        }
    }

    /**
     * Writes specified bytes into given output stream.
     *
     * @param responseBytes The bytes to write
     * @param out The output stream to write to
     * @param flushStream Whether to flush the output stream
     * @throws IOException If an I/O error occurs
     */
    protected static final void writeResponse(final byte[] responseBytes, final BlockableBufferedOutputStream out, final boolean flushStream) throws IOException {
        out.acquire();
        try {
            out.write(responseBytes);
            if (flushStream) {
                out.flush();
            }
        } finally {
            out.release();
        }
    }

    /**
     * Writes specified AJP response into given output stream.
     *
     * @param response The AJP response
     * @param out The output stream to write to
     * @param flushStream Whether to flush the output stream
     * @throws IOException If an I/O error occurs
     * @throws AJPv13Exception If an AJP error occurs
     */
    protected static final void writeResponse(final AJPv13Response response, final BlockableBufferedOutputStream out, final boolean flushStream) throws IOException, AJPv13Exception {
        writeResponse(response.getResponseBytes(), out, flushStream);
    }

    /**
     * Checks if payload data is <code>null</code>.
     *
     * @return <code>true</code> if payload data is <code>null</code>; otherwise <code>false</code>
     */
    protected final boolean isPayloadNull() {
        return payloadData == null;
    }

    /**
     * Gets the payload data length.
     *
     * @return The payload data length
     */
    protected final int getPayloadLength() {
        return payloadData.length;
    }

    /**
     * Parses the next <code>int</code> value (which consumes next two bytes).
     *
     * @return The next <code>int</code> value
     */
    protected final int parseInt() {
        return ((payloadData[pos++] & 0xff) << 8) + (payloadData[pos++] & 0xff);
    }

    /**
     * Gets the next bytes (which consumes next <code>numOfBytes</code> bytes).
     *
     * @param numOfBytes The number of bytes to return
     * @return The next bytes
     * @throws AJPv13Exception If a broken AJP cycle is detected
     */
    protected final byte[] getByteSequence(final int numOfBytes) throws AJPv13Exception {
        try {
            final byte[] retval = new byte[numOfBytes];
            final int available = payloadData.length - pos;
            System.arraycopy(payloadData, pos, retval, 0, numOfBytes > available ? available : numOfBytes);
            pos += numOfBytes;
            return retval;
        } catch (final IndexOutOfBoundsException e) {
            final AJPv13BrokenCycleException ajpExc = new AJPv13BrokenCycleException().setPayload(payloadData);
            /*
             * Dump package
             */
            final byte[] payload = payloadData;
            final byte[] clonedPackage = new byte[payload.length + 4];
            clonedPackage[0] = 0x12;
            clonedPackage[1] = 0x34;
            clonedPackage[2] = (byte) (dataLength >> 8);
            clonedPackage[3] = (byte) (dataLength & (255));
            System.arraycopy(payload, 0, clonedPackage, 4, payload.length);
            ajpExc.setDump(AJPv13Utility.dumpBytes(clonedPackage));
            throw ajpExc;
        }
    }

    /**
     * Gets the next <i>unsigned</i> byte.
     *
     * @return The next <i>unsigned</i> byte
     */
    protected final int nextByte() {
        return (payloadData[pos++] & 0xff);
    }

    /**
     * Compares next available unsigned byte with given value.
     *
     * @param compareTo The value to compare to
     * @return <code>true</code> if next available unsigned byte is equal to given value; otherwise <code>false</code>
     */
    protected final boolean compareNextByte(final int compareTo) {
        if (hasNext()) {
            return ((payloadData[pos] & 0xff) == compareTo);
        }
        return false;
    }

    /**
     * Checks if there's another byte available.
     *
     * @return <code>true</code> if there's another byte available; otherwise <code>false</code>
     */
    protected final boolean hasNext() {
        return (pos < payloadData.length);
    }

}
