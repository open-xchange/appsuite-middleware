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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package gnu.trove;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link ConcurrentTIntObjectHashMap} - A concurrent {@link TIntObjectHashMap} implementation using a {@link ReadWriteLock} instance.
 * <p>
 * An open addressed Map implementation for int keys and Object values.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConcurrentTIntObjectHashMap<V> extends TIntObjectHashMap<V> {

    private final TIntObjectHashMap<V> map;

    private ReadWriteLock readWriteLock;

    /**
     * Initializes a new {@link ConcurrentTIntObjectHashMap}.
     */
    public ConcurrentTIntObjectHashMap() {
        super();
        map = new TIntObjectHashMap<V>();
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Initializes a new {@link ConcurrentTIntObjectHashMap}.
     * 
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor used to calculate the threshold over which rehashing takes place.
     * @param strategy used to compute hash codes and to compare keys.
     */
    public ConcurrentTIntObjectHashMap(final int initialCapacity, final float loadFactor, final TIntHashingStrategy strategy) {
        super();
        map = new TIntObjectHashMap<V>(initialCapacity, loadFactor, strategy);
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Initializes a new {@link ConcurrentTIntObjectHashMap}.
     * 
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor used to calculate the threshold over which rehashing takes place.
     */
    public ConcurrentTIntObjectHashMap(final int initialCapacity, final float loadFactor) {
        super();
        map = new TIntObjectHashMap<V>(initialCapacity, loadFactor);
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Initializes a new {@link ConcurrentTIntObjectHashMap}.
     * 
     * @param initialCapacity used to find a prime capacity for the table.
     * @param strategy used to compute hash codes and to compare keys.
     */
    public ConcurrentTIntObjectHashMap(final int initialCapacity, final TIntHashingStrategy strategy) {
        super();
        map = new TIntObjectHashMap<V>(initialCapacity, strategy);
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Initializes a new {@link ConcurrentTIntObjectHashMap}.
     * 
     * @param initialCapacity used to find a prime capacity for the table.
     */
    public ConcurrentTIntObjectHashMap(final int initialCapacity) {
        super();
        map = new TIntObjectHashMap<V>(initialCapacity);
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Initializes a new {@link ConcurrentTIntObjectHashMap}.
     * 
     * @param strategy used to compute hash codes and to compare keys.
     */
    public ConcurrentTIntObjectHashMap(final TIntHashingStrategy strategy) {
        super();
        map = new TIntObjectHashMap<V>(strategy);
        readWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public ConcurrentTIntObjectHashMap<V> clone() {
        final ConcurrentTIntObjectHashMap<V> clone = (ConcurrentTIntObjectHashMap<V>) super.clone();
        clone.readWriteLock = new ReentrantReadWriteLock();
        return clone();
    }

    @Override
    public TIntObjectIterator<V> iterator() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.iterator();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.isEmpty();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean contains(final int val) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.contains(val);
        } finally {
            l.unlock();
        }
    }

    @Override
    public int size() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.size();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean forEach(final TIntProcedure procedure) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.forEach(procedure);
        } finally {
            l.unlock();
        }
    }

    @Override
    public V put(final int key, final V value) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return map.put(key, value);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void ensureCapacity(final int desiredCapacity) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            map.ensureCapacity(desiredCapacity);
        } finally {
            l.unlock();
        }
    }

    @Override
    public V putIfAbsent(final int key, final V value) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return map.putIfAbsent(key, value);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void compact() {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            map.compact();
        } finally {
            l.unlock();
        }
    }

    @Override
    public void putAll(final TIntObjectHashMap<V> map) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            map.putAll(map);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void setAutoCompactionFactor(final float factor) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            map.setAutoCompactionFactor(factor);
        } finally {
            l.unlock();
        }
    }

    @Override
    public V get(final int key) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.get(key);
        } finally {
            l.unlock();
        }
    }

    @Override
    public float getAutoCompactionFactor() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.getAutoCompactionFactor();
        } finally {
            l.unlock();
        }
    }

    @Override
    public void clear() {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            map.clear();
        } finally {
            l.unlock();
        }
    }

    @Override
    public V remove(final int key) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return map.remove(key);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean equals(final Object other) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.equals(other);
        } finally {
            l.unlock();
        }
    }

    @Override
    public int hashCode() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.hashCode();
        } finally {
            l.unlock();
        }
    }

    @Override
    public Object[] getValues() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.getValues();
        } finally {
            l.unlock();
        }
    }

    @Override
    public <T> T[] getValues(final T[] a) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.getValues(a);
        } finally {
            l.unlock();
        }
    }

    @Override
    public int[] keys() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.keys();
        } finally {
            l.unlock();
        }
    }

    @Override
    public int[] keys(final int[] a) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.keys(a);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean containsValue(final V val) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.containsValue(val);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean containsKey(final int key) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.containsKey(key);
        } finally {
            l.unlock();
        }

    }

    @Override
    public boolean forEachKey(final TIntProcedure procedure) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.forEachKey(procedure);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean forEachValue(final TObjectProcedure<V> procedure) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.forEachValue(procedure);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean forEachEntry(final TIntObjectProcedure<V> procedure) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.forEachEntry(procedure);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean retainEntries(final TIntObjectProcedure<V> procedure) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return map.retainEntries(procedure);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void transformValues(final TObjectFunction<V, V> function) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            map.transformValues(function);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            map.writeExternal(out);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            map.readExternal(in);
        } finally {
            l.unlock();
        }
    }

    @Override
    public String toString() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return map.toString();
        } finally {
            l.unlock();
        }
    }

}
