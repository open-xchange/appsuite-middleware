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

import java.sql.Connection;
import java.util.Iterator;
import java.util.Properties;
import java.util.function.Function;
import com.openexchange.pooling.PoolConfig;
import com.openexchange.pooling.PoolImplData;
import com.openexchange.pooling.PooledData;

/**
 * {@link AbstractConfigurationListener} - Bridge between {@link ConnectionPool}s and the reloadable
 * from {@link ConfigurationListener}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The Class of the data to be processed by the converters
 * @since v7.10.1
 */
public abstract class AbstractConfigurationListener<T> extends ConnectionPool implements ConfigurationListener {

    private final int poolId;
    private final Function<T, String> urlConverter;
    private final Function<T, Properties> connectionArgumentsConverter;
    private final Function<T, PoolConfig> poolConfigConverter;

    /**
     * Initializes a new {@link AbstractConfigurationListener}.
     *
     * @param poolId The pool identifier
     * @param data The initial data to feed the converters with
     * @param urlConverter Converter to get URL
     * @param connectionArgumentsConverter Converter to get connection arguments' {@link Properties}
     * @param poolConfigConverter Converter to get {@link PoolConfig}
     */
    protected AbstractConfigurationListener(int poolId, T data, Function<T, String> urlConverter, Function<T, Properties> connectionArgumentsConverter, Function<T, PoolConfig> poolConfigConverter) {
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
