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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.hazelcast;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEntry;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Expression;
import com.hazelcast.query.Predicate;

/**
 * {@link ClassLoaderAwareIMap}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ClassLoaderAwareIMap<K extends Serializable, V extends Serializable> extends AbstractClassLoaderAware implements IMap<K, V> {

    private final class EntryImpl implements Map.Entry<K, V> {

        private final Entry<Serializable, Serializable> entry;

        protected EntryImpl(Entry<Serializable, Serializable> entry) {
            this.entry = entry;
        }

        @SuppressWarnings("unchecked")
        @Override
        public K getKey() {
            return (K) entry.getKey();
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValue() {
            return (V) entry.getValue();
        }

        @SuppressWarnings("unchecked")
        @Override
        public V setValue(V value) {
            return (V) entry.setValue(value);
        }
    }

    private final IMap<Serializable, Serializable> delegate;

    /**
     * Initializes a new {@link ClassLoaderAwareIMap}.
     * 
     * @param delegate The {@link IMap} to delegate to
     */
    public ClassLoaderAwareIMap(final IMap<Serializable, Serializable> delegate, final boolean kryorize) {
        super(kryorize);
        this.delegate = delegate;
    }

    @Override
    public InstanceType getInstanceType() {
        return delegate.getInstanceType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(final Object key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (V) delegate.get(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public Object getId() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.getId();
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(final K key, final V value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (V) delegate.put(wrapper(key), wrapper(value));
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(final Object key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (V) delegate.remove(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.remove(wrapper(key), wrapper(value));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public String getName() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.getName();
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> getAll(final Set<K> keys) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            final Set<Serializable> keySet = new HashSet<Serializable>(keys.size());
            for (final K key : keys) {
                keySet.add(wrapper(key));
            }
            return (Map<K, V>) delegate.getAll(keySet);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<V> getAsync(final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Future<V>) delegate.getAsync(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<V> putAsync(final K key, final V value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Future<V>) delegate.putAsync(wrapper(key), wrapper(value));
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<V> removeAsync(final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Future<V>) delegate.removeAsync(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public Object tryRemove(final K key, final long timeout, final TimeUnit timeunit) throws TimeoutException {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.tryRemove(wrapper(key), timeout, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean tryPut(final K key, final V value, final long timeout, final TimeUnit timeunit) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.tryPut(wrapper(key), wrapper(value), timeout, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(final K key, final V value, final long ttl, final TimeUnit timeunit) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (V) delegate.put(wrapper(key), wrapper(value), ttl, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void putTransient(final K key, final V value, final long ttl, final TimeUnit timeunit) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.putTransient(wrapper(key), wrapper(value), ttl, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V putIfAbsent(final K key, final V value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (V) delegate.putIfAbsent(wrapper(key), wrapper(value));
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V putIfAbsent(final K key, final V value, final long ttl, final TimeUnit timeunit) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (V) delegate.putIfAbsent(wrapper(key), wrapper(value), ttl, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.replace(
                wrapper(key),
                wrapper(oldValue),
                wrapper(newValue));
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V replace(final K key, final V value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (V) delegate.replace(wrapper(key), wrapper(value));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void set(final K key, final V value, final long ttl, final TimeUnit timeunit) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.set(wrapper(key), wrapper(value), ttl, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V tryLockAndGet(final K key, final long time, final TimeUnit timeunit) throws TimeoutException {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (V) delegate.tryLockAndGet(wrapper(key), time, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void lock(final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.lock(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean tryLock(final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.tryLock(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void unlock(final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.unlock(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void forceUnlock(final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.forceUnlock(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean lockMap(final long time, final TimeUnit timeunit) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.lockMap(time, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void unlockMap() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.unlockMap();
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addLocalEntryListener(final EntryListener<K, V> listener) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.addLocalEntryListener((EntryListener<Serializable, Serializable>) listener);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addEntryListener(final EntryListener<K, V> listener, final boolean includeValue) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.addEntryListener((EntryListener<Serializable, Serializable>) listener, includeValue);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeEntryListener(final EntryListener<K, V> listener) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.removeEntryListener((EntryListener<Serializable, Serializable>) listener);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addEntryListener(final EntryListener<K, V> listener, final K key, final boolean includeValue) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.addEntryListener((EntryListener<Serializable, Serializable>) listener, key, includeValue);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeEntryListener(final EntryListener<K, V> listener, final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.removeEntryListener((EntryListener<Serializable, Serializable>) listener, key);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapEntry<K, V> getMapEntry(final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (MapEntry<K, V>) delegate.getMapEntry(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> localKeySet(@SuppressWarnings("rawtypes") final Predicate predicate) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Set<K>) delegate.localKeySet(predicate); // "automatically" de-serialized
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void addIndex(final String attribute, final boolean ordered) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.addIndex(attribute, ordered);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void addIndex(final Expression<?> expression, final boolean ordered) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.addIndex(expression, ordered);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void clear() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.clear();
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean containsKey(final Object key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.containsKey(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.containsValue(wrapper(value));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void destroy() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.destroy();
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            /*
             * Create a clone as per general contract of IMap: The set is NOT backed by the map, so changes to the map are NOT reflected in
             * the set, and vice-versa.
             */
            final Set<java.util.Map.Entry<Serializable, Serializable>> entrySet = delegate.entrySet();
            final Set<Map.Entry<K, V>> clone = new LinkedHashSet<Map.Entry<K, V>>(entrySet.size());
            for (final Entry<Serializable, Serializable> entry : entrySet) {
                clone.add(new EntryImpl(entry));
            }
            return clone;
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet(@SuppressWarnings("rawtypes") final Predicate predicate) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            /*
             * Create a clone as per general contract of IMap: The set is NOT backed by the map, so changes to the map are NOT reflected in
             * the set, and vice-versa.
             */
            final Set<java.util.Map.Entry<Serializable, Serializable>> entrySet = delegate.entrySet(predicate);
            final Set<Map.Entry<K, V>> clone = new LinkedHashSet<Map.Entry<K, V>>(entrySet.size());
            for (Entry<Serializable, Serializable> entry : entrySet) {
                clone.add(new EntryImpl(entry));
            }
            return clone;
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean equals(final Object other) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.equals(other);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void flush() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.flush();
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean evict(final Object key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.evict(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public LocalMapStats getLocalMapStats() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.getLocalMapStats();
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public int hashCode() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.hashCode();
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean isEmpty() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.isEmpty();
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean isLocked(final K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.isLocked(wrapper(key));
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Set<K>) delegate.keySet(); // "automatically" de-serialized
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet(@SuppressWarnings("rawtypes") final Predicate predicate) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Set<K>) delegate.keySet(predicate); // "automatically" de-serialized
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> localKeySet() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Set<K>) delegate.localKeySet(); // "automatically" de-serialized
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                delegate.put(wrapper(entry.getKey()), wrapper(entry.getValue()));
            }
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void putAndUnlock(final K key, final V value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.putAndUnlock(wrapper(key), wrapper(value));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean tryLock(final K key, final long time, final TimeUnit timeunit) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.tryLock(wrapper(key), time, timeunit);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Collection<V>) delegate.values(); // "automatically" de-serialized
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values(@SuppressWarnings("rawtypes") final Predicate predicate) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Collection<V>) delegate.values(predicate); // "automatically" de-serialized
        } finally {
            unsetClassLoader();
        }
    }

}
