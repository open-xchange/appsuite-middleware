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

package com.openexchange.mail.config;

import java.util.Collection;

/**
 * {@link IPRange} - An IP range of either IPv4 or IPv6 addresses.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IPRange {

    /**
     * The special NULL IP range.
     */
    public static final IPRange NULL = new IPRange(null) {

        @Override
        public boolean contains(String ipAddress) {
            return false;
        }

        @Override
        public String toString() {
            return "null";
        }
    };

    /**
     * Checks if specified IP address is contained in given collection of IP address ranges
     *
     * @param actual The IP address to check
     * @param ranges The collection of IP address ranges
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public static boolean isWhitelistedFromRateLimit(String actual, Collection<IPRange> ranges) {
        for (IPRange range : ranges) {
            if (range.contains(actual)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses specified string to an IP range.
     *
     * @param string The string to parse
     * @return The resulting IP range or <code>null</code> if passed string is empty
     * @throws IllegalArgumentException If parsing fails
     */
    public static IPRange parseRange(String string) {
        final com.openexchange.sessiond.impl.IPRange parsed = com.openexchange.sessiond.impl.IPRange.parseRange(string);
        return null == parsed ? null : new IPRange(parsed);
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private final com.openexchange.sessiond.impl.IPRange delegate;

    /**
     * Initializes a new {@link IPRange}.
     */
    IPRange(com.openexchange.sessiond.impl.IPRange delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * Checks if specified IPv4 octets are covered by configured IP range.
     *
     * @param octets The IPv4 octets to check
     * @param ipAddress The octets' IPv4 string representation; might be <code>null</code>
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsIPv4(byte[] octets, String ipAddress) {
        return delegate.containsIPv4(octets, ipAddress);
    }

    /**
     * Checks if specified IPv6 octets are covered by configured IP range.
     *
     * @param octets The IPv6 octets to check
     * @param ipAddress The octets' IPv6 string representation; might be <code>null</code>
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsIPv6(byte[] octets, String ipAddress) {
        return delegate.containsIPv6(octets, ipAddress);
    }

    /**
     * Checks if passed IP address is contained in this range.
     *
     * @param ipAddress The IP address to check
     * @return <code>true</code> if contained; else <code>false</code>
     */
    public boolean contains(String ipAddress) {
        return delegate.contains(ipAddress);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
