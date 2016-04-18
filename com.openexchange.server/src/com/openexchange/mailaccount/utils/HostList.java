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

package com.openexchange.mailaccount.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.java.Strings;
import com.openexchange.mail.config.IPRange;

/**
 * {@link HostList} - Represents a list of host names.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class HostList {

    /**
     * The empty host list.
     */
    public static final HostList EMPTY = new HostList(IPRange.NULL, Collections.<String> emptySet());

    /**
     * Accepts a comma-separated list of IP addresses, IP address ranges, and host names.
     *
     * @param hostList The host list to parse
     * @return The resulting {@link HostList} instance
     */
    public static HostList valueOf(String hostList) {
        if (Strings.isEmpty(hostList)) {
            return EMPTY;
        }

        String[] tokens = Strings.splitByComma(hostList);
        Set<String> hostNames = new HashSet<String>(tokens.length);
        StringBuilder ipRanges = new StringBuilder(hostList.length());
        for (String token : tokens) {
            if (false == Strings.isEmpty(token)) {
                token = Strings.asciiLowerCase(token);
                char firstChar = token.charAt(0);
                if (Strings.isDigit(firstChar) || firstChar == '[') {
                    if (ipRanges.length() > 0) {
                        ipRanges.append(',');
                    }
                    ipRanges.append(token);
                } else {
                    hostNames.add(token);
                }
            }
        }

        return new HostList(ipRanges.length() > 0 ? IPRange.parseRange(ipRanges.toString()) : IPRange.NULL, hostNames);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final IPRange ipRange;
    private final Set<String> hostNames;

    /**
     * Initializes a new {@link HostList}.
     */
    private HostList(IPRange ipRange, Set<String> hostNames) {
        super();
        this.ipRange = ipRange;
        this.hostNames = hostNames;
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

        char firstChar = hostName.charAt(0);
        if (Strings.isDigit(firstChar) || firstChar == '[') {
            // Expect IP address
            return ipRange.contains(hostName);
        }

        return hostNames.contains(Strings.asciiLowerCase(hostName));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32);
        builder.append("[");
        if (ipRange != null) {
            builder.append("ipRange=[").append(ipRange).append("], ");
        }
        if (hostNames != null) {
            builder.append("hostNames=").append(hostNames);
        }
        builder.append("]");
        return builder.toString();
    }

}
