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

package com.openexchange.realtime.packet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.realtime.cleanup.AbstractRealtimeJanitor;
import com.openexchange.realtime.cleanup.RealtimeJanitor;

/**
 * {@link IDManager} - Manages {@link Lock}s associated with {@link ID}s and can be instructed to clean those states as it acts as a
 * {@link RealtimeJanitor}.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class IDManager extends AbstractRealtimeJanitor {

    protected final ConcurrentHashMap<ID, ConcurrentHashMap<String, Lock>> LOCKS;

    public IDManager() {
        LOCKS = new ConcurrentHashMap<ID, ConcurrentHashMap<String, Lock>>();
    }

    /**
     * Get a "scope"-wide lock for a given {@link ID}.
     * 
     * @param id The id to associate the {@link Lock} with
     * @param scope The scope to be used for the {@link Lock}
     * @return The "scope"-wide lock for a given {@link ID}.
     */
    public Lock getLock(ID id, String scope) {
        ConcurrentHashMap<String, Lock> locksPerId = LOCKS.get(id);
        if (locksPerId == null) {
            locksPerId = new ConcurrentHashMap<String, Lock>();
            ConcurrentHashMap<String, Lock> meantime = LOCKS.putIfAbsent(id, locksPerId);
            locksPerId = (meantime != null) ? meantime : locksPerId;
        }
        Lock lock = locksPerId.get(scope);
        if (lock == null) {
            lock = new ReentrantLock();
            Lock l = locksPerId.putIfAbsent(scope, lock);
            lock = (l != null) ? l : lock;
        }
        return lock;
    }

    @Override
    public void cleanupForId(ID id) {
        LOCKS.remove(id);
    }

}
