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

package com.openexchange.antivirus.internal;

import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.openexchange.antivirus.AntiVirusEncapsulatedContent;
import java.util.Collections;

/**
 * {@link DefaultAntiVirusEncapsulatedContent}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class DefaultAntiVirusEncapsulatedContent implements AntiVirusEncapsulatedContent {

    private Optional<String> originalRequest = Optional.empty();
    private Map<String, String> originalRequestHeaders = ImmutableMap.of();

    private Optional<String> originalResponseLine = Optional.empty();
    private Map<String, String> originalResponseHeaders = ImmutableMap.of();

    /**
     * Initializes a new {@link DefaultAntiVirusEncapsulatedContent}.
     * 
     * @param builder The builder instance
     */
    DefaultAntiVirusEncapsulatedContent(Builder builder) {
        this.originalRequest = builder.originalRequest;
        this.originalRequestHeaders = builder.originalRequestHeaders;
        this.originalResponseLine = builder.originalResponseLine;
        this.originalResponseHeaders = builder.originalResponseHeaders;
    }

    @Override
    public Optional<String> getOriginalRequest() {
        return originalRequest;
    }

    @Override
    public Map<String, String> getOriginalRequestHeaders() {
        return originalRequestHeaders;
    }

    @Override
    public Optional<String> getOriginalResponseLine() {
        return originalResponseLine;
    }

    @Override
    public Map<String, String> getOriginalResponseHeaders() {
        return originalResponseHeaders;
    }

    /**
     * Creates builder to build {@link DefaultAntiVirusEncapsulatedContent}.
     * 
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link DefaultAntiVirusEncapsulatedContent}.
     */
    public static final class Builder {

        Optional<String> originalRequest = Optional.empty();
        Map<String, String> originalRequestHeaders = Collections.emptyMap();
        Optional<String> originalResponseLine = Optional.empty();;
        Map<String, String> originalResponseHeaders = Collections.emptyMap();

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        public Builder withOriginalRequest(String originalRequest) {
            this.originalRequest = Optional.ofNullable(originalRequest);
            return this;
        }

        public Builder withOriginalRequestHeaders(Map<String, String> originalRequestHeaders) {
            this.originalRequestHeaders = originalRequestHeaders;
            return this;
        }

        public Builder withOriginalResponseLine(String originalResponseLine) {
            this.originalResponseLine = Optional.ofNullable(originalResponseLine);
            return this;
        }

        public Builder withOriginalResponseHeaders(Map<String, String> originalResponseHeaders) {
            this.originalResponseHeaders = originalResponseHeaders;
            return this;
        }

        public DefaultAntiVirusEncapsulatedContent build() {
            return new DefaultAntiVirusEncapsulatedContent(this);
        }
    }

}
