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

package com.openexchange.ajax.ipcheck.internal;

import java.util.Queue;
import com.openexchange.ajax.ipcheck.IPCheckConfiguration;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.sessiond.impl.SubnetMask;

/**
 * The user-specific IP check configuration.
 */
public final class DefaultIPCheckConfiguration implements IPCheckConfiguration {

    /**
     * Creates a new builder
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builds a new instance of {@link DefaultGeoInformation} */
    public static class Builder {

        private Queue<IPRange> ranges;
        private SubnetMask allowedSubnet;
        private ClientWhitelist clientWhitelist;

        /**
         * Initializes a new {@link DefaultGeoInformation.Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the IP ranges
         *
         * @param continent The IP ranges to set
         * @return This builder
         */
        public Builder ranges(Queue<IPRange> ranges) {
            this.ranges = ranges;
            return this;
        }

        /**
         * Sets the client white-list
         *
         * @param country The client white-list
         * @return This builder
         */
        public Builder clientWhitelist(ClientWhitelist clientWhitelist) {
            this.clientWhitelist = clientWhitelist;
            return this;
        }

        /**
         * Sets the allowed sub-net
         *
         * @param city The allowed sub-net to set
         * @return This builder
         */
        public Builder allowedSubnet(SubnetMask allowedSubnet) {
            this.allowedSubnet = allowedSubnet;
            return this;
        }

        /**
         * Creates the new (immutable) {@link DefaultIPCheckConfiguration} instance from this builder's attributes
         *
         * @return The new {@link DefaultIPCheckConfiguration} instance
         */
        public DefaultIPCheckConfiguration build() {
            return new DefaultIPCheckConfiguration(ranges, clientWhitelist, allowedSubnet);
        }
    }

    // ---------------------------------------------------------------------------------

    private final Queue<IPRange> ranges;
    private final SubnetMask allowedSubnet;
    private final ClientWhitelist clientWhitelist;

    /**
     * Initializes a new {@link DefaultIPCheckConfiguration}.
     */
    DefaultIPCheckConfiguration(Queue<IPRange> ranges, ClientWhitelist clientWhitelist, SubnetMask allowedSubnet) {
        super();
        this.ranges = ranges;
        this.clientWhitelist = clientWhitelist;
        this.allowedSubnet = allowedSubnet;
    }

    @Override
    public Queue<IPRange> getRanges() {
        return ranges;
    }

    @Override
    public ClientWhitelist getClientWhitelist() {
        return clientWhitelist;
    }

    @Override
    public SubnetMask getAllowedSubnet() {
        return allowedSubnet;
    }

}