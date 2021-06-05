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

import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link ICAPOptions}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPOptions {

    private long previewSize;
    private long ttl;
    private long created;

    private boolean allow;

    private Set<ICAPMethod> supportedMethods;

    private String isTag;
    private String serviceId;
    private String service;

    /**
     * Initializes a new {@link ICAPOptions}.
     * 
     * @param builder the builder instance
     */
    ICAPOptions(Builder builder) {
        this.previewSize = builder.previewSize;
        this.allow = builder.allow;
        this.supportedMethods = builder.supportedMethods;
        this.isTag = builder.isTag;
        this.ttl = builder.ttl;
        this.serviceId = builder.serviceId;
        this.service = builder.service;
        this.created = builder.created;
    }

    /**
     * Gets the preview
     *
     * @return The preview
     */
    public long getPreviewSize() {
        return previewSize;
    }

    /**
     * Gets the allow
     *
     * @return The allow
     */
    public boolean isAllow() {
        return allow;
    }

    /**
     * Gets the supportedMethods
     *
     * @return The supportedMethods
     */
    public Set<ICAPMethod> getSupportedMethods() {
        return supportedMethods;
    }

    /**
     * Gets the isTag
     *
     * @return The isTag
     */
    public String getIsTag() {
        return isTag;
    }

    /**
     * Gets the optionsTTL
     *
     * @return The optionsTTL
     */
    public long getTTL() {
        return ttl;
    }

    /**
     * Gets the serviceId
     *
     * @return The serviceId
     */
    public String getServiceId() {
        return serviceId;
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
     * Gets the created
     *
     * @return The created
     */
    public long getCreated() {
        return created;
    }

    //////////////////////////BUILDER ///////////////////////////

    /**
     * Creates builder to build {@link ICAPOptions}.
     * 
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link ICAPOptions}.
     */
    public static final class Builder {

        long previewSize;
        long ttl;
        long created;

        boolean allow;

        Set<ICAPMethod> supportedMethods = ImmutableSet.of();

        String isTag;
        String serviceId;
        String service;

        /**
         * Initialises a new {@link ICAPOptions.Builder}.
         */
        public Builder() {
            super();
        }

        /**
         * The number of bytes to be sent by the ICAP client during a
         * preview.
         * 
         * @param previewSize The preview size
         * @return this instance for chained calls
         */
        public Builder withPreviewSize(long previewSize) {
            this.previewSize = previewSize;
            return this;
        }

        /**
         * A directive declaring a list of optional ICAP features that this
         * server has implemented. For example, the value "204" to
         * indicate that the ICAP server supports a 204 response.
         * 
         * @param allow Whether the 'Allow' header is present
         * @return this instance for chained calls
         */
        public Builder withAllow(boolean allow) {
            this.allow = allow;
            return this;
        }

        /**
         * The methods that are supported by the service.
         * 
         * @param methods The methods that are supported by the service.
         * @return
         */
        public Builder withSupportedMethods(Set<ICAPMethod> methods) {
            this.supportedMethods = ImmutableSet.copyOf(methods);
            return this;
        }

        /**
         * 
         * @param isTag
         * @return this instance for chained calls
         */
        public Builder withISTag(String isTag) {
            this.isTag = isTag;
            return this;
        }

        /**
         * 
         * @param optionsTTL
         * @return this instance for chained calls
         */
        public Builder withTTL(long optionsTTL) {
            this.ttl = optionsTTL;
            return this;
        }

        /**
         * 
         * @param serviceId
         * @return this instance for chained calls
         */
        public Builder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        /**
         * 
         * @param service
         * @return this instance for chained calls
         */
        public Builder withService(String service) {
            this.service = service;
            return this;
        }

        /**
         * Builds the ICAPOptions
         *
         * @return The new {@link ICAPOptions}
         */
        public ICAPOptions build() {
            return new ICAPOptions(this);
        }
    }
}
