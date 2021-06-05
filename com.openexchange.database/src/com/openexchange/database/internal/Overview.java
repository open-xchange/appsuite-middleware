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

/**
 * {@link Overview}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Overview implements OverviewMBean {

    private final Pools pools;
    private final ReplicationMonitor monitor;

    public Overview(Pools pools, ReplicationMonitor monitor) {
        super();
        this.pools = pools;
        this.monitor = monitor;
    }

    @Override
    public int getNumConnections() {
        int retval = 0;
        for (ConnectionPool pool : pools.getPools()) {
            retval += pool.getPoolSize();
        }
        return retval;
    }

    @Override
    public long getMasterConnectionsFetched() {
        return monitor.getMasterConnectionsFetched();
    }

    @Override
    public long getSlaveConnectionsFetched() {
        return monitor.getSlaveConnectionsFetched();
    }

    @Override
    public long getMasterInsteadOfSlave() {
        return monitor.getMasterInsteadOfSlave();
    }
}
