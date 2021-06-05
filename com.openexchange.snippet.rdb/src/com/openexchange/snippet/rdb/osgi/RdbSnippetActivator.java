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

package com.openexchange.snippet.rdb.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.rdb.RdbSnippetFilestoreLocationUpdater;
import com.openexchange.snippet.rdb.RdbSnippetService;
import com.openexchange.snippet.rdb.Services;
import com.openexchange.snippet.rdb.groupware.RdbSnippetAddAttachmentMimeTypeAndDisposition;
import com.openexchange.snippet.rdb.groupware.RdbSnippetAttachmentBinaryCreateTableTask;
import com.openexchange.snippet.rdb.groupware.RdbSnippetCreateTableTask;
import com.openexchange.snippet.rdb.groupware.RdbSnippetDeleteListener;
import com.openexchange.snippet.rdb.groupware.RdbSnippetFixAttachmentPrimaryKey;
import com.openexchange.snippet.rdb.groupware.RdbSnippetQuotaProvider;
import com.openexchange.snippet.rdb.groupware.RdbSnippetTablesUtf8Mb4UpdateTask;

/**
 * {@link RdbSnippetActivator} - The activator for RDB Snippet bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RdbSnippetActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link RdbSnippetActivator}.
     */
    public RdbSnippetActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            DatabaseService.class, GenericConfigurationStorageService.class, ContextService.class, CacheService.class, CryptoService.class,
            IDGeneratorService.class, ConfigViewFactory.class, ManagedFileManagement.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RdbSnippetActivator.class);
        logger.info("Starting bundle: com.openexchange.snippet.rdb");
        try {
            Services.setServiceLookup(this);
            /*
             * Register Groupware stuff
             */
            RdbSnippetCreateTableTask createTableTask = new RdbSnippetCreateTableTask();
            RdbSnippetAttachmentBinaryCreateTableTask binaryCreateTableTask = new RdbSnippetAttachmentBinaryCreateTableTask();
            registerService(UpdateTaskProviderService.class.getName(), new DefaultUpdateTaskProviderService(createTableTask, new RdbSnippetFixAttachmentPrimaryKey(), binaryCreateTableTask, new RdbSnippetAddAttachmentMimeTypeAndDisposition(), new RdbSnippetTablesUtf8Mb4UpdateTask()));
            registerService(CreateTableService.class, createTableTask);
            registerService(CreateTableService.class, binaryCreateTableTask);
            registerService(DeleteListener.class, new RdbSnippetDeleteListener());

            /*
             * Register filestore location updater for move context filestore
             */
            registerService(FileLocationHandler.class, new RdbSnippetFilestoreLocationUpdater());

            /*
             * Register
             */
            RdbSnippetQuotaProvider quotaProvider = new RdbSnippetQuotaProvider();
            RdbSnippetService snippetService = new RdbSnippetService(quotaProvider, this);

            registerService(SnippetService.class, snippetService, withRanking(0));

            quotaProvider.setSnippetService(snippetService);
            registerService(QuotaProvider.class, quotaProvider);
        } catch (Exception e) {
            logger.error("Error starting bundle: com.openexchange.snippet.rdb", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        unregisterServices();
        Services.setServiceLookup(null);
    }
}
