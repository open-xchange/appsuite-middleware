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
            } catch (final Throwable t) {
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
