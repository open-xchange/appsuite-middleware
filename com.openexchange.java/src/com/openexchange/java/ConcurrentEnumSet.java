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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ConcurrentEnumSet} - An {@link EnumSet} backed by an {@link AtomicReference} holding a delegate set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ConcurrentEnumSet<E extends Enum<E>> implements Set<E> {

    private final AtomicReference<EnumSet<E>> ref;
    private final Class<E> elementType;

    /**
     * Initializes a new {@link ConcurrentEnumSet}.
     */
    public ConcurrentEnumSet(final Class<E> elementType) {
        super();
        ref = new AtomicReference<EnumSet<E>>(EnumSet.noneOf(elementType));
        this.elementType = elementType;
    }

    /**
     * Initializes a new {@link ConcurrentEnumSet}.
     *
     * @param c The collection whose elements are to be placed into this set
     */
    public ConcurrentEnumSet(final Collection<E> c, final Class<E> elementType) {
        super();
        ref = new AtomicReference<EnumSet<E>>(EnumSet.copyOf(c));
        this.elementType = elementType;
    }

    /**
     * Gets an unmodifiable snapshot view for this concurrent set.
     *
     * @return The unmodifiable snapshot set
     */
    public Set<E> getSnapshot() {
        return Collections.unmodifiableSet(ref.get());
    }

    @Override
    public int size() {
        return ref.get().size();
    }

    @Override
    public boolean isEmpty() {
        return ref.get().isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return ref.get().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return ref.get().iterator();
    }

    @Override
    public Object[] toArray() {
        return ref.get().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return ref.get().toArray(a);
    }

    @Override
    public boolean add(final E e) {
        boolean added;
        EnumSet<E> expected;
        EnumSet<E> set;
        do {
            expected = ref.get();
            set = EnumSet.copyOf(expected);
            added = set.add(e);
        } while (!ref.compareAndSet(expected, set));

        return added;
    }

    @Override
    public boolean remove(final Object o) {
        boolean removed;
        EnumSet<E> expected;
        EnumSet<E> set;
        do {
            expected = ref.get();
            set = EnumSet.copyOf(expected);
            removed = set.remove(o);
        } while (!ref.compareAndSet(expected, set));

        return removed;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return ref.get().containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        boolean added;
        EnumSet<E> expected;
        EnumSet<E> set;
        do {
            expected = ref.get();
            set = EnumSet.copyOf(expected);
            added = set.addAll(c);
        } while (!ref.compareAndSet(expected, set));

        return added;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        boolean ret;
        EnumSet<E> expected;
        EnumSet<E> set;
        do {
            expected = ref.get();
            set = EnumSet.copyOf(expected);
            ret = set.retainAll(c);
        } while (!ref.compareAndSet(expected, set));

        return ret;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean ret;
        EnumSet<E> expected;
        EnumSet<E> set;
        do {
            expected = ref.get();
            set = EnumSet.copyOf(expected);
            ret = set.removeAll(c);
        } while (!ref.compareAndSet(expected, set));

        return ret;
    }

    @Override
    public void clear() {
        // Apply to empty set
        EnumSet<E> expected;
        EnumSet<E> set;
        do {
            expected = ref.get();
            set = EnumSet.noneOf(elementType);
        } while (!ref.compareAndSet(expected, set));
    }

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */
    @Override
    public String toString() {
        Iterator<E> it = iterator();
        if (! it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder(32);
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e);
            if (! it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

}
