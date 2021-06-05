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

package com.openexchange.net.ssl.osgi;

import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.TrustedSSLSocketFactory;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.net.ssl.internal.DefaultSSLSocketFactoryProvider;
import com.openexchange.net.ssl.management.SSLCertificateManagementService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 *
 * {@link SSLActivator} - The activator for <code>"com.openexchange.net.ssl"</code> bundle.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class SSLActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SSLActivator}.
     */
    public SSLActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { SSLConfigurationService.class, SSLCertificateManagementService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            org.slf4j.LoggerFactory.getLogger(SSLActivator.class).info("starting bundle: \"com.openexchange.net.ssl\"");
            Services.setServiceLookup(this);

            trackService(UserAwareSSLConfigurationService.class);
            openTrackers();

            TrustedSSLSocketFactory.init();

            DefaultSSLSocketFactoryProvider factoryProvider = new DefaultSSLSocketFactoryProvider(getService(SSLConfigurationService.class));
            registerService(SSLSocketFactoryProvider.class, factoryProvider);

            // Host name verification is done implicitly (if enabled through configuration) through com.openexchange.tools.ssl.DelegatingSSLSocket
            /*-
             *
            SSLConfigurationService sslConfigurationService = getService(SSLConfigurationService.class);
            if (sslConfigurationService.isVerifyHostname()) {
                HttpsURLConnection.setDefaultHostnameVerifier(new com.openexchange.net.ssl.apache.DefaultHostnameVerifier());
            } else {
                HttpsURLConnection.setDefaultHostnameVerifier(org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE);
            }
            */
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(SSLActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(SSLActivator.class).info("stopping bundle: \"com.openexchange.net.ssl\"");

        Services.setServiceLookup(null);
        super.stopBundle();
    }
}
