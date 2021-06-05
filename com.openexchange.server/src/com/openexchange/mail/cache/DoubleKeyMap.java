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

package com.openexchange.mail.cache;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link DoubleKeyMap} - A double-key map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class DoubleKeyMap<K1, K2, V extends Serializable> implements Serializable {

    private static final long serialVersionUID = 4691428774420782654L;

    private volatile transient Map<K1, Map<K2, V>> map;
    private final Class<V> clazz;

    /**
     * Initializes a new {@link DoubleKeyMap}.
     *
     * @param clazz he class of the values
     */
    public DoubleKeyMap(Class<V> clazz) {
        super();
        map = new ConcurrentHashMap<K1, Map<K2, V>>();
        this.clazz = clazz;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.flush();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Read in size
        map = new ConcurrentHashMap<K1, Map<K2, V>>();
    }

    /**
     * Lazy initialization of associated map.
     */
    private Map<K1, Map<K2, V>> getMap() {
        Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            synchronized (this) {
                m = map;
                if (null == m) {
                    m = new ConcurrentHashMap<K1, Map<K2, V>>();
                    map = m;
                }
            }
        }
        return m;
    }

    /**
     * Detects if first key is contained in this map.
     *
     * @param k1 The first key
     * @return <code>true</code> if first key is contained in this map; otherwise <code>false</code>
     */
    public boolean containsKey(K1 k1) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return false;
        }
        return m.containsKey(k1);
    }

    /**
     * Detects if key pair is contained in this map.
     *
     * @param k1 The first key
     * @param k2 The second key
     * @return <code>true</code> if key pair is contained in this map; otherwise <code>false</code>
     */
    public boolean containsKeyPair(K1 k1, K2 k2) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return false;
        }
        final Map<K2, V> innerMap = m.get(k1);
        if (null == innerMap) {
            return false;
        }
        return innerMap.containsKey(k2);
    }

    /**
     * Gets all values associated with given first key.
     *
     * @param k1 The first key
     * @return All values associated with given first key or <code>null</code> if none found
     */
    public V[] getValues(K1 k1) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return null;
        }
        final Map<K2, V> innerMap = m.get(k1);
        if (innerMap == null) {
            return null;
        }
        final @SuppressWarnings("unchecked") V[] newInstance = (V[]) Array.newInstance(clazz, innerMap.size());
        return innerMap.values().toArray(newInstance);
    }

    /**
     * Gets the values associated with given first key and given second keys.
     *
     * @param k1 The first key
     * @param keys The second keys
     * @return The values associated with given first key and given second keys
     */
    public V[] getValues(K1 k1, K2[] keys) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return null;
        }
        final Map<K2, V> innerMap = m.get(k1);
        if (innerMap == null) {
            return null;
        }
        final List<V> tmp = new ArrayList<V>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            tmp.add(innerMap.get(keys[i]));
        }
        final @SuppressWarnings("unchecked") V[] newInstance = (V[]) Array.newInstance(clazz, tmp.size());
        return tmp.toArray(newInstance);
    }

    /**
     * Gets the single value associated with given key pair.
     *
     * @param k1 The first key
     * @param k2 The second key
     * @return The single value associated with given key pair or <code>null</code> if not present
     */
    public V getValue(K1 k1, K2 k2) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return null;
        }
        final Map<K2, V> innerMap = m.get(k1);
        if (null == innerMap) {
            return null;
        }
        return innerMap.get(k2);
    }

    /**
     * Puts given values into map.
     *
     * @param k1 The first key
     * @param keys The second keys
     * @param values The values to insert
     */
    public void putValues(K1 k1, K2[] keys, V[] values) {
        if ((k1 == null) || (keys == null) || (values == null)) {
            throw new IllegalArgumentException("Argument must not be null");
        }
        final Map<K1, Map<K2, V>> m = getMap();
        Map<K2, V> innerMap = m.get(k1);
        if (innerMap == null) {
            innerMap = new ConcurrentHashMap<K2, V>(values.length, 0.9f, 1);
            m.put(k1, innerMap);
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                innerMap.put(keys[i], values[i]);
            }
        }
    }

    /**
     * Puts a single value into map.
     *
     * @param k1 The first key
     * @param k2 The second key
     * @param value The value to insert
     * @return The value formerly bound to given key pair or <code>null</code> if none was bound before
     */
    public V putValue(K1 k1, K2 k2, V value) {
        if (k1 == null) {
            throw new IllegalArgumentException("First key must not be null");
        } else if (k2 == null) {
            throw new IllegalArgumentException("Second key must not be null");
        } else if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        Map<K1, Map<K2, V>> m = getMap();
        Map<K2, V> innerMap = m.get(k1);
        if (innerMap == null) {
            innerMap = new ConcurrentHashMap<K2, V>();
            m.put(k1, innerMap);
        }
        return innerMap.put(k2, value);
    }

    /**
     * Removes all values associated with given first key.
     *
     * @param k1 The first key
     */
    public void removeValues(K1 k1) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return;
        }
        m.remove(k1);
    }

    /**
     * Removes the values associated with given first key and is in list of second keys.
     *
     * @param k1 The first key
     * @param keys The second keys
     */
    public void removeValues(K1 k1, K2[] keys) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return;
        }
        final Map<K2, V> innerMap = m.get(k1);
        if (null == innerMap) {
            return;
        }
        for (int i = 0; i < keys.length; i++) {
            innerMap.remove(keys[i]);
        }
        if (innerMap.isEmpty()) {
            /*
             * Remove empty inner map
             */
            m.remove(k1);
        }
    }

    /**
     * Removes the single value associated with given key pair.
     *
     * @param k1 The first key
     * @param k2 The second key
     * @return The removed value or <code>null</code> if not present
     */
    public V removeValue(K1 k1, K2 k2) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return null;
        }
        final Map<K2, V> innerMap = m.get(k1);
        if (null == innerMap) {
            return null;
        }
        final V retval = innerMap.remove(k2);
        if ((retval != null) && innerMap.isEmpty()) {
            /*
             * Remove empty inner map
             */
            m.remove(k1);
        }
        return retval;
    }

    /**
     * Checks if no values are bound to given first key.
     *
     * @param k1 The first key
     * @return <code>true</code> if no values are bound to given first key; otherwise <code>false</code>
     */
    public boolean isEmpty(K1 k1) {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return false;
        }
        final Map<K2, V> innerMap = m.get(k1);
        if (null == innerMap) {
            return true;
        } else if (innerMap.isEmpty()) {
            m.remove(k1);
            return true;
        }
        return false;
    }

    /**
     * Checks if whole map is empty.
     *
     * @return <code>true</code> if whole map is empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        final Map<K1, Map<K2, V>> m = map;
        if (null == m) {
            return true;
        }
        return m.isEmpty();
    }

    /**
     * Clears whole map.
     */
    public void clear() {
        final Map<K1, Map<K2, V>> m = map;
        if (null != m) {
            m.clear();
        }
    }

}
