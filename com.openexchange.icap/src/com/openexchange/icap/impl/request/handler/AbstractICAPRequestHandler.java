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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.icap.impl.request.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.icap.ICAPCommons;
import com.openexchange.icap.ICAPCommunicationStrings;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.ICAPResponse.Builder;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * {@link AbstractICAPRequestHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
abstract class AbstractICAPRequestHandler implements ICAPRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractICAPRequestHandler.class);
    static final int BUFFER_SIZE = 8192;

    /**
     * {@link Terminator} - The different terminator byte sequences for an ICAP packet.
     */
    private enum Terminator {
        HTTP_HEADER(ICAPCommunicationStrings.ICAP_TERMINATOR.getBytes(Charsets.UTF_8)),
        ICAP_HEADER(ICAPCommunicationStrings.ICAP_TERMINATOR.getBytes(Charsets.UTF_8)),
        HTTP_BODY(ICAPCommunicationStrings.HTTP_TERMINATOR.getBytes(Charsets.UTF_8));

        private byte[] terminator;

        /**
         * Initialises a new {@link Terminator}.
         * 
         * @param terminator The byte sequence of the terminator
         */
        private Terminator(byte[] terminator) {
            this.terminator = terminator;
        }

        /**
         * Returns the byte sequence of the terminator
         * 
         * @return the byte sequence of the terminator
         */
        byte[] getBytes() {
            return terminator;
        }

    }

    /**
     * Defines the size of the smallest possible message i.e. "ICAP/1.0 xxx "
     */
    private static final int MIN_MESSAGE_SIZE = 13;

    /**
     * Initialises a new {@link AbstractICAPRequestHandler}.
     */
    public AbstractICAPRequestHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.icap.request.handler.ICAPRequestHandler#handle(com.openexchange.icap.ICAPRequest, java.net.Socket)
     */
    @Override
    public ICAPResponse handle(ICAPRequest request, Socket socket) throws IOException {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream()); DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());) {
            return handle(request, socket, inputStream, outputStream);
        }
    }

    /**
     * Handles/Executes the specified {@link ICAPRequest}, i.e. sending it over the wire and performing
     * all necessary tasks to yield an {@link ICAPResponse}.
     * 
     * @param request The {@link ICAPRequest} to handle/execute
     * @param socket The socket with the open connection to the ICAP server
     * @param inputStream The {@link InputStream}, i.e. the stream for data coming from the ICAP server
     * @param outputStream The {@link OutputStream}, i.e. the stream for data going to the ICAP server
     * @return The {@link ICAPResponse} of the ICAP server
     * @throws IOException if an I/O error is occurred
     */
    abstract ICAPResponse handle(ICAPRequest request, Socket socket, InputStream inputStream, OutputStream outputStream) throws IOException;

    /**
     * Sends/writes the specified text data to the specified {@link OutputStream}
     * 
     * @param outputStream The {@link OutputStream} to write the data to
     * @param data The text data to write
     * @throws IOException if an I/O error is occurred
     */
    void sendData(OutputStream outputStream, String data) throws IOException {
        LOG.debug("Sending text data: {}", data);
        outputStream.write(data.getBytes(Charsets.UTF_8));
        LOG.debug("Done");
    }

    /**
     * Sends/writes the specified binary data to the specified {@link OutputStream}
     * 
     * @param outputStream The {@link OutputStream} to write the data to
     * @param data The binary data to write
     * @throws IOException if an I/O error is occurred
     */
    void sendData(OutputStream outputStream, byte[] data) throws IOException {
        LOG.debug("Sending binary data...");
        outputStream.write(data);
        LOG.debug("Done");
    }

    /**
     * Stream the data in chunks with size of {@link AbstractICAPRequestHandler#BUFFER_SIZE}
     * 
     * @param inputStream The {@link InputStream} that contains the data
     * @param outputStream The {@link OutputStream} to stream the data to
     * @throws IOException if an I/O error is occurred
     */
    void streamData(InputStream inputStream, OutputStream outputStream, long dataSize) throws IOException {
        int readBytes = 0;
        int readSize = BUFFER_SIZE;
        long remainingData = dataSize;
        byte[] buffer = new byte[BUFFER_SIZE];
        while (remainingData > 0 && (readBytes = inputStream.read(buffer, 0, readSize)) != -1) {
            remainingData -= readBytes;
            readSize = remainingData >= BUFFER_SIZE ? BUFFER_SIZE : (int) remainingData;
            sendPacket(readBytes < BUFFER_SIZE ? Arrays.copyOf(buffer, readBytes) : buffer, outputStream);
        }
    }

    /**
     * Stream the data in chunks with size of {@link AbstractICAPRequestHandler#BUFFER_SIZE}
     * 
     * @param inputStream The {@link InputStream} that contains the data
     * @param outputStream The {@link OutputStream} to stream the data to
     * @throws IOException if an I/O error is occurred
     */
    void streamData(InputStream inputStream, OutputStream outputStream) throws IOException {
        int readBytes = 0;
        int readSize = BUFFER_SIZE;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((readBytes = inputStream.read(buffer, 0, readSize)) != -1) {
            sendPacket(readBytes < BUFFER_SIZE ? Arrays.copyOf(buffer, readBytes) : buffer, outputStream);
        }
    }

    /**
     * Sends the specified packet along with the packet length in hexadecimal.
     * 
     * @param packet The byte packet to send
     * @param outputStream The {@link OutputStream} to send that packet to
     * @throws IOException if an I/O error is occurred
     */
    void sendPacket(byte[] packet, OutputStream outputStream) throws IOException {
        sendData(outputStream, Integer.toHexString(packet.length) + ICAPCommunicationStrings.CRLF);
        sendData(outputStream, packet);
        sendData(outputStream, ICAPCommunicationStrings.CRLF);
    }

    /**
     * Prepares the request method and builds the request string in form of:
     * <pre>
     * ICAP_METHOD icap://server:port/service ICAP/1.0
     * </pre>
     * 
     * @param request The {@link ICAPRequest} from which to construct the request string
     * @param requestBuilder The {@link StringBuilder} to build the request string
     */
    void prepareRequestMethod(ICAPRequest request, StringBuilder requestBuilder) {
        requestBuilder.append(request.getMethod().name()).append(' ');
        requestBuilder.append("icap://").append(request.getServer());
        if (request.getPort() != ICAPCommons.DEFAULT_PORT) {
            requestBuilder.append(request.getPort());
        }
        requestBuilder.append('/').append(request.getService());
        requestBuilder.append(" ICAP/").append(ICAPCommons.ICAP_VERSION).append(ICAPCommunicationStrings.CRLF);
    }

    /**
     * Prepares the request headers.
     * 
     * @param request The {@link ICAPRequest} from which to prepare the headers
     * @param requestBuilder The {@link StringBuilder} to concatenate the request headers
     */
    void prepareRequestHeaders(ICAPRequest request, StringBuilder requestBuilder) {
        for (Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(ICAPCommunicationStrings.CRLF);
        }
    }

    /**
     * Appends an empty line to indicate the end of the header fields.
     * 
     * @param requestBuilder The request builder
     */
    void markEndOfHeaders(StringBuilder requestBuilder) {
        requestBuilder.append(ICAPCommunicationStrings.CRLF);
    }

    /**
     * <p>
     * Reads and returns the raw response of the ICAP server. The response contains
     * the ICAP headers, the ICAP body (if any), the encapsulated HTTP headers of the modified
     * response (if any) and the HTTP response body (if any) as stream.
     * </p>
     * 
     * <p>
     * It reads the specified {@link InputStream} bytewise in order to determine the end of
     * the stream. The ICAP server uses the special {@link ICAPCommunicationStrings#HTTP_TERMINATOR}
     * string to indicate that. So reading by line won't work in this case.
     * </p>
     * 
     * @param inputStream The {@link InputStream} to read the response from
     * @param socket The socket with the open connection to the ICAP server
     * @return The raw response of the ICAP server as a string
     * @throws IOException if an I/O error is occurred
     */
    ICAPResponse readResponse(InputStream inputStream, Socket socket) throws IOException {
        int position = 0;
        int icapHeadersMark = 0;
        int readBytes = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        Terminator terminator = Terminator.ICAP_HEADER;
        ICAPResponse.Builder builder = new ICAPResponse.Builder();
        while (position < BUFFER_SIZE && (readBytes = inputStream.read(buffer, position, 1)) != -1) {
            position += readBytes;
            if (position < terminator.getBytes().length + MIN_MESSAGE_SIZE) {
                continue;
            }
            byte[] terminatorBytes = Arrays.copyOfRange(buffer, position - terminator.getBytes().length, position);
            if (!Arrays.equals(terminator.getBytes(), terminatorBytes)) {
                continue;
            }
            switch (terminator) {
                case ICAP_HEADER:
                    String candidate = new String(buffer, 0, position, Charsets.UTF_8);
                    String[] split = Strings.splitByCRLF(candidate);
                    if (split == null || split.length == 0) {
                        throw new IOException("Cannot extract any ICAP status code from: '" + candidate + "'");
                    }
                    int statusCode = readStatusCode(split[0]);
                    if (statusCode < 0) {
                        throw new IOException("Cannot extract any ICAP status code from: '" + candidate + "'");
                    }
                    if (statusCode == 100) {
                        // We got 100 back, instruct the client to send the rest of the data.
                        return builder.withStatusCode(statusCode).build();
                    }
                    parseICAPHeaders(builder, split);
                    if (statusCode != 200) {
                        // Collect more information?
                        return builder.build();
                    }
                    if (!isEncapsulatedHeaderPresent(candidate)) {
                        terminator = Terminator.HTTP_BODY;
                        break;
                    }
                    // There are encapsulated headers, switch to HTTP_HEADER terminator 
                    // and continue to capture the entire packet.
                    terminator = Terminator.HTTP_HEADER;
                    // We mark the end of the ICAP headers
                    icapHeadersMark = position;
                    continue;
                case HTTP_HEADER:
                    terminator = Terminator.HTTP_BODY;
                    parseEncapsulatedHTTPHeaders(builder, new String(buffer, icapHeadersMark, position - icapHeadersMark, "UTF-8"));
                    break;
                default:
                    continue;
            }
            // If we reach this point then we break out of the reading loop either because:
            //  a) we encountered the beginning of an HTTP body, or
            //  b) we encountered an unexpected character
            break;
        }
        if (terminator == Terminator.HTTP_BODY) {
            return builder.build();
        }
        throw new IOException("Cannot read data from the input stream");
    }

    ////////////////////////// HELPERS //////////////////////////

    /**
     * Checks the specified string for the 'Encapsulated' header
     * 
     * @param string The string to check
     * @return <code>true</code> if the 'Encapsulated' header is present and has
     *         a value of 'res-body' > 0; <code>false</code> otherwise
     */
    private boolean isEncapsulatedHeaderPresent(String string) {
        String[] split = Strings.splitByCRLF(string);
        if (split == null || split.length == 0) {
            return false;
        }
        // The 'Encapsulated' header always the last one
        String possibleEncapsulated = split[split.length - 1];
        if (Strings.isEmpty(possibleEncapsulated)) {
            return false;
        }
        int index = possibleEncapsulated.indexOf("res-body");
        if (index < 0) {
            return false;
        }
        int indexOfColon = possibleEncapsulated.indexOf(':');
        if (indexOfColon < 0) {
            return false;
        }
        String encapsulatedValues = possibleEncapsulated.substring(indexOfColon + 1);
        String[] valuePairs = Strings.splitByComma(encapsulatedValues);
        for (String vp : valuePairs) {
            String[] pair = Strings.splitBy(vp, '=', true);
            if (pair == null || pair.length != 2) {
                continue;
            }
            try {
                // We are looking for a 'res-body' key with a value > 0 and not 'null'
                if ("res-body".equals(pair[0]) && !"null".equals(pair[1]) && Integer.parseInt(pair[1]) >= 0) {
                    return true;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Parses the ICAPHeaders from the specified packet starting from the specified position
     * 
     * @param builder The {@link ICAPResponse.Builder}
     * @param startPosition The starting position
     * @param endPosition The end position
     * @param packet The response packet
     */
    private void parseICAPHeaders(Builder builder, String[] headers) {
        if (headers == null || headers.length == 0) {
            return;
        }
        readStatusLine(builder, headers[0]);
        for (int index = 1; index < headers.length; index++) {
            String[] header = Strings.splitByColon(headers[index]);
            if (header == null || header.length < 2) {
                continue;
            }
            builder.addHeader(header[0], header.length > 2 ? Strings.join(header, ":", 1, header.length) : header[1]);
        }
    }

    /**
     * Parses the encapsulated HTTP headers (if any) from the specified response packet starting from
     * the specified position.
     * 
     * @param builder The {@link Builder}
     * @param startPosition The starting position
     * @param endPosition The end position
     * @param packet The response packet
     */
    private void parseEncapsulatedHTTPHeaders(Builder builder, String headers) {
        String[] split = Strings.splitByCRLF(headers);
        readEncapsulatedStatusLine(builder, split[0]);
        for (String headerPair : split) {
            String[] header = Strings.splitByColon(headerPair);
            if (header == null || header.length != 2) {
                continue;
            }
            builder.addEncapsulatedHeader(header[0], header[1]);
        }
    }

    /**
     * Reads the status line from the specified string. The status line of an RFC-compliant ICAP server
     * is similar in form to that used by HTTP, including the ICAP version and a status code, e.g.
     * <code> ICAP/1.0 200 OK</code>
     * 
     * @param responseBuilder The {@link ICAPResponse.Builder}
     * @param statusLine The optional status line
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3.3">RFC-3507, Section 4.3.3</a>
     */
    private void readStatusLine(Builder responseBuilder, String statusLine) {
        if (Strings.isEmpty(statusLine)) {
            return;
        }
        responseBuilder.withStatusLine(statusLine);
        responseBuilder.withStatusCode(readStatusCode(statusLine));
    }

    /**
     * Reads the encapsulated HTTP status line from the specified string.
     * 
     * @param responseBuilder The {@link ICAPResponse.Builder}
     * @param encapsulatedStatusLine The optional status line
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.8.2">RFC-3507, Section 4.8.2</a>
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.9.2">RFC-3507, Section 4.9.2</a>
     */
    private void readEncapsulatedStatusLine(Builder responseBuilder, String encapsulatedStatusLine) {
        if (Strings.isEmpty(encapsulatedStatusLine)) {
            return;
        }
        if (!encapsulatedStatusLine.startsWith("HTTP")) {
            return;
        }
        responseBuilder.withEncapsulatedStatusLine(encapsulatedStatusLine);
        responseBuilder.withEncapsulatedStatusCode(readStatusCode(encapsulatedStatusLine));
    }

    /**
     * Reads the status code out of the specified status line
     * 
     * @param statusLine The status line from which to read the status code
     * @return The status code or <code>-1</code> to either indicate that no
     *         status code is available or no status code could be read.
     */
    private int readStatusCode(String statusLine) {
        String[] split = Strings.splitByWhitespaces(statusLine);
        if (split == null || split.length < 1) {
            return -1;
        }
        try {
            return Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            LOG.debug("Unable to determine any status code from '{}'", split[1]);
            return -1;
        }
    }
}
