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

package com.openexchange.oidc.http.outbound;

import java.util.Optional;
import com.openexchange.config.ConfigurationService;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.version.VersionService;


/**
 * {@link OIDCHttpClientConfig} - The HTTP client configuration for out-bound HTTP communication of the OpenID module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class OIDCHttpClientConfig extends DefaultHttpClientConfigProvider {

    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private static final int DEFAULT_READ_TIMEOUT = 15000;

    private static final int DEFAULT_POOL_TIMEOUT = 15000;

    private static final int DEFAULT_MAX_CONNECTIONS = 100;

    private static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 100;

    private static final String CLIENT_ID_OIDC = "oidc";

    /**
     * Gets the identifier for the HTTP client configuration for out-bound HTTP communication of the OpenID module.
     *
     * @return The client identifier
     */
    public static String getClientIdOidc() {
        return CLIENT_ID_OIDC;
    }

    /**
     * Initializes a new {@link OIDCHttpClientConfig}.
     */
    public OIDCHttpClientConfig() {
        super(CLIENT_ID_OIDC, "Open-Xchange OpenID HTTP Client v", Optional.ofNullable(Services.getOptionalService(VersionService.class)));
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        ConfigurationService configService = Services.getOptionalService(ConfigurationService.class);
        if (configService == null) {
            config.setSocketReadTimeout(DEFAULT_READ_TIMEOUT);
            config.setMaxTotalConnections(DEFAULT_MAX_CONNECTIONS);
            config.setMaxConnectionsPerRoute(DEFAULT_MAX_CONNECTIONS_PER_HOST);
            config.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
            config.setConnectionRequestTimeout(DEFAULT_POOL_TIMEOUT);
        } else {
            config.setSocketReadTimeout(configService.getIntProperty("com.openexchange.httpclient.oidc.readTimeout", DEFAULT_READ_TIMEOUT));
            config.setMaxTotalConnections(configService.getIntProperty("com.openexchange.httpclient.oidc.totalConnections", DEFAULT_MAX_CONNECTIONS));
            config.setMaxConnectionsPerRoute(configService.getIntProperty("com.openexchange.httpclient.oidc.connectionsPerRoute", DEFAULT_MAX_CONNECTIONS_PER_HOST));
            config.setConnectTimeout(configService.getIntProperty("com.openexchange.httpclient.oidc.connectTimeout", DEFAULT_CONNECT_TIMEOUT));
            config.setConnectionRequestTimeout(configService.getIntProperty("com.openexchange.httpclient.oidc.connectionRequestTimeout", DEFAULT_POOL_TIMEOUT));
        }
        return config;
    }

}
