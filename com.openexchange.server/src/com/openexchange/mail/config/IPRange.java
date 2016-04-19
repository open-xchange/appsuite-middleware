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
    public static IPRange parseRange(final String string) {
        final com.openexchange.sessiond.impl.IPRange parsed = com.openexchange.sessiond.impl.IPRange.parseRange(string);
        return null == parsed ? null : new IPRange(parsed);
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private final com.openexchange.sessiond.impl.IPRange delegate;

    /**
     * Initializes a new {@link IPRange}.
     */
    IPRange(final com.openexchange.sessiond.impl.IPRange delegate) {
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
    public boolean contains(final String ipAddress) {
        return delegate.contains(ipAddress);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
