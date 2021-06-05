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

package com.openexchange.snippet.mime.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.html.HtmlService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.snippet.QuotaAwareSnippetService;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.mime.MimeSnippetService;
import com.openexchange.snippet.mime.Services;
import com.openexchange.snippet.mime.groupware.MimeSnippetCreateTableTask;
import com.openexchange.snippet.mime.groupware.MimeSnippetDeleteListener;
import com.openexchange.snippet.mime.groupware.MimeSnippetQuotaProvider;
import com.openexchange.snippet.mime.groupware.MimeSnippetTablesUtf8Mb4UpdateTask;
import com.openexchange.snippet.mime.groupware.SnippetSizeColumnUpdateTask;

/**
 * {@link MimeSnippetActivator} - The activator for MIME Snippet bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MimeSnippetActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MimeSnippetActivator}.
     */
    public MimeSnippetActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            DatabaseService.class, ContextService.class, CacheService.class, CryptoService.class, IDGeneratorService.class,
            ConfigurationService.class, ManagedFileManagement.class, HtmlService.class, ConfigViewFactory.class, LeanConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MimeSnippetActivator.class);
        logger.info("Starting bundle: com.openexchange.snippet.mime");
        try {

            /*-
             *   How SnippetService selection works
             * =========================================
             *
             * The check if "filestore" capability is available/permitted as per CapabilityService is performed through examining "MimeSnippetService.neededCapabilities()" method
             * in "SnippetAction.getSnippetService()".
             *
             * Available SnippetServices are sorted rank-wise, with RdbSnippetService having default (0) ranking and MimeSnippetService with a rank of 10. Thus MimeSnippetService
             * is preferred provided that "filestore" capability is indicated by CapabilityService.
             *
             * If missing, RdbSnippetService is selected.
             */

            /*
             * Go ahead with starting bundle...
             */
            Services.setServiceLookup(this);
            /*
             * Register groupware stuff
             */
            MimeSnippetCreateTableTask createTableTask = new MimeSnippetCreateTableTask();
            registerService(UpdateTaskProviderService.class.getName(), new DefaultUpdateTaskProviderService(createTableTask, new SnippetSizeColumnUpdateTask(), new MimeSnippetTablesUtf8Mb4UpdateTask()));
            registerService(CreateTableService.class, createTableTask);
            registerService(DeleteListener.class, new MimeSnippetDeleteListener());
            /*
             * Register
             */
            MimeSnippetQuotaProvider quotaProvider = new MimeSnippetQuotaProvider();
            MimeSnippetService snippetService = new MimeSnippetService(quotaProvider, getServiceSafe(LeanConfigurationService.class));

            registerService(SnippetService.class, snippetService, withRanking(10));

            registerService(QuotaAwareSnippetService.class, snippetService, withRanking(10));

            registerService(QuotaProvider.class, quotaProvider);
        } catch (Exception e) {
            logger.error("Error starting bundle: com.openexchange.snippet.mime", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }
}
