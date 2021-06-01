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

package com.openexchange.chronos.provider.internal.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.internal.InternalCalendarProvider;
import com.openexchange.chronos.provider.internal.InternalFreeBusyProvider;
import com.openexchange.chronos.provider.internal.config.AllowChangeOfOrganizer;
import com.openexchange.chronos.provider.internal.config.DefaultAlarmDate;
import com.openexchange.chronos.provider.internal.config.DefaultAlarmDateTime;
import com.openexchange.chronos.provider.internal.config.DefaultFolderId;
import com.openexchange.chronos.provider.internal.config.RestrictAllowedAttendeeChanges;
import com.openexchange.chronos.provider.internal.config.RestrictAllowedAttendeeChangesPublic;
import com.openexchange.chronos.provider.internal.share.CalendarFolderHandlerModuleExtension;
import com.openexchange.chronos.provider.internal.share.CalendarModuleAdjuster;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.conversion.ConversionService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.share.core.ModuleAdjuster;
import com.openexchange.share.groupware.spi.FolderHandlerModuleExtension;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;
import com.openexchange.user.UserService;

/**
 * {@link InternalCalendarProviderActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class InternalCalendarProviderActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link InternalCalendarProviderActivator}.
     */
    public InternalCalendarProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            FolderService.class, CalendarService.class, RecurrenceService.class, UserService.class, ConversionService.class, ConfigurationService.class,
            CalendarAccountService.class, CalendarStorageFactory.class, CalendarUtilities.class, ConfigViewFactory.class
        };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { JSlobService.class, JSlobStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(InternalCalendarProviderActivator.class).info("starting bundle {}", context.getBundle());
            trackService(FolderUserPropertyStorage.class);
            openTrackers();
            registerService(CalendarProvider.class, new InternalCalendarProvider(this));
            registerService(FreeBusyProvider.class, new InternalFreeBusyProvider(this));
            registerService(ModuleAdjuster.class, new CalendarModuleAdjuster());
            registerService(FolderHandlerModuleExtension.class, new CalendarFolderHandlerModuleExtension(this));
            /*
             * register JSlob entries
             */
            registerService(JSlobEntry.class, new DefaultFolderId(this));
            registerService(JSlobEntry.class, new RestrictAllowedAttendeeChanges(this));
            registerService(JSlobEntry.class, new RestrictAllowedAttendeeChangesPublic(this));
            registerService(JSlobEntry.class, new AllowChangeOfOrganizer(this));
            registerService(JSlobEntry.class, new DefaultAlarmDate(this));
            registerService(JSlobEntry.class, new DefaultAlarmDateTime(this));
        } catch (Exception e) {
            getLogger(InternalCalendarProviderActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(InternalCalendarProviderActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
