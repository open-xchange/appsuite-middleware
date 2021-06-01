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

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * This class stores all connection pools. It also removes pools that are empty.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Pools implements Runnable {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Pools.class);

    private final List<PoolLifeCycle> lifeCycles = new ArrayList<PoolLifeCycle>(2);
    private final Lock poolsLock = new ReentrantLock(true);
    private final Map<Integer, ConnectionPool> pools = new HashMap<Integer, ConnectionPool>();

    Pools(final Timer timer) {
        super();
        timer.addTask(cleaner);
    }

    /**
     * @return an array with all connection pools.
     */
    ConnectionPool[] getPools() {
        final List<ConnectionPool> retval = new ArrayList<ConnectionPool>();
        poolsLock.lock();
        try {
            retval.addAll(pools.values());
        } finally {
            poolsLock.unlock();
        }
        return retval.toArray(new ConnectionPool[retval.size()]);
    }

    /**
     * 
     * @param poolId The db pool id
     * @return the {@link ConnectionPool} and never <code>null</code>.
     * @throws OXException if creating the pool fails.
     */
    ConnectionPool getPool(final int poolId) throws OXException {
        ConnectionPool retval = pools.get(I(poolId));
        if (null != retval) {
            return retval;
        }
        poolsLock.lock();
        try {
            retval = pools.get(I(poolId));
            if (null == retval) {
                for (final PoolLifeCycle lifeCycle : lifeCycles) {
                    retval = lifeCycle.create(poolId);
                    if (null != retval) {
                        break;
                    }
                }
                if (null == retval) {
                    throw DBPoolingExceptionCodes.NO_DBPOOL.create(I(poolId));
                }
                pools.put(I(poolId), retval);
            }
        } finally {
            poolsLock.unlock();
        }
        return retval;
    }

    private final Runnable cleaner = new Runnable() {

        @Override
        public void run() {
            try {
                final Thread thread = Thread.currentThread();
                final String origName = thread.getName();
                thread.setName("PoolsCleaner");
                Pools.this.run();
                thread.setName(origName);
            } catch (Throwable t) {
                LOG.error("", t);
            }
        }
    };

    @Override
    public void run() {
        LOG.trace("Starting cleaner run.");
        poolsLock.lock();
        try {
            final Iterator<Map.Entry<Integer, ConnectionPool>> iter = pools.entrySet().iterator();
            while (iter.hasNext()) {
                final Map.Entry<Integer, ConnectionPool> entry = iter.next();
                final ConnectionPool pool = entry.getValue();
                if (pool.isEmpty()) {
                    iter.remove();
                    destroy(entry.getKey().intValue());
                }
            }
        } finally {
            poolsLock.unlock();
        }
        LOG.trace("Cleaner run ending.");
    }

    void start(final Timer timer) {
        timer.addTask(cleaner);
    }

    void addLifeCycle(final PoolLifeCycle lifeCycle) {
        lifeCycles.add(lifeCycle);
    }

    void stop(final Timer timer) {
        timer.removeTask(cleaner);
        poolsLock.lock();
        try {
            for (final Map.Entry<Integer, ConnectionPool> entry : pools.entrySet()) {
                destroy(entry.getKey().intValue());
            }
            pools.clear();
        } finally {
            poolsLock.unlock();
        }
    }

    void destroy(int poolId) {
        boolean destroyed = false;
        for (final PoolLifeCycle lifeCycle : lifeCycles) {
            destroyed = lifeCycle.destroy(poolId);
            if (destroyed) {
                break;
            }
        }
        if (!destroyed) {
            final OXException e = DBPoolingExceptionCodes.UNKNOWN_POOL.create(I(poolId));
            LOG.error(e.getMessage(), e);
        }
    }
}
