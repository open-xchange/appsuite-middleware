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

package com.openexchange.icap;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, String> bodyHeaders;
    private ICAPMethod method;
    private OperationMode clientMode;
    private String service;
    private String server;
    private int port;
    private InputStream body;

    /**
     * Initialises a new {@link ICAPRequest}.
     */
    public ICAPRequest() {
        super();
        headers = new HashMap<>(4);
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
     * Gets the body headers
     *
     * @return an unmodifiable {@link Map} with the body headers
     */
    public Map<String, String> getBodyHeaders() {
        return bodyHeaders;
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

    ////////////////// PRIVATE SETTERS //////////////////

    /**
     * Sets the method
     *
     * @param method The method to set
     */
    private void setMethod(ICAPMethod method) {
        this.method = method;
    }

    /**
     * Sets the headers
     * 
     * @param headers The headers to set
     */
    private void setHeaders(Map<String, String> headers) {
        this.headers = Collections.unmodifiableMap(headers);
    }

    /**
     * Sets the bodyHeaders
     *
     * @param bodyHeaders The bodyHeaders to set
     */
    private void setBodyHeaders(Map<String, String> bodyHeaders) {
        this.bodyHeaders = Collections.unmodifiableMap(bodyHeaders);
    }

    /**
     * Sets the service
     *
     * @param service The service to set
     */
    private void setService(String service) {
        this.service = service;
    }

    /**
     * Sets the server
     *
     * @param server The serverIP to set
     */
    private void setServer(String server) {
        this.server = server;
    }

    /**
     * Sets the port
     *
     * @param port The port to set
     */
    private void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the body
     *
     * @param body The body to set
     */
    private void setBody(InputStream body) {
        this.body = body;
    }

    /**
     * Sets the {@link OperationMode}
     * 
     * @param mode The mode to set
     */
    private void setOperationMode(OperationMode mode) {
        this.clientMode = mode;
    }

    ///////////////////// BUILDER //////////////////////

    /**
     * {@link Builder} for the {@link ICAPRequest}s
     */
    public static final class Builder {

        /**
         * The default ICAPMethod. It is implemented by all
         * ICAP Servers.
         * 
         * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3.2">RFC-3507, Section 4.3.2</a>
         */
        private ICAPMethod method = ICAPMethod.OPTIONS;
        private final Map<String, String> headers;
        private final Map<String, String> bodyHeaders;
        private String server;
        private int port = -1;
        private String service;
        private InputStream body;

        /**
         * Initialises a new {@link ICAPRequest.Builder}.
         */
        public Builder() {
            super();
            bodyHeaders = new HashMap<>(4);
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
         * Adds a header to the request's body.
         * 
         * @param headerName The header's name
         * @param headerValue The header's value
         * @return this instance for chained calls
         */
        public Builder withBodyHeader(String headerName, String headerValue) {
            bodyHeaders.put(headerName, headerValue);
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

        /**
         * Builds and returns the {@link ICAPRequest}
         * 
         * @return The {@link ICAPRequest}
         * @throws IllegalArgumentException if the server address is not defined.
         */
        public ICAPRequest build() {
            if (Strings.isEmpty(server)) {
                throw new IllegalArgumentException("Cannot build ICAP request: 'server' is missing");
            }
            ICAPRequest request = new ICAPRequest();
            request.setMethod(method);
            request.setPort(port < 0 ? ICAPCommons.DEFAULT_PORT : port);
            request.setServer(server);
            request.setService(service);

            if (body != null) {
                request.setBody(body);
            }
            headers.put(ICAPRequestHeader.HOST, server);
            request.setOperationMode(headers.containsKey(ICAPRequestHeader.ALLOW) ? OperationMode.DOUBLE_FETCH : OperationMode.STREAMING);
            if (request.getOperationMode().equals(OperationMode.STREAMING)) {
                headers.remove(ICAPRequestHeader.PREVIEW);
            }
            if (headers.containsKey(ICAPRequestHeader.PREVIEW)) {
                long previewSize = Long.parseLong(headers.get(ICAPRequestHeader.PREVIEW));
                String cts = bodyHeaders.get(ICAPRequestHeader.CONTENT_LENGTH);
                long contentLength = Long.parseLong(cts == null ? "-1" : cts);
                if (contentLength > previewSize || contentLength < 0 || previewSize < 0) {
                    headers.remove(ICAPRequestHeader.PREVIEW);
                }
            }
            request.setHeaders(headers);
            request.setBodyHeaders(bodyHeaders);
            return request;
        }
    }
}
