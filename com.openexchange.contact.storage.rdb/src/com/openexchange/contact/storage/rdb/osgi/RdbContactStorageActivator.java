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

package com.openexchange.contact.storage.rdb.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.ContactTombstoneStorage;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.contact.storage.ContactsStorageFactory;
import com.openexchange.contact.storage.rdb.groupware.AddFulltextIndexTask;
import com.openexchange.contact.storage.rdb.groupware.ContactsAccountCreateTableService;
import com.openexchange.contact.storage.rdb.groupware.ContactsAccountCreateTableTask;
import com.openexchange.contact.storage.rdb.internal.RdbContactQuotaProvider;
import com.openexchange.contact.storage.rdb.internal.RdbContactStorage;
import com.openexchange.contact.storage.rdb.internal.RdbContactStorageFactory;
import com.openexchange.contact.storage.rdb.internal.RdbServiceLookup;
import com.openexchange.contact.storage.rdb.rmi.ContactStorageRMIServiceImpl;
import com.openexchange.contact.storage.rdb.search.FulltextAutocompleteAdapter;
import com.openexchange.contact.storage.rdb.sql.AddFilenameColumnTask;
import com.openexchange.contact.storage.rdb.sql.CorrectNumberOfImagesTask;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DatabaseServiceDBProvider;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.i18n.I18nService;
import com.openexchange.imagetransformation.ImageMetadataService;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.quota.QuotaProvider;

/**
 * {@link RdbContactStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbContactStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link RdbContactStorageActivator}.
     */
    public RdbContactStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ContextService.class, ConfigViewFactory.class, ImageTransformationService.class, ConfigurationService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { CacheService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RdbContactStorageActivator.class);
        try {
            logger.info("starting bundle: com.openexchange.contact.storage.rdb");
            RdbServiceLookup.set(this);
            RdbContactStorage service = new RdbContactStorage();
            registerService(ContactStorage.class, service);
            registerService(ContactUserStorage.class, service);
            registerService(ContactTombstoneStorage.class, service);

            registerService(CreateTableService.class, new ContactsAccountCreateTableService());
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new ContactsAccountCreateTableTask()));

            if (getService(ConfigurationService.class).getBoolProperty("com.openexchange.contact.fulltextAutocomplete", false)) {
                registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new AddFulltextIndexTask()));
            }

            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new AddFilenameColumnTask(), new CorrectNumberOfImagesTask()));

            registerService(ContactsStorageFactory.class, new RdbContactStorageFactory(this, new DatabaseServiceDBProvider(getService(DatabaseService.class)), service));

            registerService(QuotaProvider.class, new RdbContactQuotaProvider());
            registerService(Reloadable.class, FulltextAutocompleteAdapter.RELOADABLE);
            {
                Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
                serviceProperties.put("RMI_NAME", ContactStorageRMIServiceImpl.RMI_NAME);
                registerService(Remote.class, new ContactStorageRMIServiceImpl(), serviceProperties);
            }
            track(I18nService.class, new I18nTracker(context));
            trackService(ImageMetadataService.class);
            openTrackers();
        } catch (Exception e) {
            logger.error("error starting \"com.openexchange.contact.storage.rdb\"", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RdbContactStorageActivator.class);
        logger.info("stopping bundle: com.openexchange.contact.storage.rdb");
        RdbServiceLookup.set(null);
        super.stopBundle();
    }

}
