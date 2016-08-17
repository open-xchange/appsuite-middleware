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
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.HostList;

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
                            if (false == Strings.isEmpty(token)) {
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
     */
    public static boolean isDenied(String hostName, int port) {
        return false == isAllowed(port) || isBlacklisted(hostName);
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
