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

package com.openexchange.database.osgi;

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.internal.CreateReplicationTable;
import com.openexchange.database.internal.reloadable.GenericReloadable;
import com.openexchange.database.internal.reloadable.GlobalDbConfigsReloadable;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.lock.LockService;
import com.openexchange.management.ManagementService;
import com.openexchange.monitoring.osgi.SocketLoggerBlackListServiceTracker;
import com.openexchange.monitoring.sockets.SocketLoggerRegistryService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

/**
 * Activator for the database bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Activator extends HousekeepingActivator {

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(CreateTableService.class, new CreateReplicationTable(), null);

        DatabaseConnectionListenerTracker connectionListenerTracker = new DatabaseConnectionListenerTracker(context);
        rememberTracker(connectionListenerTracker);

        Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + ConfigurationService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + ConfigViewFactory.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + DBMigrationExecutorService.class.getName() + "))");
        rememberTracker(new ServiceTracker<Object, Object>(context, filter, new DatabaseServiceRegisterer(connectionListenerTracker, context)));

        track(ManagementService.class, new ManagementServiceCustomizer(context));
        track(TimerService.class, new TimerServiceCustomizer(context));
        track(CacheService.class, new CacheServiceCustomizer(context));
        track(LockService.class, new LockServiceTracker(context));
        track(SocketLoggerRegistryService.class, new SocketLoggerBlackListServiceTracker("com.openexchange.database", context));
        openTrackers();

        registerService(Reloadable.class, GenericReloadable.getInstance(), null);
        registerService(Reloadable.class, new GlobalDbConfigsReloadable(), null);
    }

}
