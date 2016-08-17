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
