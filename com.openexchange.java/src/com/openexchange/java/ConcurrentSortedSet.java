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
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ConcurrentSortedSet} - A {@link SortedSet} backed by an {@link AtomicReference} holding a delegate {@link TreeSet}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ConcurrentSortedSet<E> implements SortedSet<E> {

    private final AtomicReference<SortedSet<E>> ref;

    /**
     * Initializes a new {@link ConcurrentSortedSet}.
     */
    public ConcurrentSortedSet() {
        super();
        ref = new AtomicReference<SortedSet<E>>(new TreeSet<E>());
    }

    /**
     * Initializes a new {@link ConcurrentSet}.
     *
     * @param c The collection whose elements are to be placed into this set
     */
    public ConcurrentSortedSet(final Collection<? extends E> c) {
        super();
        ref = new AtomicReference<SortedSet<E>>(new TreeSet<E>(c));
    }

    /**
     * Gets an unmodifiable snapshot view for this concurrent set.
     *
     * @return The unmodifiable snapshot set
     */
    public SortedSet<E> getSnapshot() {
        return Collections.unmodifiableSortedSet(ref.get());
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
        SortedSet<E> expected;
        SortedSet<E> set;
        do {
            expected = ref.get();
            set = new TreeSet<E>(expected);
            added = set.add(e);
        } while (!ref.compareAndSet(expected, set));

        return added;
    }

    @Override
    public boolean remove(final Object o) {
        boolean removed;
        SortedSet<E> expected;
        SortedSet<E> set;
        do {
            expected = ref.get();
            set = new TreeSet<E>(expected);
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
        SortedSet<E> expected;
        SortedSet<E> set;
        do {
            expected = ref.get();
            set = new TreeSet<E>(expected);
            added = set.addAll(c);
        } while (!ref.compareAndSet(expected, set));

        return added;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        boolean ret;
        SortedSet<E> expected;
        SortedSet<E> set;
        do {
            expected = ref.get();
            set = new TreeSet<E>(expected);
            ret = set.retainAll(c);
        } while (!ref.compareAndSet(expected, set));

        return ret;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean ret;
        SortedSet<E> expected;
        SortedSet<E> set;
        do {
            expected = ref.get();
            set = new TreeSet<E>(expected);
            ret = set.removeAll(c);
        } while (!ref.compareAndSet(expected, set));

        return ret;
    }

    @Override
    public void clear() {
        // Apply to empty set
        SortedSet<E> expected;
        SortedSet<E> set;
        do {
            expected = ref.get();
            set = new TreeSet<E>();
        } while (!ref.compareAndSet(expected, set));
    }

    @Override
    public Comparator<? super E> comparator() {
        return ref.get().comparator();
    }

    @Override
    public SortedSet<E> subSet(final E fromElement, final E toElement) {
        return ref.get().subSet(fromElement, toElement);
    }

    @Override
    public SortedSet<E> headSet(final E toElement) {
        return ref.get().headSet(toElement);
    }

    @Override
    public SortedSet<E> tailSet(final E fromElement) {
        return ref.get().tailSet(fromElement);
    }

    @Override
    public E first() {
        return ref.get().first();
    }

    @Override
    public E last() {
        return ref.get().last();
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
            sb.append(e == this ? "(this Collection)" : e);
            if (! it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

}
