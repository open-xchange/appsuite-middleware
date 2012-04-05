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

package com.openexchange.index.solr.internal.management;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

/**
 * {@link LockBasedConcurrentMap}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class LockBasedConcurrentMap<K, V> implements ConcurrentMap<K, V> {

    private final ConcurrentMap<K, V> delegate;

    private final Lock writeLock;

    private final Lock readLock;

    LockBasedConcurrentMap(final Lock readLock, final Lock writeLock, final ConcurrentMap<K, V> delegate) {
        this.writeLock = writeLock;
        this.readLock = readLock;
        this.delegate = delegate;
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return delegate.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return delegate.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            delegate.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsKey(final Object key) {
        readLock.lock();
        try {
            return delegate.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        readLock.lock();
        try {
            return delegate.containsValue(value);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V get(final Object key) {
        readLock.lock();
        try {
            return delegate.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V put(final K key, final V value) {
        writeLock.lock();
        try {
            return delegate.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        writeLock.lock();
        try {
            return delegate.putIfAbsent(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        writeLock.lock();
        try {
            delegate.putAll(map);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(final Object key) {
        writeLock.lock();
        try {
            return delegate.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        writeLock.lock();
        try {
            return delegate.remove(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        writeLock.lock();
        try {
            return delegate.replace(key, oldValue, newValue);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V replace(final K key, final V value) {
        writeLock.lock();
        try {
            return delegate.replace(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        readLock.lock();
        try {
            return new LinkedHashSet<K>(delegate.keySet());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        readLock.lock();
        try {
            return new LinkedHashSet<V>(delegate.values());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        readLock.lock();
        try {
            return new LinkedHashSet<Entry<K, V>>(delegate.entrySet());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean equals(final Object object) {
        readLock.lock();
        try {
            return delegate.equals(object);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int hashCode() {
        readLock.lock();
        try {
            return delegate.hashCode();
        } finally {
            readLock.unlock();
        }
    }
}
