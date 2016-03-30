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

package com.openexchange.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * {@link HashKeyMap} - The <code>String</code> map backed by a <code>HashKey</code> map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @see HashKey
 */
public final class HashKeyMap<V> extends AbstractHashKeyCollection<HashKeyMap<V>> implements Map<String, V> {

    private final Map<HashKey, V> map;

    /**
     * Initializes a new {@link HashKeyMap}.
     */
    public HashKeyMap() {
        this(new HashMap<HashKey, V>());
    }

    /**
     * Initializes a new {@link HashKeyMap}.
     *
     * @param initialCapacity The initial map's capacity
     */
    public HashKeyMap(final int initialCapacity) {
        this(new HashMap<HashKey, V>(initialCapacity));
    }

    /**
     * Initializes a new {@link HashKeyMap} from specified backing map.
     *
     * @param map The backing map
     */
    public HashKeyMap(final Map<HashKey, V> map) {
        super();
        this.map = map;
    }

    @Override
    protected HashKeyMap<V> thisCollection() {
        return this;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return map.containsKey(newKey(key.toString()));
    }

    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        return map.get(newKey(key.toString()));
    }

    @Override
    public V put(final String key, final V value) {
        return map.put(newKey(key), value);
    }

    @Override
    public V remove(final Object key) {
        return map.remove(newKey(key.toString()));
    }

    @Override
    public void putAll(final Map<? extends String, ? extends V> m) {
        if (null == m || m.isEmpty()) {
            return;
        }
        for (final Iterator<? extends Map.Entry<? extends String, ? extends V>> i = m.entrySet().iterator(); i.hasNext();) {
            final Map.Entry<? extends String, ? extends V> e = i.next();
            map.put(newKey(e.getKey()), e.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return new HashKeySet(map.keySet()).setGenerator(generatorReference.get());
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, V>> entrySet() {
        return new HashKeyEntrySet<V>(map.entrySet()).setGenerator(generatorReference.get());
    }

    @Override
    public boolean equals(final Object o) {
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
