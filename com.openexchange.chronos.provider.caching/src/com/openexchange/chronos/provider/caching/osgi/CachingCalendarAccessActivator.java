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

package com.openexchange.chronos.provider.caching.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.database.DatabaseService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 *
 * {@link CachingCalendarAccessActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CachingCalendarAccessActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link CachingCalendarAccessActivator}.
     */
    public CachingCalendarAccessActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        //@formatter:off
        return new Class<?>[] { CalendarStorageFactory.class, DatabaseService.class, ContextService.class,
            CalendarUtilities.class, CalendarAccountService.class, AdministrativeCalendarAccountService.class,
            RecurrenceService.class, LeanConfigurationService.class, ConversionService.class, CalendarEventNotificationService.class };
        //@formatter:on
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(CachingCalendarAccessActivator.class).info("starting bundle {}", context.getBundle().getSymbolicName());
            Services.setServiceLookup(this);

            openTrackers();
        } catch (Exception e) {
            getLogger(CachingCalendarAccessActivator.class).error("error starting {}", context.getBundle().getSymbolicName(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(CachingCalendarAccessActivator.class).info("stopping bundle {}", context.getBundle().getSymbolicName());
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
