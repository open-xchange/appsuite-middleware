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

package com.openexchange.mailaccount.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.net.HostList;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailAccountUtils} - Utility class for mail account module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailAccountUtils {

    /**
     * Initializes a new {@link MailAccountUtils}.
     */
    private MailAccountUtils() {
        super();
    }

    private static volatile HostList blacklistedHosts;
    private static HostList blacklistedHosts() {
        HostList tmp = blacklistedHosts;
        if (null == tmp) {
            synchronized (MailAccountUtils.class) {
                tmp = blacklistedHosts;
                if (null == tmp) {
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return HostList.EMPTY;
                    }
                    String prop = service.getProperty("com.openexchange.mail.account.blacklist");
                    tmp = HostList.valueOf(prop);
                    blacklistedHosts = tmp;
                }
            }
        }
        return tmp;
    }

    private static volatile Set<Integer> allowedPorts;
    private static Set<Integer> allowedPorts() {
        Set<Integer> tmp = allowedPorts;
        if (null == tmp) {
            synchronized (MailAccountUtils.class) {
                tmp = allowedPorts;
                if (null == tmp) {
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return Collections.emptySet();
                    }
                    String prop = service.getProperty("com.openexchange.mail.account.whitelist.ports");
                    if (Strings.isEmpty(prop)) {
                        tmp = Collections.<Integer> emptySet();
                    } else {
                        String[] tokens = Strings.splitByComma(prop);
                        tmp = new HashSet<Integer>(tokens.length);
                        for (String token : tokens) {
                            if (Strings.isNotEmpty(token)) {
                                try {
                                    tmp.add(Integer.valueOf(token.trim()));
                                } catch (NumberFormatException e) {
                                    // Ignore
                                }
                            }
                        }
                    }
                    allowedPorts = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Checks if specified host name and port are denied to connect against.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostName The host name; either a machine name or a textual representation of its IP address
     * @param port The port number
     * @return <code>true</code> if denied; otherwise <code>false</code>
     * @throws OXException in case the host is blacklisted
     */
    public static boolean isDenied(String hostName, int port) throws OXException {
        if (isBlacklisted(hostName)) {
            throw MailAccountExceptionCodes.BLACKLISTED_SERVER.create(hostName);
        }
        return false == isAllowed(port);
    }

    /**
     * Checks if specified host name is black-listed.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostName The host name; either a machine name or a textual representation of its IP address
     * @return <code>true</code> if black-listed; otherwise <code>false</code>
     */
    public static boolean isBlacklisted(String hostName) {
        if (Strings.isEmpty(hostName)) {
            return false;
        }

        return blacklistedHosts().contains(hostName);
    }

    /**
     * Checks if specified port is allowed.
     *
     * @param port The port to check
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public static boolean isAllowed(int port) {
        if (port < 0) {
            // Not set; always allow
            return true;
        }

        if (port > 65535) {
            // Invalid port
            return false;
        }

        Set<Integer> allowedPorts = allowedPorts();
        return allowedPorts.isEmpty() ? true : allowedPorts.contains(Integer.valueOf(port));
    }

}
