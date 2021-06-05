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

import java.sql.Connection;
import java.util.Iterator;
import java.util.Properties;
import java.util.function.Function;
import com.openexchange.pooling.PoolConfig;
import com.openexchange.pooling.PoolImplData;
import com.openexchange.pooling.PooledData;

/**
 * {@link AbstractConfigurationReloadAwareConnectionPool} - Bridge between {@link ConnectionPool}s and the reloadable
 * from {@link ConfigurationListener}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The Class of the data to be processed by the converters
 * @since v7.10.1
 */
public abstract class AbstractConfigurationReloadAwareConnectionPool<T> extends ConnectionPool implements ConfigurationListener {

    private final int poolId;
    private final Function<T, String> urlConverter;
    private final Function<T, Properties> connectionArgumentsConverter;
    private final Function<T, PoolConfig> poolConfigConverter;

    /**
     * Initializes a new {@link AbstractConfigurationReloadAwareConnectionPool}.
     *
     * @param poolId The pool identifier
     * @param data The initial data to feed the converters with
     * @param urlConverter Converter to get URL
     * @param connectionArgumentsConverter Converter to get connection arguments' {@link Properties}
     * @param poolConfigConverter Converter to get {@link PoolConfig}
     */
    protected AbstractConfigurationReloadAwareConnectionPool(int poolId, T data, Function<T, String> urlConverter, Function<T, Properties> connectionArgumentsConverter, Function<T, PoolConfig> poolConfigConverter) {
        super(urlConverter.apply(data), connectionArgumentsConverter.apply(data), poolConfigConverter.apply(data));
        this.poolId = poolId;
        this.urlConverter = urlConverter;
        this.connectionArgumentsConverter = connectionArgumentsConverter;
        this.poolConfigConverter = poolConfigConverter;
    }

    @Override
    public int getPoolId() {
        return poolId;
    }

    /**
     * Updated the {@link ConnectionLifecycle} ({@link #getLifecycle()})
     * and the {@link PoolConfig} ({@link #setConfig(PoolConfig)})
     *
     * @param updatedData The updated data to feed the converters with
     */
    protected void update(T updatedData) {
        // Lock pool
        lock.lock();
        try {
            // Apply new JDBC URL and connection arguments
            lifecycle.setUrlAndConnectionArgs(urlConverter.apply(updatedData), connectionArgumentsConverter.apply(updatedData));

            // Destroy all idle
            PoolImplData<Connection> poolData = this.data;
            while (false == poolData.isIdleEmpty()) {
                // Don't pop idle to avoid them being marked as active
                data.removeIdle(0);
            }
            // Mark all active as deprecated
            final Iterator<PooledData<Connection>> iter = poolData.listActive();
            while (iter.hasNext()) {
                iter.next().setDeprecated();
            }
            this.setConfig(poolConfigConverter.apply(updatedData));

            // Notify all waiting about free resources
            idleAvailable.signal();
        } finally {
            lock.unlock();
        }
    }
}
