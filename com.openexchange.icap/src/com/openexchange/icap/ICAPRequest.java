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

package com.openexchange.icap;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.icap.header.ICAPRequestHeader;
import com.openexchange.java.Strings;

/**
 * {@link ICAPRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPRequest {

    private Map<String, String> headers;
    private Map<String, String> originalRequestHeaders;
    private Map<String, String> originalResponseHeaders;
    private ICAPMethod method;
    private OperationMode clientMode;
    private String service;
    private String server;
    private String originalRequest;
    private String originalStatus;
    private int port;
    private InputStream body;

    /**
     * Initializes a new {@link ICAPRequest}.
     * 
     * @param builder The builder instance
     */
    ICAPRequest(Builder builder) {
        if (Strings.isEmpty(builder.server)) {
            throw new IllegalArgumentException("Cannot build ICAP request: 'server' is missing");
        }
        this.server = builder.server;
        this.method = builder.method;
        this.port = builder.port < 0 ? ICAPCommons.DEFAULT_PORT : builder.port;
        this.service = builder.service;
        this.originalRequest = builder.originalRequest;
        this.originalStatus = builder.originalStatus;

        if (builder.body != null) {
            this.body = builder.body;
        }
        builder.headers.put(ICAPRequestHeader.HOST, builder.server);
        this.clientMode = builder.headers.containsKey(ICAPRequestHeader.ALLOW) ? OperationMode.DOUBLE_FETCH : OperationMode.STREAMING;
        if (this.clientMode.equals(OperationMode.STREAMING)) {
            builder.headers.remove(ICAPRequestHeader.PREVIEW);
        }
        if (builder.headers.containsKey(ICAPRequestHeader.PREVIEW)) {
            long previewSize = Long.parseLong(builder.headers.get(ICAPRequestHeader.PREVIEW));
            String cts = builder.originalRequestHeaders.get(ICAPRequestHeader.CONTENT_LENGTH);
            long contentLength = Long.parseLong(cts == null ? "-1" : cts);
            if (contentLength > previewSize || contentLength < 0 || previewSize < 0) {
                builder.headers.remove(ICAPRequestHeader.PREVIEW);
            }
        }
        this.headers = ImmutableMap.copyOf(builder.headers);
        this.originalRequestHeaders = ImmutableMap.copyOf(builder.originalRequestHeaders);
        this.originalResponseHeaders = ImmutableMap.copyOf(builder.originalResponseHeaders);
    }

    /**
     * Gets the method
     *
     * @return The method
     */
    public ICAPMethod getMethod() {
        return method;
    }

    /**
     * Gets the userAgent
     *
     * @return The userAgent
     */
    public String getUserAgent() {
        return headers.get(ICAPRequestHeader.USER_AGENT);
    }

    /**
     * Returns the headers
     * 
     * @return an unmodifiable {@link Map} with the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets the original request headers
     *
     * @return an unmodifiable {@link Map} with the original request headers
     */
    public Map<String, String> getOriginalRequestHeaders() {
        return originalRequestHeaders;
    }

    /**
     * Gets the service
     *
     * @return The service
     */
    public String getService() {
        return service;
    }

    /**
     * Gets the server
     *
     * @return The server
     */
    public String getServer() {
        return server;
    }

    /**
     * Gets the original request
     *
     * @return The request
     */
    public String getOriginalRequest() {
        return originalRequest;
    }

    /**
     * Gets the original status
     *
     * @return The status
     */
    public String getOriginalStatus() {
        return originalStatus;
    }

    /**
     * Gets the original response headers
     *
     * @return The original response headers
     */
    public Map<String, String> getOriginalResponseHeaders() {
        return originalResponseHeaders;
    }

    /**
     * Gets the port
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the body
     *
     * @return The body
     */
    public InputStream getBody() {
        return body;
    }

    /**
     * Gets the operation mode
     * 
     * @return the operation mode
     */
    public OperationMode getOperationMode() {
        return clientMode;
    }

    /**
     * Creates builder to build {@link ICAPRequest}.
     * 
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    ///////////////////// BUILDER //////////////////////

    /**
     * Builder to build {@link ICAPRequest}.
     */
    public static final class Builder {

        /**
         * The default ICAPMethod. It is implemented by all
         * ICAP Servers.
         * 
         * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3.2">RFC-3507, Section 4.3.2</a>
         */
        ICAPMethod method = ICAPMethod.OPTIONS;
        String service;
        String server;
        String originalRequest;
        String originalStatus;
        int port = -1;
        InputStream body;
        Map<String, String> headers = ImmutableMap.of();
        Map<String, String> originalRequestHeaders;
        Map<String, String> originalResponseHeaders;

        /**
         * Initialises a new {@link ICAPRequest.Builder}.
         */
        public Builder() {
            super();
            originalRequestHeaders = new LinkedHashMap<>(4);
            originalResponseHeaders = new LinkedHashMap<>(4);
            headers = new HashMap<>(4);
            headers.put(ICAPRequestHeader.USER_AGENT, ICAPCommons.USER_AGENT);
        }

        /**
         * Defines the address of ICAP server to be used for this request
         * 
         * @param server The ICAP server
         * @return this instance for chained calls
         */
        public Builder withServer(String server) {
            this.server = server;
            return this;
        }

        /**
         * Defines the port of the ICAP server (defaults to <code>1344</code>).
         * If the port is invalid, i.e. not in range of 0-65535, an {@link IllegalArgumentException}
         * will be thrown
         * 
         * @param port The port
         * @return this instance for chained calls
         * @throws IllegalArgumentException if the port is not in range of 0-65535
         */
        public Builder withPort(int port) {
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("Invalid port '" + port + "' specified. Accepted values are in range of 0-65535");
            }
            this.port = port;
            return this;
        }

        /**
         * Defines the service for the ICAP server.
         * 
         * @param service The service of the ICAP server
         * @return this instance for chained calls
         * @throws IllegalArgumentException if the service is either <code>null</code> or empty.
         */
        public Builder withService(String service) {
            if (Strings.isEmpty(service)) {
                throw new IllegalArgumentException("The service can neither be 'null' nor empty.");
            }
            this.service = service;
            return this;
        }

        /**
         * Defines the ICAP method for the request
         * 
         * @param method The ICAP method
         * @return this instance for chained calls
         */
        public Builder withMethod(ICAPMethod method) {
            this.method = method;
            return this;
        }

        /**
         * Adds a header to the request.
         * 
         * @param headerName The header's name
         * @param headerValue The header's value
         * @return this instance for chained calls
         */
        public Builder withHeader(String headerName, String headerValue) {
            headers.put(headerName, headerValue);
            return this;
        }

        /**
         * Adds the original request's header.
         * 
         * @param headerName The header's name
         * @param headerValue The header's value
         * @return this instance for chained calls
         */
        public Builder withOriginalRequestHeader(String headerName, String headerValue) {
            originalRequestHeaders.put(headerName, headerValue);
            return this;
        }

        public Builder withOriginalRequestHeaders(Map<String, String> headers) {
            originalRequestHeaders = headers;
            return this;
        }

        public Builder withOriginalResponseHeaders(Map<String, String> headers) {
            originalResponseHeaders = headers;
            return this;
        }

        /**
         * Adds the original request
         *
         * @param request The request
         * @return this instance for chained calls
         */
        public Builder withOriginalRequest(String request) {
            this.originalRequest = request;
            return this;
        }

        public Builder withOriginalStatus(String status) {
            this.originalStatus = status;
            return this;
        }

        /**
         * The body of the ICAP request as {@link InputStream}
         * 
         * @param inputStream The body
         * @return this instance for chained calls
         * @throws IllegalArgumentException if the input stream is <code>null</code>
         */
        public Builder withBody(InputStream inputStream) {
            if (inputStream == null) {
                throw new IllegalArgumentException("The input stream cannot be 'null'.");
            }
            this.body = inputStream;
            return this;
        }

        public ICAPRequest build() {
            return new ICAPRequest(this);
        }
    }
}
