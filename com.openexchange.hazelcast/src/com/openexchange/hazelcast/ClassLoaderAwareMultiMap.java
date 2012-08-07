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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MultiMap;

/**
 * {@link ClassLoaderAwareMultiMap}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ClassLoaderAwareMultiMap<K extends Serializable, V extends Serializable> implements MultiMap<K, V>, ClassLoaderAware {

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

    private static KryoWrapper wrapper(final Object obj, final Class<?> classLoaderSource) {
        return new KryoWrapper(obj, classLoaderSource.getClassLoader());
    }

    private final MultiMap<Serializable, Serializable> delegate;

    private final AtomicReference<Class<?>> classLoaderSourceRef;

    /**
     * Initializes a new {@link ClassLoaderAwareMultiMap}.
     */
    public ClassLoaderAwareMultiMap(final MultiMap<Serializable, Serializable> delegate) {
        super();
        classLoaderSourceRef = new AtomicReference<Class<?>>(null);
        this.delegate = delegate;
    }

    @Override
    public void setClassLoaderSource(final Class<?> classLoaderSource) {
        classLoaderSourceRef.set(classLoaderSource);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean put(K key, V value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        if (null == clazz) {
            return delegate.put(key, value);
        }
        return delegate.put(wrapper(key, clazz), wrapper(value, clazz));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> get(K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        if (null == clazz) {
            return (Collection<V>) delegate.get(key);
        }
        return (Collection<V>) delegate.get(wrapper(key, clazz));
    }

    @Override
    public InstanceType getInstanceType() {
        return delegate.getInstanceType();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public boolean remove(Object key, Object value) {
        final Class<?> clazz = classLoaderSourceRef.get();
        if (null == clazz) {
            return delegate.remove(key, value);
        }
        return delegate.remove(wrapper(key, clazz), wrapper(value, clazz));
    }

    @Override
    public Object getId() {
        return delegate.getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> remove(Object key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        if (null == clazz) {
            return (Collection<V>) delegate.remove(key);
        }
        return (Collection<V>) delegate.remove(wrapper(key, clazz));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> localKeySet() {
        return (Set<K>) delegate.localKeySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        return (Set<K>) delegate.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        return (Collection<V>) delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
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
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public boolean containsEntry(K key, V value) {
        return delegate.containsEntry(key, value);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int valueCount(Serializable key) {
        return delegate.valueCount(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addLocalEntryListener(EntryListener<K, V> listener) {
        delegate.addLocalEntryListener((EntryListener<Serializable, Serializable>) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addEntryListener(EntryListener<K, V> listener, boolean includeValue) {
        delegate.addEntryListener((EntryListener<Serializable, Serializable>) listener, includeValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeEntryListener(EntryListener<K, V> listener) {
        delegate.removeEntryListener((EntryListener<Serializable, Serializable>) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addEntryListener(EntryListener<K, V> listener, K key, boolean includeValue) {
        delegate.addEntryListener((EntryListener<Serializable, Serializable>) listener, key, includeValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeEntryListener(EntryListener<K, V> listener, K key) {
        final Class<?> clazz = classLoaderSourceRef.get();
        if (null == clazz) {
            delegate.removeEntryListener((EntryListener<Serializable, Serializable>) listener, key);
        }
        delegate.removeEntryListener((EntryListener<Serializable, Serializable>) listener, wrapper(key, clazz));
    }

    @Override
    public void lock(Serializable key) {
        delegate.lock(key);
    }

    @Override
    public boolean tryLock(Serializable key) {
        return delegate.tryLock(key);
    }

    @Override
    public boolean tryLock(Serializable key, long time, TimeUnit timeunit) {
        return delegate.tryLock(key, time, timeunit);
    }

    @Override
    public void unlock(Serializable key) {
        delegate.unlock(key);
    }

    @Override
    public boolean lockMap(long time, TimeUnit timeunit) {
        return delegate.lockMap(time, timeunit);
    }

    @Override
    public void unlockMap() {
        delegate.unlockMap();
    }
    

}
