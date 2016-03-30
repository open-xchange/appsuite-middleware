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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link HashKeyEntrySet} - A <code>String</code> {@link Entry} set backed by a <code>HashKey</code> {@link Entry} set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HashKeyEntrySet<V> extends AbstractHashKeyCollection<HashKeyEntrySet<V>> implements Set<Map.Entry<String, V>> {

    private final Set<Map.Entry<HashKey, V>> entrySet;

    private final int sz;

    /**
     * Initializes a new {@link HashKeyEntrySet}.
     */
    public HashKeyEntrySet(final Set<Map.Entry<HashKey, V>> entrySet) {
        super();
        this.entrySet = entrySet;
        sz = entrySet.size();
    }

    @Override
    protected HashKeyEntrySet<V> thisCollection() {
        return this;
    }

    @Override
    public int size() {
        return entrySet.size();
    }

    @Override
    public boolean isEmpty() {
        return entrySet.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return entrySet.contains(o);
    }

    @Override
    public Iterator<Entry<String, V>> iterator() {
        final List<Entry<String, V>> list = new ArrayList<Map.Entry<String, V>>(sz);
        for (final Map.Entry<HashKey, V> entry : entrySet) {
            list.add(new HashKeyEntry<V>(entry));
        }
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        final List<Entry<String, V>> list = new ArrayList<Map.Entry<String, V>>(sz);
        for (final Map.Entry<HashKey, V> entry : entrySet) {
            list.add(new HashKeyEntry<V>(entry));
        }
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        final List<Entry<String, V>> list = new ArrayList<Map.Entry<String, V>>(sz);
        for (final Map.Entry<HashKey, V> entry : entrySet) {
            list.add(new HashKeyEntry<V>(entry));
        }
        return list.toArray(a);
    }

    @Override
    public boolean add(final Entry<String, V> e) {
        return entrySet.add(new EntryImplementation(e));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(final Object o) {
        if (o instanceof Entry) {
            return entrySet.remove(new EntryImplementation((Entry<String, V>) o));
        }
        return entrySet.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        final List<Entry<HashKey, V>> list = new ArrayList<Map.Entry<HashKey, V>>(c.size());
        for (final Object o : c) {
            if (!(o instanceof Entry)) {
                return false;
            }
            list.add(new EntryImplementation((Entry<String, V>) o));
        }
        return entrySet.containsAll(list);
    }

    @Override
    public boolean addAll(final Collection<? extends Entry<String, V>> c) {
        final List<Entry<HashKey, V>> list = new ArrayList<Map.Entry<HashKey, V>>(c.size());
        for (final Entry<String, V> entry : c) {
            list.add(new EntryImplementation(entry));
        }
        return entrySet.addAll(list);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        final List<Entry<HashKey, V>> list = new ArrayList<Map.Entry<HashKey, V>>(c.size());
        for (final Object o : c) {
            if (o instanceof Entry) {
                list.add(new EntryImplementation((Entry<String, V>) o));
            }
        }
        return entrySet.retainAll(list);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        final List<Entry<HashKey, V>> list = new ArrayList<Map.Entry<HashKey, V>>(c.size());
        for (final Object o : c) {
            if (o instanceof Entry) {
                list.add(new EntryImplementation((Entry<String, V>) o));
            }
        }
        return entrySet.removeAll(list);
    }

    @Override
    public void clear() {
        entrySet.clear();
    }

    @Override
    public boolean equals(final Object o) {
        return entrySet.equals(o);
    }

    @Override
    public int hashCode() {
        return entrySet.hashCode();
    }

    @Override
    public String toString() {
        return entrySet.toString();
    }

    private final class EntryImplementation implements Entry<HashKey, V> {

        private final Entry<String, V> e;

        protected EntryImplementation(final Entry<String, V> e) {
            super();
            this.e = e;
        }

        @Override
        public HashKey getKey() {
            return newKey(e.getKey());
        }

        @Override
        public V getValue() {
            return e.getValue();
        }

        @Override
        public V setValue(final V value) {
            return e.setValue(value);
        }
    }

    private static final class HashKeyEntry<V> implements java.util.Map.Entry<String, V> {

        private final Map.Entry<HashKey, V> entry;

        protected HashKeyEntry(final Map.Entry<HashKey, V> entry) {
            super();
            this.entry = entry;
        }

        @Override
        public String getKey() {
            return entry.getKey().toString();
        }

        @Override
        public V getValue() {
            return entry.getValue();
        }

        @Override
        public V setValue(final V value) {
            return entry.setValue(value);
        }

        @Override
        public boolean equals(final Object o) {
            return entry.equals(o);
        }

        @Override
        public int hashCode() {
            return entry.hashCode();
        }

    }

}
