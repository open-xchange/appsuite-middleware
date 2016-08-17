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

package com.openexchange.snippet.mime.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.Constants;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
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
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.mime.MimeSnippetService;
import com.openexchange.snippet.mime.Services;
import com.openexchange.snippet.mime.groupware.MimeSnippetCreateTableTask;
import com.openexchange.snippet.mime.groupware.MimeSnippetDeleteListener;
import com.openexchange.snippet.mime.groupware.MimeSnippetQuotaProvider;

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
            ConfigurationService.class, ManagedFileManagement.class, HtmlService.class, ConfigViewFactory.class };
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
            final MimeSnippetCreateTableTask createTableTask = new MimeSnippetCreateTableTask();
            registerService(UpdateTaskProviderService.class.getName(), new DefaultUpdateTaskProviderService(createTableTask));
            registerService(CreateTableService.class, createTableTask);
            registerService(DeleteListener.class, new MimeSnippetDeleteListener());
            /*
             * Register
             */
            MimeSnippetQuotaProvider quotaProvider = new MimeSnippetQuotaProvider();
            MimeSnippetService snippetService = new MimeSnippetService(quotaProvider);

            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(10));
            registerService(SnippetService.class, snippetService, properties);

            quotaProvider.setSnippetService(snippetService);
            registerService(QuotaProvider.class, quotaProvider);
        } catch (final Exception e) {
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
