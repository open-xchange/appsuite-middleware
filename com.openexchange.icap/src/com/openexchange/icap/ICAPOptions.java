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

import java.util.Collections;
import java.util.Set;

/**
 * {@link ICAPOptions}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPOptions {

    private long previewSize;
    private boolean allow;
    private Set<ICAPMethod> supportedMethods;
    private String isTag;
    private long ttl;
    private String serviceId;
    private String service;
    private long created;

    /**
     * Initialises a new {@link ICAPOptions}.
     */
    public ICAPOptions() {
        super();
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

    /////////////////////// PRIVATE SETTERS /////////////////////////

    /**
     * Sets the previewSize
     *
     * @param previewSize The previewSize to set
     */
    private void setPreviewSize(long previewSize) {
        this.previewSize = previewSize;
    }

    /**
     * Sets the allow
     *
     * @param allow The allow to set
     */
    private void setAllow(boolean allow) {
        this.allow = allow;
    }

    /**
     * Sets the supportedMethods
     *
     * @param supportedMethods The supportedMethods to set
     */
    private void setSupportedMethods(Set<ICAPMethod> supportedMethods) {
        this.supportedMethods = Collections.unmodifiableSet(supportedMethods);
    }

    /**
     * Sets the isTag
     *
     * @param isTag The isTag to set
     */
    private void setIsTag(String isTag) {
        this.isTag = isTag;
    }

    /**
     * Sets the optionsTTL
     *
     * @param optionsTTL The optionsTTL to set
     */
    private void setTTL(long optionsTTL) {
        this.ttl = optionsTTL;
    }

    /**
     * Sets the serviceId
     *
     * @param serviceId The serviceId to set
     */
    private void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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
     * Sets the created
     *
     * @param created The created to set
     */
    private void setCreated(long created) {
        this.created = created;
    }

    ////////////////////////// BUILDER ///////////////////////////

    public static final class Builder {

        private long previewSize;
        private boolean allow;
        private Set<ICAPMethod> supportedMethods;
        private String isTag;
        private long ttl;
        private String service;
        private String serviceId;

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
            this.supportedMethods = methods;
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
         * Builds the {@link ICAPOptions}
         * 
         * @return The built {@link ICAPOptions}
         */
        public ICAPOptions build() {
            ICAPOptions options = new ICAPOptions();
            options.setAllow(allow);
            options.setIsTag(isTag);
            options.setTTL(ttl);
            options.setPreviewSize(previewSize);
            options.setSupportedMethods(supportedMethods);
            options.setService(service);
            options.setServiceId(serviceId);
            options.setCreated(System.currentTimeMillis());
            return options;
        }
    }
}
