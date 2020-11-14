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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ICAPResponse}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPResponse {

    private String statusLine;
    private String encapsulatedBody;
    private String encapsulatedStatusLine;

    private int statusCode;
    private int encapsulatedStatusCode;

    private Map<String, String> headers;
    private Map<String, String> encapsulatedHeaders;

    /**
     * Initialises a new {@link ICAPResponse}.
     * 
     * @param builder The builder instance
     */
    ICAPResponse(Builder builder) {
        this.statusLine = builder.statusLine;
        this.statusCode = builder.statusCode;
        this.headers = toImmutableMap(builder.headers, String::toLowerCase, v -> v);
        this.encapsulatedStatusLine = builder.encapsulatedStatusLine;
        this.encapsulatedStatusCode = builder.encapsulatedStatusCode;
        this.encapsulatedBody = builder.encapsulatedBody;
        this.encapsulatedHeaders = toImmutableMap(builder.encapsulatedHeaders, k -> k, v -> v);
    }

    /**
     * Converts the specified {@link Map} to an {@link ImmutableMap} while processing the keys and values
     * with the specified {@link Function}s
     * 
     * @param map The {@link Map} to convert
     * @param keyProcessor The key processor
     * @param valueProcessor The value processor
     * @return The {@link ImmutableMap}
     */
    private ImmutableMap<String, String> toImmutableMap(Map<String, String> map, Function<String, String> keyProcessor, Function<String, String> valueProcessor) {
        ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
        for (Entry<String, String> entry : map.entrySet()) {
            mapBuilder.put(keyProcessor.apply(entry.getKey()), valueProcessor.apply(entry.getValue()));
        }
        return mapBuilder.build();
    }

    /**
     * Returns the value of the specific header. The header's name is
     * <b>CaSe-InSeNsItIvE</b>
     * 
     * @param headerName The header's name
     * @return The header's value or <code>null</code> if no such header exists
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3">RFC-3507, Section 4.3</a>
     */
    public String getHeader(String headerName) {
        return headers.get(headerName.toLowerCase());
    }

    /**
     * Gets the statusLine
     *
     * @return The statusLine or <code>null</code> if no status line is available
     */
    public String getStatusLine() {
        return statusLine;
    }

    /**
     * Gets the status code
     *
     * @return The status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the encapsulated body
     * 
     * @return The encapsulated body, or <code>null</code> if no encapsulated body is available
     */
    public String getEncapsulatedBody() {
        return encapsulatedBody;
    }

    /**
     * <p>Returns an unmodifiable {@link Map} with all headers of the response.</p>
     * <p><b>Note that the headers' names are case-insensitive.</b></p>
     * 
     * @return an unmodifiable {@link Map} with all headers of the response
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3">RFC-3507, Section 4.3</a>
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets the encapsulatedStatusLine
     *
     * @return The encapsulatedStatusLine
     */
    public String getEncapsulatedStatusLine() {
        return encapsulatedStatusLine;
    }

    /**
     * Gets the encapsulatedStatusCode
     *
     * @return The encapsulatedStatusCode
     */
    public int getEncapsulatedStatusCode() {
        return encapsulatedStatusCode;
    }

    /**
     * Returns the value of the specific encapsulated header.
     * 
     * @param headerName The encapsulated header's name
     * @return The header's value or <code>null</code> if no such header exists
     */
    public String getEncapsulatedHeader(String headerName) {
        return encapsulatedHeaders.get(headerName);
    }

    /**
     * Returns the encapsulated headers
     * 
     * @return the encapsulated headers or <code>null</code> if no encapsulated headers exists
     */
    public Map<String, String> getEncapsulatedHeaders() {
        return encapsulatedHeaders;
    }

    ////////////////////// BUILDER ///////////////////////

    /**
     * Creates builder to build {@link ICAPResponse}.
     * 
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link ICAPResponse}.
     */
    public static final class Builder {

        int statusCode;
        int encapsulatedStatusCode;

        String statusLine;
        String encapsulatedBody;
        String encapsulatedStatusLine;

        Map<String, String> headers = ImmutableMap.of();
        Map<String, String> encapsulatedHeaders = ImmutableMap.of();

        /**
         * Initialises a new {@link ICAPResponse.Builder}.
         */
        public Builder() {
            super();
            headers = new HashMap<>(8);
            encapsulatedHeaders = new HashMap<>(8);
        }

        /**
         * Adds the status code to the response
         * 
         * @param statusCode The status code of the response
         * @return The {@link Builder} instance for chained calls
         */
        public Builder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * Adds the status line to the response
         * 
         * @param statusLine The status line
         * @return The {@link Builder} instance for chained calls
         */
        public Builder withStatusLine(String statusLine) {
            this.statusLine = statusLine;
            return this;
        }

        /**
         * Adds the specified header with the specified value.
         * If another header with the same name already exists
         * its value will be overridden.
         * 
         * @param key The header's name
         * @param value The header's value
         * @return The {@link Builder} instance for chained calls
         */
        public Builder addHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        /**
         * Adds the specified encapsulated header with the specified value.
         * If another header with the same name already exists its value
         * will be overridden.
         * 
         * @param key The header's name
         * @param value The header's value
         * @return The {@link Builder} instance for chained calls
         */
        public Builder addEncapsulatedHeader(String key, String value) {
            encapsulatedHeaders.put(key, value);
            return this;
        }

        /**
         * Sets the encapsulated body of the response.
         * 
         * @param encapsulatedBody The encapsulated body to set
         * @return The {@link Builder} instance for chained calls
         */
        public Builder withEncapsulatedBody(String encapsulatedBody) {
            this.encapsulatedBody = encapsulatedBody;
            return this;
        }

        /**
         * Sets the encapsulatedStatusLine
         *
         * @param encapsulatedStatusLine The encapsulatedStatusLine to set
         * @return The {@link Builder} instance for chained calls
         */
        public Builder withEncapsulatedStatusLine(String encapsulatedStatusLine) {
            this.encapsulatedStatusLine = encapsulatedStatusLine;
            return this;
        }

        /**
         * Sets the encapsulatedStatusCode
         *
         * @param encapsulatedStatusCode The encapsulatedStatusCode to set
         * @return The {@link Builder} instance for chained calls
         */
        public Builder withEncapsulatedStatusCode(int encapsulatedStatusCode) {
            this.encapsulatedStatusCode = encapsulatedStatusCode;
            return this;
        }

        /**
         * Builds the {@link ICAPResponse}
         * 
         * @return The built {@link ICAPResponse}
         */
        public ICAPResponse build() {
            return new ICAPResponse(this);
        }
    }
}
