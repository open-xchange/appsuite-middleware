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

package com.openexchange.net.ssl.config.impl.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.TrustLevel;
import com.openexchange.net.ssl.config.impl.internal.RestrictedSSLConfigurationService;
import com.openexchange.net.ssl.config.impl.internal.SSLProperties;
import com.openexchange.net.ssl.config.impl.internal.SSLPropertiesReloadable;
import com.openexchange.net.ssl.config.impl.internal.TrustAllSSLConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 *
 * {@link SSLConfigActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class SSLConfigActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SSLConfigActivator}.
     */
    public SSLConfigActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            org.slf4j.LoggerFactory.getLogger(SSLConfigActivator.class).info("starting bundle: \"com.openexchange.net.ssl.config.impl\"");

            // Pre-initialize cipher suites
            com.openexchange.net.ssl.config.impl.internal.SSLProperties.initJvmDefaults();

            ConfigurationService configService = getService(ConfigurationService.class);
            ConfigViewFactory configViewFactory = getService(ConfigViewFactory.class);

            if (configService.getBoolProperty(SSLProperties.SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED.getName(), SSLProperties.SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED.getDefaultBoolean())) {
                System.setProperty("javax.net.debug", "ssl:record");
                org.slf4j.LoggerFactory.getLogger(SSLConfigActivator.class).info("Enabled SSL debug logging.");
            }

            UserAwareSSLConfigurationServiceRegisterer registerer = new UserAwareSSLConfigurationServiceRegisterer(configViewFactory, configService, context);
            track(registerer.getFilter(), registerer);
            openTrackers();

            SSLConfigurationService sslConfigurationService;
            {
                TrustLevel trustLevel = SSLProperties.trustLevel(configService);
                if (TrustLevel.TRUST_ALL.equals(trustLevel)) {
                    sslConfigurationService = TrustAllSSLConfigurationService.getInstance();
                } else {
                    RestrictedSSLConfigurationService restrictedSslConfigurationService = new RestrictedSSLConfigurationService(trustLevel, configService);
                    sslConfigurationService = restrictedSslConfigurationService;
                    registerService(Reloadable.class, new SSLPropertiesReloadable(restrictedSslConfigurationService));
                }
            }
            registerService(SSLConfigurationService.class, sslConfigurationService);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(SSLConfigActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(SSLConfigActivator.class).info("stopping bundle: \"com.openexchange.net.ssl.config.impl\"");
        super.stopBundle();
    }

}
