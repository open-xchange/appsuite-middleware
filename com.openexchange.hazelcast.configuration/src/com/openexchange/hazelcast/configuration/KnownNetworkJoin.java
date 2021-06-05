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

package com.openexchange.hazelcast.configuration;

import com.openexchange.java.Strings;

/**
 * {@link KnownNetworkJoin} - An enumeration of known network joins.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public enum KnownNetworkJoin {

    /**
     * Empty. No discovery for single-node setups.
     */
    EMPTY("empty"),
    /**
     * Fixed set of cluster member nodes given by configuration.
     */
    STATIC("static"),
    /**
     * Automatic discovery of other nodes via multicast.
     */
    MULTICAST("multicast"),
    /**
     * AWS discovery mechanism.
     */
    AWS("aws"),
    /**
     * Consult a DNS server to resolve the domain names to the most recent set of IP addresses of all service nodes.
     */
    DNS("dns"),
    /**
     * Automatically joins with other nodes in a kubernetes cluster
     */
    KUBERNETES("kubernetes"),
    ;

    private final String identifier;

    private KnownNetworkJoin(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the network join for given identifier.
     *
     * @param join The identifier to look-up
     * @return The appropriate network join or <code>null</code>
     */
    public static KnownNetworkJoin networkJoinFor(String join) {
        if (Strings.isEmpty(join)) {
            return null;
        }

        String lookUp = Strings.asciiLowerCase(join);
        for (KnownNetworkJoin networkJoin : KnownNetworkJoin.values()) {
            if (lookUp.equals(networkJoin.identifier)) {
                return networkJoin;
            }
        }
        return null;
    }

}
