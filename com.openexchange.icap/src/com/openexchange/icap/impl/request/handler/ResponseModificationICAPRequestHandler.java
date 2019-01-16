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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.icap.ICAPCommunicationStrings;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.header.ICAPRequestHeader;
import com.openexchange.java.Strings;

/**
 * {@link ResponseModificationICAPRequestHandler} - Handles the Response Modification Mode's Response
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.9">RFC-3507, Section 4.9</a>
 */
public class ResponseModificationICAPRequestHandler extends AbstractICAPRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseModificationICAPRequestHandler.class);

    /**
     * Initialises a new {@link ResponseModificationICAPRequestHandler}.
     */
    public ResponseModificationICAPRequestHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.icap.request.handler.AbstractICAPRequestHandler#handle(com.openexchange.icap.ICAPRequest, java.net.Socket, java.io.InputStream, java.io.OutputStream)
     */
    @Override
    public ICAPResponse handle(ICAPRequest request, Socket socket, InputStream inputStream, OutputStream outputStream) throws IOException {
        StringBuilder requestBuilder = new StringBuilder(128);
        prepareRequestMethod(request, requestBuilder);
        prepareRequestHeaders(request, requestBuilder);

        int previewSize = getPreviewSize(request);
        long contentLength = getContentLength(request);

        String bodyHeaders = prepareBodyHeaders(contentLength, previewSize);
        if (false == request.getHeaders().containsKey(ICAPRequestHeader.ENCAPSULATED)) {
            requestBuilder.append(ICAPRequestHeader.ENCAPSULATED).append(": ");
            requestBuilder.append(Strings.isNotEmpty(bodyHeaders) ? "res-hdr=0, " : "").append("res-body=").append(bodyHeaders.length()).append(ICAPCommunicationStrings.CRLF);
        }
        markEndOfHeaders(requestBuilder);
        // Send the ICAP headers
        sendData(outputStream, requestBuilder.toString());
        // Send the HTTP headers
        sendData(outputStream, bodyHeaders);

        if (contentLength < 0) {
            // Stream the whole data
            streamData(request.getBody(), outputStream);
            sendData(outputStream, ICAPCommunicationStrings.HTTP_TERMINATOR);
            return readResponse(inputStream, socket);
        }

        // Prepare to send the binary data
        if (previewSize > 0) {
            // Send the first chunk
            byte[] chunk = new byte[previewSize];
            request.getBody().read(chunk);
            sendPacket(chunk, outputStream);
            // Indicate to the server either the end of preview chunk or the end of data respectively
            sendData(outputStream, contentLength <= previewSize ? ICAPCommunicationStrings.ICAP_CHUNK_EOF : ICAPCommunicationStrings.HTTP_TERMINATOR);
        } else {
            // Stream the whole data
            if (contentLength > BUFFER_SIZE) {
                streamData(request.getBody(), outputStream, contentLength);
            } else {
                byte[] buffer = new byte[(int) contentLength];
                request.getBody().read(buffer);
                sendPacket(buffer, outputStream);
            }
            sendData(outputStream, ICAPCommunicationStrings.HTTP_TERMINATOR);
        }

        ICAPResponse response = readResponse(inputStream, socket);
        if (response.getStatusCode() != 100) {
            return response;
        }

        // Stream the whole data
        streamData(request.getBody(), outputStream, contentLength - previewSize);
        sendData(outputStream, ICAPCommunicationStrings.HTTP_TERMINATOR);
        return readResponse(inputStream, socket);
    }

    /**
     * Prepares the body headers.
     * 
     * @param contentLength The optional length of the data
     * @param previewSize The optional supported preview size from the ICAP Server
     * @return The prepared body headers or an empty string if content length is unavailable (i.e. is <code>-1</code>).
     */
    private String prepareBodyHeaders(long contentLength, long previewSize) {
        if (contentLength < 0) {
            return "";
        }
        StringBuilder bodyHeaders = new StringBuilder(128);
        bodyHeaders.append(ICAPRequestHeader.CONTENT_LENGTH).append(": ").append(previewSize > 0 && previewSize <= contentLength ? previewSize : contentLength).append(ICAPCommunicationStrings.CRLF);
        // More body headers?
        markEndOfHeaders(bodyHeaders);

        return bodyHeaders.toString();
    }

    /**
     * Get the preview size from the specified {@link ICAPRequest}
     * 
     * @param request The {@link ICAPRequest} from which to fetch the preview size header
     * @return The preview size or <code>-1</code> if no preview size is available
     */
    private int getPreviewSize(ICAPRequest request) {
        String previewSizeStr = request.getHeaders().get(ICAPRequestHeader.PREVIEW);
        if (Strings.isEmpty(previewSizeStr)) {
            LOG.debug("No 'Preview' header is specified! If the service '{}' in the ICAP server '{}' has indicated that a preview size is useful, one should be provided! Use the OPTIONS method to determine whether a 'Preview' should be provided.", request.getService(), request.getServer());
            return -1;
        }
        try {
            return Integer.parseInt(previewSizeStr);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid value '{}' for the '{}' header was detected in the ICAPRequest.", previewSizeStr, ICAPRequestHeader.PREVIEW);
            return -1;
        }
    }

    /**
     * Get the 'Content-Length' header from the specified {@link ICAPRequest}
     * 
     * @param request The {@link ICAPRequest} from which to fetch the 'Content-Length' header
     * @return The value of the 'Content-Length' header or <code>-1</code> if none available
     * @throws IOException if an I/O error occurs
     */
    private long getContentLength(ICAPRequest request) throws IOException {
        String contentLengthStr = request.getHeaders().get(ICAPRequestHeader.CONTENT_LENGTH);
        return Strings.isEmpty(contentLengthStr) ? -1 : Long.parseLong(contentLengthStr);
    }
}
