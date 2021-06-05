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

package com.openexchange.ipcheck.countrycode.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.MultipleServiceTracker;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;

/**
 * {@link CountryCodeIpCheckerActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class CountryCodeIpCheckerActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(CountryCodeIpCheckerActivator.class);

    /**
     * Initializes a new {@link CountryCodeIpCheckerActivator}.
     */
    public CountryCodeIpCheckerActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { TimerService.class, SessiondService.class, ConfigurationService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { GeoLocationService.class, ManagementService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        ConfigurationService configService = getService(ConfigurationService.class);
        boolean ipCheck = Boolean.parseBoolean(configService.getProperty(ServerConfig.Property.IP_CHECK.getPropertyName()));
        if (ipCheck) {
            LOG.info("IPCheck is enabled. No need to register the 'CountryCodeIpChecker'.");
            return;
        }
        String ipCheckMode = configService.getProperty("com.openexchange.ipcheck.mode");
        if (Strings.isEmpty(ipCheckMode)) {
            LOG.warn("The '{}' is disabled and no other mode has been enabled! Check the 'com.openexchange.ipcheck.mode' property!", ServerConfig.Property.IP_CHECK.getPropertyName());
            return;
        }

        if (false == ipCheckMode.equals("countrycode")) {
            LOG.info("The '{}' is disabled mode '{}' has been enabled. No need to register the 'CountryCodeIpChecker'.", ServerConfig.Property.IP_CHECK.getPropertyName(), ipCheckMode);
            return;
        }
        MultipleServiceTracker tracker = new GeoLocationServiceTracker(this, context);
        rememberTracker(tracker.createTracker());
        openTrackers();
    }
}
