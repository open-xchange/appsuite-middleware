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
