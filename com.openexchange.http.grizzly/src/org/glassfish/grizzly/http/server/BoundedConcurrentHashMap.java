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

package org.glassfish.grizzly.http.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * {@link BoundedConcurrentHashMap}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class BoundedConcurrentHashMap<K, V> implements Map<K, V> {

    private final Map<K, V> map;
    final AtomicInteger counter;
    final Lock lock;
    final int maxSize;

    /**
     * Initializes a new {@link BoundedConcurrentHashMap}.
     */
    public BoundedConcurrentHashMap(int initialCapacity, int maxSize) {
        super();
        counter = new AtomicInteger(0);
        lock = new ReentrantLock(false);
        map = new ConcurrentHashMap<K, V>(initialCapacity, 0.9f, 1);
        this.maxSize = maxSize;
    }

    @Override
    public V put(K key, V value) {
        lock.lock();
        try {
            V prev = map.put(key, value);
            if (null == prev && counter.get() >= maxSize) {
                map.remove(key);
                throw new IllegalStateException(new StringBuilder("Max. number of HTTP session (").append(maxSize).append(") exceeded.").toString());
            }
            counter.incrementAndGet();
            return prev;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        lock.lock();
        try {
            int curcount = counter.get();
            for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
                V prev = map.put(entry.getKey(), entry.getValue());
                if (null == prev && curcount >= maxSize) {
                    removeAll(m.keySet());
                    throw new IllegalStateException(new StringBuilder("Max. number of HTTP session (").append(maxSize).append(") exceeded.").toString());
                }
            }
            counter.addAndGet(m.size());
        } finally {
            lock.unlock();
        }
    }

    private void removeAll(Collection<? extends K> keys) {
        for (K k : keys) {
            map.remove(k);
        }
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
                    throw new IllegalStateException(new StringBuilder("Max. number of HTTP session (").append(maxSize).append(") exceeded.").toString());
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
                    throw new IllegalStateException(new StringBuilder("Max. number of HTTP session (").append(maxSize).append(") exceeded.").toString());
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
