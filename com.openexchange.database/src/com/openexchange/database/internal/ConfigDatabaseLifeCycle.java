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
            return "ConfigDB";
        }
    }
}
