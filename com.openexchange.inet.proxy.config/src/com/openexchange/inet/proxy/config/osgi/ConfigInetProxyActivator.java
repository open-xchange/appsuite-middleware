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

package com.openexchange.inet.proxy.config.osgi;

import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.inet.proxy.DefaultInetProxyInformation;
import com.openexchange.inet.proxy.InetProxyService;
import com.openexchange.inet.proxy.config.ConfigInetProxyService;
import com.openexchange.inet.proxy.config.CustomProxySelector;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ConfigInetProxyActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConfigInetProxyActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ConfigInetProxyActivator}.
     */
    public ConfigInetProxyActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ConfigurationService service = getService(ConfigurationService.class);
        // Check enabled
        final boolean proxyEnabled = service.getBoolProperty("com.openexchange.inet.proxy.config.proxyEnabled", true);
        // Read proxy information from configuration
        final String proxyHost = service.getProperty("com.openexchange.inet.proxy.config.proxyHost", "localhost");
        final int proxPort = service.getIntProperty("com.openexchange.inet.proxy.config.proxyPort", 80);
        final boolean proxSecure = service.getBoolProperty("com.openexchange.inet.proxy.config.proxySecure", false);
        final List<String> nonProxyHosts = parseNonProxyHosts(service.getProperty("com.openexchange.inet.proxy.config.nonProxyHosts"));

        // http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
        //http://www.rgagnon.com/javadetails/java-0085.html

        if (proxyEnabled) {
            // Create proxy information
            final DefaultInetProxyInformation proxyInformation = new DefaultInetProxyInformation();
            proxyInformation.setHost(proxyHost);
            proxyInformation.setNonProxyHosts(nonProxyHosts);
            proxyInformation.setPort(proxPort);
            proxyInformation.setSecure(proxSecure);
            // Set ProxySelector
            final CustomProxySelector ps = new CustomProxySelector(ProxySelector.getDefault(), proxyInformation);
            ProxySelector.setDefault(ps);
            // Apply to System settings
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", Integer.toString(proxPort));
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", Integer.toString(proxPort));
            System.setProperty("ftp.proxHost", proxyHost);
            System.setProperty("ftp.proxyPort", Integer.toString(proxPort));
            if (null != nonProxyHosts && !nonProxyHosts.isEmpty()) {
                final StringBuilder sb = new StringBuilder(256);
                sb.append(nonProxyHosts.get(0));
                final int size = nonProxyHosts.size();
                for (int i = 1; i < size; i++) {
                    sb.append('|').append(nonProxyHosts.get(i));
                }
                final String sNoProxyHosts = sb.toString();
                System.setProperty("http.noProxyHosts", sNoProxyHosts);
                System.setProperty("ftp.noProxyHosts", sNoProxyHosts);
            }
            // Create service
            final ConfigInetProxyService inetProxyService = new ConfigInetProxyService(proxyEnabled, proxyInformation);
            registerService(InetProxyService.class, inetProxyService, null);
        }
    }

    private List<String> parseNonProxyHosts(final String nonProxyHosts) {
        if (com.openexchange.java.Strings.isEmpty(nonProxyHosts)) {
            return Collections.emptyList();
        }
        final String[] csv = Strings.splitByComma(nonProxyHosts);
        final List<String> ret = new ArrayList<String>(csv.length);
        for (final String nonProxyHost : csv) {
            ret.add(nonProxyHost.trim());
        }
        return ret;
    }
}
