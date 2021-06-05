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

package com.openexchange.database.internal;

import java.util.Properties;
import java.util.function.Function;
import com.openexchange.database.ConnectionType;
import com.openexchange.database.internal.ConfigurationListener.ConfigDBListener;
import com.openexchange.pooling.PoolConfig;

/**
 * Creates the pools for the configuration database connections.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ConfigDatabaseLifeCycle implements PoolLifeCycle {

    private final ConfigPoolAdapter configDBWrite;

    private final ConfigPoolAdapter configDBRead;

    ConfigDatabaseLifeCycle(final Configuration configuration, final Management management, final Timer timer, ConnectionReloaderImpl reloader) {
        super();

        configDBWrite = new ConfigPoolAdapter(Constants.CONFIGDB_WRITE_ID, new ConnectionTypeAwareConfigurationWrapper(ConnectionType.WRITABLE, configuration),
            (c) -> c.getConfig().getWriteUrl(),
            (c) -> c.getConfig().getConfigDbWriteProps(),
            (c) -> c.getConfig().getPoolConfig());
        timer.addTask(configDBWrite.getCleanerTask());
        management.addPool(Constants.CONFIGDB_WRITE_ID, configDBWrite);
        reloader.setConfigurationListener(configDBWrite);

        configDBRead = new ConfigPoolAdapter(Constants.CONFIGDB_READ_ID, new ConnectionTypeAwareConfigurationWrapper(ConnectionType.READONLY, configuration),
            (c) -> c.getConfig().getReadUrl(),
            (c) -> c.getConfig().getConfigDbReadProps(),
            (c) -> c.getConfig().getPoolConfig());
        timer.addTask(configDBRead.getCleanerTask());
        management.addPool(Constants.CONFIGDB_READ_ID, configDBRead);
        reloader.setConfigurationListener(configDBRead);
    }

    @Override
    public ConnectionPool create(final int poolId) {
        switch (poolId) {
            case Constants.CONFIGDB_WRITE_ID:
                return configDBWrite;
            case Constants.CONFIGDB_READ_ID:
                return configDBRead;
            default:
                return null;
        }
    }

    @Override
    public boolean destroy(final int poolId) {
        // Pools to configuration database will not be destroyed.
        return poolId == Constants.CONFIGDB_WRITE_ID || poolId == Constants.CONFIGDB_READ_ID;
    }

    /**
     * {@link ConfigPoolAdapter}
     *
     */
    private static class ConfigPoolAdapter extends AbstractMetricAwarePool<ConnectionTypeAwareConfigurationWrapper> implements ConfigDBListener {

        /**
         * Initializes a new {@link ConfigPoolAdapter}.
         *
         * @param poolId The pool id
         * @param configuration A {@link ConnectionTypeAwareConfigurationWrapper} containing the configuration and the {@link ConnectionType}
         * @param toUrl A function for getting the url
         * @param toConnectionArguments A function for getting the connection arguments
         * @param toPoolConf A function for getting the {@link PoolConfig}
         */
        ConfigPoolAdapter(int poolId, ConnectionTypeAwareConfigurationWrapper configuration, Function<ConnectionTypeAwareConfigurationWrapper, String> toUrl, Function<ConnectionTypeAwareConfigurationWrapper, Properties> toConnectionArguments, Function<ConnectionTypeAwareConfigurationWrapper, PoolConfig> toPoolConf) {
            super(poolId, configuration, toUrl, toConnectionArguments, toPoolConf);
        }

        @Override
        public void notify(Configuration configuration) {
            update(new ConnectionTypeAwareConfigurationWrapper(getType(), configuration));
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        protected String getPoolClass() {
            return "configdb";
        }
    }
}
