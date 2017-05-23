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

package com.openexchange.pns.mobile.api.facade;


/**
 * {@link ClientConfig} - A client configuration for Mobile API Facade.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ClientConfig {

    /** The generic client configuration for brand-less Mobile API Facade */
    public static final ClientConfig GENERIC_CLIENT_CONFIG = builder("open-xchange-mobile-api-facade").enabled(true).build();

    /**
     * Creates a new builder instance for specified client identifier
     *
     * @param clientId The client identifier
     * @return The new builder instance
     */
    public static Builder builder(String clientId) {
        return new Builder(clientId);
    }

    public static class Builder {

        private final String clientId;
        private boolean enabled;
        private String description;

        /**
         * Initializes a new {@link ClientConfig.Builder}.
         */
        Builder(String clientId) {
            super();
            this.clientId = clientId;
            enabled = true;
        }

        /**
         * Sets the enabled flag
         *
         * @param enabled The enabled to set
         * @return This builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the description
         *
         * @param description The description to set
         * @return This builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Creates the client configuration from this builder's arguments.
         *
         * @return The client configuration
         */
        public ClientConfig build() {
            return new ClientConfig(clientId, enabled, description);
        }
    }

    // -------------------------------------------------------------------------------------------------------------

    private final String clientId;
    private final boolean enabled;
    private final String description;

    /**
     * Initializes a new {@link ClientConfig}.
     */
    ClientConfig(String clientId, boolean enabled, String description) {
        super();
        this.clientId = clientId;
        this.enabled = enabled;
        this.description = description;
    }

    /**
     * Gets the client identifier
     *
     * @return The client identifier
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the enabled flag
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

}
