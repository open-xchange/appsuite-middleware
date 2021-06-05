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

package com.openexchange.filestore.sproxyd.http;

import static com.openexchange.filestore.utils.PropertyNameBuilder.optIntProperty;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.utils.PropertyNameBuilder;
import com.openexchange.rest.client.httpclient.AbstractHttpClientModifer;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.rest.client.httpclient.WildcardHttpClientConfigProvider;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SproxydHttpClientConfig}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class SproxydHttpClientConfig extends AbstractHttpClientModifer implements WildcardHttpClientConfigProvider {

    private static final String HTTP_CLIENT_IDENTIFIER = "sproxyd";
    
    private static final int DEF_READ_TIMEOUT = 15000;
    private static final int DEF_CON_TIMEOUT = 5000;
    private static final int DEF_MAX_CONNECTIONS_PER_HOST = 100;
    private static final int DEF_MAX_CONNECTIONS = 100;

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link SproxydHttpClientConfig}.
     *
     * @param serviceLookup The {@link ServiceLookup}
     */
    public SproxydHttpClientConfig(ServiceLookup serviceLookup) {
        super(null);
        this.serviceLookup = serviceLookup;
    }

    @Override
    public @NonNull String getClientIdPattern() {
        return HTTP_CLIENT_IDENTIFIER + "*";
    }
    
    @Override
    @NonNull
    public String getGroupName() {
        return HTTP_CLIENT_IDENTIFIER;
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(String clientId, HttpBasicConfig config) {
        try {
            return getFromConfiguration(clientId, config);
        } catch (OXException e) {
            LoggerFactory.getLogger(SproxydHttpClientConfig.class).error("Unable to load correct properties for sproxyd HTTP client. Falling back to defaults", e);
        }
        // Fallback to default values
        return config.setMaxTotalConnections(DEF_MAX_CONNECTIONS).setMaxConnectionsPerRoute(DEF_MAX_CONNECTIONS_PER_HOST).setConnectTimeout(DEF_CON_TIMEOUT).setSocketReadTimeout(DEF_READ_TIMEOUT);
    }

    @Override
    public Interests getAdditionalInterests() {
        return DefaultInterests.builder().propertiesOfInterest("com.openexchange.filestore.sproxyd.*").build();
    }

    /**
     * Get the {@link HttpBasicConfig} from the configuration
     *
     * @param clientId The client id
     * @param config The {@link HttpBasicConfig}
     * @return The configured {@link HttpBasicConfig}
     * @throws OXException in case the configuration service is missing
     */
    private HttpBasicConfig getFromConfiguration(String clientId, HttpBasicConfig config) throws OXException {
        PropertyNameBuilder nameBuilder = new PropertyNameBuilder("com.openexchange.filestore.sproxyd.");
        ConfigurationService configService = serviceLookup.getServiceSafe(ConfigurationService.class);

        config.setMaxTotalConnections(optIntProperty(clientId, "maxConnections", DEF_MAX_CONNECTIONS, nameBuilder, configService));
        config.setMaxConnectionsPerRoute(optIntProperty(clientId, "maxConnectionsPerHost", DEF_MAX_CONNECTIONS_PER_HOST, nameBuilder, configService));
        config.setConnectTimeout(optIntProperty(clientId, "connectionTimeout", DEF_CON_TIMEOUT, nameBuilder, configService));
        config.setSocketReadTimeout(optIntProperty(clientId, "socketReadTimeout", DEF_READ_TIMEOUT, nameBuilder, configService));
        return config;
    }

}
