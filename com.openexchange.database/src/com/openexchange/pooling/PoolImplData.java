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

package com.openexchange.pooling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Stores the data structures for a pool. Access to this class must be
 * synchronized because it isn't thread safe.
 * 
 * @param <T> type of objects to pool.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class PoolImplData<T> implements PoolData<T> {

    /**
     * Idle pooled objects.
     */
    private final List<PooledData<T>> idle;

    /**
     * Pooled objects that are currently active.
     */
    private final Map<T, PooledData<T>> active;

    /**
     * Pooled objects that are currently active referenced by threads.
     */
    private final Map<Thread, PooledData<T>> activeByThread;

    private int creating = 0;

    /**
     * Default constructor.
     */
    PoolImplData() {
        super();
        idle = new ArrayList<PooledData<T>>();
        active = new HashMap<T, PooledData<T>>();
        activeByThread = new HashMap<Thread, PooledData<T>>();
    }

    @Override
    public PooledData<T> popIdle() {
        PooledData<T> retval;
        final int pos = idle.size();
        if (0 == pos) {
            retval = null;
        } else {
            retval = idle.remove(pos - 1);
            active.put(retval.getPooled(), retval);
        }
        return retval;
    }

    @Override
    public void addActive(final PooledData<T> newActive) {
        active.put(newActive.getPooled(), newActive);
    }

    @Override
    public PooledData<T> getActive(final T pooled) {
        return active.get(pooled);
    }

    @Override
    public Iterator<PooledData<T>> listActive() {
        return active.values().iterator();
    }

    @Override
    public boolean removeActive(final PooledData<T> toRemove) {
        return toRemove.equals(active.remove(toRemove.getPooled()));
    }

    @Override
    public void addCreating() {
        creating++;
    }

    @Override
    public void removeCreating() {
        creating--;
    }

    @Override
    public int getCreating() {
        return creating;
    }

    @Override
    public int numActive() {
        return active.size() + creating;
    }

    @Override
    public boolean isActiveEmpty() {
        return active.isEmpty() && creating == 0;
    }

    @Override
    public void addIdle(final PooledData<T> newIdle) {
        idle.add(newIdle);
    }

    @Override
    public PooledData<T> getIdle(final int index) {
        return idle.get(index);
    }

    @Override
    public void removeIdle(final int index) {
        idle.remove(index);
    }

    @Override
    public int numIdle() {
        return idle.size();
    }

    @Override
    public boolean isIdleEmpty() {
        return idle.isEmpty();
    }

    @Override
    public void addByThread(final PooledData<T> toCheck) {
        activeByThread.put(toCheck.getThread(), toCheck);
    }

    @Override
    public PooledData<T> getByThread(final Thread thread) {
        return activeByThread.get(thread);
    }

    @Override
    public void removeByThread(final PooledData<T> toCheck) {
        activeByThread.remove(toCheck.getThread());
    }

    @Override
    public Collection<PooledData<T>> getActive() {
        return new ArrayList<PooledData<T>>(active.values());
    }
}
