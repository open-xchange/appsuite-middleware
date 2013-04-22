/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index.solr.osgi;

import org.apache.commons.logging.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.service.event.EventAdmin;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexManagementService;
import com.openexchange.index.solr.groupware.IndexDeleteListener;
import com.openexchange.index.solr.groupware.IndexedFoldersCreateTableService;
import com.openexchange.index.solr.groupware.IndexedFoldersCreateTableTask;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.SolrIndexFacadeService;
import com.openexchange.index.solr.internal.SolrIndexManagementService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreConfigService;
import com.openexchange.textxtraction.TextXtractService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link SolrIndexActivator} - The activator of the index bundle.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SolrIndexActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrIndexActivator.class));

    private SolrIndexFacadeService solrFacadeService;

    private BundleActivator fragmentActivator;


    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            DatabaseService.class, ContextService.class, UserService.class, ConfigurationService.class, TimerService.class, ThreadPoolService.class,
            SolrAccessService.class, TextXtractService.class, InfostoreFacade.class, SolrCoreConfigService.class, EventAdmin.class,
            ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting Bundle com.openexchange.index.solr");
        Services.setServiceLookup(this);

        solrFacadeService = new SolrIndexFacadeService();
        solrFacadeService.init();
        registerService(IndexFacadeService.class, solrFacadeService);
        addService(IndexFacadeService.class, solrFacadeService);
        IndexedFoldersCreateTableService createTableService = new IndexedFoldersCreateTableService();
        registerService(CreateTableService.class, createTableService);
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new IndexedFoldersCreateTableTask(createTableService)));
        registerService(DeleteListener.class, new IndexDeleteListener());

        IndexManagementService managementService = new SolrIndexManagementService();
        registerService(IndexManagementService.class, managementService);
        this.addService(IndexManagementService.class, managementService);
//        final SolrCoreConfigService indexService = new SolrCoreConfigServiceImpl();
//        registerService(SolrCoreConfigService.class, indexService);
    }

    @Override
    protected void stopBundle() throws Exception {
        if (solrFacadeService != null) {
            solrFacadeService.shutDown();
        }

        try {
            if (fragmentActivator != null) {
                fragmentActivator.stop(context);
            }
        } catch (Exception e) {
            // ignore
        }

        super.stopBundle();
    }
}
