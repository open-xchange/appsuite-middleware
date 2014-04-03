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

package com.openexchange.cluster.lock.internal;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.exception.OXException;

/**
 * {@link ClusterLockServiceImpl}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ClusterLockServiceImpl implements ClusterLockService {

    private final HazelcastInstance hazelcastInstance;

    private final ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * Initializes a new {@link ClusterLockServiceImpl}.
     */
    public ClusterLockServiceImpl(HazelcastInstance hazelcastInstance) {
        super();
        this.hazelcastInstance = hazelcastInstance;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.cluster.lock.ClusterLock#acquireClusterLock()
     */
    @Override
    public Lock acquireClusterLock(String action) throws OXException {
        // TODO
        return hazelcastInstance.getLock(action);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.cluster.lock.ClusterLockService#acquirePeriodicClusterLock(java.lang.String, java.lang.Long)
     */
    @Override
    public Lock acquirePeriodicClusterLock(String action, Long period) throws OXException {
        Lock lock = null;
        try {
            reentrantLock.tryLock(5, TimeUnit.SECONDS);
            Long now = System.currentTimeMillis();
            ConcurrentMap<String, Long> map = hazelcastInstance.getMap("ClusterLocks");
            Long timestamp = map.get(action);
            if (timestamp != null) {
                if (now - timestamp < period) {
                    throw ClusterLockExceptionCodes.CLUSTER_PERIODIC_LOCKED.create(period, action, period - (now - timestamp));
                }
            }
            lock = hazelcastInstance.getLock(action);
            map.putIfAbsent(action, now);
        } catch (InterruptedException e) {
            throw ClusterLockExceptionCodes.TIMEOUT.create();
        } finally {
            reentrantLock.unlock();
        }
        return lock;
    }
}
