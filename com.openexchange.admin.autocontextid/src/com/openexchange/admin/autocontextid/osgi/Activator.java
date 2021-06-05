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

package com.openexchange.admin.autocontextid.osgi;

import java.sql.SQLException;
import java.util.Hashtable;
import com.openexchange.admin.autocontextid.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.autocontextid.rmi.impl.OXAutoCIDContextImpl;
import com.openexchange.admin.autocontextid.tools.AdminCacheExtended;
import com.openexchange.admin.daemons.AdminDaemonService;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    @Override
    public void startBundle() throws Exception {
        try {
            AdminCache.compareAndSetBundleContext(null, context);
            ConfigurationService service = getService(ConfigurationService.class);
            AdminCache.compareAndSetConfigurationService(null, service);
            initCache(service);
            track(DatabaseService.class, new DatabaseServiceCustomizer(context, ClientAdminThreadExtended.autocontextidCache.getPool()));
            track(DBMigrationExecutorService.class, new AutoCIDDBMigrationServiceTracker(this, context));
            openTrackers();

            final Hashtable<String, String> props = new Hashtable<String, String>(2);
            props.put("name", "OXContext");
            LOG.info(OXContextPluginInterface.class.getName());
            registerService(OXContextPluginInterface.class, new OXAutoCIDContextImpl(), props);
        } catch (SQLException e) {
            LOG.error("", e);
            throw e;
        } catch (OXGenericException e) {
            LOG.error("", e);
            throw e;
        }
    }

    private void initCache(final ConfigurationService service) throws SQLException, OXGenericException {
        final AdminCacheExtended cache = new AdminCacheExtended();
        cache.initCache(service);
        cache.initCacheExtended();
        cache.initIDGenerator();
        ClientAdminThreadExtended.autocontextidCache = cache;
        LOG.info("AutocontextID Bundle: Cache and Pools initialized!");
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, AdminDaemonService.class, DatabaseService.class, DBMigrationExecutorService.class };
    }
}
