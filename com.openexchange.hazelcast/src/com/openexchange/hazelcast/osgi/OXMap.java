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

package com.openexchange.hazelcast.osgi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcasts;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEntry;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Expression;
import com.hazelcast.query.Predicate;

/**
 * {@link OXMap}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXMap<K, V> implements IMap<K, V> {
	
	private IMap<K, V> delegate;
	private Class classLoaderSource;
	
	// TODO: Add Kryo Serzialization
	
	public OXMap(IMap<K, V> delegate) {
		this.delegate = delegate;
	}
	

	public InstanceType getInstanceType() {
		return delegate.getInstanceType();
	}

	public V get(Object key) {
		try {
			setClassLoader();
			return delegate.get(key);
		} finally {
			restoreClassLoader();
		}
	}

	public Object getId() {
		return delegate.getId();
	}

	public V put(K key, V value) {
		try {
			setClassLoader();
			return delegate.put(key, value);
		} finally {
			restoreClassLoader();
		}
	}

	public V remove(Object key) {
		try {
			setClassLoader();
			return delegate.remove(key);
		} finally {
			restoreClassLoader();
		}
	}

	public boolean remove(Object key, Object value) {
		try {
			setClassLoader();
			return delegate.remove(key, value);
		} finally {
			restoreClassLoader();
		}
	}

	public String getName() {
		return delegate.getName();
	}

	public Map<K, V> getAll(Set<K> keys) {
		try {
			setClassLoader();
			return delegate.getAll(keys);
		} finally {
			restoreClassLoader();
		}
	}

	public Future<V> getAsync(K key) {
		try {
			setClassLoader();
			return delegate.getAsync(key);
		} finally {
			restoreClassLoader();
		}
	}

	public Future<V> putAsync(K key, V value) {
		try {
			setClassLoader();
			return delegate.putAsync(key, value);
		} finally {
			restoreClassLoader();
		}
	}

	public Future<V> removeAsync(K key) {
		try {
			setClassLoader();
			return delegate.removeAsync(key);
		} finally {
			restoreClassLoader();
		}
	}

	public Object tryRemove(K key, long timeout, TimeUnit timeunit)
			throws TimeoutException {
		try {
			setClassLoader();
			return delegate.tryRemove(key, timeout, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public boolean tryPut(K key, V value, long timeout, TimeUnit timeunit) {
		try {
			setClassLoader();
			return delegate.tryPut(key, value, timeout, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public V put(K key, V value, long ttl, TimeUnit timeunit) {
		try {
			setClassLoader();
			return delegate.put(key, value, ttl, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public void putTransient(K key, V value, long ttl, TimeUnit timeunit) {
		try {
			setClassLoader();
			delegate.putTransient(key, value, ttl, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public V putIfAbsent(K key, V value) {
		try {
			setClassLoader();
			return delegate.putIfAbsent(key, value);
		} finally {
			restoreClassLoader();
		}
	}

	public V putIfAbsent(K key, V value, long ttl, TimeUnit timeunit) {
		try {
			setClassLoader();
			return delegate.putIfAbsent(key, value, ttl, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public boolean replace(K key, V oldValue, V newValue) {
		try {
			setClassLoader();
			return delegate.replace(key, oldValue, newValue);
		} finally {
			restoreClassLoader();
		}
	}

	public V replace(K key, V value) {
		try {
			setClassLoader();
			return delegate.replace(key, value);
		} finally {
			restoreClassLoader();
		}
	}

	public void set(K key, V value, long ttl, TimeUnit timeunit) {
		try {
			setClassLoader();
			delegate.set(key, value, ttl, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public V tryLockAndGet(K key, long time, TimeUnit timeunit)
			throws TimeoutException {
		try {
			setClassLoader();
			return delegate.tryLockAndGet(key, time, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public void lock(K key) {
		try {
			setClassLoader();
			delegate.lock(key);
		} finally {
			restoreClassLoader();
		}
	}

	public boolean tryLock(K key) {
		try {
			setClassLoader();
			return delegate.tryLock(key);
		} finally {
			restoreClassLoader();
		}
	}

	public void unlock(K key) {
		try {
			setClassLoader();
			delegate.unlock(key);
		} finally {
			restoreClassLoader();
		}
	}

	public void forceUnlock(K key) {
		try {
			setClassLoader();
			delegate.forceUnlock(key);
		} finally {
			restoreClassLoader();
		}
	}

	public boolean lockMap(long time, TimeUnit timeunit) {
		try {
			setClassLoader();
			return delegate.lockMap(time, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public void unlockMap() {
		try {
			setClassLoader();
			delegate.unlockMap();
		} finally {
			restoreClassLoader();
		}
	}

	public void addLocalEntryListener(EntryListener<K, V> listener) {
		delegate.addLocalEntryListener(listener);
	}

	public void addEntryListener(EntryListener<K, V> listener,
			boolean includeValue) {
		delegate.addEntryListener(listener, includeValue);
	}

	public void removeEntryListener(EntryListener<K, V> listener) {
		delegate.removeEntryListener(listener);
	}

	public void addEntryListener(EntryListener<K, V> listener, K key,
			boolean includeValue) {
		delegate.addEntryListener(listener, key, includeValue);
	}

	public void removeEntryListener(EntryListener<K, V> listener, K key) {
		delegate.removeEntryListener(listener, key);
	}

	public MapEntry<K, V> getMapEntry(K key) {
		try {
			setClassLoader();
			return delegate.getMapEntry(key);
		} finally {
			restoreClassLoader();
		}
	}

	public Set<K> localKeySet(Predicate predicate) {
		try {
			setClassLoader();
			return delegate.localKeySet(predicate);
		} finally {
			restoreClassLoader();
		}
	}

	public void addIndex(String attribute, boolean ordered) {
		delegate.addIndex(attribute, ordered);
	}

	public void addIndex(Expression<?> expression, boolean ordered) {
		delegate.addIndex(expression, ordered);
	}

	public void clear() {
		delegate.clear();
	}

	public boolean containsKey(Object key) {
		try {
			setClassLoader();
			return delegate.containsKey(key);
		} finally {
			restoreClassLoader();
		}
	}

	public boolean containsValue(Object value) {
		try {
			setClassLoader();
			return delegate.containsValue(value);
		} finally {
			restoreClassLoader();
		}
	}

	public void destroy() {
		delegate.destroy();
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		try {
			setClassLoader();
			return delegate.entrySet();
		} finally {
			restoreClassLoader();
		}
	}

	public Set<java.util.Map.Entry<K, V>> entrySet(Predicate predicate) {
		try {
			setClassLoader();
			return delegate.entrySet(predicate);
		} finally {
			restoreClassLoader();
		}
	}

	public boolean equals(Object arg0) {
		try {
			setClassLoader();
			return delegate.equals(arg0);
		} finally {
			restoreClassLoader();
		}
	}

	public void flush() {
		delegate.flush();
	}

	public boolean evict(Object key) {
		try {
			setClassLoader();
			return delegate.evict(key);
		} finally {
			restoreClassLoader();
		}
	}

	public LocalMapStats getLocalMapStats() {
		try {
			setClassLoader();
			return delegate.getLocalMapStats();
		} finally {
			restoreClassLoader();
		}
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public boolean isLocked(K key) {
		try {
			setClassLoader();
			return delegate.isLocked(key);
		} finally {
			restoreClassLoader();
		}
	}

	public Set<K> keySet() {
		try {
			setClassLoader();
			return delegate.keySet();
		} finally {
			restoreClassLoader();
		}
	}

	public Set<K> keySet(Predicate predicate) {
		try {
			setClassLoader();
			return delegate.keySet(predicate);
		} finally {
			restoreClassLoader();
		}
	}

	public Set<K> localKeySet() {
		try {
			setClassLoader();
			return delegate.localKeySet();
		} finally {
			restoreClassLoader();
		}
	}

	public void putAll(Map<? extends K, ? extends V> arg0) {
		try {
			setClassLoader();
			delegate.putAll(arg0);
		} finally {
			restoreClassLoader();
		}
	}

	public void putAndUnlock(K key, V value) {
		try {
			setClassLoader();
			delegate.putAndUnlock(key, value);
		} finally {
			restoreClassLoader();
		}
	}

	public int size() {
		return delegate.size();
	}

	public boolean tryLock(K key, long time, TimeUnit timeunit) {
		try {
			setClassLoader();
			return delegate.tryLock(key, time, timeunit);
		} finally {
			restoreClassLoader();
		}
	}

	public Collection<V> values() {
		try {
			setClassLoader();
			return delegate.values();
		} finally {
			restoreClassLoader();
		}
	}

	public Collection<V> values(Predicate predicate) {
		try {
			setClassLoader();
			return delegate.values(predicate);
		} finally {
			restoreClassLoader();
		}
	}


	private void setClassLoader() {
		if (classLoaderSource != null) {
			Hazelcasts.setClassLoader(classLoaderSource);
		}
	}
	
	private void restoreClassLoader() {
		Hazelcasts.restoreClassLoader();
	}


	public void setClassLoaderSource(Class<?> classLoaderSource) {
		this.classLoaderSource = classLoaderSource;
	}

	
	
}
