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

package com.openexchange.hazelcast.dns;

/**
 * {@link HazelcastDnsResolverConfig} - The configuration for a Hazelcast DNS resolver.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class HazelcastDnsResolverConfig {

    /** The default port for DNS server */
    public static final int DEFAULT_PORT = 53;

    /**
     * Creates a new builder.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>HazelcastDnsResolverConfig</code> */
    public static class Builder {

        private String resolverHost;
        private int resolverPort;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the resolver host
         *
         * @param resolverHost The resolver host to set or <code>null</code>
         * @return This builder
         */
        public Builder withResolverHost(String resolverHost) {
            this.resolverHost = resolverHost;
            return this;
        }

        /**
         * Sets the resolver port
         *
         * @param resolverPort The resolver port to set or <code>-1</code>
         * @return This builder
         */
        public Builder withResolverPort(int resolverPort) {
            this.resolverPort = resolverPort;
            return this;
        }

        /**
         * Builds the instance of <code>HazelcastDnsResolverConfig</code> from this builders attributes.
         *
         * @return The <code>HazelcastDnsResolverConfig</code> instance
         */
        public HazelcastDnsResolverConfig build() {
            return new HazelcastDnsResolverConfig(resolverHost, resolverPort);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String resolverHost;
    private final int resolverPort;

    /**
     * Initializes a new {@link HazelcastDnsResolverConfig}.
     */
    HazelcastDnsResolverConfig(String resolverHost, int resolverPort) {
        super();
        this.resolverHost = resolverHost;
        this.resolverPort = resolverPort <= 0 ? -1 : resolverPort;
    }

    /**
     * Gets the optional resolver host
     *
     * @return The resolver host or <code>null</code>
     */
    public String getResolverHost() {
        return resolverHost;
    }

    /**
     * Gets the optional resolver port
     *
     * @return The resolver port or <code>-1</code>
     */
    public int getResolverPort() {
        return resolverPort;
    }

}
