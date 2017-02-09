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