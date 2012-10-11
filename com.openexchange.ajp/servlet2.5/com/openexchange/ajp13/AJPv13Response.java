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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13Exception.AJPCode;
import com.openexchange.ajp13.exception.AJPv13MaxPackgeSizeException;
import com.openexchange.ajp13.servlet.http.HttpServletRequestWrapper;
import com.openexchange.ajp13.servlet.http.HttpServletResponseWrapper;
import com.openexchange.log.Log;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link AJPv13Response} - Constructs AJP response packages for <code>END_RESPONSE</code>, <code>SEND_BODY_CHUNK</code>,
 * <code>SEND_HEADERS</code>, <code>GET_BODY_CHUNK</code>, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJPv13Response {

    private static final String STR_EMPTY = "";

    private static final String STR_SET_COOKIE = "Set-Cookie";

    /**
     * The max. int value that can be written to AJP package.
     */
    public static final int MAX_INT_VALUE = 65535;

    /**
     * The max. size of an AJP package.
     */
    public static final int MAX_PACKAGE_SIZE = 8192;

    /**
     * The max. allowed chunk size in a SEND_BODY_CHUNK package, which is the max. package size of 8192 (8K) minus 8 bytes (MagicBytes (2) +
     * DataLength (2) + PrefixCode (1) + CunkLength (2) + TerminatingZeroByte (1))
     *
     * <pre>
     * 'A' 'B' Data-Length(2) Prefix-Code(1) Chunk-Length(2) [chunk bytes] '00'
     * </pre>
     */
    public static final int MAX_SEND_BODY_CHUNK_SIZE = 8184; // 8192 - 8

    /**
     * Byte sequence indicating a packet from Servlet Container to Web Server.
     */
    private static final byte[] PACKAGE_FROM_CONTAINER_TO_SERVER = { (byte) 'A', (byte) 'B' };

    private static final int SEND_BODY_CHUNK_PREFIX_CODE = 3;

    private static final int SEND_HEADERS_PREFIX_CODE = 4;

    private static final int END_RESPONSE_PREFIX_CODE = 5;

    private static final int GET_BODY_CHUNK_PREFIX_CODE = 6;

    private static final int CPONG_REPLY_PREFIX_CODE = 9;

    private static final Map<String, Integer> HEADER_MAP;

    private static final byte[] CPONG_RESPONSE_BYTES;

    /**
     * Starting first 4 bytes:
     *
     * <pre>
     * 'A' + 'B' + [data length as 2 byte integer]
     * </pre>
     */
    private static final int RESPONSE_PREFIX_LENGTH = 4;

    static {
        /*
         * Headers
         */
        HEADER_MAP = new HashMap<String, Integer>(11);
        HEADER_MAP.put("Content-Type", Integer.valueOf(0x01));
        HEADER_MAP.put("Content-Language", Integer.valueOf(0x02));
        HEADER_MAP.put("Content-Length", Integer.valueOf(0x03));
        HEADER_MAP.put("Date", Integer.valueOf(0x04));
        HEADER_MAP.put("Last-Modified", Integer.valueOf(0x05));
        HEADER_MAP.put("Location", Integer.valueOf(0x06));
        HEADER_MAP.put("Set-Cookie", Integer.valueOf(0x07));
        HEADER_MAP.put("Set-Cookie2", Integer.valueOf(0x08));
        HEADER_MAP.put("Servlet-Engine", Integer.valueOf(0x09));
        HEADER_MAP.put("Status", Integer.valueOf(0x0A));
        HEADER_MAP.put("WWW-Authenticate", Integer.valueOf(0x0B));
        /*
         * CPong reply
         */
        CPONG_RESPONSE_BYTES = new byte[5];
        CPONG_RESPONSE_BYTES[0] = PACKAGE_FROM_CONTAINER_TO_SERVER[0];
        CPONG_RESPONSE_BYTES[1] = PACKAGE_FROM_CONTAINER_TO_SERVER[1];
        CPONG_RESPONSE_BYTES[2] = 0;
        CPONG_RESPONSE_BYTES[3] = 1;
        CPONG_RESPONSE_BYTES[4] = CPONG_REPLY_PREFIX_CODE;
    }

    private final int prefixCode;

    private int contentLength = -1;

    private byte[] responseDataChunk;

    private HttpServletResponseWrapper servletResponse;

    private boolean closeConnection;

    /**
     * Initializes a new {@link AJPv13Response}.
     *
     * @param prefixCode The prefix code determining kind of response package
     */
    public AJPv13Response(final int prefixCode) {
        super();
        this.prefixCode = prefixCode;
    }

    /**
     * Constructor for <code>END_RESPONSE</code>.
     *
     * @param prefixCode - the <code>END_RESPONSE</code> prefix code
     * @param closeConnection - whether or not to signal to close the connection
     */
    public AJPv13Response(final int prefixCode, final boolean closeConnection) {
        super();
        this.prefixCode = prefixCode;
        this.closeConnection = closeConnection;
    }

    /**
     * Constructor for <code>SEND_BODY_CHUNK</code>.
     *
     * @param prefixCode - the <code>SEND_BODY_CHUNK</code> prefix code
     * @param responseDataChunk - the data chunk as array of <code>byte</code>
     */
    public AJPv13Response(final int prefixCode, final byte[] responseDataChunk) {
        super();
        this.prefixCode = prefixCode;
        this.responseDataChunk = new byte[responseDataChunk.length];
        System.arraycopy(responseDataChunk, 0, this.responseDataChunk, 0, responseDataChunk.length);
    }

    /**
     * Constructor for <code>SEND_HEADERS</code>.
     *
     * @param prefixCode - the <code>SEND_HEADERS</code> prefix code
     * @param resp - the <code>HttpServletResponse</code> object containing http header data
     */
    public AJPv13Response(final int prefixCode, final HttpServletResponseWrapper resp) {
        super();
        this.prefixCode = prefixCode;
        servletResponse = resp;
    }

    /**
     * Constructor for <code>GET_BODY_CHUNK</code>.
     *
     * @param prefixCode - the <code>GET_BODY_CHUNK</code> prefix code
     * @param requestedLength - the requested body chunk's length
     */
    public AJPv13Response(final int prefixCode, final int requestedLength) {
        super();
        this.prefixCode = prefixCode;
        contentLength = requestedLength;
    }

    /**
     * Gets the response bytes corresponding to this AJP response.
     *
     * @return The response bytes
     * @throws AJPv13Exception If response bytes cannot be created
     */
    public final byte[] getResponseBytes() throws AJPv13Exception {
        switch (prefixCode) {
        case SEND_BODY_CHUNK_PREFIX_CODE:
            return getSendBodyChunkBytes(responseDataChunk);
        case SEND_HEADERS_PREFIX_CODE:
            return getSendHeadersBytes(servletResponse);
        case END_RESPONSE_PREFIX_CODE:
            return getEndResponseBytes(closeConnection);
        case GET_BODY_CHUNK_PREFIX_CODE:
            return getGetBodyChunkBytes(contentLength);
        case CPONG_REPLY_PREFIX_CODE:
            return getCPongBytes();
        default:
            throw new AJPv13Exception(AJPCode.UNKNOWN_PREFIX_CODE, true, Integer.valueOf(prefixCode));
        }
    }

    /*-
     * +++++++++++++++++++++++++ Static package building methods +++++++++++++++++++++++++
     */

    /**
     * Creates the <code>SEND_BODY_CHUNK</code> response bytes
     *
     * @param responseDataChunk - the data chunk
     * @return an array of <code>byte</code> containing the <code>SEND_BODY_CHUNK</code> response bytes
     * @throws AJPv13Exception If <code>SEND_BODY_CHUNK</code> response bytes cannot be created.
     */
    public static final byte[] getSendBodyChunkBytes(final byte[] responseDataChunk) throws AJPv13Exception {
        return getSendBodyChunkBytes(responseDataChunk, 0, responseDataChunk.length);
    }

    /**
     * Data length of SEND_BODY_CHUNK:
     *
     * <pre>
     * prefix (1) + chunk_length (2) + terminating zero byte (1)
     * </pre>
     */
    private static final int SEND_BODY_CHUNK_LENGTH = 4;

    /**
     * Creates the <code>SEND_BODY_CHUNK</code> response bytes.
     *
     * @param responseDataChunk The data chunk
     * @param off The start offset in data chunk
     * @param len The number of bytes to write
     * @return an array of <code>byte</code> containing the <code>SEND_BODY_CHUNK</code> response bytes
     * @throws AJPv13Exception If code>SEND_BODY_CHUNK</code> response bytes cannot be created.
     */
    public static final byte[] getSendBodyChunkBytes(final byte[] responseDataChunk, final int off, final int len) throws AJPv13Exception {
        if (len < 0) {
            throw new AJPv13Exception(AJPCode.NO_EMPTY_SENT_BODY_CHUNK, true);
        }
        /*
         * prefix + chunk_length (2 bytes) + chunk bytes + terminating zero byte
         */
        final int dataLength = SEND_BODY_CHUNK_LENGTH + len;
        final int total = dataLength + RESPONSE_PREFIX_LENGTH;
        if (total > MAX_PACKAGE_SIZE) {
            throw new AJPv13MaxPackgeSizeException((total));
        }
        final ByteArrayOutputStream sink = new UnsynchronizedByteArrayOutputStream(MAX_PACKAGE_SIZE);
        fillStartBytes(SEND_BODY_CHUNK_PREFIX_CODE, dataLength, sink);
        writeInt(len, sink);
        writeByteArray(responseDataChunk, off, len, sink);
        writeByte(0, sink);
        return sink.toByteArray();
    }

    /**
     * Data length of SEND_BODY_CHUNK:
     *
     * <pre>
     * prefix(1) + http_status_code(2) + http_status_msg(3) + num_headers(2)
     * </pre>
     */
    private static final int SEND_HEADERS_LENGTH = 8;

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
     * @return The header name
     */
    public static String getEchoHeaderName() {
        return ECHO_HEADER_NAME.get();
    }

    /**
     * Creates the <code>SEND_HEADERS</code> response bytes.
     *
     * @param servletResponse - the <code>HttpServletResponse</code> object containing http header data
     * @return an array of <code>byte</code> containing the <code>SEND_HEADERS</code> response bytes
     * @throws AJPv13Exception If code>SEND_HEADERS</code> response bytes cannot be created.
     */
    public static final byte[] getSendHeadersBytes(final com.openexchange.ajp13.servlet.http.HttpServletResponseWrapper servletResponse) throws AJPv13Exception {
        /*
         * prefix + http_status_code + http_status_msg (empty string) + num_headers (integer)
         */
        String statusMsg = servletResponse.getStatusMsg();
        if (null == statusMsg) {
            statusMsg = "";
        }
        /*-
         * Check for echo header presence
         */
        final String echoHeaderName = ECHO_HEADER_NAME.get();
        if (null != echoHeaderName) {
            final HttpServletRequestWrapper request = servletResponse.getRequest();
            if (null != request) {
                final String echoValue = request.getHeader(echoHeaderName);
                if (null != echoValue) {
                    servletResponse.setHeader(echoHeaderName, echoValue);
                }
            }
        }
        /*
         * Write to sink
         */
        final ByteArrayOutputStream sink = new UnsynchronizedByteArrayOutputStream(MAX_PACKAGE_SIZE);
        final int headersSize = servletResponse.getHeadersSize();
        final byte[] headers;
        {
            sink.reset();
            final Iterator<String> iterator = servletResponse.getHeaderNames();
            for (int i = 0; i < headersSize; i++) {
                final String headerName = iterator.next();
                writeHeader(headerName, servletResponse.getHeader(headerName), sink);
            }
            headers = sink.toByteArray();
        }
        final byte[] cookies;
        final int numOfCookieHdrs;
        {
            sink.reset();
            final String[][] formattedCookies = servletResponse.getFormatedCookies();
            if (formattedCookies.length > 0) {
                for (int j = 0; j < formattedCookies[0].length; j++) {
                    writeHeader(STR_SET_COOKIE, formattedCookies[0][j], sink);
                }
                if (formattedCookies.length > 1) {
                    final StringBuilder sb = new StringBuilder(STR_SET_COOKIE.length() + 1);
                    for (int i = 1; i < formattedCookies.length; i++) {
                        sb.setLength(0);
                        final String hdrName = sb.append(STR_SET_COOKIE).append(i + 1).toString();
                        for (int j = 0; j < formattedCookies[i].length; j++) {
                            writeHeader(hdrName, formattedCookies[i][j], sink);
                        }
                    }
                }
            }
            cookies = sink.toByteArray();
            numOfCookieHdrs = getNumOfCookieHeader(formattedCookies);
        }
        final int dataLength = SEND_HEADERS_LENGTH + headers.length + cookies.length + statusMsg.length();
        if (dataLength + RESPONSE_PREFIX_LENGTH > MAX_PACKAGE_SIZE) {
            throw new AJPv13MaxPackgeSizeException((dataLength + RESPONSE_PREFIX_LENGTH));
        }
        sink.reset();
        fillStartBytes(SEND_HEADERS_PREFIX_CODE, dataLength, sink);
        writeInt(servletResponse.getStatus(), sink);
        writeString(statusMsg, sink);
        writeInt(headersSize + numOfCookieHdrs, sink);
        writeByteArray(headers, sink);
        writeByteArray(cookies, sink);
        return sink.toByteArray();
    }

    /**
     * Creates the <code>END_RESPONSE</code> response bytes.
     *
     * @return an array of <code>byte</code> containing the <code>END_RESPONSE</code> response bytes
     */
    public static final byte[] getEndResponseBytes() {
        return getEndResponseBytes(false);
    }

    /**
     * Data length of END_RESPONSE:
     *
     * <pre>
     * prefix(1) + close_connection_boolean(1)
     * </pre>
     */
    private static final int END_RESPONSE_LENGTH = 2;

    private static final byte[] END_RESPONSE_REUSE;

    private static final byte[] END_RESPONSE_CLOSE;

    static {
        final ByteArrayOutputStream sink = new UnsynchronizedByteArrayOutputStream(6);
        sink.write('A');
        sink.write('B');
        final int dataLength = END_RESPONSE_LENGTH;
        sink.write((dataLength >> 8)); // high
        sink.write((dataLength & (255))); // low
        sink.write(END_RESPONSE_PREFIX_CODE);
        sink.write(1);
        END_RESPONSE_REUSE = sink.toByteArray();
        sink.reset();
        sink.write('A');
        sink.write('B');
        sink.write((dataLength >> 8)); // high
        sink.write((dataLength & (255))); // low
        sink.write(END_RESPONSE_PREFIX_CODE);
        sink.write(0);
        END_RESPONSE_CLOSE = sink.toByteArray();
    }

    /**
     * Creates the <code>END_RESPONSE</code> response bytes.
     *
     * @param closeConnection - whether or not to signal connection closure
     * @return an array of <code>byte</code> containing the <code>END_RESPONSE</code> response bytes
     */
    public static final byte[] getEndResponseBytes(final boolean closeConnection) {
        /*
         * No need to check against max package size cause it's a static package size of 6
         */
        return closeConnection ? END_RESPONSE_CLOSE : END_RESPONSE_REUSE;
    }

    /**
     * Data length of GET_BODY_CHUNK:
     *
     * <pre>
     * prefix(1) + requested_size_as_integer(2)
     * </pre>
     */
    private static final int GET_BODY_CHUNK_LENGTH = 3;

    /**
     * Creates the <code>GET_BODY_CHUNK</code> response bytes.
     *
     * @param requestedLength - the requested chunk's size
     * @return an array of <code>byte</code> containing the <code>GET_BODY_CHUNK</code> response bytes
     * @throws AJPv13Exception If <code>GET_BODY_CHUNK</code> response bytes cannot be created.
     */
    public static final byte[] getGetBodyChunkBytes(final int requestedLength) throws AJPv13Exception {
        /*
         * No need to check against max package size cause it's a static package size of 7
         */
        final ByteArrayOutputStream sink = new UnsynchronizedByteArrayOutputStream(GET_BODY_CHUNK_LENGTH + RESPONSE_PREFIX_LENGTH);
        fillStartBytes(GET_BODY_CHUNK_PREFIX_CODE, GET_BODY_CHUNK_LENGTH, sink);
        writeInt(requestedLength, sink);
        return sink.toByteArray();
    }

    /**
     * Creates the CPong response bytes.
     *
     * @return an array of <code>byte</code> containing the CPong response bytes
     */
    public static final byte[] getCPongBytes() {
        final byte[] response = new byte[CPONG_RESPONSE_BYTES.length];
        System.arraycopy(CPONG_RESPONSE_BYTES, 0, response, 0, response.length);
        return response;
    }

    /*-
     * +++++++++++++++++++++++++ Static helper methods +++++++++++++++++++++++++
     */

    private static final int getHeaderSizeInBytes(final Set<Map.Entry<String, String[]>> set) {
        int retval = 0;
        final StringBuilder sb = new StringBuilder(128);
        for (final Map.Entry<String, String[]> hdr : set) {
            final String name = hdr.getKey();
            if (HEADER_MAP.containsKey(name)) {
                /*
                 * Header can be encoded as an integer
                 */
                retval += 2;
            } else {
                /*
                 * Header must be written as string which takes three extra bytes in addition to header name length
                 */
                retval += name.length() + 3;
            }
            sb.setLength(0);
            retval += array2string(hdr.getValue(), sb).length() + 3;
        }
        return retval;
    }

    private static final String array2string(final String[] sa, final StringBuilder sb) {
        if (sa == null || sa.length == 0) {
            return STR_EMPTY;
        }
        sb.append(sa[0]);
        for (int i = 1; i < sa.length; i++) {
            sb.append(',');
            sb.append(sa[i]);
        }
        return sb.toString();
    }

    private static final int getCookiesSizeInBytes(final String[][] formattedCookies) {
        int retval = 0;
        for (int i = 0; i < formattedCookies.length; i++) {
            final int hdrNameLen;
            {
                final String hdrName = i == 0 ? STR_SET_COOKIE : new StringBuilder(STR_SET_COOKIE.length() + 1).append(STR_SET_COOKIE).append(
                    i + 1).toString();
                /*
                 * Set-Cookie and Set-Cookie2 is encoded in AJP protocol as an integer value
                 */
                hdrNameLen = HEADER_MAP.containsKey(hdrName) ? 2 : hdrName.length() + 3;
            }
            for (int j = 0; j < formattedCookies[i].length; j++) {
                retval += hdrNameLen;
                retval += formattedCookies[i][j].length() + 3;
            }
        }
        return retval;
    }

    private static final int getNumOfCookieHeader(final String[][] formattedCookies) {
        int retval = 0;
        for (final String[] formattedCookie : formattedCookies) {
            retval += formattedCookie.length;
        }
        return retval;
    }

//    private static void writeHeader(final String name, final String value, final ByteArrayOutputStream byteArray) throws AJPv13Exception {
//        if (HEADER_MAP.containsKey(name)) {
//            final int code = (0xA0 << 8) + (HEADER_MAP.get(name)).intValue();
//            writeInt(code, byteArray);
//        } else {
//            writeString(name, byteArray);
//        }
//        writeString(value, byteArray);
//    }

    private static void writeHeader(final String name, final String value, final ByteArrayOutputStream sink) throws AJPv13Exception {
        if (HEADER_MAP.containsKey(name)) {
            final int code = (0xA0 << 8) + (HEADER_MAP.get(name)).intValue();
            writeInt(code, sink);
        } else {
            writeString(name, sink);
        }
        writeString(value, sink);
    }

    /**
     * Writes specified header to given byte sink.
     *
     * @param name The header name
     * @param value The header value
     * @param sink The byte sink
     */
    public static void writeHeaderSafe(final String name, final String value, final ByteArrayOutputStream sink) {
        try {
            writeHeader(name, value, sink);
        } catch (final AJPv13Exception e) {
            Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13Response.class)).error(e.getMessage(), e);
        }
    }

    private static int writeHeader(final String name, final String value, final byte[] byteArray, final int count) throws AJPv13Exception {
        int c = count;
        if (HEADER_MAP.containsKey(name)) {
            final int code = (0xA0 << 8) + (HEADER_MAP.get(name)).intValue();
            c = writeInt(code, byteArray, c);
        } else {
            c = writeString(name, byteArray, c);
        }
        c = writeString(value, byteArray, c);
        return c;
    }

//    /**
//     * Writes the first 5 bytes of an AJP response:
//     * <ol>
//     * <li>Two bytes signaling a package from container to web server: <tt>A</tt> <tt>B</tt></li>
//     * <li>The data length as an integer (takes two bytes)</li>
//     * <li>The response's prefix code</li>
//     * </ol>
//     *
//     * @throws AJPv13Exception If starting bytes cannot be written
//     */
//    private static final void fillStartBytes(final int prefixCode, final int dataLength, final ByteArrayOutputStream byteArray) throws AJPv13Exception {
//        writeByte(PACKAGE_FROM_CONTAINER_TO_SERVER[0], byteArray);
//        writeByte(PACKAGE_FROM_CONTAINER_TO_SERVER[1], byteArray);
//        writeInt(dataLength, byteArray);
//        writeByte(prefixCode, byteArray);
//    }

    public static final void fillStartBytes(final int prefixCode, final int dataLength, final ByteArrayOutputStream sink) throws AJPv13Exception {
        sink.write(PACKAGE_FROM_CONTAINER_TO_SERVER, 0, 2);
        writeInt(dataLength, sink);
        writeByte(prefixCode, sink);
    }

    /**
     * Writes the first 5 bytes of an AJP response:
     * <ol>
     * <li>Two bytes signaling a package from container to web server: <tt>A</tt> <tt>B</tt></li>
     * <li>The data length as an integer (takes two bytes)</li>
     * <li>The response's prefix code</li>
     * </ol>
     *
     * @throws AJPv13Exception If starting bytes cannot be written
     */
    private static final int fillStartBytes(final int prefixCode, final int dataLength, final byte[] byteArray) throws AJPv13Exception {
        byteArray[0] = PACKAGE_FROM_CONTAINER_TO_SERVER[0];
        byteArray[1] = PACKAGE_FROM_CONTAINER_TO_SERVER[1];
        int c = writeInt(dataLength, byteArray, 2);
        c = writeByte(prefixCode, byteArray, c);
        return c;
    }

//    private static final void writeByte(final int byteValue, final ByteArrayOutputStream byteArray) {
//        byteArray.write(byteValue);
//    }

    private static final void writeByte(final int byteValue, final ByteArrayOutputStream sink) {
        sink.write(byteValue);
    }

    private static final int writeByte(final int byteValue, final byte[] byteArray, final int count) {
        byteArray[count] = (byte) byteValue;
        return count + 1;
    }

//    private static final void writeByteArray(final byte[] bytes, final ByteArrayOutputStream byteArray) {
//        byteArray.write(bytes, 0, bytes.length);
//    }

    public static final void writeByteArray(final byte[] bytes, final ByteArrayOutputStream sink) {
        sink.write(bytes, 0, bytes.length);
    }

    private static final int writeByteArray(final byte[] bytes, final byte[] byteArray, final int count) {
        System.arraycopy(bytes, 0, byteArray, count, bytes.length);
        return count + bytes.length;
    }

    private static final void writeByteArray(final byte[] bytes, final int off, final int len, final ByteArrayOutputStream sink) {
        sink.write(bytes, off, len);
    }

    private static final int writeByteArray(final byte[] bytes, final int off, final int len, final byte[] byteArray, final int count) {
        System.arraycopy(bytes, off, byteArray, count, len);
        return count + len;
    }

//    private static final void writeInt(final int intValue, final ByteArrayOutputStream byteArray) throws AJPv13Exception {
//        if (intValue > MAX_INT_VALUE) {
//            throw new AJPv13Exception(AJPCode.INTEGER_VALUE_TOO_BIG, true, Integer.valueOf(intValue));
//        }
//        byteArray.write((intValue >> 8)); // high
//        byteArray.write((intValue & (255))); // low
//    }

    public static final void writeInt(final int intValue, final ByteArrayOutputStream sink) throws AJPv13Exception {
        if (intValue > MAX_INT_VALUE) {
            throw new AJPv13Exception(AJPCode.INTEGER_VALUE_TOO_BIG, true, Integer.valueOf(intValue));
        }
        sink.write((intValue >> 8)); // high
        sink.write((intValue & (255))); // low
    }

    private static final int writeInt(final int intValue, final byte[] byteArray, final int count) throws AJPv13Exception {
        if (intValue > MAX_INT_VALUE) {
            throw new AJPv13Exception(AJPCode.INTEGER_VALUE_TOO_BIG, true, Integer.valueOf(intValue));
        }
        byteArray[count] = (byte) (intValue >> 8); // high
        byteArray[count + 1] = (byte) (intValue & (255)); // low
        return count + 2;
    }

//    private static final void writeBoolean(final boolean boolValue, final ByteArrayOutputStream byteArray) {
//        byteArray.write(boolValue ? 1 : 0);
//    }

    private static final void writeBoolean(final boolean boolValue, final ByteArrayOutputStream sink) {
        sink.write((boolValue ? 1 : 0));
    }

    private static final int writeBoolean(final boolean boolValue, final byte[] byteArray, final int count) {
        byteArray[count] = (byte) (boolValue ? 1 : 0);
        return count + 1;
    }

//    private static final void writeString(final String strValue, final ByteArrayOutputStream byteArray) throws AJPv13Exception {
//        final int strLength = strValue.length();
//        writeInt(strLength, byteArray);
//        /*
//         * Write string content and terminating '0'
//         */
//        if (strLength > 0) {
//            final char[] chars = strValue.toCharArray();
//            final byte[] bytes = new byte[strLength];
//            for (int i = 0; i < strLength; i++) {
//                bytes[i] = (byte) chars[i];
//            }
//            byteArray.write(bytes, 0, strLength);
//        }
//        byteArray.write(0);
//    }

    public static final void writeString(final String strValue, final ByteArrayOutputStream sink) throws AJPv13Exception {
        final int strLength = strValue.length();
        writeInt(strLength, sink);
        /*
         * Write string content and terminating '0'
         */
        if (strLength > 0) {
            final char[] chars = strValue.toCharArray();
            for (int i = 0; i < strLength; i++) {
                sink.write((byte) chars[i]);
            }
        }
        sink.write(0);
    }

    private static final int writeString(final String strValue, final byte[] byteArray, final int count) throws AJPv13Exception {
        final int strLength = strValue.length();
        int c = writeInt(strLength, byteArray, count);
        /*
         * Write string content and terminating '0'
         */
        if (strLength > 0) {
            final char[] chars = strValue.toCharArray();
            final byte[] bytes = new byte[strLength];
            System.arraycopy(chars, 0, bytes, 0, strLength);
            /*for (int i = 0; i < strLength; i++) {
                bytes[i] = (byte) chars[i];
            }*/
            c = writeByteArray(bytes, byteArray, c);
        }
        byteArray[c] = 0;
        return c + 1;
    }
}
