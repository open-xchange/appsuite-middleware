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

package com.openexchange.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
    public static final HostList EMPTY = new HostList(Collections.<IPRange> emptyList(), Collections.<String> emptySet(), Collections.<String> emptySet(), "") {

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(InetAddress hostAddress) {
            return false;
        }

        @Override
        public boolean contains(String hostName) {
            return false;
        }
    };

    private static final ConcurrentMap<String, HostList> CACHE = new ConcurrentHashMap<>(32, 0.9F, 1);

    /**
     * Flushes internal cache.
     */
    public static void flushCache() {
        CACHE.clear();
    }

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

        HostList instance = CACHE.get(hostList);
        if (instance == null) {
            HostList newInstance = valueOf0(hostList);
            instance = CACHE.putIfAbsent(hostList, newInstance);
            if (instance == null) {
                instance = newInstance;
            }
        }
        return instance;
    }

    private static HostList valueOf0(String hostList) {
        String[] tokens = Strings.splitByComma(hostList);
        Set<String> matchingHostNames = new HashSet<String>(tokens.length);
        Set<String> matchingAppendixHostNames = new HashSet<String>(tokens.length);
        List<IPRange> ipRanges = new ArrayList<IPRange>(tokens.length);
        for (String token : tokens) {
            if (Strings.isNotEmpty(token)) {
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

        return ipRanges.isEmpty() && matchingAppendixHostNames.isEmpty() && matchingHostNames.isEmpty() ?
            EMPTY : new HostList(ipRanges, matchingAppendixHostNames, matchingHostNames, hostList);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final List<IPRange> ipRanges;
    private final Set<String> matchingAppendixHostNames;
    private final Set<String> matchingHostNames;
    private final String hostList;

    /**
     * Initializes a new {@link HostList}.
     */
    HostList(List<IPRange> ipRanges, Set<String> matchingAppendixHostNames, Set<String> matchingHostNames, String hostList) {
        super();
        this.ipRanges = ImmutableList.copyOf(ipRanges);
        this.hostList = hostList;
        this.matchingAppendixHostNames = ImmutableSet.copyOf(matchingAppendixHostNames);
        this.matchingHostNames = ImmutableSet.copyOf(matchingHostNames);
    }

    private final static int INADDR4SZ = 4;
    private final static int INADDR16SZ = 16;
    private final static int INT16SZ = 2;

    static String numericToTextFormatV4(byte[] src) {
        StringBuilder sb = new StringBuilder(16);
        sb.append((src[0] & 0xff)).append('.');
        sb.append((src[1] & 0xff)).append('.');
        sb.append((src[2] & 0xff)).append('.');
        sb.append((src[3] & 0xff));
        return sb.toString();
    }

    static String numericToTextFormatV6(byte[] src) {
        StringBuffer sb = new StringBuffer(39);
        for (int i = 0; i < (INADDR16SZ / INT16SZ); i++) {
            sb.append(Integer.toHexString(((src[i << 1] << 8) & 0xff00) | (src[(i << 1) + 1] & 0xff)));
            if (i < (INADDR16SZ / INT16SZ) - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    /**
     * Checks if this host list is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return ipRanges.isEmpty() && matchingAppendixHostNames.isEmpty() && matchingHostNames.isEmpty();
    }

    /**
     * Gets the string representing the host list from which this instance was parsed.
     *
     * @return The host string
     */
    public String getHostList() {
        return hostList;
    }

    /**
     * Checks if specified host name is contained in this host list.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostAddress The host address
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(InetAddress hostAddress) {
        return contains(hostAddress, true);
    }

    private boolean contains(InetAddress hostAddress, boolean checkHostName) {
        if (null == hostAddress) {
            return false;
        }

        byte[] octets = hostAddress.getAddress();
        if (null != octets) {
            if (INADDR4SZ == octets.length) {
                // IPv4
                for (IPRange ipRange : this.ipRanges) {
                    if (ipRange.containsIPv4(octets, numericToTextFormatV4(octets))) {
                        return true;
                    }
                }
            } else if (INADDR16SZ == octets.length) {
                // IPv6
                for (IPRange ipRange : this.ipRanges) {
                    if (ipRange.containsIPv6(octets, numericToTextFormatV6(octets))) {
                        return true;
                    }
                }
            }
        }

        return checkHostName ? contains(hostAddress.getHostName()) : false;
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
            if (octets.length == 4) {
                // IPv4 mapped IPv6 address
                for (IPRange ipRange : this.ipRanges) {
                    if (ipRange.containsIPv4(octets, toCheck)) {
                        return true;
                    }
                }
            }

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

        if (this.matchingHostNames.contains(toCheck)) {
            return true;
        }

        // Need to resolve as last resort
        try {
            return contains(InetAddress.getByName(toCheck), false);
        } catch (UnknownHostException e) {
            // Cannot be resolved
            return false;
        }
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
