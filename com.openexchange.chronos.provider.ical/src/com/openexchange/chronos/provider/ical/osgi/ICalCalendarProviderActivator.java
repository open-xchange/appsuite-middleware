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

package com.openexchange.chronos.provider.ical.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.ical.BasicICalCalendarProvider;
import com.openexchange.chronos.provider.ical.properties.ICalCalendarProviderReloadable;
import com.openexchange.chronos.provider.ical.properties.IcalCalendarHttpProperties;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.version.VersionService;

/**
 *
 * {@link ICalCalendarProviderActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalCalendarProviderActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ICalCalendarProviderActivator}.
     */
    public ICalCalendarProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ICalService.class, LeanConfigurationService.class, SSLSocketFactoryProvider.class, SSLConfigurationService.class, CryptoService.class, 
                                ConversionService.class, AdministrativeCalendarAccountService.class, CalendarUtilities.class, DatabaseService.class, 
                                HttpClientService.class, VersionService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(ICalCalendarProviderActivator.class).info("starting bundle {}", context.getBundle());

            Services.setServiceLookup(this);

            registerService(CalendarProvider.class, new BasicICalCalendarProvider());
            registerService(Reloadable.class, new ICalCalendarProviderReloadable());
            registerService(SpecificHttpClientConfigProvider.class, new IcalCalendarHttpProperties(this));
        } catch (Exception e) {
            getLogger(ICalCalendarProviderActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(ICalCalendarProviderActivator.class).info("stopping bundle {}", context.getBundle());

        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
