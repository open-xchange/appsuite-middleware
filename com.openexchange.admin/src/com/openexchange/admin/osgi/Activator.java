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

package com.openexchange.admin.osgi;

import java.util.Dictionary;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
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
import com.openexchange.admin.mysql.CreateTaskTables;
import com.openexchange.admin.mysql.CreateVirtualFolderTables;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.log.LogFactory;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersService;
import com.openexchange.version.Version;

public class Activator extends HousekeepingActivator {

    static Log log = LogFactory.getLog(AdminDaemon.class);

    private AdminDaemon daemon = null;

    private final Stack<ServiceTracker<?,?>> trackers = new Stack<ServiceTracker<?,?>>();

    @Override
    public void startBundle() throws Exception {
        track(PipesAndFiltersService.class, new RegistryServiceTrackerCustomizer<PipesAndFiltersService>(context, AdminServiceRegistry.getInstance(), PipesAndFiltersService.class));
        track(ContextService.class, new RegistryServiceTrackerCustomizer<ContextService>(context, AdminServiceRegistry.getInstance(), ContextService.class));
        track(MailAccountStorageService.class, new RegistryServiceTrackerCustomizer<MailAccountStorageService>(context, AdminServiceRegistry.getInstance(), MailAccountStorageService.class));
        track(PublicationTargetDiscoveryService.class, new RegistryServiceTrackerCustomizer<PublicationTargetDiscoveryService>(context, AdminServiceRegistry.getInstance(), PublicationTargetDiscoveryService.class));
        AdminCache.compareAndSetBundleContext(null, context);
        final ConfigurationService configurationService = getService(ConfigurationService.class);
        AdminCache.compareAndSetConfigurationService(null, configurationService);
        AdminServiceRegistry.getInstance().addService(ConfigurationService.class, configurationService);
        track(CreateTableService.class, new CreateTableCustomizer(context));
        openTrackers();

        log.info("Starting Admindaemon...");
        this.daemon = new AdminDaemon();
        this.daemon.getCurrentBundleStatus(context);
        this.daemon.registerBundleListener(context);
        try {
            AdminDaemon.initCache(configurationService);
            this.daemon.initAccessCombinationsInCache();
        } catch (final OXGenericException e) {
            log.fatal(e.getMessage(), e);
            throw e;
        } catch (final ClassNotFoundException e) {
            log.fatal(e.getMessage(), e);
            throw e;
        }
        track(DatabaseService.class, new DatabaseServiceCustomizer(context, ClientAdminThread.cache.getPool())).open();
        this.daemon.initRMI(context);


        if (log.isInfoEnabled()) {
            final Dictionary<?, ?> headers = context.getBundle().getHeaders();
            log.info("Version: " + headers.get("Bundle-Version"));
            log.info("Name: " + headers.get("Bundle-SymbolicName"));
            log.info("Build: " + Version.getInstance().getVersionString());
        }
        log.info("Admindaemon successfully started.");

        // The listener which is called if a new plugin is registered
        final ServiceListener sl = new ServiceListener() {
            @Override
            public void serviceChanged(final ServiceEvent ev) {
                if (log.isInfoEnabled()) {
                    log.info("Service: " + ev.getServiceReference().getBundle().getSymbolicName() + ", " + ev.getType());
                }
                switch (ev.getType()) {
                    case ServiceEvent.REGISTERED:
                        if(log.isInfoEnabled()){
                            log.info(ev.getServiceReference().getBundle().getSymbolicName() + " registered service");
                        }
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
        
        //Register CreateTableServices
        registerService(CreateTableService.class, new CreateSequencesTables());
        registerService(CreateTableService.class, new CreateLdap2SqlTables());
        registerService(CreateTableService.class, new CreateOXFolderTables());
        registerService(CreateTableService.class, new CreateVirtualFolderTables());
        registerService(CreateTableService.class, new CreateSettingsTables());
        registerService(CreateTableService.class, new CreateCalendarTables());
        registerService(CreateTableService.class, new CreateContactsTables());
        registerService(CreateTableService.class, new CreateTaskTables());
        registerService(CreateTableService.class, new CreateInfostoreTables());
        registerService(CreateTableService.class, new CreateAttachmentTables());
        registerService(CreateTableService.class, new CreateMiscTables());
        registerService(CreateTableService.class, new CreateIcalVcardTables());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopBundle() throws Exception {
        closeTrackers();
        while (!trackers.isEmpty()) {
            final ServiceTracker<?,?> tracker = trackers.pop();
            tracker.close();
        }
        log.info("Stopping RMI...");
        final AdminDaemon daemon = this.daemon;
        if (null != daemon) {
            daemon.unregisterRMI(context);
        }
        log.info("Thanks for using Open-Xchange AdminDaemon");
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }
}
