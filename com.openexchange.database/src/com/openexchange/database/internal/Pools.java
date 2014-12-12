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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * This class stores all connection pools. It also removes pools that are empty.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Pools implements Runnable {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Pools.class);

    private final Runnable cleaner;
    private final Queue<PoolLifeCycle> lifeCycles;
    private final ConcurrentMap<Integer, ConnectionPool> pools;

    /**
     * Initializes a new instance.
     *
     * @param timer The timer used to schedule clean-up task
     */
    Pools(Timer timer) {
        super();
        lifeCycles = new ConcurrentLinkedQueue<PoolLifeCycle>();
        pools = new ConcurrentHashMap<Integer, ConnectionPool>();

        cleaner = new Runnable() {
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

        timer.addTask(cleaner);
    }

    /**
     * @return an array with all connection pools.
     */
    ConnectionPool[] getPools() {
        List<ConnectionPool> retval = new ArrayList<ConnectionPool>();
        retval.addAll(pools.values());
        return retval.toArray(new ConnectionPool[retval.size()]);
    }

    /**
     * @return the {@link ConnectionPool} and never <code>null</code>.
     * @throws OXException if creating the pool fails.
     */
    ConnectionPool getPool(final int poolId) throws OXException {
        ConnectionPool retval = pools.get(I(poolId));
        if (null == retval) {
            ConnectionPool connectionPool = null;
            for (Iterator<PoolLifeCycle> it = lifeCycles.iterator(); null == connectionPool && it.hasNext();) {
                PoolLifeCycle lifeCycle = it.next();
                connectionPool = lifeCycle.create(poolId);
            }

            if (null == connectionPool) {
                throw DBPoolingExceptionCodes.NO_DBPOOL.create(I(poolId));
            }

            retval = pools.putIfAbsent(I(poolId), connectionPool);
            if (null == retval) {
                retval = connectionPool;
            }
        }
        return retval;
    }

    @Override
    public void run() {
        LOG.trace("Starting cleaner run.");

        for (Iterator<Map.Entry<Integer, ConnectionPool>> iter = pools.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Integer, ConnectionPool> entry = iter.next();
            ConnectionPool pool = entry.getValue();
            if (pool.isEmpty()) {
                iter.remove();
                boolean destroyed = false;
                for (Iterator<PoolLifeCycle> it = lifeCycles.iterator(); !destroyed && it.hasNext();) {
                    PoolLifeCycle lifeCycle = it.next();
                    destroyed = lifeCycle.destroy(entry.getKey().intValue());
                }
                if (!destroyed) {
                    LOG.error("", DBPoolingExceptionCodes.UNKNOWN_POOL.create(entry.getKey()));
                }
            }
        }

        LOG.trace("Cleaner run done.");
    }

    void start(final Timer timer) {
        timer.addTask(cleaner);
    }

    void addLifeCycle(final PoolLifeCycle lifeCycle) {
        lifeCycles.add(lifeCycle);
    }

    void stop(final Timer timer) {
        timer.removeTask(cleaner);

        for (Map.Entry<Integer, ConnectionPool> entry : pools.entrySet()) {
            boolean destroyed = false;
            for (Iterator<PoolLifeCycle> it = lifeCycles.iterator(); !destroyed && it.hasNext();) {
                PoolLifeCycle lifeCycle = it.next();
                destroyed = lifeCycle.destroy(entry.getKey().intValue());
            }
            if (!destroyed) {
                LOG.error("", DBPoolingExceptionCodes.UNKNOWN_POOL.create(entry.getKey()));
            }
        }
        pools.clear();
    }

}
