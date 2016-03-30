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

package com.openexchange.realtime.packet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import com.openexchange.realtime.cleanup.AbstractRealtimeJanitor;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.util.LockMap;
import com.openexchange.realtime.util.OwnerAwareReentrantLock;

/**
 * {@link IDManager} - Manages {@link Lock}s associated with {@link ID}s and can be instructed to clean those states as it acts as a
 * {@link RealtimeJanitor}.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class IDManager extends AbstractRealtimeJanitor {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(IDManager.class);

    /** The global map for locks associated with an ID */
    protected final ConcurrentHashMap<ID, LockMap> LOCKS;

    /**
     * Initializes a new {@link IDManager}.
     */
    public IDManager() {
        LOCKS = new ConcurrentHashMap<ID, LockMap>(16, 0.9F, 1);
    }

    /**
     * Gets a "scope"-wide lock for a given {@link ID}.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;">
     * <b>NOTE</b>:<br>
     * Only for testing!
     * </div>
     *
     * @param id The id to associate the {@link Lock} with
     * @param scope The scope to be used for the {@link Lock}
     * @return The "scope"-wide lock for a given {@link ID}.
     */
    protected OwnerAwareReentrantLock getLock(ID id, String scope) {
        LockMap locksPerId = LOCKS.get(id);
        if (locksPerId == null) {
            locksPerId = new LockMap();
            LockMap meantime = LOCKS.putIfAbsent(id, locksPerId);
            locksPerId = (meantime != null) ? meantime : locksPerId;
        }

        OwnerAwareReentrantLock lock = null;
        synchronized (locksPerId) {
            if (locksPerId.isValid()) {
                lock = locksPerId.get(scope);
                if (lock == null) {
                    lock = new OwnerAwareReentrantLock();
                    locksPerId.put(scope, lock);
                }
            }
        }

        if (lock != null) {
            return lock;
        }

        // Retry w/o holding monitor...
        return getLock(id, scope);
    }

    /**
     * Acquires a "scope"-wide lock for a given {@link ID}.
     *
     * @param id The id to associate the {@link Lock} with
     * @param scope The scope to be used for the {@link Lock}
     * @return The acquired "scope"-wide lock for a given {@link ID}.
     */
    public void lock(ID id, String scope) {
        LockMap locksPerId = LOCKS.get(id);
        if (locksPerId == null) {
            locksPerId = new LockMap();
            LockMap meantime = LOCKS.putIfAbsent(id, locksPerId);
            locksPerId = (meantime != null) ? meantime : locksPerId;
        }

        OwnerAwareReentrantLock lock = null;
        synchronized (locksPerId) {
            if (locksPerId.isValid()) {
                lock = locksPerId.get(scope);
                if (lock == null) {
                    lock = new OwnerAwareReentrantLock();
                    locksPerId.put(scope, lock);
                }
            }
        }

        // Acquire lock w/o holding monitor
        if (null != lock) {
            lock.lock();
            return;
        }

        // Retry w/o holding monitor...
        lock(id, scope);
    }

    /**
     * Releases a "scope"-wide lock for a given {@link ID}.
     *
     * @param id The id to associate the {@link Lock} with
     * @param scope The scope to be used for the {@link Lock}
     * @return The acquired "scope"-wide lock for a given {@link ID}.
     */
    public void unlock(ID id, String scope) {
        LockMap locksPerId = LOCKS.get(id);
        if (locksPerId == null) {
            return;
        }

        OwnerAwareReentrantLock lock = null;
        synchronized (locksPerId) {
            lock = locksPerId.get(scope);
        }

        // Unlock w/o holding monitor
        if (lock != null) {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            } else {
                Thread owner = lock.getOwner();
                if (null != owner) {
                    Throwable t = new FastThrowable();
                    t.setStackTrace(owner.getStackTrace());
                    LOG.error("Tried to unlock, but is no owner. Current owner is {}.", owner.getName(), t);
                }
            }
        }
    }


    @Override
    public void cleanupForId(ID id) {
        LockMap locksPerId = LOCKS.get(id);
        if (locksPerId == null) {
            return;
        }

        synchronized (locksPerId) {
            if (false == locksPerId.hasOwners()) {
                locksPerId.markDeprecated();
                LOCKS.remove(id);
            }
        }
    }

    static final class FastThrowable extends Throwable {

        FastThrowable() {
            super("No owner");
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

}
