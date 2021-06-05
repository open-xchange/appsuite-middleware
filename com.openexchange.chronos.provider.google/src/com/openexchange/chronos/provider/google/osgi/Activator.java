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

package com.openexchange.chronos.provider.google.osgi;

import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.google.GoogleCalendarProvider;
import com.openexchange.chronos.provider.google.migration.GoogleSubscriptionsMigrationTask;
import com.openexchange.chronos.provider.google.oauth.GoogleCalendarOAuthAccountAssociationProvider;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthAccountStorage;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;
import com.openexchange.user.UserService;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        //@formatter:off
        return new Class[] {
            OAuthService.class, OAuthServiceMetaDataRegistry.class, OAuthAccountStorage.class, CalendarAccountService.class, AdministrativeCalendarAccountService.class, LeanConfigurationService.class, RecurrenceService.class,
            // The services below are only required by migration
            GenericConfigurationStorageService.class, CalendarStorageFactory.class, ContextService.class, GroupService.class, UserService.class, FolderService.class
        };
        //@formatter:on
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        trackService(FolderSubscriptionHelper.class);
        openTrackers();

        registerService(CalendarProvider.class, new GoogleCalendarProvider(this));
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new GoogleSubscriptionsMigrationTask()));
        registerService(OAuthAccountDeleteListener.class, new com.openexchange.chronos.provider.google.access.OAuthAccountDeleteListener());
        registerService(OAuthAccountAssociationProvider.class, new GoogleCalendarOAuthAccountAssociationProvider());
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }
}
