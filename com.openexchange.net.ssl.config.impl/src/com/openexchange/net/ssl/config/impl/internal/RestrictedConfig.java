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
