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

package com.openexchange.authentication.application.storage.rdb.osgi;

import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.authentication.application.storage.AppPasswordStorage;
import com.openexchange.authentication.application.storage.rdb.AppPasswordStorageProperty;
import com.openexchange.authentication.application.storage.rdb.passwords.AppPasswordChangeEventHandler;
import com.openexchange.authentication.application.storage.rdb.passwords.AppPasswordDeleteListener;
import com.openexchange.authentication.application.storage.rdb.passwords.AppPasswordStorageRDB;
import com.openexchange.authentication.application.storage.rdb.passwords.CreateAppHistoryUpdateTask;
import com.openexchange.authentication.application.storage.rdb.passwords.CreateAppPasswordUpdateTask;
import com.openexchange.authentication.application.storage.rdb.passwords.CreateHistoryTable;
import com.openexchange.authentication.application.storage.rdb.passwords.CreatePasswordTable;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.mailmapping.MailResolverService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

/**
 * {@link ApplicationPasswordStorageActivator}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class ApplicationPasswordStorageActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApplicationPasswordStorageActivator.class);

    private static final int DEFAULT_SERVICE_RANKING = -1;  // Default ranking for services likely to be overwritten

    /**
     * Initializes a new {@link ApplicationPasswordStorageActivator}.
     */
    public ApplicationPasswordStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DatabaseService.class, ContextService.class, CryptoService.class, CapabilityService.class, LeanConfigurationService.class, UserService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { MailResolverService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle {}", context.getBundle());
        if (false == getService(LeanConfigurationService.class).getBooleanProperty(AppPasswordStorageProperty.ENABLED)) {
            LOG.info("Database-backed application password storage is disabled by configuration.");
            return;
        }

        // Database setup
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CreateAppPasswordUpdateTask(), new CreateAppHistoryUpdateTask()));
        registerService(CreateTableService.class, new CreatePasswordTable());
        registerService(CreateTableService.class, new CreateHistoryTable());

        // Register the default storage services 
        AppPasswordStorageRDB storage = new AppPasswordStorageRDB(this);
        registerService(AppPasswordStorage.class, storage, DEFAULT_SERVICE_RANKING);

        // Register password change monitor
        registerService(EventHandler.class, new AppPasswordChangeEventHandler(storage), singletonDictionary(EventConstants.EVENT_TOPIC, "com/openexchange/passwordchange"));

        // Cleanup modules
        registerService(DeleteListener.class, new AppPasswordDeleteListener(storage));
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
