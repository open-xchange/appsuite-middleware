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
