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

import java.util.Optional;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.version.VersionService;

/**
 * {@link DefaultHttpClientConfigProvider} - Provider with the most basic and common used functions
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class DefaultHttpClientConfigProvider extends AbstractHttpClientModifer implements SpecificHttpClientConfigProvider {

    private final String clientId;

    /**
     * Initializes a new {@link DefaultHttpClientConfigProvider} with the default user agent
     *
     * @param clientId The identifier of the HTTP client
     */
    public DefaultHttpClientConfigProvider(String clientId) {
        this(clientId, null, Optional.empty());
    }

    /**
     * Initializes a new {@link DefaultHttpClientConfigProvider}.
     *
     * @param clientId The identifier of the HTTP client
     * @param userAgent The user agent to set "as-is", never <code>null</code>
     */
    public DefaultHttpClientConfigProvider(String clientId, @NonNull String userAgent) {
        this(clientId, userAgent, Optional.empty());
    }

    /**
     * Initializes a new {@link DefaultHttpClientConfigProvider}.
     *
     * @param clientId The identifier of the HTTP client
     * @param userAgent The user agent to set; or <code>null</code> to use the default user agent string
     * @param optionalVersionService The optional version service. Will be used to append the version to the user agent
     */
    public DefaultHttpClientConfigProvider(String clientId, @Nullable String userAgent, Optional<VersionService> optionalVersionService) {
        super(getUserAgent(userAgent, optionalVersionService));
        this.clientId = clientId;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    private static String getUserAgent(String userAgent, Optional<VersionService> optionalVersionService) {
        if (!optionalVersionService.isPresent()) {
            return null == userAgent ? DEFAULT_UA : userAgent;
        }

        StringBuilder sb = new StringBuilder(64);
        if (null == userAgent) {
            sb.append(DEFAULT_UA).append('/');
        } else {
            sb.append(userAgent);
        }
        sb.append(optionalVersionService.get().getVersionString());
        return sb.toString();
    }

}
