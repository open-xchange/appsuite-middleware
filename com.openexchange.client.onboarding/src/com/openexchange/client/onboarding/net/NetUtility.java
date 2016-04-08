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

package com.openexchange.client.onboarding.net;

import com.openexchange.java.Strings;

/**
 * {@link NetUtility} - A utility class for network-related parsing/processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class NetUtility {

    /**
     * Initializes a new {@link NetUtility}.
     */
    private NetUtility() {
        super();
    }

    /**
     * Checks if specified host name string implies SSL; assuming <code>true</code> if host name string provides no protocol/scheme part.
     *
     * @param hostNameString The host name string to examine; e.g. <code>"dav.example.com"</code>, <code>"http://dav.example.com/mydav"</code>
     * @return <code>true</code> if SSL is implied; otherwise <code>false</code>
     */
    public static boolean impliesSsl(String hostNameString) {
        return impliesSsl(hostNameString, true);
    }

    /**
     * Checks if specified host name string implies SSL; assuming <code>defaultValue</code> if host name string provides no protocol/scheme part.
     *
     * @param hostNameString The host name string to examine; e.g. <code>"dav.example.com"</code>, <code>"http://dav.example.com/mydav"</code>
     * @param defaultValue The default value to return in case host name string contains no protocol/scheme information
     * @return <code>true</code> if SSL is implied; otherwise <code>false</code>
     */
    public static boolean impliesSsl(String hostNameString, boolean defaultValue) {
        if (Strings.isEmpty(hostNameString)) {
            return defaultValue;
        }

        String toExamine = Strings.asciiLowerCase(hostNameString);
        if (toExamine.startsWith("http://")) {
            return false;
        }
        return toExamine.startsWith("https://") ? true : defaultValue;
    }

    /**
     * Parses given host name string to its immutable {@link HostAndPort} representation.
     *
     * @param hostNameString The host name string to parse; e.g. <code>"dav.example.com:8843"</code>, <code>"http://dav.example.com/mydav"</code>
     * @return The appropriate {@code HostAndPort} instance
     * @throws IllegalArgumentException If host name string is empty or port cannot be parsed to a number
     */
    public static HostAndPort parseHostNameString(String hostNameString) {
        if (Strings.isEmpty(hostNameString)) {
            throw new IllegalArgumentException("Empty host name string");
        }

        // Drop scheme part
        String hostName = hostNameString.trim();
        if (hostName.startsWith("http://")) {
            hostName = hostName.substring(7);
        } else if (hostName.startsWith("https://")) {
            hostName = hostName.substring(8);
        }

        // Strip path information
        int pos = hostName.indexOf('/');
        if (pos > 0) {
            hostName = hostName.substring(0, pos);
        }

        // Check for possible port
        int port = 0;
        pos = hostName.indexOf(':');
        if (pos > 0) {
            try {
                port = Integer.parseInt(hostName.substring(pos + 1));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unparseable port number in : " + hostNameString);
            }
            hostName = hostName.substring(0, pos);
        }

        return new HostAndPort(hostName, port);
    }
}
