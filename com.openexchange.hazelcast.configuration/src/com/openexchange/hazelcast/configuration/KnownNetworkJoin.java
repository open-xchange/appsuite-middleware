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
 *    trademarks of the OX Software GmbH group of companies.
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
