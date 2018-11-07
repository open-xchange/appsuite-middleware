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

package com.openexchange.database.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import com.openexchange.database.ConfigDatabaseService;
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

    private static final String SELECT = "SELECT url,driver,login,password,hardlimit,max,initial FROM db_pool WHERE db_pool_id=?";

    private final Management management;

    private final Timer timer;

    private final ConnectionReloaderImpl reloader;

    private final ConfigDatabaseService configDatabaseService;

    private final PoolConfig defaultPoolConfig;

    private final Map<Integer, ConnectionPool> pools = new ConcurrentHashMap<Integer, ConnectionPool>();

    private final Properties jdbcProperties;

    public ContextDatabaseLifeCycle(final Configuration configuration, final Management management, final Timer timer, ConnectionReloaderImpl reloader, final ConfigDatabaseService configDatabaseService) {
        super();
        this.management = management;
        this.timer = timer;
        this.reloader = reloader;
        this.configDatabaseService = configDatabaseService;
        this.defaultPoolConfig = configuration.getPoolConfig();
        this.jdbcProperties = configuration.getJdbcProps();
    }

    @Override
    public ConnectionPool create(final int poolId) throws OXException {
        final ConnectionData data = loadPoolData(poolId, jdbcProperties);
        try {
            Class.forName(data.driverClass);
        } catch (final ClassNotFoundException e) {
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

    private String removeParametersFromUrl(String url) {
        if (null == url) {
            return url;
        }

        int paramStart = url.indexOf('?');
        return paramStart >= 0 ? url.substring(0, paramStart) : url;
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
            String url = result.getString(1);
            conDataBuilder.withDriverClass(result.getString(2));
            defaults.put("user", result.getString(3));
            defaults.put("password", result.getString(4));
            conDataBuilder.withBlock(result.getBoolean(5));
            conDataBuilder.withMax(result.getInt(6));
            conDataBuilder.withMin(result.getInt(7));

            // Apply JDBC properties (and drop any parameters from JDBC URL)
            url = removeParametersFromUrl(url);
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

    private class ContextPoolAdapter extends AbstractConfigurationListener<ConnectionData> {

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
            } catch (OXException oxe) {
                LOG.error("Unable to load pool data.", oxe);
            } catch (final ClassNotFoundException e) {
                OXException exception = DBPoolingExceptionCodes.NO_DRIVER.create(e, data.driverClass);
                LOG.error("Unable to reload configuration", exception);
            }
        }
    }
}
