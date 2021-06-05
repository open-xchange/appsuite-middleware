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

package com.openexchange.java;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ConcurrentEnumMap} - A concurrent {@link EnumMap} backed by a {@link AtomicReference} holding a delegate map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @param <E> The {@link Enum}
 * @param <V> The Object to map to
 * @since 7.4.0
 */
public class ConcurrentEnumMap<E extends Enum<E>, V> implements Map<E, V> {

    private final AtomicReference<EnumMap<E, V>> ref;

    /**
     * Initializes a new {@link ConcurrentEnumMap}.
     * 
     * @param elementType The {@link Class} of the elements 
     */
    public ConcurrentEnumMap(final Class<E> elementType) {
        super();
        ref = new AtomicReference<EnumMap<E, V>>(new EnumMap<E, V>(elementType));
    }

    @Override
    public boolean isEmpty() {
        return ref.get().isEmpty();
    }

    @Override
    public int size() {
        return ref.get().size();
    }

    @Override
    public boolean containsValue(final Object value) {
        return ref.get().containsValue(value);
    }

    @Override
    public boolean containsKey(final Object key) {
        return ref.get().containsKey(key);
    }

    @Override
    public V get(final Object key) {
        return ref.get().get(key);
    }

    @Override
    public V put(final E key, final V value) {
        V added;
        EnumMap<E, V> expected;
        EnumMap<E, V> map;
        do {
            expected = ref.get();
            map = new EnumMap<E, V>(expected);
            added = map.put(key, value);
        } while (!ref.compareAndSet(expected, map));

        return added;
    }

    /**
     * If the specified key is not already associated with a value, associate it with the given value. This is equivalent to
     *
     * <pre>
     * if (!map.containsKey(key))
     *     return map.put(key, value);
     * else
     *     return map.get(key);
     * </pre>
     *
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return The previous value associated with the specified key, or <tt>null</tt> if there was no mapping for the key
     * @throws UnsupportedOperationException If the <tt>put</tt> operation is not supported by this map
     * @throws ClassCastException If the class of the specified key or value prevents it from being stored in this map
     * @throws NullPointerException If the specified key or value is null, and this map does not permit null keys or values
     * @throws IllegalArgumentException If some property of the specified key or value prevents it from being stored in this map
     */
    @Override
    public V putIfAbsent(final E key, final V value) {
        EnumMap<E, V> expected;
        EnumMap<E, V> map;
        do {
            expected = ref.get();
            map = new EnumMap<E, V>(expected);
            final V prev = map.get(key);
            if (null != prev) {
                return prev;
            }
            map.put(key, value);
        } while (!ref.compareAndSet(expected, map));

        return null;
    }

    @Override
    public V remove(final Object key) {
        V removed;
        EnumMap<E, V> expected;
        EnumMap<E, V> map;
        do {
            expected = ref.get();
            map = new EnumMap<E, V>(expected);
            removed = map.remove(key);
        } while (!ref.compareAndSet(expected, map));

        return removed;
    }

    @Override
    public void putAll(final Map<? extends E, ? extends V> m) {
        EnumMap<E, V> expected;
        EnumMap<E, V> map;
        do {
            expected = ref.get();
            map = new EnumMap<E, V>(expected);
            map.putAll(m);
        } while (!ref.compareAndSet(expected, map));
    }

    @Override
    public void clear() {
        ref.get().clear();
    }

    @Override
    public Set<E> keySet() {
        return ref.get().keySet();
    }

    @Override
    public Collection<V> values() {
        return ref.get().values();
    }

    @Override
    public Set<java.util.Map.Entry<E, V>> entrySet() {
        return ref.get().entrySet();
    }

}
