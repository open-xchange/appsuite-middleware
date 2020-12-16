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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
