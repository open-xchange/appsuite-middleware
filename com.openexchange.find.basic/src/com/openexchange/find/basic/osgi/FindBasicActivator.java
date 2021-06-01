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

package com.openexchange.find.basic.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.conversion.ConversionService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.find.basic.Services;
import com.openexchange.find.basic.calendar.BasicCalendarDriver;
import com.openexchange.find.basic.contacts.AutocompleteFields;
import com.openexchange.find.basic.contacts.BasicContactsDriver;
import com.openexchange.find.basic.contacts.ShowDepartmentJSlobEntry;
import com.openexchange.find.basic.drive.BasicDriveDriver;
import com.openexchange.find.basic.mail.BasicMailDriver;
import com.openexchange.find.basic.tasks.BasicTasksDriver;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link FindBasicActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class FindBasicActivator extends HousekeepingActivator {

    //@formatter:off
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedContactsAccessFactory.class, FolderService.class, MailService.class,
            MailAccountStorageService.class, IDBasedFileAccessFactory.class, UnifiedInboxManagement.class,
            ThreadPoolService.class, IDBasedFolderAccessFactory.class, ConfigurationService.class,
            InfostoreSearchEngine.class, FileStorageServiceRegistry.class, ConfigViewFactory.class,
            IDBasedCalendarAccessFactory.class, RecurrenceService.class, LeanConfigurationService.class, ConversionService.class
        };
    }
    //@formatter:on

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        ConfigurationService configService = getService(ConfigurationService.class);
        boolean searchMailBody = configService.getBoolProperty("com.openexchange.find.basic.mail.searchmailbody", false);
        registerService(ModuleSearchDriver.class, new BasicMailDriver(searchMailBody), withRanking(0));
        registerService(ModuleSearchDriver.class, new BasicDriveDriver(), withRanking(0));
        registerService(ModuleSearchDriver.class, new BasicContactsDriver(), withRanking(0));
        registerService(ModuleSearchDriver.class, new BasicCalendarDriver(), withRanking(0));
        registerService(ModuleSearchDriver.class, new BasicTasksDriver(), withRanking(0));
        registerService(PreferencesItemService.class, new AutocompleteFields());

        // Register the 'showDepartment' jslob
        registerService(JSlobEntry.class, new ShowDepartmentJSlobEntry());
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
