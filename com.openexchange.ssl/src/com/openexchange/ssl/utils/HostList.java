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

package com.openexchange.ssl.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.java.IPAddressUtil;
import com.openexchange.java.Strings;

/**
 * 
 * {@link HostList}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class HostList {

    /**
     * The empty host list.
     */
    public static final HostList EMPTY = new HostList(Collections.<IPRange> emptyList(), Collections.<String> emptySet(), Collections.<String> emptySet());

    /**
     * Accepts a comma-separated list of IP addresses, IP address ranges, and host names.
     *
     * @param hostList The host list to parse
     * @return The resulting {@link HostList} instance
     * @throws IllegalArgumentException If parsing fails
     */
    public static HostList valueOf(String hostList) {
        if (Strings.isEmpty(hostList)) {
            return EMPTY;
        }

        String[] tokens = Strings.splitByComma(hostList);
        Set<String> matchingHostNames = new HashSet<String>(tokens.length);
        Set<String> matchingAppendixHostNames = new HashSet<String>(tokens.length);
        List<IPRange> ipRanges = new ArrayList<IPRange>(tokens.length);
        for (String token : tokens) {
            if (false == Strings.isEmpty(token)) {
                token = Strings.asciiLowerCase(token);
                boolean isIp = false;
                try {
                    ipRanges.add(IPRange.parseRange(token));
                    isIp = true;
                } catch (IllegalArgumentException e) {
                    // Apparently no IP address
                }

                if (false == isIp) {
                    if (token.startsWith("*")) {
                        // Wild-card host name; e.g. "*.open-xchange.com"
                        String appendixToken = token.substring(1);
                        if (Strings.isEmpty(appendixToken) || appendixToken.indexOf('*') >= 0) {
                            throw new IllegalArgumentException("Invalid wild-card host name: " + token);
                        }
                        matchingAppendixHostNames.add(appendixToken);
                    } else {
                        if (token.indexOf('*') > 0) {
                            // Wild-card only allowed at first position
                            throw new IllegalArgumentException("Invalid wild-card host name: " + token);
                        }
                        // Exact match
                        matchingHostNames.add(token);
                    }
                }
            }
        }

        return new HostList(ipRanges, matchingAppendixHostNames, matchingHostNames);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final List<IPRange> ipRanges;
    private final Set<String> matchingAppendixHostNames;
    private final Set<String> matchingHostNames;

    /**
     * Initializes a new {@link HostList}.
     */
    private HostList(List<IPRange> ipRanges, Set<String> matchingAppendixHostNames, Set<String> matchingHostNames) {
        super();
        this.ipRanges = ipRanges;
        this.matchingAppendixHostNames = matchingAppendixHostNames.isEmpty() ? null : matchingAppendixHostNames;
        this.matchingHostNames = matchingHostNames;
    }

    /**
     * Checks if specified host name is contained in this host list.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostName The host name; either a machine name or a textual representation of its IP address
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(String hostName) {
        if (Strings.isEmpty(hostName)) {
            return false;
        }

        String toCheck = Strings.asciiLowerCase(hostName);

        // Test for IP address
        byte[] octets = IPAddressUtil.textToNumericFormatV4(toCheck);
        if (octets != null) {
            // IPv4
            for (IPRange ipRange : this.ipRanges) {
                if (ipRange.containsIPv4(octets, toCheck)) {
                    return true;
                }
            }
        }

        octets = IPAddressUtil.textToNumericFormatV6(toCheck);
        if (octets != null) {
            // IPv6
            for (IPRange ipRange : this.ipRanges) {
                if (ipRange.containsIPv6(octets, toCheck)) {
                    return true;
                }
            }
        }

        if (this.matchingAppendixHostNames != null) {
            for (String appendixHostName : this.matchingAppendixHostNames) {
                if (toCheck.endsWith(appendixHostName)) {
                    return true;
                }
            }
        }
        return this.matchingHostNames.contains(Strings.asciiLowerCase(toCheck));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32);
        builder.append("[");
        if (this.ipRanges != null) {
            builder.append("ipRanges=").append(this.ipRanges).append(", ");
        }
        if (this.matchingAppendixHostNames != null) {
            builder.append("wild-card_hostNames=[");
            Iterator<String> it = this.matchingAppendixHostNames.iterator();
            if (it.hasNext()) {
                builder.append('*').append(it.next());
                while (it.hasNext()) {
                    builder.append(", ").append('*').append(it.next());
                }
            }
            builder.append("], ");
        }
        if (this.matchingHostNames != null) {
            builder.append("hostNames=").append(this.matchingHostNames);
        }
        builder.append("]");
        return builder.toString();
    }
}
