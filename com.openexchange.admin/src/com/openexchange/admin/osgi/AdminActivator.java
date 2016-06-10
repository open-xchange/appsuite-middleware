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

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.mysql.CreateAttachmentTables;
import com.openexchange.admin.mysql.CreateCalendarTables;
import com.openexchange.admin.mysql.CreateContactsTables;
import com.openexchange.admin.mysql.CreateIcalVcardTables;
import com.openexchange.admin.mysql.CreateInfostoreTables;
import com.openexchange.admin.mysql.CreateLdap2SqlTables;
import com.openexchange.admin.mysql.CreateMiscTables;
import com.openexchange.admin.mysql.CreateOXFolderTables;
import com.openexchange.admin.mysql.CreateSequencesTables;
import com.openexchange.admin.mysql.CreateSettingsTables;
import com.openexchange.admin.mysql.CreateVirtualFolderTables;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.OXGroupPluginInterface;
import com.openexchange.admin.plugins.OXResourcePluginInterface;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.plugins.UserServiceInterceptorBridge;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandlerExtended;
import com.openexchange.auth.Authenticator;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.filestore.FileStorageUnregisterListenerRegistry;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.passwordmechs.PasswordMechFactory;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersService;
import com.openexchange.user.UserService;
import com.openexchange.user.UserServiceInterceptor;
import com.openexchange.user.UserServiceInterceptorRegistry;
import com.openexchange.version.Version;

public class AdminActivator extends HousekeepingActivator {

    private volatile AdminDaemon daemon;

    /**
     * Initializes a new {@link AdminActivator}.
     */
    public AdminActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ThreadPoolService.class };
    }

    @Override
    public void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminActivator.class);
        AdminServiceRegistry.getInstance().addService(ThreadPoolService.class, getService(ThreadPoolService.class));

        track(PasswordMechFactory.class, new RegistryServiceTrackerCustomizer<PasswordMechFactory>(context, AdminServiceRegistry.getInstance(), PasswordMechFactory.class));
        track(PipesAndFiltersService.class, new RegistryServiceTrackerCustomizer<PipesAndFiltersService>(context, AdminServiceRegistry.getInstance(), PipesAndFiltersService.class));
        track(ContextService.class, new RegistryServiceTrackerCustomizer<ContextService>(context, AdminServiceRegistry.getInstance(), ContextService.class));

        track(TimerService.class, new RegistryServiceTrackerCustomizer<TimerService>(context, AdminServiceRegistry.getInstance(), TimerService.class) {
            @Override
            protected void serviceAcquired(TimerService timerService) {
                TaskManager.getInstance().startCleaner(timerService);
            }
        });

        track(MailAccountStorageService.class, new RegistryServiceTrackerCustomizer<MailAccountStorageService>(context, AdminServiceRegistry.getInstance(), MailAccountStorageService.class));
        track(PublicationTargetDiscoveryService.class, new RegistryServiceTrackerCustomizer<PublicationTargetDiscoveryService>(context, AdminServiceRegistry.getInstance(), PublicationTargetDiscoveryService.class));
        track(ConfigViewFactory.class, new RegistryServiceTrackerCustomizer<ConfigViewFactory>(context, AdminServiceRegistry.getInstance(), ConfigViewFactory.class));
        AdminCache.compareAndSetBundleContext(null, context);
        final ConfigurationService configurationService = getService(ConfigurationService.class);
        AdminCache.compareAndSetConfigurationService(null, configurationService);
        AdminServiceRegistry.getInstance().addService(ConfigurationService.class, configurationService);
        track(CreateTableService.class, new CreateTableCustomizer(context));
        track(CacheService.class, new RegistryServiceTrackerCustomizer<CacheService>(context, AdminServiceRegistry.getInstance(), CacheService.class));
        track(CapabilityService.class, new RegistryServiceTrackerCustomizer<CapabilityService>(context, AdminServiceRegistry.getInstance(), CapabilityService.class));
        track(SessiondService.class, new RegistryServiceTrackerCustomizer<SessiondService>(context, AdminServiceRegistry.getInstance(), SessiondService.class));
        track(Remote.class, new OXContextInterfaceTracker(context)).open();
        UserServiceInterceptorRegistry interceptorRegistry = new UserServiceInterceptorRegistry(context);
        track(UserServiceInterceptor.class, interceptorRegistry);
        track(UserService.class, new RegistryServiceTrackerCustomizer<UserService>(context, AdminServiceRegistry.getInstance(), UserService.class));
        track(UserAliasStorage.class, new RegistryServiceTrackerCustomizer<UserAliasStorage>(context, AdminServiceRegistry.getInstance(), UserAliasStorage.class));
        track(FileStorageUnregisterListenerRegistry.class, new RegistryServiceTrackerCustomizer<FileStorageUnregisterListenerRegistry>(context, AdminServiceRegistry.getInstance(), FileStorageUnregisterListenerRegistry.class));

        // Plugin interfaces
        {
            final int defaultRanking = 100;

            final RankingAwareNearRegistryServiceTracker<BasicAuthenticatorPluginInterface> batracker = new RankingAwareNearRegistryServiceTracker<BasicAuthenticatorPluginInterface>(context, BasicAuthenticatorPluginInterface.class, defaultRanking);
            rememberTracker(batracker);

            final RankingAwareNearRegistryServiceTracker<OXContextPluginInterface> ctracker = new RankingAwareNearRegistryServiceTracker<OXContextPluginInterface>(context, OXContextPluginInterface.class, defaultRanking);
            rememberTracker(ctracker);

            final RankingAwareNearRegistryServiceTracker<OXUserPluginInterface> utracker = new RankingAwareNearRegistryServiceTracker<OXUserPluginInterface>(context, OXUserPluginInterface.class, defaultRanking);
            rememberTracker(utracker);

            final RankingAwareNearRegistryServiceTracker<OXGroupPluginInterface> gtracker = new RankingAwareNearRegistryServiceTracker<OXGroupPluginInterface>(context, OXGroupPluginInterface.class, defaultRanking);
            rememberTracker(gtracker);

            final RankingAwareNearRegistryServiceTracker<OXResourcePluginInterface> rtracker = new RankingAwareNearRegistryServiceTracker<OXResourcePluginInterface>(context, OXResourcePluginInterface.class, defaultRanking);
            rememberTracker(rtracker);

            final PluginInterfaces.Builder builder = new PluginInterfaces.Builder().basicAuthenticatorPlugins(batracker).contextPlugins(ctracker).groupPlugins(gtracker).resourcePlugins(rtracker).userPlugins(utracker);

            PluginInterfaces.setInstance(builder.build());
        }

        track(FileLocationHandler.class, new FilestoreLocationUpdaterCustomizer(context));

        log.info("Starting Admindaemon...");
        final AdminDaemon daemon = new AdminDaemon();
        this.daemon = daemon;
        daemon.getCurrentBundleStatus(context);
        daemon.registerBundleListener(context);
        try {
            AdminDaemon.initCache(configurationService);
            daemon.initAccessCombinationsInCache();
        } catch (final OXGenericException e) {
            log.error("", e);
            throw e;
        } catch (final ClassNotFoundException e) {
            log.error("", e);
            throw e;
        }

        {
            AdminCache.compareAndSetBundleContext(null, context);
            AdminCache.compareAndSetConfigurationService(null, configurationService);

            PropertyHandlerExtended prop = initCache(configurationService, log);
            log.debug("Loading context implementation: {}", prop.getProp(PropertyHandlerExtended.CONTEXT_STORAGE, null));
            log.debug("Loading util implementation: {}", prop.getProp(PropertyHandlerExtended.UTIL_STORAGE, null));
        }

        track(DatabaseService.class, new DatabaseServiceCustomizer(context, ClientAdminThread.cache.getPool())).open();
        track(DatabaseService.class, new DatabaseServiceCustomizer(context, ClientAdminThreadExtended.cache.getPool())).open();
        track(DatabaseService.class, new AdminDaemonInitializer(daemon, context)).open();

        // Open trackers
        openTrackers();

        {
            final Dictionary<?, ?> headers = context.getBundle().getHeaders();
            log.info("Version: {}", headers.get("Bundle-Version"));
            log.info("Name: {}", headers.get("Bundle-SymbolicName"));
        }
        log.info("Build: {}", Version.getInstance().getVersionString());
        log.info("Admindaemon successfully started.");

        // The listener which is called if a new plugin is registered
        final ServiceListener sl = new ServiceListener() {
            @Override
            public void serviceChanged(final ServiceEvent ev) {
                log.info("Service: {}, {}", ev.getServiceReference().getBundle().getSymbolicName(), ev.getType());
                switch (ev.getType()) {
                    case ServiceEvent.REGISTERED:
                    log.info("{} registered service", ev.getServiceReference().getBundle().getSymbolicName());
                        break;
                    default:
                        break;
                }
            }
        };
        final String filter = "(objectclass=" + OXUserPluginInterface.class.getName() + ")";
        try {
            context.addServiceListener(sl, filter);
        } catch (final InvalidSyntaxException e) {
            e.printStackTrace();
        }

        // UserServiceInterceptor Bridge
        Dictionary<String, Object> props = new Hashtable<String, Object>(2);
        props.put("name", "OXUser");
        props.put(Constants.SERVICE_RANKING, Integer.valueOf(200));
        registerService(OXUserPluginInterface.class, new UserServiceInterceptorBridge(interceptorRegistry), props);

        //Register CreateTableServices
        registerService(CreateTableService.class, new CreateSequencesTables());
        registerService(CreateTableService.class, new CreateLdap2SqlTables());
        registerService(CreateTableService.class, new CreateOXFolderTables());
        registerService(CreateTableService.class, new CreateVirtualFolderTables());
        registerService(CreateTableService.class, new CreateSettingsTables());
        registerService(CreateTableService.class, new CreateCalendarTables());
        registerService(CreateTableService.class, new CreateContactsTables());
        registerService(CreateTableService.class, new CreateInfostoreTables());
        registerService(CreateTableService.class, new CreateAttachmentTables());
        registerService(CreateTableService.class, new CreateMiscTables());
        registerService(CreateTableService.class, new CreateIcalVcardTables());

        // Register authenticator
        AuthenticatorImpl authenticator = new AuthenticatorImpl();
        registerService(Authenticator.class, authenticator);
        registerService(Reloadable.class, authenticator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopBundle() throws Exception {
        {
            TaskManager taskManager = TaskManager.getInstance();
            if (null != taskManager) {
                taskManager.shutdown();
            }
        }

        PluginInterfaces.setInstance(null);
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminActivator.class);
        log.info("Stopping RMI...");
        final AdminDaemon daemon = this.daemon;
        if (null != daemon) {
            daemon.unregisterRMI(context);
            this.daemon = null;
        }

        AdminServiceRegistry.getInstance().removeService(ThreadPoolService.class);

        cleanUp();
    }

    private PropertyHandlerExtended initCache(ConfigurationService service, org.slf4j.Logger logger) throws OXGenericException {
        AdminCacheExtended cache = new AdminCacheExtended();
        cache.initCache(service);
        cache.initCacheExtended();
        ClientAdminThreadExtended.cache = cache;
        PropertyHandlerExtended prop = cache.getProperties();
        logger.info("Cache and Pools initialized!");
        return prop;
    }

}
