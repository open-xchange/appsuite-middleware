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

package com.openexchange.websockets.grizzly;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * {@link BoundedConcurrentHashMap}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class BoundedConcurrentHashMap<K, V> implements ConcurrentMap<K, V> {

    /** Thrown when boundary is exceeded */
    public static final class BoundaryExceededException extends RuntimeException {

        private static final long serialVersionUID = -4673465413956939708L;

        private final int maxSize;

        /**
         * Initializes a new {@link BoundaryExceededException}.
         */
        public BoundaryExceededException(int maxSize) {
            super("Max. size of " + maxSize + " is exceeded.");
            this.maxSize = maxSize;
        }


        /**
         * Gets the max. size that was exceeded.
         *
         * @return The max. size
         */
        public int getMaxSize() {
            return maxSize;
        }
    }

    // ----------------------------------------------------------------------------------------

    private final ConcurrentMap<K, V> map;
    final AtomicInteger counter;
    final Lock lock;
    final int maxSize;

    /**
     * Initializes a new {@link BoundedConcurrentHashMap}.
     */
    public BoundedConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, int maxSize) {
        super();
        counter = new AtomicInteger(0);
        lock = new ReentrantLock(false);
        map = new ConcurrentHashMap<K, V>(initialCapacity, loadFactor, concurrencyLevel);
        this.maxSize = maxSize;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        lock.lock();
        try {
            V existing = map.putIfAbsent(key, value);
            if (null == existing) {
                if (counter.get() >= maxSize) {
                    map.remove(key);
                    throw new BoundaryExceededException(maxSize);
                }
                counter.incrementAndGet();
            }
            return existing;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        lock.lock();
        try {
            V prev = map.put(key, value);
            if (null == prev) {
                if (counter.get() >= maxSize) {
                    map.remove(key);
                    throw new BoundaryExceededException(maxSize);
                }
                counter.incrementAndGet();
            }
            return prev;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        lock.lock();
        try {
            int prevcount = counter.get();
            int numAdded = 0;
            Set<K> added = new HashSet<>();
            Map<K, V> replaced = new HashMap<>();
            for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
                V prev = map.put(entry.getKey(), entry.getValue());
                if (null == prev) {
                    if ((prevcount + ++numAdded) >= maxSize) {
                        restoreAll(added, replaced);
                        throw new BoundaryExceededException(maxSize);
                    }
                    added.add(entry.getKey());
                } else {
                    replaced.put(entry.getKey(), prev);
                }
            }
            counter.addAndGet(numAdded);
        } finally {
            lock.unlock();
        }
    }

    private void restoreAll(Set<K> added, Map<K, V> replaced) {
        for (K k : added) {
            map.remove(k);
        }
        map.putAll(replaced);
    }

    @Override
    public int size() {
        return counter.get();
    }

    @Override
    public boolean isEmpty() {
        return counter.get() <= 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V remove(Object key) {
        lock.lock();
        try {
            V removed = map.remove(key);
            if (null != removed) {
                counter.decrementAndGet();
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        lock.lock();
        try {
            boolean removed = map.remove(key, value);
            if (removed) {
                counter.decrementAndGet();
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            map.clear();
            counter.set(0);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return map.replace(key, value);
    }

    @Override
    public Set<K> keySet() {
        return new BoundedSet<K>(map.keySet());
    }

    @Override
    public Collection<V> values() {
        return new BoundedCollection<V>(map.values());
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return new BoundedSet<java.util.Map.Entry<K, V>>(map.entrySet());
    }

    @Override
    public String toString() {
        return map.toString();
    }

    // --------------------------------------------------------------------------------------------------------------------------

    private class BoundedIterator<E> implements Iterator<E> {

        private final Iterator<E> iter;

        BoundedIterator(Iterator<E> iter) {
            super();
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public E next() {
            return iter.next();
        }

        @Override
        public void remove() {
            lock.lock();
            try {
                iter.remove();
                counter.decrementAndGet();
            } finally {
                lock.unlock();
            }
        }
    }

    private class BoundedSet<E> implements Set<E> {

        private final Set<E> set;

        BoundedSet(Set<E> set) {
            super();
            this.set = set;
        }

        @Override
        public int size() {
            return counter.get();
        }

        @Override
        public boolean isEmpty() {
            return counter.get() <= 0;
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return new BoundedIterator<E>(set.iterator());
        }

        @Override
        public Object[] toArray() {
            return set.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }

        @Override
        public boolean add(E e) {
            lock.lock();
            try {
                boolean added = set.add(e);
                if (added && counter.get() >= maxSize) {
                    set.remove(e);
                    throw new BoundaryExceededException(maxSize);
                }
                return added;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean remove(Object o) {
            lock.lock();
            try {
                boolean removed = set.remove(o);
                if (removed) {
                    counter.decrementAndGet();
                }
                return removed;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            lock.lock();
            try {
                set.clear();
                counter.set(0);
            } finally {
                lock.unlock();
            }
        }
    }

    private class BoundedCollection<E> implements Collection<E> {

        private final Collection<E> col;

        BoundedCollection(Collection<E> col) {
            super();
            this.col = col;
        }

        @Override
        public int size() {
            return counter.get();
        }

        @Override
        public boolean isEmpty() {
            return counter.get() <= 0;
        }

        @Override
        public boolean contains(Object o) {
            return col.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return new BoundedIterator<E>(col.iterator());
        }

        @Override
        public Object[] toArray() {
            return col.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return col.toArray(a);
        }

        @Override
        public boolean add(E e) {
            lock.lock();
            try {
                boolean added = col.add(e);
                if (added && counter.get() >= maxSize) {
                    col.remove(e);
                    throw new BoundaryExceededException(maxSize);
                }
                return added;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean remove(Object o) {
            lock.lock();
            try {
                boolean removed = col.remove(o);
                if (removed) {
                    counter.decrementAndGet();
                }
                return removed;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return col.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            lock.lock();
            try {
                col.clear();
                counter.set(0);
            } finally {
                lock.unlock();
            }
        }
    }

}
