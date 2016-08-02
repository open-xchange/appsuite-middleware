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

package com.openexchange.ssl.osgi;

import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.ssl.SSLSocketFactoryProvider;
import com.openexchange.ssl.apache.DefaultHostnameVerifier;
import com.openexchange.ssl.config.SSLProperties;
import com.openexchange.ssl.internal.SSLPropertiesReloadable;

/**
 * 
 * {@link SSLActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class SSLActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            org.slf4j.LoggerFactory.getLogger(SSLActivator.class).info("starting bundle: \"com.openexchange.ssl\"");
            Services.setBundleContext(this.context);

            ConfigurationService configService = getService(ConfigurationService.class);
            if (configService.getBoolProperty(SSLProperties.SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED.getName(), SSLProperties.SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED.getDefaultBoolean())) {
                System.setProperty("javax.net.debug", "ssl:record");
                org.slf4j.LoggerFactory.getLogger(SSLActivator.class).info("Enabeld SSL debug logging.");
            }

            if (SSLProperties.isVerifyHostname()) {
                HttpsURLConnection.setDefaultHostnameVerifier(new DefaultHostnameVerifier());
            } else {
                HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
            }

            HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactoryProvider.getDefault());

            registerService(Reloadable.class, new SSLPropertiesReloadable());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(SSLActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(SSLActivator.class).info("stopping bundle: \"com.openexchange.ssl\"");

        Services.setBundleContext(null);
        super.stopBundle();
    }
}
