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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.proxy.authenticator.impl;

import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.proxy.authenticator.DefaultPasswordAuthenticationProvider;
import com.openexchange.proxy.authenticator.PasswordAuthenticationProvider;

/**
 * {@link ReportClientUtil} provides {@link PasswordAuthenticationProvider} for the report client to ensure compatibility with the old configuration.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ReportClientUtil {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ReportClientUtil.class);

    private ReportClientUtil() {
       // prevents initialization
    }

    /**
     * Returns the needed {@link PasswordAuthenticationProvider} for the report client or null
     *
     * @return The providers or null
     */
    public static PasswordAuthenticationProvider getProvider(ConfigurationService configurationService) {
        if(isProxyPasswordAuthenticationProviderEnabled(configurationService)) {
            String user = getProxyUsername(configurationService);
            String password = getProxyPassword(configurationService);
            String host = getProxyAddress(configurationService);
            int port = getProxyPort(configurationService);

            if(validProxySettings(host, port, user, password)) {
                return new DefaultPasswordAuthenticationProvider("https", host, port, user, password);
            } else {
                LOG.warn("The configured report client proxy configuration is invalid. Please review your configuration.");
            }
        }
        return null;
    }

    private static boolean isProxyPasswordAuthenticationProviderEnabled(ConfigurationService configurationService) {
        return configurationService.getBoolProperty("com.openexchange.report.client.proxy.useproxy", false) && configurationService.getBoolProperty("com.openexchange.report.client.proxy.authrequired", false);
    }

    private static String getProxyPassword(ConfigurationService configurationService) {
        return configurationService.getProperty("com.openexchange.report.client.proxy.password");
    }

    private static String getProxyUsername(ConfigurationService configurationService) {
        return configurationService.getProperty("com.openexchange.report.client.proxy.username");
    }

    private static String getProxyAddress(ConfigurationService configurationService) {
        return configurationService.getProperty("com.openexchange.report.client.proxy.address");
    }

    private static int getProxyPort(ConfigurationService configurationService) {
        return configurationService.getIntProperty("com.openexchange.report.client.proxy.port", 80);
    }

    /**
     * Checks if the given proxy configuration is valid
     *
     * @param host The host
     * @param port The port
     * @param user The proxy user
     * @param password the proxy password
     * @return true if it is valid, false otherwise
     */
    private static boolean validProxySettings(String host, int port, String user, String password) {
        return Strings.isNotEmpty(host) && port > 0 && Strings.isNotEmpty(user) && Strings.isNotEmpty(password);
    }

}
