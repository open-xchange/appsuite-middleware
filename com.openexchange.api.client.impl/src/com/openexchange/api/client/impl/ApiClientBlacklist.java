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

package com.openexchange.api.client.impl;

import static com.openexchange.java.Autoboxing.I;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.Nullable;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;

/**
 * {@link ApiClientBlacklist}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ApiClientBlacklist {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClientBlacklist.class);

    private final static Property BLACKLIST = DefaultProperty.valueOf("com.openexchange.api.client.blacklistedHosts", "127.0.0.1-127.255.255.255,localhost");

    private final static Property ALLOWED_PORTS = DefaultProperty.valueOf("com.openexchange.api.client.allowedPorts", "");

    /**
     * Checks if specified host name is black-listed.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     * 
     * @param configurationService The lean config service to get configuration from
     * @param url The URl to check against blacklisted hosts or ports
     * @param contextId The context ID
     * @param userId The user ID
     * @return <code>true</code> if black-listed; otherwise <code>false</code>
     */
    public static boolean isBlacklisted(@Nullable LeanConfigurationService configurationService, URL url, int contextId, int userId) {
        if (Strings.isEmpty(url.getHost())) {
            return true;
        }
        return getBlacklistedHosts(configurationService, contextId, userId).contains(url.getHost());
    }

    /**
     * Checks if specified port is allowed
     * <p>
     * 
     * @param configurationService The lean config service to get configuration from
     * @param url The URl to check the against allowed ports
     * @param contextId The context ID
     * @param userId The user ID
     * @return <code>true</code> if port is allowed; otherwise <code>false</code>
     */
    public static boolean isPortAllowed(@Nullable LeanConfigurationService configurationService, URL url, int contextId, int userId) {
        if (isPortAllowed(url)) {
            Set<Integer> allowedPorts = getAllowedPorts(configurationService, contextId, userId);
            return allowedPorts.isEmpty() ? true : allowedPorts.contains(I(url.getPort()));
        }
        return false;
    }

    /**
     * Get the blacklisted hosts for the user
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @return The list of blacklisted hosts for the user
     */
    private static HostList getBlacklistedHosts(LeanConfigurationService configurationService, int contextId, int userId) {
        String blackListed;
        if (null == configurationService) {
            blackListed = BLACKLIST.getDefaultValue(String.class);
        } else {
            blackListed = configurationService.getProperty(userId, contextId, BLACKLIST);
        }
        return HostList.valueOf(blackListed);
    }

    /**
     * Get the blacklisted hosts for the user
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @return The list of blacklisted hosts for the user
     */
    private static Set<Integer> getAllowedPorts(LeanConfigurationService configurationService, int contextId, int userId) {
        String portString;
        if (null == configurationService) {
            portString = ALLOWED_PORTS.getDefaultValue(String.class);
        } else {
            portString = configurationService.getProperty(userId, contextId, ALLOWED_PORTS);
        }
        return parsePortString(portString);
    }

    /**
     * Parses the given port string which contains the allowed ports as a comma separated list to a {@link Set} of ports.
     *
     * @param portString The comma separated ports
     * @return An {@link Set} of ports
     */
    private static Set<Integer> parsePortString(String portString) {
        if (Strings.isEmpty(portString)) {
            return Collections.emptySet();
        }
        String[] ports = Strings.splitByComma(portString);
        HashSet<Integer> ret = new HashSet<Integer>(ports.length);
        for (String port : ports) {
            if (Strings.isNotEmpty(port)) {
                try {
                    ret.add(Integer.valueOf(port.trim()));
                } catch (NumberFormatException e) {
                    LOGGER.error("Ignored unkown port number " + port, e);
                }
            }
        }
        return ret;
    }

    /**
     * Checks if the given port is allowed
     *
     * @param URL The URL to check
     * @return <code>true</code> if the given port of the URL is allowed <code>false</code> otherwise
     */
    private static boolean isPortAllowed(URL url) {
        if (url == null) {
            return false;
        }
        int port = url.getPort();
        if (port < 0) {
            String protocol = Strings.asciiLowerCase(url.getProtocol());
            if (Strings.isEmpty(protocol)) {
                // Assume HTTP as default
                port = 80;
            } else {
                protocol = protocol.trim();
                if ("https".equals(protocol)) {
                    port = 443;
                } else {
                    port = 80;
                }
            }
        }

        if (port > 65535) {
            // invalid port
            return false;
        }
        return true;
    }

}
