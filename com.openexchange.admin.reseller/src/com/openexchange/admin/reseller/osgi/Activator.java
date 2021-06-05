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

package com.openexchange.admin.reseller.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.admin.daemons.AdminDaemonService;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.plugins.OXResellerPluginInterface;
import com.openexchange.admin.reseller.rmi.impl.OXReseller;
import com.openexchange.admin.reseller.rmi.impl.OXResellerContextImpl;
import com.openexchange.admin.reseller.rmi.impl.OXResellerUserImpl;
import com.openexchange.admin.reseller.rmi.impl.ResellerAuth;
import com.openexchange.admin.reseller.services.PluginInterfaces;
import com.openexchange.admin.reseller.tools.AdminCacheExtended;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;

public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    public Activator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        try {
            AdminCache.compareAndSetBundleContext(null, context);

            CacheService cacheService = getService(CacheService.class);
            AdminCache.compareAndSetCacheService(null, cacheService);

            ConfigurationService configurationService = getService(ConfigurationService.class);
            AdminCache.compareAndSetConfigurationService(null, configurationService);
            initCache(configurationService);

            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put("RMI_NAME", OXReseller.RMI_NAME);
            registerService(Remote.class, new OXReseller(), serviceProperties);
            LOG.info("RMI Interface for reseller bundle bound to RMI registry");

            Hashtable<String, String> props = new Hashtable<String, String>(2);
            props.put("name", "BasicAuthenticator");
            LOG.info(BasicAuthenticatorPluginInterface.class.getName());
            registerService(BasicAuthenticatorPluginInterface.class, new ResellerAuth(), props);

            props = new Hashtable<String, String>(2);
            props.put("name", "OXContext");
            LOG.info(OXContextPluginInterface.class.getName());
            registerService(OXContextPluginInterface.class, new OXResellerContextImpl(), props);

            props = new Hashtable<String, String>(2);
            props.put("name", "OXUser");
            LOG.info(OXUserPluginInterface.class.getName());
            registerService(OXUserPluginInterface.class, new OXResellerUserImpl(), props);

            track(DatabaseService.class, new DatabaseServiceCustomizer(context, ClientAdminThreadExtended.cache.getPool()));
            track(DBMigrationExecutorService.class, new ResellerDBMigrationServiceTracker(this, context));

            // Plugin interfaces
            {
                final int defaultRanking = 100;
                final RankingAwareNearRegistryServiceTracker<OXResellerPluginInterface> rtracker = new RankingAwareNearRegistryServiceTracker<OXResellerPluginInterface>(context, OXResellerPluginInterface.class, defaultRanking);
                rememberTracker(rtracker);

                final PluginInterfaces.Builder builder = new PluginInterfaces.Builder().resellerPlugins(rtracker);

                PluginInterfaces.setInstance(builder.build());
            }

            openTrackers();
        } catch (StorageException e) {
            LOG.error("Error while creating one instance for RMI interface", e);
            throw e;
        } catch (OXGenericException e) {
            LOG.error("", e);
            throw e;
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        PluginInterfaces.setInstance(null);
        super.stopBundle();
    }

    private void initCache(final ConfigurationService service) throws OXGenericException {
        final AdminCacheExtended cache = new AdminCacheExtended();
        cache.initCache(service);
        cache.initCacheExtended();
        ClientAdminThreadExtended.cache = cache;
        LOG.info("ResellerBundle: Cache and Pools initialized!");
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, AdminDaemonService.class, DatabaseService.class, DBMigrationExecutorService.class, CacheService.class };
    }
}
