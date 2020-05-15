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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.time.Duration;
import java.util.Properties;
import java.util.function.Function;
import com.openexchange.database.ConnectionType;
import com.openexchange.pooling.PoolConfig;
import com.openexchange.pooling.PoolingException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * {@link AbstractMetricAwarePool}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public abstract class AbstractMetricAwarePool<T extends ConnectionTypeAware> extends AbstractConfigurationReloadAwareConnectionPool<T> {

    private static final String GROUP = "appsuite.mysql.connections.";

    private final ConnectionType type;

    private Counter timeout;
    private Timer createTimer;
    private Timer usage;
    private Timer acquireTimer;

    /**
     * Initializes a new {@link AbstractMetricAwarePool}.
     *
     * @param poolId
     * @param data
     * @param urlConverter
     * @param connectionArgumentsConverter
     * @param poolConfigConverter
     */
    protected AbstractMetricAwarePool(int poolId,
                                      T data,
                                      Function<T, String> urlConverter,
                                      Function<T, Properties> connectionArgumentsConverter,
                                      Function<T, PoolConfig> poolConfigConverter
                                      ) {
        super(poolId, data, urlConverter, connectionArgumentsConverter, poolConfigConverter);
        type = data.getConnectionType();
        initMetrics();
    }

    /**
     * Initializes the metrics for this pool
     */
    private void initMetrics() {
        Tags tags = Tags.of("class", getPoolClass(), "type", type.getTagName(), "pool", String.valueOf(getPoolId()));
        // @formatter:off
        Gauge.builder(GROUP + "active", () -> I(getNumActive()))
             .description("The currently active connections of this db pool")
             .tags(tags)
             .register(Metrics.globalRegistry);
        Gauge.builder(GROUP + "max", () -> I(getMaxActive()))
             .description("The maximum number of active connections of this db pool")
             .tags(tags)
             .register(Metrics.globalRegistry);
        Gauge.builder(GROUP + "total", () -> I(getNumActive() + getNumIdle()))
             .description("The total number of pooled connections of this db pool")
             .tags(tags)
             .register(Metrics.globalRegistry);
        Gauge.builder(GROUP + "idle", () -> I(getNumIdle()))
             .description("The number of idle connections of this db pool")
             .tags(tags)
             .register(Metrics.globalRegistry);
        createTimer = Timer.builder(GROUP + "create")
                           .description("The time it takes to initialize a new connection")
                           .tags(tags)
                           .register(Metrics.globalRegistry);
        usage = Timer.builder(GROUP + "usage")
                     .description("The time between acquiration and returning a connection back to pool")
                     .tags(tags)
                     .register(Metrics.globalRegistry);
        acquireTimer = Timer.builder(GROUP + "acquire")
                       .description("The time it takes for a thread to aquire a connection")
                       .tags(tags)
                       .register(Metrics.globalRegistry);
        timeout = Counter.builder(GROUP + "timeout")
                         .description("The number of timeouts")
                         .tags(tags)
                         .register(Metrics.globalRegistry);
        // @formatter:on
    }

    @Override
    public Connection get() throws PoolingException {
        long start = System.nanoTime();

        try {
            return new ConnectionWrapper(super.get());
        } finally {
            acquireTimer.record(Duration.ofNanos(System.nanoTime() - start));
        }
    }

    @Override
    public Connection getWithoutTimeout() throws PoolingException {
        long start = System.nanoTime();
        try {
            return new ConnectionWrapper(super.getWithoutTimeout());
        } finally {
            createTimer.record(Duration.ofNanos(System.nanoTime() - start));
            acquireTimer.record(Duration.ofNanos(System.nanoTime() - start));
        }
    }

    @Override
    public void back(Connection pooled) throws PoolingException {
        if(pooled instanceof ConnectionWrapper) {
            try {
                trackTimeout((ConnectionWrapper) pooled);
                super.back(((ConnectionWrapper) pooled).getDelegate());
            } finally {
                usage.record(Duration.ofNanos(System.nanoTime() - ((ConnectionWrapper) pooled).getStart()));
            }
        } else {
            super.back(pooled);
        }
    }

    /**
     * Tracks connections with timeouts
     *
     * @param wrapper The {@link ConnectionWrapper}
     */
    private void trackTimeout(ConnectionWrapper wrapper) {
        if(wrapper.hasTimeout()) {
            timeout.increment();
        }
    }

    @Override
    public void backWithoutTimeout(Connection pooled) {
        if(pooled instanceof ConnectionWrapper) {
            try {
                trackTimeout((ConnectionWrapper) pooled);
                super.backWithoutTimeout(((ConnectionWrapper) pooled).getDelegate());
            } finally {
                usage.record(Duration.ofNanos(System.nanoTime() - ((ConnectionWrapper) pooled).getStart()));
            }
        } else {
            super.backWithoutTimeout(pooled);
        }
    }

    @Override
    protected Connection createPooledObject() throws Exception {
        long start = System.nanoTime();
        try {
            return super.createPooledObject();
        } finally {
            createTimer.record(Duration.ofNanos(System.nanoTime() - start));
        }
    }

    /**
     * Gets the pool class
     *
     * @return The name of the pool class
     */
    protected abstract String getPoolClass();

    /**
     * Gets the {@link ConnectionType}
     *
     * @return The connection type
     */
    protected ConnectionType getType() {
        return type;
    }

}
