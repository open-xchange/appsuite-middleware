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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * {@link HashKeySet} - The <code>String</code> set backed by a <code>HashKey</code> set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HashKeySet extends AbstractHashKeyCollection<HashKeySet> implements Set<String> {

    private final Set<HashKey> set;

    /**
     * Initializes a new {@link HashKeySet}.
     */
    public HashKeySet(final Set<HashKey> set) {
        super();
        this.set = set;
    }

    @Override
    protected HashKeySet thisCollection() {
        return this;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return set.contains(newKey(o.toString()));
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = new ArrayList<String>(set.size());
        for (final HashKey key : set) {
            list.add(key.toString());
        }
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        final List<String> list = new ArrayList<String>(set.size());
        for (final HashKey key : set) {
            list.add(key.toString());
        }
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        final List<String> list = new ArrayList<String>(set.size());
        for (final HashKey key : set) {
            list.add(key.toString());
        }
        return list.toArray(a);
    }

    @Override
    public boolean add(final String e) {
        return set.add(newKey(e));
    }

    @Override
    public boolean remove(final Object o) {
        return set.remove(newKey(o.toString()));
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        final Collection<HashKey> col = new ArrayList<HashKey>(c.size());
        for (final Object o : c) {
            col.add(newKey(o.toString()));
        }
        return set.containsAll(col);
    }

    @Override
    public boolean addAll(final Collection<? extends String> c) {
        final Collection<HashKey> col = new ArrayList<HashKey>(c.size());
        for (final Object o : c) {
            col.add(newKey(o.toString()));
        }
        return set.addAll(col);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        final Collection<HashKey> col = new ArrayList<HashKey>(c.size());
        for (final Object o : c) {
            col.add(newKey(o.toString()));
        }
        return set.retainAll(col);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        final Collection<HashKey> col = new ArrayList<HashKey>(c.size());
        for (final Object o : c) {
            col.add(newKey(o.toString()));
        }
        return set.removeAll(col);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public boolean equals(final Object o) {
        return set.equals(o);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public String toString() {
        return set.toString();
    }

}
