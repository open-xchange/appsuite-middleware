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

package com.openexchange.chronos.provider.birthdays.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.birthdays.BirthdaysCalendarProvider;
import com.openexchange.chronos.provider.birthdays.ContactEventHandler;
import com.openexchange.chronos.provider.birthdays.DefaultAlarmDate;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.database.DatabaseService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.regional.RegionalSettingsService;

/**
 * {@link BirthdaysCalendarProviderActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class BirthdaysCalendarProviderActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link BirthdaysCalendarProviderActivator}.
     */
    public BirthdaysCalendarProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ContactService.class, RecurrenceService.class, CalendarUtilities.class, FolderService.class, CalendarStorageFactory.class,
            DatabaseService.class, ContextService.class, AdministrativeCalendarAccountService.class, ConversionService.class,
            CapabilityService.class, CalendarAccountService.class, I18nServiceRegistry.class, CalendarEventNotificationService.class,
            RegionalSettingsService.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(BirthdaysCalendarProviderActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * register calendar provider
             */
            BirthdaysCalendarProvider calendarProvider = new BirthdaysCalendarProvider(this);
            registerService(CalendarProvider.class, calendarProvider);
            registerService(JSlobEntry.class, new DefaultAlarmDate(this));
            /*
             * register event handler for contact changes
             */
            registerService(EventHandler.class, new ContactEventHandler(this), singletonDictionary(EventConstants.EVENT_TOPIC, ContactEventHandler.TOPICS));
        } catch (Exception e) {
            getLogger(BirthdaysCalendarProviderActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(BirthdaysCalendarProviderActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
