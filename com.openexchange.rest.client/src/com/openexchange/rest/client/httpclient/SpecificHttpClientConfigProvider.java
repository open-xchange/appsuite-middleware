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

package com.openexchange.rest.client.httpclient;

/**
 * {@link SpecificHttpClientConfigProvider} - A HTTP configuration provider for a specific client.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public interface SpecificHttpClientConfigProvider extends HttpClientConfigProvider {

    /**
     * Get the unique identifier
     *
     * @return The identifier of the HTTP client
     */
    String getClientId();

    /**
     * Configures the {@link HttpBasicConfig}.
     * <p>
     * This method is for implementations that needs to set values based on additional properties.
     * If no adjustments needs to be performed, the implementor <b>MUST</b> not change anything.
     *
     * @param config The HTTP configuration initialized with default values.
     * @return The {@link HttpBasicConfig}
     */
    default HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        return config;
    }

}
