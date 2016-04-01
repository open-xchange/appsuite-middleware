/*-
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.snippet.rdb.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.Constants;
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
import com.openexchange.snippet.rdb.groupware.RdbSnippetAttachmentBinaryCreateTableTask;
import com.openexchange.snippet.rdb.groupware.RdbSnippetCreateTableTask;
import com.openexchange.snippet.rdb.groupware.RdbSnippetDeleteListener;
import com.openexchange.snippet.rdb.groupware.RdbSnippetFixAttachmentPrimaryKey;
import com.openexchange.snippet.rdb.groupware.RdbSnippetQuotaProvider;

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
            registerService(UpdateTaskProviderService.class.getName(), new DefaultUpdateTaskProviderService(createTableTask, new RdbSnippetFixAttachmentPrimaryKey(), binaryCreateTableTask));
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

            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(0));
            registerService(SnippetService.class, snippetService, properties);

            quotaProvider.setSnippetService(snippetService);
            registerService(QuotaProvider.class, quotaProvider);
        } catch (final Exception e) {
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
