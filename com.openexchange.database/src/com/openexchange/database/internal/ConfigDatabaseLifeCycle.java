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
import com.openexchange.database.ConfigurationListener.ConfigDBListener;
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

        configDBWrite = new ConfigPoolAdapter(Constants.CONFIGDB_WRITE_ID, configuration, (Configuration c) -> {
            return c.getWriteUrl();
        }, (Configuration c) -> {
            return c.getWriteProps();
        }, (Configuration c) -> {
            return c.getPoolConfig();
        });
        timer.addTask(configDBWrite.getCleanerTask());
        management.addPool(Constants.CONFIGDB_WRITE_ID, configDBWrite);
        reloader.setConfigurationListener(configDBWrite);

        configDBRead = new ConfigPoolAdapter(Constants.CONFIGDB_READ_ID, configuration, (Configuration c) -> {
            return c.getReadUrl();
        }, (Configuration c) -> {
            return c.getReadProps();
        }, (Configuration c) -> {
            return c.getPoolConfig();
        });
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

    private class ConfigPoolAdapter extends AbstractConfigurationListener<Configuration> implements ConfigDBListener {

        public ConfigPoolAdapter(int poolId, Configuration configuration, Function<Configuration, String> toUrl, Function<Configuration, Properties> toInfo, Function<Configuration, PoolConfig> toConf) {
            super(poolId, configuration, toUrl, toInfo, toConf);
        }

        @Override
        public void notify(Configuration configuration) {
            update(configuration);
        }

        @Override
        public int getPriority() {
            return 1;
        }
    }
}
