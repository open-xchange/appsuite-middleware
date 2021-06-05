/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.icap.impl.request.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map.Entry;
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

    @Override
    public ICAPResponse handle(ICAPRequest request, Socket socket, InputStream inputStream, OutputStream outputStream) throws IOException {
        StringBuilder requestBuilder = new StringBuilder(128);
        prepareRequestMethod(request, requestBuilder);
        prepareRequestHeaders(request, requestBuilder);

        int previewSize = getPreviewSize(request);
        long contentLength = getContentLength(request);

        String originalRequestHeaders = prepareOriginalRequestHeaders(request, contentLength, previewSize);
        String originalResponseHeaders = prepareOriginalResponseHeaders(request);
        if (false == request.getHeaders().containsKey(ICAPRequestHeader.ENCAPSULATED)) {
            requestBuilder.append(ICAPRequestHeader.ENCAPSULATED).append(": ");
            if (Strings.isNotEmpty(originalRequestHeaders)) {
                requestBuilder.append("req-hdr=0, ");
                if (Strings.isNotEmpty(originalResponseHeaders)) {
                    requestBuilder.append("res-hdr=").append(originalRequestHeaders.length()).append(", ");
                }
                requestBuilder.append("res-body=").append(originalRequestHeaders.length() + originalResponseHeaders.length()).append(ICAPCommunicationStrings.CRLF);
            } else {
                requestBuilder.append("res-body=0");
            }
        }
        markEndOfHeaders(requestBuilder);
        // Send the ICAP headers
        sendData(outputStream, requestBuilder.toString());
        // Send the HTTP headers
        if (Strings.isNotEmpty(originalRequestHeaders)) {
            sendData(outputStream, originalRequestHeaders);
        }
        if (Strings.isNotEmpty(originalResponseHeaders)) {
            sendData(outputStream, originalResponseHeaders);
        }

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
     * Prepares the original request's headers.
     * 
     * @param contentLength The optional length of the data
     * @param previewSize The optional supported preview size from the ICAP Server
     * @return The prepared original request's headers or an empty string if content length is unavailable (i.e. is <code>-1</code>).
     */
    private String prepareOriginalRequestHeaders(ICAPRequest request, long contentLength, long previewSize) {
        StringBuilder builder = new StringBuilder(128);
        if (Strings.isNotEmpty(request.getOriginalRequest())) {
            builder.append(request.getOriginalRequest()).append(ICAPCommunicationStrings.CRLF);
        }
        for (Entry<String, String> entry : request.getOriginalRequestHeaders().entrySet()) {
            if (entry.getKey().equals(ICAPRequestHeader.CONTENT_LENGTH)) {
                // Skip, we will handle them later
                continue;
            }
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append(ICAPCommunicationStrings.CRLF);
        }
        if (contentLength > 0) {
            builder.append(ICAPRequestHeader.CONTENT_LENGTH).append(": ").append(previewSize > 0 && previewSize <= contentLength ? previewSize : contentLength).append(ICAPCommunicationStrings.CRLF);
        }
        if (builder.length() == 0) {
            return "";
        }
        // More original request headers?
        markEndOfHeaders(builder);

        return builder.toString();
    }

    private String prepareOriginalResponseHeaders(ICAPRequest request) {
        StringBuilder bodyHeaders = new StringBuilder(128);
        if (Strings.isNotEmpty(request.getOriginalStatus())) {
            bodyHeaders.append(request.getOriginalStatus()).append(ICAPCommunicationStrings.CRLF);
        }
        for (Entry<String, String> entry : request.getOriginalResponseHeaders().entrySet()) {
            bodyHeaders.append(entry.getKey()).append(": ").append(entry.getValue()).append(ICAPCommunicationStrings.CRLF);
        }
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("", e);
            }
            return -1;
        }
    }

    /**
     * Get the 'Content-Length' header from the specified {@link ICAPRequest}
     * 
     * @param request The {@link ICAPRequest} from which to fetch the 'Content-Length' header
     * @return The value of the 'Content-Length' header or <code>-1</code> if none available
     */
    private long getContentLength(ICAPRequest request) {
        String contentLengthStr = request.getHeaders().get(ICAPRequestHeader.CONTENT_LENGTH);
        return Strings.isEmpty(contentLengthStr) ? -1 : Long.parseLong(contentLengthStr);
    }
}
