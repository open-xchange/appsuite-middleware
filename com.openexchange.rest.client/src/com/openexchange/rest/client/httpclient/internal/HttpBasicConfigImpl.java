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

package com.openexchange.rest.client.httpclient.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Optional;
import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.rest.client.httpclient.HttpClientProperty;

/**
 * {@link HttpBasicConfigImpl} - Represents the basic configuration for a HTTP client.
 * <p>
 * Contains only values a administrator can modify.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
@NotThreadSafe
public class HttpBasicConfigImpl implements HttpBasicConfig {

    /**
     * Creates a new instance of {@link HttpBasicConfigImpl}.
     *
     * @param optionalLeanService The optional service to obtain the default configuration from
     * @return The {@code HttpBasicConfigImpl} instance
     */
    public static HttpBasicConfigImpl createInstance(Optional<LeanConfigurationService> optionalLeanService) {
        HttpBasicConfigImpl instance = new HttpBasicConfigImpl();
        if (optionalLeanService.isPresent()) {
            // Use passed service
            LeanConfigurationService service = optionalLeanService.get();
            for (HttpClientProperty property : HttpClientProperty.values()) {
                // Read from default configuration
                property.setInConfig(instance, I(service.getIntProperty(property.getProperty())));
            }
        } else {
            for (HttpClientProperty property : HttpClientProperty.values()) {
                // Apply defaults
                property.setInConfig(instance, null);
            }
        }
        return instance;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private int socketReadTimeout;
    private int connectTimeout;
    private int connectionRequestTimeout;
    private int maxTotalConnections;
    private int maxConnectionsPerRoute;
    private int keepAliveDuration;
    private int keepAliveMonitorInterval;
    private int socketBufferSize;

    /**
     * Initializes a new {@link HttpBasicConfigImpl}.
     */
    private HttpBasicConfigImpl() {
        super();
    }

    @Override
    public HttpBasicConfig setSocketReadTimeout(int socketReadTimeout) {
        this.socketReadTimeout = socketReadTimeout;
        return this;
    }

    @Override
    public HttpBasicConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public HttpBasicConfig setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
        return this;
    }

    @Override
    public HttpBasicConfig setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
        return this;
    }

    @Override
    public HttpBasicConfig setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        return this;
    }

    @Override
    public HttpBasicConfig setKeepAliveDuration(int keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
        return this;
    }

    @Override
    public HttpBasicConfig setKeepAliveMonitorInterval(int keepAliveMonitorInterval) {
        this.keepAliveMonitorInterval = keepAliveMonitorInterval;
        return this;
    }

    @Override
    public HttpBasicConfig setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
        return this;
    }

    @Override
    public int getSocketReadTimeout() {
        return socketReadTimeout;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    @Override
    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    @Override
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    @Override
    public int getKeepAliveDuration() {
        return keepAliveDuration;
    }

    @Override
    public int getKeepAliveMonitorInterval() {
        return keepAliveMonitorInterval;
    }

    @Override
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    @Override
    public int hashCode() {
        final int prime = 131;
        int result = 1;
        result = prime * result + connectionRequestTimeout;
        result = prime * result + connectTimeout;
        result = prime * result + keepAliveDuration;
        result = prime * result + keepAliveMonitorInterval;
        result = prime * result + maxConnectionsPerRoute;
        result = prime * result + maxTotalConnections;
        result = prime * result + socketBufferSize;
        result = prime * result + socketReadTimeout;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HttpBasicConfig)) {
            return false;
        }
        HttpBasicConfig other = (HttpBasicConfig) obj;
        if (connectionRequestTimeout != other.getConnectionRequestTimeout()) {
            return false;
        }
        if (connectTimeout != other.getConnectTimeout()) {
            return false;
        }
        if (keepAliveDuration != other.getKeepAliveDuration()) {
            return false;
        }
        if (keepAliveMonitorInterval != other.getKeepAliveMonitorInterval()) {
            return false;
        }
        if (maxConnectionsPerRoute != other.getMaxConnectionsPerRoute()) {
            return false;
        }
        if (maxTotalConnections != other.getMaxTotalConnections()) {
            return false;
        }
        if (socketBufferSize != other.getSocketBufferSize()) {
            return false;
        }
        if (socketReadTimeout != other.getSocketReadTimeout()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        //@formatter:off
        return "HttpBasicConfig [socketReadTimeout=" + socketReadTimeout + ", connectTimeout=" + connectTimeout + ", connectionRequestTimeout=" + connectionRequestTimeout
            + ", maxTotalConnections=" + maxTotalConnections + ", maxConnectionsPerRoute=" + maxConnectionsPerRoute + ", keepAliveDuration=" + keepAliveDuration
            + ", keepAliveMonitorInterval=" + keepAliveMonitorInterval + ", socketBufferSize=" + socketBufferSize + "]";
        //@formatter:on
    }

}
