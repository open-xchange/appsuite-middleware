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

package com.openexchange.dovecot.doveadm.client.http;

import java.util.Optional;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.dovecot.doveadm.client.internal.ClientConfig;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmCall;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmEndpointManager;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.version.VersionService;

/**
 * {@link DoveAdmHttpClientConfig}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class DoveAdmHttpClientConfig extends DefaultHttpClientConfigProvider {

    /**
     * Generates the client identifier for the DoveAdm call.
     *
     * @param call The call to get the identifier for
     * @return The client identifier for the HTTP client
     */
    public static String generateClientId(HttpDoveAdmCall call) {
        String name = "doveadm";
        if (call != HttpDoveAdmCall.DEFAULT) {
            name = "doveadm-" + call.getName();
        }
        return name;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ClientConfig clientConfig;

    /**
     * Initializes a new {@link DoveAdmHttpClientConfig}.
     *
     * @param httpClientId The identifier for the HTTP client
     * @param clientConfig The client configuration
     * @param services The service look-up
     */
    public DoveAdmHttpClientConfig(String httpClientId, ClientConfig clientConfig, ServiceLookup services) {
        super(httpClientId, "OX Dovecot Http Client v", Optional.ofNullable(services.getService(VersionService.class)));
        this.clientConfig = clientConfig;
    }

    @Override
    public Interests getAdditionalInterests() {
        return DefaultInterests.builder().propertiesOfInterest(HttpDoveAdmEndpointManager.DOVEADM_ENDPOINTS + ".*").build();
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        config.setConnectTimeout(clientConfig.getConnectTimeout());
        config.setMaxConnectionsPerRoute(clientConfig.getMaxConnectionsPerRoute());
        config.setMaxTotalConnections(clientConfig.getTotalConnections());
        config.setSocketReadTimeout(clientConfig.getReadTimeout());
        return config;
    }

}
