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

package com.openexchange.net.ssl.config.impl.internal;

import com.openexchange.java.Strings;
import com.openexchange.net.HostList;

/**
 * {@link RestrictedConfig} - The immutable configuration representation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class RestrictedConfig {

    private final String[] protocols;
    private final String[] ciphers;
    private final HostList whitelistedHosts;

    RestrictedConfig(String[] protocols, String[] ciphers, HostList whitelistedHosts) {
        super();
        this.protocols = protocols;
        this.ciphers = ciphers;
        this.whitelistedHosts = whitelistedHosts;
    }

    /**
     * Gets the protocols
     *
     * @return The protocols
     */
    public String[] getProtocols() {
        return protocols;
    }

    /**
     * Gets the ciphers
     *
     * @return The ciphers
     */
    public String[] getCiphers() {
        return ciphers;
    }

    /**
     * Gets the white-listed hosts
     *
     * @return The white-listed hosts
     */
    public HostList getWhitelistedHosts() {
        return whitelistedHosts;
    }

    /**
     * Checks if specified host name is white-listed.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostName The host name; either a machine name or a textual representation of its IP address
     * @return <code>true</code> if white-listed; otherwise <code>false</code>
     */
    public boolean isWhitelisted(String hostName) {
        if (Strings.isEmpty(hostName)) {
            return false;
        }
        return whitelistedHosts.contains(hostName);
    }

    /**
     * Checks if one of the specified host names is white-listed.
     * <p>
     * The host names can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostNames The host names as an array; either a machine name or a textual representation of its IP address
     * @return <code>true</code> if at least one of the hosts is white-listed; otherwise <code>false</code>
     */
    public boolean isWhitelisted(String... hostNames) {
        for (String hostName : hostNames) {
            boolean whitelisted = isWhitelisted(hostName);
            if (whitelisted) {
                return true;
            }
        }
        return false;
    }

}
