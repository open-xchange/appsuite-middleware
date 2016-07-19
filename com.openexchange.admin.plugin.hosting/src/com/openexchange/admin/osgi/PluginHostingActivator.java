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

package com.openexchange.admin.osgi;

import com.openexchange.admin.PluginStarter;
import com.openexchange.admin.daemons.AdminDaemonService;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.OXGroupPluginInterface;
import com.openexchange.admin.plugins.OXResourcePluginInterface;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXContextGroupStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.OXContextGroupMySQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.i18n.I18nService;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersService;

public class PluginHostingActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PluginHostingActivator.class);

    private PluginStarter starter = null;

    /**
     * Initializes a new {@link PluginHostingActivator}.
     */
    public PluginHostingActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        AdminCache.compareAndSetBundleContext(null, context);
        final ConfigurationService configurationService = getService(ConfigurationService.class);
        AdminCache.compareAndSetConfigurationService(null, configurationService);
        AdminServiceRegistry.getInstance().addService(ConfigurationService.class, configurationService);
        final ConfigViewFactory configViewFactory = getService(ConfigViewFactory.class);
        AdminServiceRegistry.getInstance().addService(ConfigViewFactory.class, configViewFactory);
        track(ThreadPoolService.class, new RegistryServiceTrackerCustomizer<>(context, AdminServiceRegistry.getInstance(), ThreadPoolService.class));
        track(ContextService.class, new RegistryServiceTrackerCustomizer<>(context, AdminServiceRegistry.getInstance(), ContextService.class));
        track(I18nService.class, new I18nServiceCustomizer(context));
        track(ManagementService.class, new ManagementCustomizer(context));
        track(PipesAndFiltersService.class, new RegistryServiceTrackerCustomizer<>(context, AdminServiceRegistry.getInstance(), PipesAndFiltersService.class));
        track(CacheService.class, new RegistryServiceTrackerCustomizer<>(context, AdminServiceRegistry.getInstance(), CacheService.class));
        track(DatabaseService.class, new RegistryServiceTrackerCustomizer<>(context, AdminServiceRegistry.getInstance(), DatabaseService.class));

        // Register and track
        registerService(OXContextGroupStorageInterface.class, new OXContextGroupMySQLStorage());
        track(OXContextGroupStorageInterface.class, new RegistryServiceTrackerCustomizer<>(context, AdminServiceRegistry.getInstance(), OXContextGroupStorageInterface.class));

        // Plugin interfaces
        {
            final int defaultRanking = 100;

            final RankingAwareNearRegistryServiceTracker<BasicAuthenticatorPluginInterface> batracker = new RankingAwareNearRegistryServiceTracker<>(context, BasicAuthenticatorPluginInterface.class, defaultRanking);
            rememberTracker(batracker);

            final RankingAwareNearRegistryServiceTracker<OXContextPluginInterface> ctracker = new RankingAwareNearRegistryServiceTracker<>(context, OXContextPluginInterface.class, defaultRanking);
            rememberTracker(ctracker);

            final RankingAwareNearRegistryServiceTracker<OXUserPluginInterface> utracker = new RankingAwareNearRegistryServiceTracker<>(context, OXUserPluginInterface.class, defaultRanking);
            rememberTracker(utracker);

            final RankingAwareNearRegistryServiceTracker<OXGroupPluginInterface> gtracker = new RankingAwareNearRegistryServiceTracker<>(context, OXGroupPluginInterface.class, defaultRanking);
            rememberTracker(gtracker);

            final RankingAwareNearRegistryServiceTracker<OXResourcePluginInterface> rtracker = new RankingAwareNearRegistryServiceTracker<>(context, OXResourcePluginInterface.class, defaultRanking);
            rememberTracker(rtracker);

            final PluginInterfaces.Builder builder = new PluginInterfaces.Builder().basicAuthenticatorPlugins(batracker).contextPlugins(ctracker).groupPlugins(gtracker).resourcePlugins(rtracker).userPlugins(utracker);

            PluginInterfaces.setInstance(builder.build());
        }

        // Open trackers
        openTrackers();

        this.starter = new PluginStarter();
        try {
            this.starter.start(context);
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final PluginStarter starter = this.starter;
        if (starter != null) {
            starter.stop();
        }
        closeTrackers();
        cleanUp();
        PluginInterfaces.setInstance(null);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, AdminDaemonService.class, ConfigViewFactory.class };
    }
}
