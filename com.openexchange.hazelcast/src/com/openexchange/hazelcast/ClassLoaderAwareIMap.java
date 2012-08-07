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
import java.util.concurrent.atomic.AtomicReference;
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
public class ClassLoaderAwareIMap<K extends Serializable, V extends Serializable> implements IMap<K, V>, ClassLoaderAware {

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

    private final AtomicReference<Class<?>> classLoaderSourceRef;

    /**
     * Initializes a new {@link ClassLoaderAwareIMap}.
     * 
     * @param delegate The {@link IMap} to delegate to
     */
    public ClassLoaderAwareIMap(final IMap<Serializable, Serializable> delegate) {
        super();
        classLoaderSourceRef = new AtomicReference<Class<?>>(null);
        this.delegate = delegate;
    }

    @Override
    public void setClassLoaderSource(final Class<?> classLoaderSource) {
        classLoaderSourceRef.set(classLoaderSource);
    }

    @Override
    public InstanceType getInstanceType() {
        return delegate.getInstanceType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(final Object key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (V) delegate.get(key);
        }
        return (V) delegate.get(wrapper(key, classLoaderSource));
    }

    @Override
    public Object getId() {
        return delegate.getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(final K key, final V value) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (V) delegate.put(key, value);
        }
        return (V) delegate.put(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource));
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(final Object key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (V) delegate.remove(key);
        }
        return (V) delegate.remove(wrapper(key, classLoaderSource));
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.remove(key, value);
        }
        return delegate.remove(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource));
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> getAll(final Set<K> keys) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (Map<K, V>) delegate.getAll((Set<Serializable>) keys);
        }
        final Set<Serializable> keySet = new HashSet<Serializable>(keys.size());
        for (final K key : keys) {
            keySet.add(wrapper(key, classLoaderSource));
        }
        return (Map<K, V>) delegate.getAll(keySet);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<V> getAsync(final K key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (Future<V>) delegate.getAsync(key);
        }
        return (Future<V>) delegate.getAsync(wrapper(key, classLoaderSource));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<V> putAsync(final K key, final V value) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (Future<V>) delegate.putAsync(key, value);
        }
        return (Future<V>) delegate.putAsync(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<V> removeAsync(final K key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (Future<V>) delegate.removeAsync(key);
        }
        return (Future<V>) delegate.removeAsync(wrapper(key, classLoaderSource));
    }

    @Override
    public Object tryRemove(final K key, final long timeout, final TimeUnit timeunit) throws TimeoutException {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.tryRemove(key, timeout, timeunit);
        }
        return delegate.tryRemove(wrapper(key, classLoaderSource), timeout, timeunit);
    }

    @Override
    public boolean tryPut(final K key, final V value, final long timeout, final TimeUnit timeunit) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.tryPut(key, value, timeout, timeunit);
        }
        return delegate.tryPut(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource), timeout, timeunit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(final K key, final V value, final long ttl, final TimeUnit timeunit) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (V) delegate.put(key, value, ttl, timeunit);
        }
        return (V) delegate.put(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource), ttl, timeunit);
    }

    @Override
    public void putTransient(final K key, final V value, final long ttl, final TimeUnit timeunit) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            delegate.putTransient(key, value, ttl, timeunit);
        } else {
            delegate.putTransient(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource), ttl, timeunit);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V putIfAbsent(final K key, final V value) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (V) delegate.putIfAbsent(key, value);

        }
        return (V) delegate.putIfAbsent(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource));
    }

    @SuppressWarnings("unchecked")
    @Override
    public V putIfAbsent(final K key, final V value, final long ttl, final TimeUnit timeunit) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (V) delegate.putIfAbsent(key, value, ttl, timeunit);
        }
        return (V) delegate.putIfAbsent(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource), ttl, timeunit);
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.replace(key, oldValue, newValue);
        }
        return delegate.replace(wrapper(key, classLoaderSource), wrapper(oldValue, classLoaderSource), wrapper(newValue, classLoaderSource));
    }

    @SuppressWarnings("unchecked")
    @Override
    public V replace(final K key, final V value) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (V) delegate.replace(key, value);
        }
        return (V) delegate.replace(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource));
    }

    @Override
    public void set(final K key, final V value, final long ttl, final TimeUnit timeunit) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            delegate.set(key, value, ttl, timeunit);
        } else {
            delegate.set(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource), ttl, timeunit);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V tryLockAndGet(final K key, final long time, final TimeUnit timeunit) throws TimeoutException {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (V) delegate.tryLockAndGet(key, time, timeunit);
        }
        return (V) delegate.tryLockAndGet(wrapper(key, classLoaderSource), time, timeunit);
    }

    @Override
    public void lock(final K key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            delegate.lock(key);
        } else {
            delegate.lock(wrapper(key, classLoaderSource));
        }
    }

    @Override
    public boolean tryLock(final K key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.tryLock(key);
        }
        return delegate.tryLock(wrapper(key, classLoaderSource));
    }

    @Override
    public void unlock(final K key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            delegate.unlock(key);
        } else {
            delegate.unlock(wrapper(key, classLoaderSource));
        }
    }

    @Override
    public void forceUnlock(final K key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            delegate.forceUnlock(key);
        } else {
            delegate.forceUnlock(wrapper(key, classLoaderSource));
        }
    }

    @Override
    public boolean lockMap(final long time, final TimeUnit timeunit) {
        return delegate.lockMap(time, timeunit);
    }

    @Override
    public void unlockMap() {
        delegate.unlockMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addLocalEntryListener(final EntryListener<K, V> listener) {
        delegate.addLocalEntryListener((EntryListener<Serializable, Serializable>) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addEntryListener(final EntryListener<K, V> listener, final boolean includeValue) {
        delegate.addEntryListener((EntryListener<Serializable, Serializable>) listener, includeValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeEntryListener(final EntryListener<K, V> listener) {
        delegate.removeEntryListener((EntryListener<Serializable, Serializable>) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addEntryListener(final EntryListener<K, V> listener, final K key, final boolean includeValue) {
        delegate.addEntryListener((EntryListener<Serializable, Serializable>) listener, key, includeValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeEntryListener(final EntryListener<K, V> listener, final K key) {
        delegate.removeEntryListener((EntryListener<Serializable, Serializable>) listener, key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapEntry<K, V> getMapEntry(final K key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return (MapEntry<K, V>) delegate.getMapEntry(key);
        }
        return (MapEntry<K, V>) delegate.getMapEntry(wrapper(key, classLoaderSource));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> localKeySet(@SuppressWarnings("rawtypes") final Predicate predicate) {
        return (Set<K>) delegate.localKeySet(predicate); // "automatically" de-serialized
    }

    @Override
    public void addIndex(final String attribute, final boolean ordered) {
        delegate.addIndex(attribute, ordered);
    }

    @Override
    public void addIndex(final Expression<?> expression, final boolean ordered) {
        delegate.addIndex(expression, ordered);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean containsKey(final Object key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.containsKey(key);
        }
        return delegate.containsKey(wrapper(key, classLoaderSource));
    }

    @Override
    public boolean containsValue(final Object value) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.containsValue(value);
        }
        return delegate.containsValue(wrapper(value, classLoaderSource));
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        /*
         * Create a clone as per general contract of IMap: The set is NOT backed by the map, so changes to the map are NOT reflected in the
         * set, and vice-versa.
         */
        final Set<java.util.Map.Entry<Serializable, Serializable>> entrySet = delegate.entrySet();
        final Set<Map.Entry<K, V>> clone = new LinkedHashSet<Map.Entry<K, V>>(entrySet.size());
        for (final Entry<Serializable, Serializable> entry : entrySet) {
            clone.add(new EntryImpl(entry));
        }
        return clone;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet(@SuppressWarnings("rawtypes") final Predicate predicate) {
        /*
         * Create a clone as per general contract of IMap: The set is NOT backed by the map, so changes to the map are NOT reflected in the
         * set, and vice-versa.
         */
        final Set<java.util.Map.Entry<Serializable, Serializable>> entrySet = delegate.entrySet(predicate);
        final Set<Map.Entry<K, V>> clone = new LinkedHashSet<Map.Entry<K, V>>(entrySet.size());
        for (Entry<Serializable, Serializable> entry : entrySet) {
            clone.add(new EntryImpl(entry));
        }
        return clone;
    }

    @Override
    public boolean equals(final Object other) {
        return delegate.equals(other);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public boolean evict(final Object key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.evict(key);
        }
        return delegate.evict(wrapper(key, classLoaderSource));
    }

    @Override
    public LocalMapStats getLocalMapStats() {
        return delegate.getLocalMapStats();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean isLocked(final K key) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.isLocked(key);
        }
        return delegate.isLocked(wrapper(key, classLoaderSource));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        return (Set<K>) delegate.keySet(); // "automatically" de-serialized
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet(@SuppressWarnings("rawtypes") final Predicate predicate) {
        return (Set<K>) delegate.keySet(predicate); // "automatically" de-serialized
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> localKeySet() {
        return (Set<K>) delegate.localKeySet(); // "automatically" de-serialized
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            delegate.putAll(map);
        } else {
            for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                delegate.put(wrapper(entry.getKey(), classLoaderSource), wrapper(entry.getValue(), classLoaderSource));
            }
        }
    }

    @Override
    public void putAndUnlock(final K key, final V value) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            delegate.putAndUnlock(key, value);
        } else {
            delegate.putAndUnlock(wrapper(key, classLoaderSource), wrapper(value, classLoaderSource));
        }
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean tryLock(final K key, final long time, final TimeUnit timeunit) {
        final Class<?> classLoaderSource = classLoaderSourceRef.get();
        if (null == classLoaderSource) {
            return delegate.tryLock(key, time, timeunit);
        }
        return delegate.tryLock(wrapper(key, classLoaderSource), time, timeunit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        return (Collection<V>) delegate.values(); // "automatically" de-serialized
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values(@SuppressWarnings("rawtypes") final Predicate predicate) {
        return (Collection<V>) delegate.values(predicate); // "automatically" de-serialized
    }

    private static KryoWrapper wrapper(final Object obj, final Class<?> classLoaderSource) {
        return new KryoWrapper(obj, classLoaderSource.getClassLoader());
    }

}
