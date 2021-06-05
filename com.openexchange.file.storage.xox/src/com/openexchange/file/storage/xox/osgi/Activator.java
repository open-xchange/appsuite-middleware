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

package com.openexchange.file.storage.xox.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.conversion.ConversionService;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.file.storage.xox.XOXFileStorageService;
import com.openexchange.file.storage.xox.subscription.XOXContactPicutreFinder;
import com.openexchange.file.storage.xox.subscription.XOXShareSubscriptionProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class Activator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        //@formatter:off
        return new Class[] { FileStorageAccountManagerLookupService.class,
            ApiClientService.class,
            FileStorageServiceRegistry.class,
            ConversionService.class,
            UserPermissionService.class };
        //@formatter:on
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());

        XOXFileStorageService fileStorageService = new XOXFileStorageService(this);
        registerService(FileStorageService.class, fileStorageService);
        registerService(ShareSubscriptionProvider.class, new XOXShareSubscriptionProvider(this, fileStorageService));
        registerService(ContactPictureFinder.class, new XOXContactPicutreFinder(this, fileStorageService));
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
