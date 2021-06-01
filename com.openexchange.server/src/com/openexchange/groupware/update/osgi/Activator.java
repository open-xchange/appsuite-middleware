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

package com.openexchange.groupware.update.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.groupware.update.ExtendedUpdateTaskService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.internal.CreateUpdateTaskTable;
import com.openexchange.groupware.update.internal.ExcludedSet;
import com.openexchange.groupware.update.internal.ExtendedUpdateTaskServiceImpl;
import com.openexchange.groupware.update.internal.InternalList;
import com.openexchange.groupware.update.internal.NamespaceAwareExcludedSet;
import com.openexchange.groupware.update.internal.UpdateTaskServiceImpl;
import com.openexchange.groupware.update.tasks.objectpermission.ObjectPermissionCreateTableService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * This {@link Activator} currently is only used to initialize some structures within the database update component. Later on this may used
 * to start up the bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Activator extends HousekeepingActivator {

    // private static final String APPLICATION_ID = "com.openexchange.groupware.update";

    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, LeanConfigurationService.class };
    }

    @Override
    public void startBundle() {
        final ConfigurationService configService = getService(ConfigurationService.class);
        final LeanConfigurationService leanConfigService = getService(LeanConfigurationService.class);

        ExcludedSet.getInstance().configure(configService);
        NamespaceAwareExcludedSet.getInstance().loadExcludedNamespaces(leanConfigService);
        try {
            InternalList.getInstance().start();
        } catch (Error e) {
            // Helps finding errors in a lot of static code initialization.
            e.printStackTrace();
        }

        rememberTracker(new ServiceTracker<UpdateTaskProviderService, UpdateTaskProviderService>(context, UpdateTaskProviderService.class, new UpdateTaskCustomizer(context)));
        rememberTracker(new ServiceTracker<CacheService, CacheService>(context, CacheService.class.getName(), new CacheCustomizer(context)));

        openTrackers();

        registerService(CreateTableService.class, new CreateUpdateTaskTable());
        registerService(CreateTableService.class, new ObjectPermissionCreateTableService());
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put("RMI_NAME", UpdateTaskServiceImpl.RMI_NAME);
        
        ExtendedUpdateTaskServiceImpl extendedUpdateTaskServiceImpl = new ExtendedUpdateTaskServiceImpl();
        UpdateTaskServiceImpl updateTaskServiceImpl = extendedUpdateTaskServiceImpl;
        registerService(Remote.class, updateTaskServiceImpl, serviceProperties);
        registerService(ExtendedUpdateTaskService.class, extendedUpdateTaskServiceImpl);
    }

    @Override
    protected void stopBundle() throws Exception {
        InternalList.getInstance().stop();
        super.stopBundle();
    }
}
