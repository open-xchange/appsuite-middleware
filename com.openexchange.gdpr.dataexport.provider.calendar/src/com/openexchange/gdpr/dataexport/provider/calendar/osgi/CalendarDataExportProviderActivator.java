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

package com.openexchange.gdpr.dataexport.provider.calendar.osgi;

import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.gdpr.dataexport.DataExportProvider;
import com.openexchange.gdpr.dataexport.DataExportStatusChecker;
import com.openexchange.gdpr.dataexport.provider.calendar.CalendarDataExportProvider;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link CalendarDataExportProviderActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class CalendarDataExportProviderActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link CalendarDataExportProviderActivator}.
     */
    public CalendarDataExportProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, ConfigurationService.class, ContextService.class, UserService.class, DataExportStatusChecker.class,
            UserConfigurationService.class, CalendarService.class, ICalService.class, FolderService.class, TranslatorFactory.class, FullNameBuilderService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(DataExportProvider.class, new CalendarDataExportProvider(this));
    }

}
