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

package com.openexchange.download.limit.osgi;

import com.openexchange.ajax.requesthandler.DispatcherListener;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.download.limit.internal.Services;
import com.openexchange.download.limit.limiter.FilesDownloadLimiter;
import com.openexchange.download.limit.limiter.InfostoreDownloadLimiter;
import com.openexchange.download.limit.rdb.FileAccessConvertUtf8ToUtf8mb4UpdateTask;
import com.openexchange.download.limit.rdb.FileAccessCreateTableService;
import com.openexchange.download.limit.rdb.FileAccessCreateTableTask;
import com.openexchange.download.limit.util.LimitConfig;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;

/**
 *
 * {@link ShareLimitActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class ShareLimitActivator extends AJAXModuleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareLimitActivator.class);

    /**
     * Initializes a new {@link ShareLimitActivator}.
     */
    public ShareLimitActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class, DatabaseService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.share.limit\"");

        Services.set(this);

        // Register create table services for user schema
        registerService(CreateTableService.class, new FileAccessCreateTableService());

        // Register update tasks for user schema
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new FileAccessCreateTableTask(getService(DatabaseService.class)), new FileAccessConvertUtf8ToUtf8mb4UpdateTask()));

        ConfigViewFactory configView = getService(ConfigViewFactory.class);
        final FilesDownloadLimiter filesDownloadLimiter = new FilesDownloadLimiter(configView);
        registerService(DispatcherListener.class, filesDownloadLimiter);
        final InfostoreDownloadLimiter infostoreDownloadLimiter = new InfostoreDownloadLimiter(configView);
        registerService(DispatcherListener.class, infostoreDownloadLimiter);

        registerService(Reloadable.class, LimitConfig.getInstance());
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.share.limit\"");
        Services.set(null);
        super.stopBundle();
    }
}
