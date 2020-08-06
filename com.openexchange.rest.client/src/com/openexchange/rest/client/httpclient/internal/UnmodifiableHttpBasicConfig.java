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

package com.openexchange.rest.client.httpclient.internal;

import com.openexchange.rest.client.httpclient.HttpBasicConfig;

/**
 * {@link UnmodifiableHttpBasicConfig} - Unmodifiable version of the {@link HttpBasicConfig}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class UnmodifiableHttpBasicConfig implements HttpBasicConfig {

    private final int socketReadTimeout;
    private final int connectTimeout;
    private final int connectionRequestTimeout;
    private final int maxTotalConnections;
    private final int maxConnectionsPerRoute;
    private final int keepAliveDuration;
    private final int keepAliveMonitorInterval;
    private final int socketBufferSize;

    /**
     * Initializes a new {@link UnmodifiableHttpBasicConfig}.
     * 
     * @param config The configuration to clone
     */
    public UnmodifiableHttpBasicConfig(HttpBasicConfig config) {
        super();
        this.socketReadTimeout = config.getSocketReadTimeout();
        this.connectTimeout = config.getConnectTimeout();
        this.connectionRequestTimeout = config.getConnectionRequestTimeout();
        this.maxTotalConnections = config.getMaxTotalConnections();
        this.maxConnectionsPerRoute = config.getMaxConnectionsPerRoute();
        this.keepAliveDuration = config.getKeepAliveDuration();
        this.keepAliveMonitorInterval = config.getKeepAliveMonitorInterval();
        this.socketBufferSize = config.getSocketBufferSize();
    }

    @Override
    public HttpBasicConfig setSocketReadTimeout(int socketReadTimeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpBasicConfig setConnectTimeout(int connectTimeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpBasicConfig setConnectionRequestTimeout(int connectionRequestTimeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpBasicConfig setMaxTotalConnections(int maxTotalConnections) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpBasicConfig setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpBasicConfig setKeepAliveDuration(int keepAliveDuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpBasicConfig setKeepAliveMonitorInterval(int keepAliveMonitorInterval) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpBasicConfig setSocketBufferSize(int socketBufferSize) {
        throw new UnsupportedOperationException();
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
        return "UnmodifiableHttpBasicConfig [socketReadTimeout=" + socketReadTimeout + ", connectTimeout=" + connectTimeout + ", connectionRequestTimeout=" + connectionRequestTimeout
            + ", maxTotalConnections=" + maxTotalConnections + ", maxConnectionsPerRoute=" + maxConnectionsPerRoute + ", keepAliveDuration=" + keepAliveDuration
            + ", keepAliveMonitorInterval=" + keepAliveMonitorInterval + ", socketBufferSize=" + socketBufferSize + "]";
        //@formatter:on
    }

}
