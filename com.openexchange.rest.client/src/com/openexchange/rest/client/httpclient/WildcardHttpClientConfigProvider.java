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

import com.openexchange.annotation.NonNull;

/**
 * {@link WildcardHttpClientConfigProvider} - A provider for a concrete HTTP configuration using a wild-card expression to determine the
 * associated client.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public interface WildcardHttpClientConfigProvider extends HttpClientConfigProvider {

    /**
     * Provides a wild-card expression as {@link String} that can be used to determine if a specific
     * client identifier can be used to generate a configuration.
     * <p>
     * In other words, the wild-card expression is used to identify the correct provider.
     *
     * @return A wild-card expression to match client identifier, never <code>null</code>. Expression should be a simple wild-card,
     *         like e.g. <code>myRegex-id-?</code> for matching one specific character or <code>myRegex*</code> for multiple
     *         characters followed.
     */
    @NonNull
    String getClientIdPattern();

    /**
     * Get the name of the group each client created with this provider belongs to.
     * <p>
     * Should be nearly the same value as per {@link #getClientIdPattern()} but without wild-cards.
     * This name will be used in case no specific configuration for the client is set, but for the
     * group of client represented by this provider.
     *
     * @return The group name
     */
    @NonNull
    String getGroupName();

    /**
     * Configures the {@link HttpBasicConfig}.
     * <p>
     * This method is for implementations that needs to set values based on additional properties.
     * If no adjustments needs to be performed, the implementor <b>MUST</b> not change anything.
     *
     * @param clientId The actual HTTP client identifier
     * @param config The configuration to adjust
     * @return The {@link HttpBasicConfig}
     */
    default HttpBasicConfig configureHttpBasicConfig(String clientId, HttpBasicConfig config) {
        return config;
    }

}
