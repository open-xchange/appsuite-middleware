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
                throw new IllegalArgumentException("Unparseable port number in : " + hostNameString, e);
            }
            hostName = hostName.substring(0, pos);
        }

        return new HostAndPort(hostName, port);
    }
}
