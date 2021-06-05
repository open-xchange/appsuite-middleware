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

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.ConnectionType;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.pooling.ExhaustedActions;
import com.openexchange.pooling.PoolConfig;

/**
 * Handles the life cycle of database connection pools for contexts databases.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContextDatabaseLifeCycle implements PoolLifeCycle {

    private static final String SELECT = "SELECT p.url,p.driver,p.login,p.password,p.hardlimit,p.max,p.initial,c.write_db_pool_id "
                                       + "FROM db_pool AS p JOIN db_cluster AS c ON p.db_pool_id = c.read_db_pool_id OR p.db_pool_id = c.write_db_pool_id "
                                       + "WHERE p.db_pool_id=?";

    private final Management management;

    private final Timer timer;

    private final ConnectionReloaderImpl reloader;

    private final ConfigDatabaseService configDatabaseService;

    private final PoolConfig defaultPoolConfig;

    private final Map<Integer, ConnectionPool> pools = new ConcurrentHashMap<Integer, ConnectionPool>();

    private final Properties jdbcProperties;

    final ConfigurationService configurationService;

    /**
     * Initializes a new {@link ContextDatabaseLifeCycle}.
     *
     * @param configuration
     * @param management
     * @param timer
     * @param reloader
     * @param configDatabaseService
     * @param configurationService
     */
    public ContextDatabaseLifeCycle(final Configuration configuration, final Management management, final Timer timer, ConnectionReloaderImpl reloader, final ConfigDatabaseService configDatabaseService, final ConfigurationService configurationService) {
        super();
        this.management = management;
        this.timer = timer;
        this.reloader = reloader;
        this.configDatabaseService = configDatabaseService;
        this.defaultPoolConfig = configuration.getPoolConfig();
        this.jdbcProperties = configuration.getJdbcProps();
        this.configurationService = configurationService;
    }

    @Override
    public ConnectionPool create(final int poolId) throws OXException {
        final ConnectionData data = loadPoolData(poolId, jdbcProperties);
        try {
            Class.forName(data.driverClass);
        } catch (ClassNotFoundException e) {
            throw DBPoolingExceptionCodes.NO_DRIVER.create(e, data.driverClass);
        }
        final ContextPoolAdapter retval = new ContextPoolAdapter(poolId, data, (ConnectionData c) -> {
            return c.url;
        }, (ConnectionData c) -> {
            return c.props;
        }, (ConnectionData c) -> {
            return getConfig(c);
        });

        pools.put(I(poolId), retval);
        timer.addTask(retval.getCleanerTask());
        management.addPool(poolId, retval);
        reloader.setConfigurationListener(retval);
        return retval;
    }

    @Override
    public boolean destroy(final int poolId) {
        final ConnectionPool toDestroy = pools.remove(I(poolId));
        if (null == toDestroy) {
            return false;
        }
        management.removePool(poolId);
        timer.removeTask(toDestroy.getCleanerTask());
        reloader.removeConfigurationListener(poolId);
        toDestroy.destroy();
        return true;
    }

    private PoolConfig getConfig(final ConnectionData data) {
        final PoolConfig.Builder retval = PoolConfig.builder(defaultPoolConfig);
        retval.withMaxActive(data.max);
        if (data.block) {
            retval.withExhaustedAction(ExhaustedActions.BLOCK);
        } else {
            retval.withExhaustedAction(ExhaustedActions.GROW);
        }
        return retval.build();
    }

    ConnectionData loadPoolData(int poolId, Properties jdbcProperties) throws OXException {
        Connection con = configDatabaseService.getReadOnly();
        try {
            return loadPoolData(poolId, jdbcProperties, con);
        } finally {
            configDatabaseService.backReadOnly(con);
        }
    }

    ConnectionData loadPoolData(int poolId, Properties jdbcProperties, Connection con) throws OXException {
        if (null == con) {
            return loadPoolData(poolId, jdbcProperties);
        }

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setInt(1, poolId);
            result = stmt.executeQuery();
            if (false == result.next()) {
                throw DBPoolingExceptionCodes.NO_DBPOOL.create(I(poolId));
            }

            ConnectionData.Builder conDataBuilder = ConnectionData.builder();
            Properties defaults = new Properties();

            // Apply arguments read from database
            int index = 1;
            String url = result.getString(index++);
            conDataBuilder.withDriverClass(result.getString(index++));
            defaults.put("user", result.getString(index++));
            defaults.put("password", result.getString(index++));
            conDataBuilder.withBlock(result.getBoolean(index++));
            conDataBuilder.withMax(result.getInt(index++));
            conDataBuilder.withMin(result.getInt(index++));
            conDataBuilder.withType(ConnectionType.get(poolId == result.getInt(index++)));
            // Apply JDBC properties (and drop any parameters from JDBC URL)
            url = JdbcPropertiesImpl.doRemoveParametersFromJdbcUrl(url);
            conDataBuilder.withUrl(url);

            defaults.putAll(jdbcProperties);
            conDataBuilder.withProps(defaults);

            return conDataBuilder.build();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private class ContextPoolAdapter extends AbstractMetricAwarePool<ConnectionData> {

        private volatile Set<Integer> globalDBPoolIds;

        ContextPoolAdapter(int poolId, ConnectionData data, Function<ConnectionData, String> toURL, Function<ConnectionData, Properties> toConnectionArguments, Function<ConnectionData, PoolConfig> toConfig) {
            super(poolId, data, toURL, toConnectionArguments, toConfig);
        }

        @Override
        public void notify(Configuration configuration) {
            ConnectionData data = null;
            try {
                data = loadPoolData(getPoolId(), configuration.getJdbcProps());
                Class.forName(data.driverClass);
                update(data);
                globalDBPoolIds = GlobalDbInit.getGlobalDBPoolIds(configurationService);
                initMetrics();
            } catch (OXException oxe) {
                LOG.error("Unable to load pool data.", oxe);
            } catch (ClassNotFoundException e) {
                OXException exception = DBPoolingExceptionCodes.NO_DRIVER.create(e, data.driverClass);
                LOG.error("Unable to reload configuration", exception);
            }
        }

        @Override
        protected String getPoolClass() {
            Set<Integer> globalDBPoolIds = this.globalDBPoolIds;
            if (globalDBPoolIds == null) {
                synchronized (this) {
                    globalDBPoolIds = this.globalDBPoolIds;
                    if (globalDBPoolIds == null) {
                        globalDBPoolIds = GlobalDbInit.getGlobalDBPoolIds(configurationService);
                        this.globalDBPoolIds = globalDBPoolIds;
                    }
                }
            }
            return globalDBPoolIds != null && globalDBPoolIds.contains(I(getPoolId())) ? "globaldb" : "userdb";
        }
    }

}
